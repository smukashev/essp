package kz.bsbnb.usci.receiver.monitor;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.json.BatchFullStatusJModel;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.receiver.singleton.StatusSingleton;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author abukabayev
 */

public class ZipFilesMonitor{
    private final Logger logger = LoggerFactory.getLogger(ZipFilesMonitor.class);

    private CouchbaseClient couchbaseClient;

    //@Autowired
    //private ICouchbaseClientFactory clientFactory;

    @Autowired
    private IServiceRepository serviceFactory;

    @Autowired
    private StatusSingleton statusSingleton;

    @Autowired
    private JobLauncher jobLauncher;

    private Map<String,Job> jobs;

    private List<Creditor> creditors;

    SenderThread sender;

    //private static Gson gson = new Gson();

    public static final int ZIP_BUFFER_SIZE = 1024;
    public static final int MAX_SYNC_QUEUE_SIZE = 128;

    private static final long WAIT_TIMEOUT = 3600; //in sec

    private static final int MAX_FILES_IN_PROCESSING = 20;

    public ZipFilesMonitor(Map<String, Job> jobs) {
        this.jobs = jobs;

        sender = new SenderThread();

        sender.start();
    }

    @PostConstruct
    public void init() {
        System.out.println("Retrieving creditors list");
        creditors = serviceFactory.getRemoteCreditorBusiness().findMainOfficeCreditors();
        System.out.println("Found " + creditors.size() + " creditors");

        Gson gson = new Gson();

        System.setProperty("viewmode", "production");
        //System.setProperty("viewmode", "development");

        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {
            couchbaseClient = new CouchbaseClient(nodes, "test", "");
        } catch (Exception e) {
            logger.error("Error connecting to Couchbase: " + e.getMessage());
        }

        View view = couchbaseClient.getView("batch", "batch_pending");
        Query query = new Query();

        ViewResponse response = couchbaseClient.query(view, query);

        Iterator<ViewRow> rows = response.iterator();

        if (response.size() > 0) {
            System.out.println("Found pending jobs: ");
            System.out.println("-------------------------------------------------------------------------");

            while(rows.hasNext()) {
                ViewRowNoDocs viewRowNoDocs = (ViewRowNoDocs) rows.next();
                long batchId = Long.parseLong(viewRowNoDocs.getKey());

                System.out.println("batchId: " + batchId + ", status: " + viewRowNoDocs.getValue());

                String batchInfoStr = couchbaseClient.get("manifest:" + batchId).toString();

                System.out.println(batchInfoStr);
                System.out.println("-------------------------------------------------------------------------");

                BatchInfo batchInfo = gson.fromJson(batchInfoStr, BatchInfo.class);

                sender.addJob(batchId, batchInfo);
            }
        }
    }

    private class SenderThread extends Thread {

        private class JobInfo {
            long batchId;
            BatchInfo batchInfo;

            private JobInfo(long batchId, BatchInfo batchInfo)
            {
                this.batchId = batchId;
                this.batchInfo = batchInfo;
            }

            public long getBatchId()
            {
                return batchId;
            }

            public BatchInfo getBatchInfo()
            {
                return batchInfo;
            }
        }

        private Stack<JobInfo> ids = new Stack<JobInfo>();

        private synchronized void addJob(long id, BatchInfo batchInfo) {
            ids.push(new JobInfo(id, batchInfo));
        }

        private synchronized JobInfo getNextJob() {
            if (ids.size() > 0)
                return ids.pop();
            return null;
        }

        public void run()
        {
            long sleepCounter = 0;
            while(true) {
                JobInfo nextJob;

                if ((statusSingleton != null && statusSingleton.getProcessingCount() > MAX_FILES_IN_PROCESSING) || (serviceFactory != null && serviceFactory.getEntityService().getQueueSize() > MAX_SYNC_QUEUE_SIZE)) {
                    try
                    {
                        sleep(1000L);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    sleepCounter++;
                    if (sleepCounter > WAIT_TIMEOUT) {
                        throw new IllegalStateException("Sync timeout in reader.");
                    }
                    continue;
                }
                sleepCounter = 0;

                if ((nextJob = getNextJob()) != null) {
                    logger.debug("Sending file with batchId: " + nextJob.getBatchId());
                    System.out.println("Sending file with batchId: " + nextJob.getBatchId());

                    try {
                        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
                        jobParametersBuilder.addParameter("batchId", new JobParameter(nextJob.getBatchId()));
                        jobParametersBuilder.addParameter("userId", new JobParameter(nextJob.getBatchInfo().getUserId()));

                        Job job = jobs.get(nextJob.getBatchInfo().getBatchType());

                        if (job != null) {
                            jobLauncher.run(job, jobParametersBuilder.toJobParameters());
                        } else {
                            logger.error("Unknown batch file type: " + nextJob.getBatchInfo().getBatchType() +
                                    " in batch with id: " + nextJob.getBatchId());

                            statusSingleton.addBatchStatus(nextJob.getBatchId(),
                                    new BatchStatusJModel(Global.BATCH_STATUS_ERROR, "Unknown batch file type: " +
                                            nextJob.getBatchInfo().getBatchType(), new Date(), nextJob.getBatchInfo().getUserId()));
                        }

                        sleep(10000);
                    } catch (JobExecutionAlreadyRunningException e) {
                        e.printStackTrace();
                    } catch (JobRestartException e) {
                        e.printStackTrace();
                    } catch (JobInstanceAlreadyCompleteException e) {
                        e.printStackTrace();
                    } catch (JobParametersInvalidException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try
                    {
                        sleep(1000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    logger.debug("No files to send");
                }
            }
        }
    }

    public void saveData(BatchInfo batchInfo, String filename, byte[] bytes){
        IBatchService batchService = serviceFactory.getBatchService();

        Batch batch = new Batch(new java.sql.Date(new java.util.Date().getTime()));
        batch.setUserId(batchInfo.getUserId());
        long batchId = batchService.save(batch);

        Long cId = -1l;

        if(batchInfo.getUserId() != 100500L) {
            List<Creditor> cList = serviceFactory.getUserService().getPortalUserCreditorList(batchInfo.getUserId());

            if (cList.size() > 0) {
                cId = cList.get(0).getId();
            } else {
                cId = -1L;
            }
        } else {
            if(batchInfo.getAdditionalParams() != null) {
                String creditorCode = batchInfo.getAdditionalParams().get("CODE");

                boolean foundCreditor = false;
                for (Creditor creditor : creditors) {
                    if (creditor.getCode() != null) {
                        if (creditor.getCode().equals(creditorCode)) {
                            cId = creditor.getId();
                            foundCreditor = true;
                            break;
                        }
                    }
                }

                if (!foundCreditor) {
                    String creditorBIN = batchInfo.getAdditionalParams().get("BIN");

                    for (Creditor creditor : creditors) {
                        if (creditor.getBIN() != null) {
                            if (creditor.getBIN().equals(creditorBIN)) {
                                cId = creditor.getId();
                                foundCreditor = true;
                                break;
                            }
                        }
                    }

                    if (!foundCreditor) {
                        throw new IllegalStateException("Can't find creditor with code: " + creditorCode + " or BIN: " +
                            creditorBIN);
                    }
                }
            }
        }

        BatchFullJModel batchFullJModel = new BatchFullJModel(batchId, filename, bytes, new Date(),
                batchInfo.getUserId(), cId);
        statusSingleton.startBatch(batchId, batchFullJModel, batchInfo);
        statusSingleton.addBatchStatus(batchId,
                new BatchStatusJModel(Global.BATCH_STATUS_WAITING, null, new Date(), batchInfo.getUserId()));
        statusSingleton.addBatchStatus(batchId,
                new BatchStatusJModel(Global.BATCH_STATUS_PROCESSING, null, new Date(), batchInfo.getUserId()));

        sender.addJob(batchId, batchInfo);

//        try {
//            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
//            jobParametersBuilder.addParameter("batchId", new JobParameter(batchId));
//            jobParametersBuilder.addParameter("userId", new JobParameter(batchInfo.getUserId()));
//
//            Job job = jobs.get(batchInfo.getBatchType());
//
//            if (job != null) {
//                jobLauncher.run(job, jobParametersBuilder.toJobParameters());
//            } else {
//                logger.error("Unknown batch file type: " + batchInfo.getBatchType() + " in batch with id: " + batchId);
//
//                statusSingleton.addBatchStatus(batchId,
//                        new BatchStatusJModel(Global.BATCH_STATUS_ERROR, "Unknown batch file type: " +
//                                batchInfo.getBatchType(), new Date(), batchInfo.getUserId()));
//            }
//        } catch (JobExecutionAlreadyRunningException e) {
//            e.printStackTrace();
//        } catch (JobRestartException e) {
//            e.printStackTrace();
//        } catch (JobInstanceAlreadyCompleteException e) {
//            e.printStackTrace();
//        } catch (JobParametersInvalidException e) {
//            e.printStackTrace();
//        }
    }

    public byte[] inputStreamToByte(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = in.read(data,0,data.length)) != -1){
            buffer.write(data,0,nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public void readFiles(String filename) {
        readFiles(filename, null);
    }

    public void readFiles(String filename, Long userId) {
        BatchInfo batchInfo = new BatchInfo();
        try{

            ZipFile zipFile = new ZipFile(filename);

            ZipEntry manifestEntry = zipFile.getEntry("manifest.xml");

            InputStream inManifest = zipFile.getInputStream(manifestEntry);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = null;
            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            Document document = null;
            try {
                document = documentBuilder.parse(inManifest);
            } catch (SAXException e) {
                e.printStackTrace();
            }


            batchInfo.setBatchType(document.getElementsByTagName("type").item(0).getTextContent());
            batchInfo.setBatchName(document.getElementsByTagName("name").item(0).getTextContent());

            batchInfo.setUserId(
                    userId == null ?
                    Long.parseLong(document.getElementsByTagName("userid").item(0).getTextContent()) :
                            userId
            );

            batchInfo.setSize(Long.parseLong(document.getElementsByTagName("size").item(0).getTextContent()));


            Date date = null;
            try {
                date = new SimpleDateFormat("dd.MM.yyyy").parse(document.getElementsByTagName("date").item(0).getTextContent());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            batchInfo.setRepDate(date);

            System.out.println(batchInfo.getSize());
            System.out.println(batchInfo.getRepDate());


            ZipEntry dataEntry = zipFile.getEntry(batchInfo.getBatchName());
            InputStream inData = zipFile.getInputStream(dataEntry);


            saveData(batchInfo,filename,inputStreamToByte(inData));


        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void readFilesWithoutUser(String filename) {
        BatchInfo batchInfo = new BatchInfo();
        try{

            ZipFile zipFile = new ZipFile(filename);

            ZipEntry manifestEntry = zipFile.getEntry("manifest.xml");

            InputStream inManifest = zipFile.getInputStream(manifestEntry);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = null;
            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            Document document = null;
            try {
                document = documentBuilder.parse(inManifest);
            } catch (SAXException e) {
                e.printStackTrace();
            }


            batchInfo.setBatchType(document.getElementsByTagName("type").item(0).getTextContent());
            batchInfo.setBatchName(document.getElementsByTagName("name").item(0).getTextContent());

            batchInfo.setUserId(100500L);
            NodeList nlist = document.getElementsByTagName("property");
            HashMap<String, String> params = new HashMap<String, String>();
            for (int i = 0; i < nlist.getLength(); i++) {
                Node node = nlist.item(i);
                NodeList childrenList = node.getChildNodes();
                String name = "";
                String value = "";
                for (int j = 0; j < childrenList.getLength(); j++) {
                    Node curChild = childrenList.item(j);
                    if (curChild.getNodeName().equals("name")) {
                        name = curChild.getTextContent();
                    }
                    if (curChild.getNodeName().equals("value")) {
                        value = curChild.getTextContent();
                    }
                }
                params.put(name, value);
            }

            batchInfo.setAdditionalParams(params);

            batchInfo.setSize(Long.parseLong(document.getElementsByTagName("size").item(0).getTextContent()));


            Date date = null;
            try {
                date = new SimpleDateFormat("dd.MM.yyyy").parse(document.getElementsByTagName("date").item(0).getTextContent());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            batchInfo.setRepDate(date);

            System.out.println(batchInfo.getSize());
            System.out.println(batchInfo.getRepDate());


            ZipEntry dataEntry = zipFile.getEntry(batchInfo.getBatchName());
            InputStream inData = zipFile.getInputStream(dataEntry);


            saveData(batchInfo,filename,inputStreamToByte(inData));


        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void monitor(Path path) throws InterruptedException, IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        IEntityService entityService = serviceFactory.getEntityService();

        boolean valid = true;
        long sleepCounter = 0;
        do {
            while(entityService.getQueueSize() > MAX_SYNC_QUEUE_SIZE) {
                Thread.sleep(1000);

                sleepCounter++;
                if (sleepCounter > WAIT_TIMEOUT) {
                    throw new IllegalStateException("Sync timeout in reader.");
                }
            }
            sleepCounter = 0;

            WatchKey watchKey = watchService.take();

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                    String fileName = event.context().toString();
                    System.out.println("File Created:" + fileName);

                    Thread.sleep(1000);

                    readFiles(path+"/"+fileName);

                    System.out.println("File sent to parser:" + fileName);

                }
            }
            valid = watchKey.reset();

        } while (valid);

    }
    public byte[] extract(byte[] zippedBytes) throws IOException {
        ByteArrayInputStream bais = null;
        ZipArchiveInputStream zis = null;

        try {
            bais = new ByteArrayInputStream(zippedBytes);
            zis = new ZipArchiveInputStream(bais);

            while (zis.getNextZipEntry() != null) {
                ByteArrayOutputStream baos = null;
                try {
                    int size;
                    byte[] buffer = new byte[ZIP_BUFFER_SIZE];

                    baos = new ByteArrayOutputStream();

                    while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                        baos.write(buffer, 0, size);
                    }
                    return baos.toByteArray();
                } finally {
                    if (baos != null) {
                        baos.flush();
                        baos.close();
                    }
                }
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
        }
        throw new IOException("ZIP file does not contain any files.");
    }

}
