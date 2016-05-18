package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Scope("step")
public class StaxEventEntityReader<T> extends CommonReader<T> {
    @Autowired
    private IServiceRepository serviceFactory;

    private Logger logger = Logger.getLogger(StaxEventEntityReader.class);

    private Stack<IBaseContainer> stack = new Stack<>();

    private Stack<Boolean> flagsStack = new Stack<>();

    private IBaseContainer currentContainer;

    private Long index = 1L, level = 0L;

    private boolean hasMembers = false;

    private int totalCount = 0;

    private boolean rootEntityExpected = false;

    private String currentRootMeta = null;

    @PostConstruct
    public void init() {
        batchService = serviceRepository.getBatchService();
        reportService = serviceFactory.getReportBeanRemoteBusinessService();
        metaFactoryService = serviceRepository.getMetaFactoryService();

        batch = batchService.getBatch(batchId);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(batch.getContent());
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry entry;
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = null;
        try {
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("manifest.xml"))
                    continue;

                int len;

                out = new ByteArrayOutputStream(4096);
                while ((len = zis.read(buffer, 0, 4096)) > 0) {
                    out.write(buffer, 0, len);
                }
                break;
            }
        } catch (IOException e) {
            logger.error("Batch: " + batchId + " error in entity reader.");

            batchService.addBatchStatus(new BatchStatus()
                    .setBatchId(batchId)
                    .setStatus(BatchStatuses.ERROR)
                    .setDescription(e.getMessage())
                    .setReceiptDate(new Date()));

            throw new IllegalStateException(e);
        }

        try {
            if (out != null) {
                if (validateSchema(true, new ByteArrayInputStream(out.toByteArray()))) {
                    xmlEventReader = inputFactory.createXMLEventReader(new ByteArrayInputStream(out.toByteArray()));
                } else {
                    throw new RuntimeException(Errors.compose(Errors.E193));
                }
            }
        } catch (XMLStreamException | SAXException | IOException e) {
            batchService.addBatchStatus(new BatchStatus()
                    .setBatchId(batchId)
                    .setStatus(BatchStatuses.ERROR)
                    .setDescription(e.getMessage())
                    .setReceiptDate(new Date()));

            throw new RuntimeException(e);
        }
    }

    private HashMap<String, MetaClass> metaCache = new HashMap<>();

    private MetaClass getMeta(String metaName) {
        if(!metaCache.containsKey(metaName)) {
            MetaClass meta = metaFactoryService.getMetaClass(metaName);
            metaCache.put(metaName, meta);
        }

        return metaCache.get(metaName);
    }

    private void startElement(StartElement startElement, String localName) {
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
            BaseEntity baseEntity = new BaseEntity(metaClass, batch.getRepDate(), creditorId);

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
                currentContainer = new BaseEntity((MetaClass) metaType, batch.getRepDate(), creditorId);
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
                        creditorId, batch.getRepDate(), obj, false, true);

                if (hasOperationNew(startElement)) {
                    IBaseValue newBaseValue = BaseValueFactory.create(currentContainer.getBaseContainerType(), metaType,
                            0, creditorId, batch.getRepDate(), DataTypes.getCastObject(metaValue.getTypeCode(),
                                        startElement.getAttributeByName(new QName("data")).getValue()), false, true);

                    baseValue.setNewBaseValue(newBaseValue);
                }

                currentContainer.put(memberName, baseValue);

                level++;
            }
        }
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        try {
            return readInner();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);

            batchService.addEntityStatus(new EntityStatus()
                    .setBatchId(batchId)
                    .setStatus(EntityStatuses.ERROR)
                    .setDescription(e.getLocalizedMessage())
                    .setReceiptDate(new Date()));
            
            return null;
        }
    }

    private T readInner() {
        waitSync(serviceFactory);

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
                        ((BaseEntity) currentContainer).setBatchId(batchId);
                        ((BaseEntity) currentContainer).setIndex(index);
                        return (T) currentContainer;
                    }
                }
            } else if (event.isEndDocument()) {
                logger.debug("end document");
            } else {
                logger.debug(event);
            }
        }

        saveTotalCounts();

        return null;
    }

    private boolean endElement(String localName) {
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
                                metaType, 0, creditorId, batch.getRepDate(), obj, false, true));
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
                                    batch.getRepDate(), obj, false, true));
                        flagsStack.pop();
                        hasMembers = true;
                    } else {
                        currentContainer.put(localName, BaseValueFactory
                                .create(currentContainer.getBaseContainerType(), metaType, 0, creditorId,
                                        batch.getRepDate(), null, false, true));
                        hasMembers = flagsStack.pop();
                    }
                }
            }

            level--;
        }

        return false;
    }

    private void saveTotalCounts() {
        reportService.setTotalCount(reportId, totalCount);

        batchService.addEntityStatus(new EntityStatus()
                .setBatchId(batchId)
                .setStatus(EntityStatuses.ACTUAL_COUNT)
                .setDescription(String.valueOf(actualCount))
                .setReceiptDate(new Date()));

        batchService.addEntityStatus(new EntityStatus()
                .setBatchId(batchId)
                .setStatus(EntityStatuses.TOTAL_COUNT)
                .setDescription(String.valueOf(totalCount))
                .setReceiptDate(new Date()));
    }

    private static final String OPERATION_STR = "operation";

    private boolean hasOperationDelete(StartElement startElement) {
        return startElement.getAttributeByName(new QName(OPERATION_STR)) != null &&
                startElement.getAttributeByName(new QName(OPERATION_STR)).getValue()
                        .equalsIgnoreCase(OperationType.DELETE.toString());
    }

    private boolean hasOperationClose(StartElement startElement) {
        return startElement.getAttributeByName(new QName(OPERATION_STR)) != null &&
                startElement.getAttributeByName(new QName(OPERATION_STR)).getValue()
                        .equalsIgnoreCase(OperationType.CLOSE.toString());
    }

    private boolean hasOperationNew(StartElement startElement) {
        return startElement.getAttributeByName(new QName(OPERATION_STR)) != null &&
                startElement.getAttributeByName(new QName(OPERATION_STR)).getValue()
                        .equalsIgnoreCase(OperationType.NEW.toString());
    }

    private boolean hasOperationInsert(StartElement startElement) {
        return startElement.getAttributeByName(new QName(OPERATION_STR)) != null &&
                startElement.getAttributeByName(new QName(OPERATION_STR)).getValue()
                        .equalsIgnoreCase(OperationType.INSERT.toString());
    }
}
