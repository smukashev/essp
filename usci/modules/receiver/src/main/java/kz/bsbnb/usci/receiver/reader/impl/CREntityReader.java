package kz.bsbnb.usci.receiver.reader.impl;

import kz.bsbnb.usci.bconv.cr.parser.impl.MainParser;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.ReportBeanRemoteBusiness;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.log4j.Logger;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

@Component
@Scope("step")
public class CREntityReader<T> extends CommonReader<T> {
    private Logger logger = Logger.getLogger(CREntityReader.class);

    @Autowired
    private IServiceRepository serviceFactory;

    @Autowired
    private MainParser crParser;

    @PostConstruct
    public void init() {
        batchService = serviceRepository.getBatchService();
        reportService = serviceRepository.getReportBeanRemoteBusinessService();

        batch = batchService.getBatch(batchId);

        ZipArchiveInputStream zais = new ZipArchiveInputStream(new ByteArrayInputStream(batch.getContent()));
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out;

        try {
            zais.getNextZipEntry();
            int len;
            out = new ByteArrayOutputStream(4096);
            while ((len = zais.read(buffer, 0, 4096)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("Batch: " + batchId + " error in entity reader.");

            batchService.addBatchStatus(new BatchStatus()
                    .setBatchId(batchId)
                    .setStatus(BatchStatuses.ERROR)
                    .setDescription(e.getMessage())
                    .setReceiptDate(new Date()));

            throw new RuntimeException(e);
        }

        try {
            if (validateSchema(false, new ByteArrayInputStream(out.toByteArray()))) {
                xmlEventReader = inputFactory.createXMLEventReader(new ByteArrayInputStream(out.toByteArray()));
            } else {
                throw new RuntimeException(Errors.compose(Errors.E193));
            }
        } catch (XMLStreamException | SAXException | IOException e) {
            batchService.addBatchStatus(new BatchStatus()
                    .setBatchId(batchId)
                    .setStatus(BatchStatuses.ERROR)
                    .setDescription("XML не прошёл проверку XSD: " + e.getMessage())
                    .setReceiptDate(new Date()));

            throw new RuntimeException(e);
        }

        try {
            crParser.parse(xmlEventReader, batch, 1L, creditorId);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        T entity = (T) crParser.getCurrentBaseEntity();

        long index = crParser.getIndex();

        waitSync(serviceFactory);

        if (crParser.hasMore()) {
            try {
                crParser.parse(xmlEventReader, batch, index, creditorId);
            } catch (SAXException e) {
                batchService.addEntityStatus(new EntityStatus()
                        .setBatchId(batchId)
                        .setReceiptDate(new Date())
                        .setDescription("Can't parse")
                        .setStatus(EntityStatuses.ERROR)
                        .setIndex(index));

                e.printStackTrace();
                return null;
            }

            ((BaseEntity) entity).setBatchId(batchId);
            ((BaseEntity) entity).setIndex(index);

            return entity;
        }

        saveTotalCounts();

        return null;
    }

    private void saveTotalCounts() {
        reportService.setTotalCount(reportId, crParser.getPackageCount());

        batchService.addEntityStatus(new EntityStatus()
                .setBatchId(batchId)
                .setStatus(EntityStatuses.ACTUAL_COUNT)
                .setDescription(String.valueOf(actualCount))
                .setReceiptDate(new Date()));

        batchService.addEntityStatus(new EntityStatus()
                .setBatchId(batchId)
                .setStatus(EntityStatuses.TOTAL_COUNT)
                .setDescription(String.valueOf(crParser.getPackageCount()))
                .setReceiptDate(new Date()));
    }
}
