package kz.bsbnb.usci.receiver.reader.parser;

import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.receiver.reader.impl.StaxEventEntityReader;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by bauka on 9/22/16.
 */
@Component
public class CommonParser<T> {

    @Autowired
    protected IServiceRepository serviceFactory;

    protected Logger logger = Logger.getLogger(CommonParser.class);

    protected Stack<IBaseContainer> stack = new Stack<>();

    protected Stack<Boolean> flagsStack = new Stack<>();

    protected IBaseContainer currentContainer;

    protected Long index = 0L, level = 0L;

    protected boolean hasMembers = false;

    protected int totalCount = 0;

    protected boolean rootEntityExpected = true;

    protected String currentRootMeta = null;

    protected Date reportDate;

    protected long creditorId;

    @Autowired
    protected IMetaFactoryService metaFactoryService;

    protected HashMap<String, MetaClass> metaCache = new HashMap<>();

    protected XMLEventReader xmlEventReader;

    protected MetaClass getMeta(String metaName) {
        if(!metaCache.containsKey(metaName)) {
            MetaClass meta = metaFactoryService.getMetaClass(metaName);
            metaCache.put(metaName, meta);
        }

        return metaCache.get(metaName);
    }

    protected void startElement(StartElement startElement, String localName) {
        if (localName.equals("batch")) {
            logger.debug("batch");
        } else if (localName.equals("entities")) {
            rootEntityExpected = true;
            logger.debug("entities");
        } else if (rootEntityExpected) {
            currentRootMeta = localName;
            rootEntityExpected = false;

            logger.debug(localName);
            MetaClass metaClass = getMeta(localName);
            BaseEntity baseEntity = new BaseEntity(metaClass, reportDate, creditorId);

            if (hasOperationDelete(startElement))
                baseEntity.setOperation(OperationType.DELETE);

            if (hasOperationClose(startElement))
                baseEntity.setOperation(OperationType.CLOSE);

            if(hasOperationInsert(startElement))
                baseEntity.setOperation(OperationType.INSERT);
            currentContainer = baseEntity;
        } else {
            logger.debug("other: " + localName);
            IMetaType metaType = currentContainer.getMemberType(localName);

            if (metaType.isSet()) {
                hasMembers = false;
                stack.push(currentContainer);
                flagsStack.push(hasMembers);
                currentContainer = new BaseSet(((MetaSet) metaType).getMemberType(), creditorId);
                level++;
            } else if (metaType.isComplex()) {
                stack.push(currentContainer);
                currentContainer = new BaseEntity((MetaClass) metaType, reportDate, creditorId);
                flagsStack.push(hasMembers);
                level++;
                hasMembers = false;
            } else {
                Object obj = null;
                MetaValue metaValue = (MetaValue) metaType;

                try {
                    XMLEvent event = (XMLEvent) xmlEventReader.next();
                    obj = DataTypes.getCastObject(metaValue.getTypeCode(), event.asCharacters().getData().trim());
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                    throw new RuntimeException(Errors.compose(Errors.E194, localName, n));
                } catch (ClassCastException ex) {
                    logger.debug("Empty tag: " + localName);
                    level--;
                }

                if (obj != null)
                    hasMembers = true;

                String memberName = localName;
                if (currentContainer.getBaseContainerType() == BaseContainerType.BASE_SET)
                    memberName += "_" + currentContainer.getValueCount();


                IBaseValue baseValue = BaseValueFactory.create(currentContainer.getBaseContainerType(), metaType, 0,
                        creditorId, reportDate, obj, false, true);

                if (hasOperationNew(startElement)) {
                    IBaseValue newBaseValue = BaseValueFactory.create(currentContainer.getBaseContainerType(), metaType,
                            0, creditorId, reportDate, DataTypes.getCastObject(metaValue.getTypeCode(),
                                    startElement.getAttributeByName(new QName("data")).getValue()), false, true);

                    baseValue.setNewBaseValue(newBaseValue);
                }

                currentContainer.put(memberName, baseValue);

                level++;
            }
        }
    }

    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        try {
            return readInner();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            return null;
        }
    }

    protected T readInner() {

        while (xmlEventReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlEventReader.next();

            if (event.isStartDocument()) {
                logger.debug("start document");
            } else if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                startElement(startElement, localName);
            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if (endElement(localName)) {
                    if (currentContainer == null) {
                        break;
                    } else {
                        totalCount++;
                        //((BaseEntity) currentContainer).setBatchId(batchId);
                        currentContainer.setBatchIndex(index);
                        return (T) currentContainer;
                    }
                }
            } else if (event.isEndDocument()) {
                logger.debug("end document");
            } else {
                logger.debug(event);
            }
        }

        return null;
    }

    protected boolean endElement(String localName) {
        if (localName.equals("batch")) {
            logger.debug("batch");
        } else if (localName.equals("entities")) {
            logger.debug("entities");
            currentContainer = null;
            return true;
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
                        ((BaseSet) currentContainer).put(BaseValueFactory.create(currentContainer.getBaseContainerType(),
                                metaType, 0, creditorId, reportDate, obj, false, true));
                        flagsStack.pop();
                        hasMembers = true;
                    } else {
                        hasMembers = flagsStack.pop();
                    }
                } else {
                    /* Временный костыль для филиалов без документа */
                    /* fixme */
                    if (localName.equals("creditor_branch") &&
                            ((((BaseEntity) obj).getBaseValue("docs") == null) ||
                                    ((BaseEntity) obj).getBaseValue("docs").getValue() == null)) obj = null;

                    if (hasMembers) {
                        currentContainer.put(localName, BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, 0, creditorId,
                                        reportDate, obj, false, true));
                        flagsStack.pop();
                        hasMembers = true;
                    } else {
                        currentContainer.put(localName, BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, 0, creditorId,
                                        reportDate, null, false, true));
                        hasMembers = flagsStack.pop();
                    }
                }
            }

            level--;
        }

        return false;
    }

    protected static final String OPERATION_STR = "operation";

    protected boolean hasOperationDelete(StartElement startElement) {
        return startElement.getAttributeByName(new QName(OPERATION_STR)) != null &&
                startElement.getAttributeByName(new QName(OPERATION_STR)).getValue()
                        .equalsIgnoreCase(OperationType.DELETE.toString());
    }

    protected boolean hasOperationClose(StartElement startElement) {
        return startElement.getAttributeByName(new QName(OPERATION_STR)) != null &&
                startElement.getAttributeByName(new QName(OPERATION_STR)).getValue()
                        .equalsIgnoreCase(OperationType.CLOSE.toString());
    }

    protected boolean hasOperationNew(StartElement startElement) {
        return startElement.getAttributeByName(new QName(OPERATION_STR)) != null &&
                startElement.getAttributeByName(new QName(OPERATION_STR)).getValue()
                        .equalsIgnoreCase(OperationType.NEW.toString());
    }

    protected boolean hasOperationInsert(StartElement startElement) {
        return startElement.getAttributeByName(new QName(OPERATION_STR)) != null &&
                startElement.getAttributeByName(new QName(OPERATION_STR)).getValue()
                        .equalsIgnoreCase(OperationType.INSERT.toString());
    }

    public void setXml(String xml) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);
        xmlEventReader = inputFactory.createXMLEventReader(new ByteArrayInputStream(xml.getBytes()));
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public void setCreditorId(long creditorId) {
        this.creditorId = creditorId;
    }
}
