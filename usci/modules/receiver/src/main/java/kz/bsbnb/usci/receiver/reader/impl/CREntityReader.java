package kz.bsbnb.usci.receiver.reader.impl;

import com.google.gson.Gson;
import kz.bsbnb.usci.bconv.cr.parser.impl.MainParser;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
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
import java.util.Stack;

/**
 * @author k.tulbassiyev
 */
@Component
@Scope("step")
public class CREntityReader<T> extends CommonReader<T> {
    private Logger logger = Logger.getLogger(CREntityReader.class);
    private Stack<IBaseContainer> stack = new Stack<IBaseContainer>();
    private IBaseContainer currentContainer;
    private Batch batch;

    private IBatchService batchService;
    private IMetaFactoryService metaFactoryService;
    private ReportBeanRemoteBusiness reportService;

    private Gson gson = new Gson();

    @Autowired
    private MainParser crParser;

    @Value("#{jobParameters['reportId']}")
    private Long reportId;

    @Value("#{jobParameters['actualCount']}")
    private Long actualCount;

    @Value("#{jobParameters['batchId']}")
    private Long batchId;

    @PostConstruct
    public void init() {
        logger.info("Reader init.");
        batchService = serviceRepository.getBatchService();
        metaFactoryService = serviceRepository.getMetaFactoryService();
        reportService = serviceRepository.getReportBeanRemoteBusinessService();

        batch = batchService.getBatch(batchId);

        ZipArchiveInputStream zais = new ZipArchiveInputStream(new ByteArrayInputStream(batch.getContent()));
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = null;

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

        try {
            crParser.parse(xmlEventReader, batch, 1L);
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        logger.info("Read called");

        T entity = (T) crParser.getCurrentBaseEntity();

        long index = crParser.getIndex();

        if (crParser.hasMore()) {
            try {
                crParser.parse(xmlEventReader, batch, index);
            } catch (SAXException e) {
                batchService.addEntityStatus(new EntityStatus()
                                .setBatchId(batchId)
                                .setReceiptDate(new Date())
                                .setDescription("Can't parse")
                                .setStatus(EntityStatuses.ERROR)
                                .setIndex(index)
                );

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
                        .setReceiptDate(new Date())
        );

        batchService.addEntityStatus(new EntityStatus()
                        .setBatchId(batchId)
                        .setStatus(EntityStatuses.TOTAL_COUNT)
                        .setDescription(String.valueOf(crParser.getPackageCount()))
                        .setReceiptDate(new Date())
        );
    }

}
