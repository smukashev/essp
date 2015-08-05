package kz.bsbnb.usci.receiver.reader.impl;

import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import kz.bsbnb.usci.sync.service.ReportBeanRemoteBusiness;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Scope("step")
public class StaxEventEntityReader<T> extends CommonReader<T> {
    @Autowired
    private IServiceRepository serviceFactory;

    @Value("#{jobParameters['reportId']}")
    private Long reportId;

    @Value("#{jobParameters['actualCount']}")
    private Long actualCount;

    private static final long WAIT_TIMEOUT = 3600; // in sec
    private Logger logger = Logger.getLogger(StaxEventEntityReader.class);
    private Stack<IBaseContainer> stack = new Stack<>();
    private Stack<Boolean> flagsStack = new Stack<>();
    private IBaseContainer currentContainer;
    private Batch batch;
    private Long index = 1L, level = 0L;
    private IBatchService batchService;
    private IMetaFactoryService metaFactoryService;
    private ReportBeanRemoteBusiness reportService;

    private boolean hasMembers = false;

    private int totalCount = 0;

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
                    .setReceiptDate(new Date())
            );

            throw new RuntimeException(e);
        }

        try {
            if (out != null)
                xmlEventReader = inputFactory.createXMLEventReader(new ByteArrayInputStream(out.toByteArray()));
        } catch (XMLStreamException e) {
            batchService.addBatchStatus(new BatchStatus()
                    .setBatchId(batchId)
                    .setStatus(BatchStatuses.ERROR)
                    .setDescription(e.getMessage())
                    .setReceiptDate(new Date())
            );

            throw new RuntimeException(e);
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
            logger.debug("batch");
        } else if (localName.equals("entities")) {
            logger.debug("entities");
        } else if (localName.equals("entity")) {
            logger.debug("entity " + startElement.getAttributeByName(new QName("class")).getValue());
            BaseEntity baseEntity = metaFactoryService.getBaseEntity(
                    startElement.getAttributeByName(new QName("class")).getValue(), batch.getRepDate());

            if (hasOperationDelete(startElement))
                baseEntity.setOperation(OperationType.DELETE);

            if (hasOperationClose(startElement))
                baseEntity.setOperation(OperationType.CLOSE);

            currentContainer = baseEntity;
        } else {
            logger.debug("other: " + localName);
            IMetaType metaType = currentContainer.getMemberType(localName);

            if (metaType.isSet()) {
                stack.push(currentContainer);
                flagsStack.push(hasMembers);
                hasMembers = false;
                currentContainer = metaFactoryService.getBaseSet(((MetaSet) metaType).getMemberType());
                level++;
            } else if (metaType.isComplex()) {
                stack.push(currentContainer);
                currentContainer = new BaseEntity((MetaClass) metaType, batch.getRepDate());
                flagsStack.push(hasMembers);
                hasMembers = false;
                level++;
            } else {
                Object o = null;
                MetaValue metaValue = (MetaValue) metaType;

                try {
                    event = (XMLEvent) xmlEventReader.next();
                    o = parserHelper.getCastObject(metaValue.getTypeCode(), event.asCharacters().getData());
                } catch (NumberFormatException n) {
                    n.printStackTrace();
                    logger.error("Cast error: " + localName + ", exception text: " + n.getMessage());
                    throw new RuntimeException("Cast error: " + localName + ", exception text: " + n.getMessage());
                } catch (ClassCastException ex) {
                    logger.debug("Empty tag: " + localName);
                    level--;
                }

                if (o != null)
                    hasMembers = true;

                String memberName = localName;
                if (currentContainer.getBaseContainerType() == BaseContainerType.BASE_SET)
                    memberName += "_" + currentContainer.getValueCount();


                IBaseValue baseValue = BaseValueFactory
                        .create(currentContainer.getBaseContainerType(), metaType, batch, index, o);

                if (hasOperationNew(startElement)) {
                    IBaseValue newBaseValue = BaseValueFactory
                            .create(currentContainer.getBaseContainerType(), metaType, batch, index,
                                    parserHelper.getCastObject(metaValue.getTypeCode(),
                                            startElement.getAttributeByName(new QName("data")).getValue()));

                    baseValue.setNewBaseValue(newBaseValue);
                }

                currentContainer.put(memberName, baseValue);

                level++;
            }
        }
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        logger.debug("Read called");
        logger.debug("Sync queue size: " + serviceFactory.getEntityService().getQueueSize());
        long sleepCounter = 0;
        while (serviceFactory.getEntityService().getQueueSize() > ZipFilesMonitor.MAX_SYNC_QUEUE_SIZE) {
            logger.debug("Sync queue limit exceeded: " + serviceFactory.getEntityService().getQueueSize());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sleepCounter++;
            if (sleepCounter > WAIT_TIMEOUT) {
                throw new IllegalStateException("Sync timeout in reader.");
            }
        }
        while (xmlEventReader.hasNext()) {
            XMLEvent event = (XMLEvent) xmlEventReader.next();

            if (event.isStartDocument()) {
                logger.debug("start document");
            } else if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                startElement(event, startElement, localName);
            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if (endElement(localName)) {
                    if (currentContainer == null) {
                        break;
                    } else {
                        totalCount++;
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

    private void saveTotalCounts() {
        reportService.setTotalCount(reportId, totalCount);

        batchService.addEntityStatus(new EntityStatus()
                .setBatchId(batchId)
                        .setStatus(EntityStatuses.ACTUAL_COUNT)
                        .setDescription(String.valueOf(actualCount))
                        .setReceiptDate(new Date())
        );

        batchService.addEntityStatus(new EntityStatus()
                .setBatchId(batchId)
                        .setStatus(EntityStatuses.TOTAL_COUNT)
                        .setDescription(String.valueOf(totalCount))
                        .setReceiptDate(new Date())
        );
    }

    public boolean endElement(String localName) {
        if (localName.equals("batch")) {
            logger.debug("batch");
        } else if (localName.equals("entities")) {
            logger.debug("entities");
            currentContainer = null;
            return true;
        } else if (localName.equals("entity")) {
            index++;
            return true;
        } else {
            IMetaType metaType;

            if (level == stack.size())
                metaType = stack.peek().getMemberType(localName);
            else
                metaType = currentContainer.getMemberType(localName);

            if (metaType.isComplex() || metaType.isSet()) {
                Object o = currentContainer;
                currentContainer = stack.pop();

                if (currentContainer.isSet()) {
                    if (hasMembers) {
                        ((BaseSet) currentContainer).put(BaseValueFactory
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
