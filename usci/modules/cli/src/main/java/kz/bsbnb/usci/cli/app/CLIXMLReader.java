package kz.bsbnb.usci.cli.app;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.apache.log4j.Logger;
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
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

//TODO: merge with StaxEventEntityReader from receiver
public class CLIXMLReader
{
    private Logger logger = Logger.getLogger(CLIXMLReader.class);
    private Stack<IBaseContainer> stack = new Stack<IBaseContainer>();
    private IBaseContainer currentContainer;
    private Batch batch;
    private Long index = 1L, level = 0L;

    private IMetaClassRepository metaClassRepository;

    protected XMLEventReader xmlEventReader;

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    protected DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    public CLIXMLReader(String fileName, IMetaClassRepository metaRepo, IBatchRepository batchRepository) throws FileNotFoundException
    {
        logger.info("Reader init.");
        metaClassRepository = metaRepo;

        FileInputStream inputStream = new FileInputStream(fileName);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        try {
            xmlEventReader = inputFactory.createXMLEventReader(inputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        batch = new Batch(new Date(), 1L);

        batchRepository.addBatch(batch);
    }

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

    public void startElement(XMLEvent event, StartElement startElement, String localName) {
        if(localName.equals("batch")) {
            logger.info("batch");
        } else if(localName.equals("entities")) {
            logger.info("entities");
        } else if(localName.equals("entity")) {
            logger.info("entity");
            currentContainer =
                    new BaseEntity(
                            metaClassRepository.getMetaClass(startElement.getAttributeByName(
                                    new QName("class")).getValue()), new Date());
        } else {
            logger.info("other: " + localName);
            IMetaType metaType = currentContainer.getMemberType(localName);

            if(metaType.isSet()) {
                stack.push(currentContainer);
                currentContainer = new BaseSet(((MetaSet)metaType).getMemberType());
                level++;
            } else if(metaType.isComplex() && !metaType.isSet()) {
                stack.push(currentContainer);
                currentContainer = new BaseEntity((MetaClass)metaType, new Date());
                level++;
            } else if(!metaType.isComplex() && !metaType.isSet()) {
                Object o = null;
                MetaValue metaValue = (MetaValue) metaType;

                try {
                    event = (XMLEvent) xmlEventReader.next();
                    o = getCastObject(metaValue.getTypeCode(), event.asCharacters().getData());
                    xmlEventReader.next();
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                }

                currentContainer.put(localName, new BaseValue(batch, index, o));
            }
        }
    }

    public BaseEntity read() {
        logger.info("Read called");
        while(xmlEventReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlEventReader.next();

            if(event.isStartDocument()) {
                logger.info("start document");
            } else if(event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                startElement(event, startElement, localName);
            } else if(event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if(endElement(localName)) return (BaseEntity) currentContainer;
            } else if(event.isEndDocument()) {
                logger.info("end document");
            } else {
                logger.info(event);
            }
        }

        return null;
    }

    public boolean endElement(String localName) {
        if(localName.equals("batch")) {
            logger.info("batch");
        } else if(localName.equals("entities")) {
            logger.info("entities");
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
                level--;
            }
        }

        return false;
    }
}
