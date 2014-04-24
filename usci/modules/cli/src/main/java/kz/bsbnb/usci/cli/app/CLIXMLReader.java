package kz.bsbnb.usci.cli.app;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import kz.bsbnb.usci.tool.couchbase.BatchStatuses;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

//TODO: merge with StaxEventEntityReader from receiver
public class CLIXMLReader
{
    /////////////////////////
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    protected DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    private IMetaClassRepository metaClassRepository;

    protected XMLEventReader xmlEventReader;
    private Date reportDate;

    public Object getCastObject(DataTypes typeCode, String value) {
        switch(typeCode) {
            case INTEGER:
                return Integer.parseInt(value);
            case DATE:
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
                Date date = null;

                try {
                    date = simpleDateFormat.parse(value);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return date;
            case STRING:
                return value;
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            case DOUBLE:
                return Double.parseDouble(value);
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

    public CLIXMLReader(String fileName, IMetaClassRepository metaRepo, IBatchRepository batchRepository, Date repDate) throws FileNotFoundException
    {
        logger.info("Reader init.");
        metaClassRepository = metaRepo;

        FileInputStream inputStream = new FileInputStream(fileName);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        try {
            xmlEventReader = inputFactory.createXMLEventReader(inputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        this.reportDate = repDate;

        batch = new Batch(reportDate, 1L);

        batchRepository.addBatch(batch);
    }
    /////////////////////////
    private Logger logger = Logger.getLogger(CLIXMLReader.class);
    private Stack<IBaseContainer> stack = new Stack<IBaseContainer>();
    private Stack<Boolean> flagsStack = new Stack<Boolean>();
    private IBaseContainer currentContainer;
    private Batch batch;
    private Long index = 1L, level = 0L;

    private boolean hasMembers = false;

    public void startElement(XMLEvent event, StartElement startElement, String localName) {
        if(localName.equals("batch")) {
            //logger.info("batch");
        } else if(localName.equals("entities")) {
            //logger.info("entities");
        } else if(localName.equals("entity")) {
            //logger.info("entity");
            currentContainer = new BaseEntity(metaClassRepository.getMetaClass(
                    startElement.getAttributeByName(new QName("class")).getValue()), batch.getRepDate());
        } else {
            //logger.info("other: " + localName);
            IMetaType metaType = currentContainer.getMemberType(localName);

            if(metaType.isSet()) {
                stack.push(currentContainer);
                flagsStack.push(hasMembers);
                hasMembers = false;
                currentContainer = new BaseSet(((MetaSet)metaType).getMemberType());
                level++;
            } else if(metaType.isComplex() && !metaType.isSet()) {
                stack.push(currentContainer);
                currentContainer = new BaseEntity((MetaClass)metaType, batch.getRepDate());
                flagsStack.push(hasMembers);
                hasMembers = false;
                //metaFactoryService.getBaseEntity((MetaClass)metaType, batch.getRepDate());
                level++;
            } else if(!metaType.isComplex() && !metaType.isSet()) {
                Object o = null;
                MetaValue metaValue = (MetaValue) metaType;

                try {
                    event = (XMLEvent) xmlEventReader.next();
                    o = getCastObject(metaValue.getTypeCode(), event.asCharacters().getData());
                    //xmlEventReader.next();
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                } catch (ClassCastException ex) {
                    logger.debug("Empty tag: " + localName);
                    level--;
                }

                if (o != null) {
                    hasMembers = true;
                }

                String memberName = localName;
                if (currentContainer.getBaseContainerType() == BaseContainerType.BASE_SET) {
                    memberName += "_" + currentContainer.getValueCount();
                }

                currentContainer.put(memberName, BaseValueFactory
                        .create(currentContainer.getBaseContainerType(), metaType, batch, index, o));

                level++;
            }
        }
    }

    public BaseEntity read() throws UnexpectedInputException, org.springframework.batch.item.ParseException, NonTransientResourceException {
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

                if(endElement(localName)) return (BaseEntity)currentContainer;
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
                    if (hasMembers) {
                        ((BaseSet)currentContainer).put(BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, batch, index, o));
                        flagsStack.pop();
                        hasMembers = true;
                    } else {
                        hasMembers = flagsStack.pop();
                    }
                } else {
                    if (hasMembers) {
                        currentContainer.put(localName, BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, batch, index, o));
                        flagsStack.pop();
                        hasMembers = true;
                    } else {
                        currentContainer.put(localName, BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, batch, index, null));
                        hasMembers = flagsStack.pop();
                    }
                }

                //hasMembers = flagsStack.pop();
            }
            level--;
        }

        return false;
    }
}
