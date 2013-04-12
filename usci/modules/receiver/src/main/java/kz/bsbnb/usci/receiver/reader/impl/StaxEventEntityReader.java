package kz.bsbnb.usci.receiver.reader.impl;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.model.BatchModel;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Stack;

/**
 * @author k.tulbassiyev
 */
@Component
@Scope("step")
public class StaxEventEntityReader<T> extends CommonReader<T> {
    private Logger logger = Logger.getLogger(StaxEventEntityReader.class);
    private Stack<IBaseContainer> stack = new Stack<IBaseContainer>();
    private IBaseContainer currentContainer;
    private Batch batch;
    private int index = 1, level = 0;

    private IBatchService batchService;
    private IMetaFactoryService metaFactoryService;

    private CouchbaseClient couchbaseClient;
    private Gson gson = new Gson();

    private BatchModel batchModel;

    @PostConstruct
    public void init() {
        batchService = serviceRepository.getBatchService();
        metaFactoryService = serviceRepository.getMetaFactoryService();
        couchbaseClient = couchbaseClientFactory.getCouchbaseClient();
        batchModel = gson.fromJson(couchbaseClient.get("batch:" + batchId).toString(), BatchModel.class);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(batchModel.getContent());
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        try {
            xmlEventReader = inputFactory.createXMLEventReader(inputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        batch = batchService.load(batchId);
        couchbaseClient.set("batch:" + batchId + ":status", 0, Global.BATCH_STATUS_STARTED);
    }

    public void startElement(XMLEvent event, StartElement startElement, String localName) {
        if(localName.equals("batch")) {
            logger.info("batch");
        } else if(localName.equals("entities")) {
            logger.info("entities");
        } else if(localName.equals("entity")) {
            currentContainer = metaFactoryService.getBaseEntity(
                    startElement.getAttributeByName(new QName("class")).getValue());

            couchbaseClient.set("batch:" + batchId + ":contract:" + index + ":status", 0,
                    Global.CONTRACT_STATUS_PROCESSING);
        } else {
            IMetaType metaType = currentContainer.getMemberType(localName);

            if(metaType.isSet()) {
                stack.push(currentContainer);
                currentContainer = metaFactoryService.getBaseSet(((MetaSet)metaType).getMemberType());
                level++;
            } else if(metaType.isComplex() && !metaType.isSet()) {
                stack.push(currentContainer);
                currentContainer = metaFactoryService.getBaseEntity((MetaClass)metaType);
                level++;
            } else if(!metaType.isComplex() && !metaType.isSet()) {
                Object o = null;
                MetaValue metaValue = (MetaValue) metaType;

                try {
                    event = (XMLEvent) xmlEventReader.next();
                    o = parserHelper.getCastObject(metaValue.getTypeCode(), event.asCharacters().getData());
                    xmlEventReader.next();
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                }

                currentContainer.put(localName, new BaseValue(batch, index, o));
            }
        }
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        while(xmlEventReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlEventReader.next();

            if(event.isStartDocument()) {
                logger.info("start document");
                couchbaseClient.set("batch:" + batchId + ":status", 0, Global.BATCH_STATUS_PROCESSING);
            } else if(event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                startElement(event, startElement, localName);
            } else if(event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if(localName.equals("batch")) {
                    logger.info("batch");
                } else if(localName.equals("entities")) {
                    logger.info("entities");
                } else if(localName.equals("entity")) {
                    couchbaseClient.set("batch:" + batchId + ":contract:" + index + ":status", 0,
                            Global.CONTRACT_STATUS_COMPLETED);

                    T entity = (T) currentContainer;
                    currentContainer = null;
                    index++;

                    return entity;
                } else {
                    IMetaType metaType;

                    if(level == stack.size())
                        metaType = stack.peek().getMemberType(localName);
                    else
                        metaType = currentContainer.getMemberType(localName);

                    if(metaType.isComplex() || metaType.isSet()) {
                        Object o = currentContainer;
                        currentContainer = stack.pop();

                        currentContainer.put(localName, new BaseValue(batch, index, o));
                        level--;
                    }
                }
            } else if(event.isEndDocument()) {
                logger.info("end document");
                couchbaseClient.set("batch:" + batchId + ":status", 0, Global.BATCH_STATUS_COMPLETED);
            } else {
                logger.info(event);
            }
        }

        return null;
    }
}
