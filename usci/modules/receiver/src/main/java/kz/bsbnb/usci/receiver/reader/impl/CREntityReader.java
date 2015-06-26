package kz.bsbnb.usci.receiver.reader.impl;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.bconv.cr.parser.impl.MainParser;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.eav.model.json.EntityStatusJModel;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import kz.bsbnb.usci.sync.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.tool.couchbase.BatchStatuses;
import kz.bsbnb.usci.tool.couchbase.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.CouchbaseClientManager;
import org.apache.log4j.Logger;
import org.springframework.batch.core.JobParameters;
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
import java.util.zip.ZipInputStream;

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

    @Autowired
    private CouchbaseClientManager couchbaseClientManager;

    private CouchbaseClient couchbaseClient;

    private Gson gson = new Gson();

    private BatchFullJModel batchFullJModel;

    @Autowired
    private MainParser crParser;

    @Value("#{jobParameters['reportId']}")
    private Long reportId;

    @Value("#{jobParameters['actualCount']}")
    private Long actualCount;

    @PostConstruct
    public void init() {
        logger.info("Reader init.");
        batchService = serviceRepository.getBatchService();
        metaFactoryService = serviceRepository.getMetaFactoryService();
        couchbaseClient = couchbaseClientManager.get();
        reportService = serviceRepository.getReportBeanRemoteBusinessService();

        int counter = 100;
        Object obj = null;

        while(counter-- > 0 && (obj = couchbaseClient.get("batch:" + batchId)) == null) {
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e)
            {
                break;
            }
        }

        if (obj == null) {
            statusSingleton.addBatchStatus(batchId,
                    new BatchStatusJModel(EntityStatuses.ERROR, "Can't load batch from couchbase!", new Date(),
                            userId));

            throw new IllegalStateException("Can't load batch from couchbase!");
        }

        batchFullJModel = gson.fromJson(obj.toString(), BatchFullJModel.class);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(batchFullJModel.getContent());
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        ZipInputStream zis = new ZipInputStream(inputStream);

        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = null;

        try {
            zis.getNextEntry();
            int len;
            /*
            out = new ByteArrayOutputStream((int)entry.getSize());
            int size = (int)entry.getSize();
            while ((len = zis.read(buffer, 0, Math.min(buffer.length, size))) > 0) {
                size -= len;
                out.write(buffer, 0, len);
                if (size <= 0)
                    break;
            }
            */
            // modified for generated batch files
            out = new ByteArrayOutputStream(4096);
            while ((len = zis.read(buffer, 0, 4096)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("Batch: " + batchId + " error in entity reader.");
            statusSingleton.addBatchStatus(batchId,
                    new BatchStatusJModel(BatchStatuses.ERROR, e.getMessage(), new Date(), 0L));
            throw new RuntimeException(e);
        }

        try {
            if (out != null)
                xmlEventReader = inputFactory.createXMLEventReader(new ByteArrayInputStream(out.toByteArray()));
        } catch (XMLStreamException e) {
            statusSingleton.addBatchStatus(batchId,
                    new BatchStatusJModel(BatchStatuses.ERROR, e.getMessage(), new Date(), 0L));
            throw new RuntimeException(e);
        }
        //

        batch = batchService.load(batchId);

        try
        {
            crParser.parse(xmlEventReader, batch, 1L);
        } catch (SAXException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public T read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        logger.info("Read called");

        T entity = (T)crParser.getCurrentBaseEntity();

        long index = crParser.getIndex();

        if (crParser.hasMore()) {
            try
            {
                crParser.parse(xmlEventReader, batch, index);
            } catch (SAXException e)
            {
                EntityStatusJModel entityStatusJModel = new EntityStatusJModel(index,
                        EntityStatuses.ERROR, "Can't parse", new Date());

                statusSingleton.addContractStatus(batchId, entityStatusJModel);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return null;
            }

//            System.out.println("####");
//            System.out.println(entity.toString());

            return entity;
        }

        //statusSingleton.addBatchStatus(batchId, new BatchStatusJModel(
                //Global.BATCH_STATUS_COMPLETED, null, new Date(), userId));

        //EntityStatusArrayJModel statusJModel = statusSingleton.endBatch(batchId);
        //statusSingleton.endBatch(batchId);
        //batchFullJModel.setStatus(statusJModel);

        //couchbaseClient.set("batch:" + batchId, 0, gson.toJson(batchFullJModel));

        saveTotalCounts();

        return null;
    }

    private void saveTotalCounts() {
        reportService.setTotalCount(reportId, crParser.getPackageCount());
        {
            EntityStatusJModel entityStatus = new EntityStatusJModel(
                    0L, EntityStatuses.ACTUAL_COUNT, String.valueOf(actualCount), new Date());
            statusSingleton.addContractStatus(batchId, entityStatus);
        }
        {
            EntityStatusJModel entityStatus = new EntityStatusJModel(
                    0L, EntityStatuses.TOTAL_COUNT, String.valueOf(crParser.getPackageCount()), new Date());
            statusSingleton.addContractStatus(batchId, entityStatus);
        }
    }

}
