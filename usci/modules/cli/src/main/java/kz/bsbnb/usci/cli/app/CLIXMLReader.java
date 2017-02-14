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
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
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

class CLIXMLReader {
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    protected DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private XMLEventReader xmlEventReader;
    private IMetaClassRepository metaClassRepository;
    private Date reportDate;
    private FileInputStream inputStream;
    private Logger logger = Logger.getLogger(CLIXMLReader.class);
    private Stack<IBaseContainer> stack = new Stack<>();
    private Stack<Boolean> flagsStack = new Stack<>();
    private IBaseContainer currentContainer;
    private IBatchService batchService;
    private Batch batch;
    private Long level = 0L;
    private boolean hasMembers = false;
    private boolean rootEntityExpected = false;
    private String currentRootMeta = null;

    private long creditorId = 0L;

    private void init(InputStream inputStream, IMetaClassRepository metaRepo, IBatchService batchService,
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

    CLIXMLReader(InputStream inputStream, IMetaClassRepository metaRepo, IBatchService batchService,
                 Date repDate, long creditorId) {
        this.creditorId = creditorId;
        DataUtils.toBeginningOfTheMonth(repDate);
        DataUtils.toBeginningOfTheDay(repDate);
        init(inputStream, metaRepo, batchService, repDate);
    }

    CLIXMLReader(String fileName, IMetaClassRepository metaRepo, IBatchService batchService,
                 Date repDate, long creditorId) throws FileNotFoundException {
        logger.info("Reader init.");
        metaClassRepository = metaRepo;
        this.batchService = batchService;
        this.creditorId = creditorId;

        inputStream = new FileInputStream(fileName);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        try {
            xmlEventReader = inputFactory.createXMLEventReader(inputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        this.reportDate = repDate;
        DataUtils.moveMonthIfNecessary(this.reportDate);
        DataUtils.toBeginningOfTheMonth(this.reportDate);
        DataUtils.toBeginningOfTheDay(this.reportDate);

        batch = new Batch(reportDate, 1L);
        batch.setFileName(new File(fileName).getName());
        long batchId = this.batchService.save(batch);
        batch.setId(batchId);
    }

    Batch getBatch(){
        return batch;
    }

    private Object getCastObject(DataTypes typeCode, String value) {
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
                throw new IllegalArgumentException(Errors.compose(Errors.E127));
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

    private boolean hasOperationCheckedRemove(StartElement startElement) {
        return startElement.getAttributeByName(new QName("operation")) != null &&
                startElement.getAttributeByName(new QName("operation")).getValue()
                        .equalsIgnoreCase(OperationType.CHECKED_REMOVE.toString());
    }

    private void startElement(XMLEvent event, StartElement startElement, String localName) {
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

            if(hasOperationCheckedRemove(startElement))
                baseEntity.setOperation(OperationType.CHECKED_REMOVE);

            currentContainer = baseEntity;
        } else {
            logger.info("other: " + localName);
            IMetaType metaType = currentContainer.getMemberType(localName);

            if (metaType.isSet()) {
                stack.push(currentContainer);
                flagsStack.push(hasMembers);
                hasMembers = false;
                currentContainer = new BaseSet(((MetaSet) metaType).getMemberType(), creditorId);
                level++;
            } else if (metaType.isComplex() && !metaType.isSet()) {
                stack.push(currentContainer);
                currentContainer = new BaseEntity((MetaClass) metaType, batch.getRepDate(), creditorId);
                flagsStack.push(hasMembers);
                hasMembers = false;
                level++;
            } else if (!metaType.isComplex() && !metaType.isSet()) {
                Object obj = null;
                MetaValue metaValue = (MetaValue) metaType;

                try {
                    event = (XMLEvent) xmlEventReader.next();
                    obj = getCastObject(metaValue.getTypeCode(), event.asCharacters().getData().trim());
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
                        .create(currentContainer.getBaseContainerType(), metaType, 0, creditorId, batch.getRepDate(), obj,
                                false, true);

                if (hasOperationNew(startElement)) {
                    IBaseValue newBaseValue = BaseValueFactory.create(currentContainer.getBaseContainerType(),
                            metaType, 0, creditorId, batch.getRepDate(),
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

    void close() {
        try {
            xmlEventReader.close();
            inputStream.close();
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
    }

    private boolean endElement(String localName) {
        if (localName.equals("batch")) {
            //logger.info("batch");
        } else if (localName.equals("entities")) {
            //logger.info("entities");
            currentContainer = null;
            return true;
        } else if (localName.equals("entity")) {
        } else if (localName.equals(currentRootMeta)) {
            rootEntityExpected = true;
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
                                .create(currentContainer.getBaseContainerType(), metaType, 0, creditorId, batch.getRepDate(),
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
                                .create(currentContainer.getBaseContainerType(), metaType, 0, creditorId, batch.getRepDate(),
                                        obj, false, true));
                        flagsStack.pop();
                        hasMembers = true;
                    } else {
                        currentContainer.put(localName, BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, 0, creditorId, batch.getRepDate(),
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
