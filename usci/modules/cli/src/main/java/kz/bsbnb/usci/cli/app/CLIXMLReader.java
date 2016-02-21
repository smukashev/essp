package kz.bsbnb.usci.cli.app;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.UnexpectedInputException;

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

public class CLIXMLReader {
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    protected DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    protected XMLEventReader xmlEventReader;
    private IMetaClassRepository metaClassRepository;
    private Date reportDate;
    private FileInputStream inputStream;
    private Logger logger = Logger.getLogger(CLIXMLReader.class);
    private Stack<IBaseContainer> stack = new Stack<>();
    private Stack<Boolean> flagsStack = new Stack<>();
    private IBaseContainer currentContainer;
    private IBatchService batchService;
    private Batch batch;
    private Long index = 1L, level = 0L;
    private boolean hasMembers = false;
    private boolean rootEntityExpected = false;
    private String currentRootMeta = null;

    private long creditorId = 0L;

    public void init (InputStream inputStream, IMetaClassRepository metaRepo, IBatchService batchService,
                      Date repDate) {
        logger.info("Reader init.");
        metaClassRepository = metaRepo;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        try {
            xmlEventReader = inputFactory.createXMLEventReader(inputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        this.reportDate = repDate;

        batch = new Batch(reportDate, 1L);
        this.batchService = batchService;

        this.batchService.save(batch);
    }
    public CLIXMLReader(InputStream inputStream, IMetaClassRepository metaRepo, IBatchService batchService, Date repDate) {
        init(inputStream, metaRepo, batchService, repDate);
    }

    public CLIXMLReader(InputStream inputStream, IMetaClassRepository metaRepo, IBatchService batchService, Date repDate, long creditorId) {
        this.creditorId = creditorId;
        init(inputStream, metaRepo, batchService, repDate);
    }

    public CLIXMLReader(String fileName, IMetaClassRepository metaRepo, IBatchService batchService, Date repDate)
            throws FileNotFoundException {
        logger.info("Reader init.");
        metaClassRepository = metaRepo;
        this.batchService = batchService;

        inputStream = new FileInputStream(fileName);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        try {
            xmlEventReader = inputFactory.createXMLEventReader(inputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        this.reportDate = repDate;

        batch = new Batch(reportDate, 1L);
        batch.setFileName(new File(fileName).getName());
        long batchId = this.batchService.save(batch);
        batch.setId(batchId);
    }

    public Batch getBatch(){
        return batch;
    }

    public Object getCastObject(DataTypes typeCode, String value) {
        switch (typeCode) {
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
                try {
                    int i = Integer.parseInt(value);
                    return i == 1;
                } catch (Exception e) {
                    return Boolean.parseBoolean(value);
                }
            case DOUBLE:
                return Double.parseDouble(value);
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

    private boolean hasOperationDelete(StartElement startElement) {
        return startElement.getAttributeByName(new QName("operation")) != null &&
                startElement.getAttributeByName(new QName("operation")).getValue()
                        .equalsIgnoreCase(OperationType.DELETE.toString());
    }

    private boolean hasOperationClose(StartElement startElement) {
        return startElement.getAttributeByName(new QName("operation")) != null &&
                startElement.getAttributeByName(new QName("operation")).getValue()
                        .equalsIgnoreCase(OperationType.CLOSE.toString());
    }

    private boolean hasOperationNew(StartElement startElement) {
        return startElement.getAttributeByName(new QName("operation")) != null &&
                startElement.getAttributeByName(new QName("operation")).getValue()
                        .equalsIgnoreCase(OperationType.NEW.toString());
    }

    public void startElement(XMLEvent event, StartElement startElement, String localName) {
        if (localName.equals("batch")) {
            logger.info("batch");
        } else if (localName.equals("entities")) {
            logger.info("entities");
            rootEntityExpected = true;
        } else if (rootEntityExpected) {
            currentRootMeta = localName;
            rootEntityExpected = false;

            BaseEntity baseEntity = new BaseEntity(metaClassRepository.getMetaClass(localName), batch.getRepDate(), creditorId);

            if (hasOperationDelete(startElement))
                baseEntity.setOperation(OperationType.DELETE);

            if (hasOperationClose(startElement))
                baseEntity.setOperation(OperationType.CLOSE);

            currentContainer = baseEntity;
        } else {
            logger.info("other: " + localName);
            IMetaType metaType = currentContainer.getMemberType(localName);

            if (metaType.isSet()) {
                stack.push(currentContainer);
                flagsStack.push(hasMembers);
                hasMembers = false;
                currentContainer = new BaseSet(((MetaSet) metaType).getMemberType());
                level++;
            } else if (metaType.isComplex() && !metaType.isSet()) {
                stack.push(currentContainer);
                currentContainer = new BaseEntity((MetaClass) metaType, batch.getRepDate(), creditorId);
                flagsStack.push(hasMembers);
                hasMembers = false;
                //metaFactoryService.getBaseEntity((MetaClass)metaType, batch.getRepDate());
                level++;
            } else if (!metaType.isComplex() && !metaType.isSet()) {
                Object obj = null;
                MetaValue metaValue = (MetaValue) metaType;

                try {
                    event = (XMLEvent) xmlEventReader.next();
                    obj = getCastObject(metaValue.getTypeCode(), event.asCharacters().getData());
                    //xmlEventReader.next();
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                } catch (ClassCastException ex) {
                    logger.debug("Empty tag: " + localName);
                    level--;
                }

                if (obj != null) {
                    hasMembers = true;
                }

                String memberName = localName;
                if (currentContainer.getBaseContainerType() == BaseContainerType.BASE_SET) {
                    memberName += "_" + currentContainer.getValueCount();
                }

                IBaseValue baseValue = BaseValueFactory
                        .create(currentContainer.getBaseContainerType(), metaType, 0, -1, batch.getRepDate(), obj,
                                false, true);

                if (hasOperationNew(startElement)) {
                    IBaseValue newBaseValue = BaseValueFactory.create(currentContainer.getBaseContainerType(),
                            metaType, 0, -1, batch.getRepDate(),
                            getCastObject(metaValue.getTypeCode(),
                                    startElement.getAttributeByName(new QName("data")).getValue()), false, true);

                    baseValue.setNewBaseValue(newBaseValue);
                }

                currentContainer.put(memberName, baseValue);

                level++;
            }
        }
    }

    public BaseEntity read() throws UnexpectedInputException, org.springframework.batch.item.ParseException,
            NonTransientResourceException {
        while (xmlEventReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlEventReader.next();

            if (event.isStartDocument()) {
                logger.info("start document");
            } else if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                startElement(event, startElement, localName);
            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if (endElement(localName)) return (BaseEntity) currentContainer;
            } else if (event.isEndDocument()) {
                logger.info("end document");
            } else {
                logger.info(event);
            }
        }

        return null;
    }

    public void close() {
        try {
            xmlEventReader.close();
            inputStream.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean endElement(String localName) {
        if (localName.equals("batch")) {
            //logger.info("batch");
        } else if (localName.equals("entities")) {
            //logger.info("entities");
            currentContainer = null;
            return true;
        } else if (localName.equals("entity")) {
        } else if (localName.equals(currentRootMeta)) {
            rootEntityExpected = true;
            index++;
            return true;
        } else {
            IMetaType metaType;

            if (level == stack.size())
                metaType = stack.peek().getMemberType(localName);
            else
                metaType = currentContainer.getMemberType(localName);

            if (metaType.isComplex() || metaType.isSet()) {
                Object obj = currentContainer;
                currentContainer = stack.pop();

                if (currentContainer.isSet()) {
                    if (hasMembers) {
                        ((BaseSet) currentContainer).put(BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, 0, -1, batch.getRepDate(),
                                        obj, false, true));
                        flagsStack.pop();
                        hasMembers = true;
                    } else {
                        hasMembers = flagsStack.pop();
                    }
                } else {
                    /* fixme */
                    if (localName.equals("creditor_branch") &&
                            ((((BaseEntity) obj).getBaseValue("docs") == null) || ((BaseEntity) obj).getBaseValue("docs").getValue() == null))
                        obj = null;


                    if (hasMembers) {
                        currentContainer.put(localName, BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, 0, -1, batch.getRepDate(),
                                        obj, false, true));
                        flagsStack.pop();
                        hasMembers = true;
                    } else {
                        currentContainer.put(localName, BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, 0, -1, batch.getRepDate(),
                                        null, false, true));
                        hasMembers = flagsStack.pop();
                    }
                }
            }

            level--;
        }

        return false;
    }

    public void setXmlEventReader(XMLEventReader xmlEventReader) {
        this.xmlEventReader = xmlEventReader;
    }
}
