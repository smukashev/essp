package kz.bsbnb.usci.receiver.reader.impl;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusArrayJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Date;
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
    private Long index = 1L, level = 0L;

    private IBatchService batchService;
    private IMetaFactoryService metaFactoryService;

    private CouchbaseClient couchbaseClient;
    private Gson gson = new Gson();

    private BatchFullJModel batchFullJModel;

    private static final long WAIT_TIMEOUT = 3600; //in sec

    @Autowired
    private IServiceRepository serviceFactory;

    @PostConstruct
    public void init() {
        //logger.info("Reader init.");
        batchService = serviceRepository.getBatchService();
        metaFactoryService = serviceRepository.getMetaFactoryService();
        couchbaseClient = couchbaseClientFactory.getCouchbaseClient();
        batchFullJModel = gson.fromJson(couchbaseClient.get("batch:" + batchId).toString(), BatchFullJModel.class);

        couchbaseClient.shutdown();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(batchFullJModel.getContent());
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        try {
            xmlEventReader = inputFactory.createXMLEventReader(inputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        batch = batchService.load(batchId);
    }

    public void startElement(XMLEvent event, StartElement startElement, String localName) {
        if(localName.equals("batch")) {
            //logger.info("batch");
        } else if(localName.equals("entities")) {
            //logger.info("entities");
        } else if(localName.equals("entity")) {
            //logger.info("entity");
            currentContainer = metaFactoryService.getBaseEntity(
                    startElement.getAttributeByName(new QName("class")).getValue(), batch.getRepDate());
        } else {
            //logger.info("other: " + localName);
            IMetaType metaType = currentContainer.getMemberType(localName);

            if(metaType.isSet()) {
                stack.push(currentContainer);
                currentContainer = metaFactoryService.getBaseSet(((MetaSet)metaType).getMemberType());
                level++;
            } else if(metaType.isComplex() && !metaType.isSet()) {
                stack.push(currentContainer);
                currentContainer = new BaseEntity((MetaClass)metaType, batch.getRepDate());
                //metaFactoryService.getBaseEntity((MetaClass)metaType, batch.getRepDate());
                level++;
            } else if(!metaType.isComplex() && !metaType.isSet()) {
                Object o = null;
                MetaValue metaValue = (MetaValue) metaType;

                try {
                    event = (XMLEvent) xmlEventReader.next();
                    o = parserHelper.getCastObject(metaValue.getTypeCode(), event.asCharacters().getData());
                    //xmlEventReader.next();
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                } catch (ClassCastException ex) {
                    logger.debug("Empty tag: " + localName);
                    level--;
                }

                currentContainer.put(localName, new BaseValue(batch, index, o));
                level++;
            }
        }
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        //logger.info("Read called");
        //System.out.println("Sync queue size: " + serviceFactory.getEntityService().getQueueSize());
        long sleepCounter = 0;
        while(serviceFactory.getEntityService().getQueueSize() > ZipFilesMonitor.MAX_SYNC_QUEUE_SIZE) {
            //System.out.println("Sync queue limit exceeded: " + serviceFactory.getEntityService().getQueueSize());
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            sleepCounter++;
            if (sleepCounter > WAIT_TIMEOUT) {
                throw new IllegalStateException("Sync timeout in reader.");
            }
        }
        while(xmlEventReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlEventReader.next();

            if(event.isStartDocument()) {
                //logger.info("start document");
            } else if(event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                startElement(event, startElement, localName);
            } else if(event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if(endElement(localName)) return (T) currentContainer;
            } else if(event.isEndDocument()) {
                //logger.info("end document");
                //couchbaseClient.set("batch:" + batchId, 0, gson.toJson(batchFullJModel));
            } else {
                //logger.info(event);
            }
        }

        return null;
    }

    public boolean endElement(String localName) {
        if(localName.equals("batch")) {
            //logger.info("batch");
        } else if(localName.equals("entities")) {
            //logger.info("entities");
            currentContainer = null;
            return true;
        } else if(localName.equals("entity")) {
            index++;
            return true;
        } else {
            IMetaType metaType;

            if(level == stack.size())
                metaType = stack.peek().getMemberType(localName);
            else
                metaType = currentContainer.getMemberType(localName);

            if(metaType.isComplex() || metaType.isSet()) {
                Object o = currentContainer;
                currentContainer = stack.pop();

                if (currentContainer.isSet()) {
                    ((BaseSet)currentContainer).put(new BaseValue(batch, index, o));
                } else {
                    currentContainer.put(localName, new BaseValue(batch, index, o));
                }

            }
            level--;
        }

        return false;
    }
}
