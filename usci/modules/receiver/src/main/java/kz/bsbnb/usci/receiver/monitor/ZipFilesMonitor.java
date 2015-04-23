package kz.bsbnb.usci.receiver.monitor;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.google.gson.Gson;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.model.json.*;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.tool.couchbase.BatchStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusSingleton;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.tool.status.ReceiverStatusSingleton;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang.time.DateUtils;
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
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author abukabayev
 */

public class ZipFilesMonitor{
    private final Logger logger = LoggerFactory.getLogger(ZipFilesMonitor.class);

    private CouchbaseClient couchbaseClient;

    @Autowired
    private IServiceRepository serviceFactory;

    @Autowired
    private StatusSingleton statusSingleton;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private ReceiverStatusSingleton receiverStatusSingleton;

    private Map<String,Job> jobs;

    private List<Creditor> creditors;

    SenderThread sender;

    public static final int ZIP_BUFFER_SIZE = 1024;
    public static final int MAX_SYNC_QUEUE_SIZE = 512;

    private static final long WAIT_TIMEOUT = 360; //in 10 sec units

    public ZipFilesMonitor(Map<String, Job> jobs) {
        this.jobs = jobs;
        sender = new SenderThread();
        sender.start();
    }

    public boolean restartBatch(long batchId) {
        Gson gson = new Gson();
        IBatchService batchService = serviceFactory.getBatchService();

        System.setProperty("viewmode", "production");
        //System.setProperty("viewmode", "development");

        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {
            couchbaseClient = new CouchbaseClient(nodes, "test", "");
        } catch (Exception e) {
            logger.error("Error connecting to Couchbase: " + e.getMessage());
        }

        try {
            Object batchObject = couchbaseClient.get("batch:" + batchId);
            Object manifestObject = couchbaseClient.get("manifest:" + batchId);

            if (batchObject == null || manifestObject == null) {
                System.out.println("Batch with id: " + batchId + " has no manifest or batch. Restart failed.");
                return false;
            }

            try {
                batchService.load(batchId);
            } catch(Exception e) {
                System.out.println("Can't get batch from eav DB. Skipped.");
                return false;
            }

            String batchInfoStr = manifestObject.toString();
            BatchInfo batchInfo = gson.fromJson(batchInfoStr, BatchInfo.class);

            System.out.println(batchId + " - restarted");
            sender.addJob(batchId, batchInfo);
            receiverStatusSingleton.batchReceived();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            couchbaseClient.shutdown();
        }

        return false;
    }

    @PostConstruct
    public void init() {
        sender.setReceiverStatusSingleton(receiverStatusSingleton);
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

        IBatchService batchService = serviceFactory.getBatchService();

        Iterator<ViewRow> rows = null;
        ViewResponse response = null;

        while(true) {
                if(rows == null) {
                    View view = couchbaseClient.getView("batch", "batch_pending");
                    Query query = new Query();
                    query.setDebug(false);
                    query.setStale(Stale.FALSE);

                    response = couchbaseClient.query(view, query);

                    rows = response.iterator();
                }

                if (response.size() > 0) {
                    System.out.println("Found pending jobs: " + response.size());
                    System.out.println("-------------------------------------------------------------------------");

                    int jobsRestarted = 0;

                    while(rows.hasNext()) {
                        try {
                            ViewRowNoDocs viewRowNoDocs = (ViewRowNoDocs) rows.next();
                            long batchId = Long.parseLong(viewRowNoDocs.getKey());

                            if (viewRowNoDocs.getValue().equals("ERROR"))
                            {
                                System.out.println("Skipped because of error!");
                                continue;
                            }

                            System.out.println("batchId: " + batchId + ", status: " + viewRowNoDocs.getValue());

                            Object batchObject = couchbaseClient.get("batch:" + batchId);
                            Object manifestObject = couchbaseClient.get("manifest:" + batchId);

                            if (batchObject == null || manifestObject == null) {
                                System.out.println("Batch with id: " + batchId + " has no manifest or batch. Restart failed.");
                                continue;
                            }

                            System.out.println(manifestObject.toString());
                            System.out.println("-------------------------------------------------------------------------");

                            BatchInfo batchInfo = gson.fromJson(manifestObject.toString(), BatchInfo.class);
                            try {
                                batchService.load(batchId);
                            } catch(Exception e) {
                                System.out.println("Can't get batch from eav DB. Skipped.");
                            }



    //                        if (DataUtils.compareBeginningOfTheDay(batchInfo.getRepDate(), cal.getTime()) != 0)
    //                        {
    //                            System.out.println("Skipping wrone dates: " + batchInfo.getRepDate());
    //                            System.out.println("Must be: " + cal.getTime());
    //                            continue;
    //                        }

                            sender.addJob(batchId, batchInfo);
                            receiverStatusSingleton.batchReceived();
                            System.out.println("Restarted job #" + ++jobsRestarted);
                        } catch (Exception e) {
                            System.out.println("Error in pending batches view: " + e.getMessage());
                            System.out.println("Retrying...");
                        }
                    }
                }
                break;

        }

//        restartBatch(22265);

        //////////////////////////

        /*File f = new File("D:\\usci\\out.txt");
        FileOutputStream fout = null;
        try {
            f.createNewFile();

            fout = new FileOutputStream(f);

            while(true) {
                try {
                    for(int batchId = 1; batchId < 5344; batchId++) {
                        Object batchObject = couchbaseClient.get("batch:" + batchId);
                        Object manifestObject = couchbaseClient.get("manifest:" + batchId);
                        Object batchStatusObject = couchbaseClient.get("batch_status:" + batchId);

                        if (batchObject == null || manifestObject == null) {
                            System.out.println("Batch with id: " + batchId + " has no manifest or batch!");

                            if (batchObject != null) {
                                couchbaseClient.delete("batch:" + batchId);
                            }
                            if (manifestObject != null) {
                                couchbaseClient.delete("manifest:" + batchId);
                            }
                            if (batchStatusObject != null) {
                                couchbaseClient.delete("batch_status:" + batchId);
                            }
                            continue;
                        }

                        String batchStr = batchObject.toString();

                        BatchFullJModel batchFull = gson.fromJson(batchStr, BatchFullJModel.class);



                        String batchInfoStr = manifestObject.toString();

                        BatchInfo batchInfo = gson.fromJson(batchInfoStr, BatchInfo.class);

                        View view = couchbaseClient.getView("batch", "entity_status");
                        Query query = new Query();
                        query.setDescending(true);
                        query.setRangeEnd("[" + batchId + ", 0]");
                        query.setRangeStart("[" + batchId + ", 999999999999999]");


                        ViewResponse response = couchbaseClient.query(view, query);

                        Iterator<ViewRow> rows = response.iterator();

                        int row_count = 0;
                        int error_count = 0;
                        while(rows.hasNext()) {
                            ViewRow viewRowNoDocs = rows.next();

                            row_count++;

                            EntityStatusArrayJModel batchFullStatusJModel =
                                    gson.fromJson(viewRowNoDocs.getValue(), EntityStatusArrayJModel.class);

                            boolean errorFound = false;
                            boolean completedFound = false;
                            for (EntityStatusJModel csajm : batchFullStatusJModel.getEntityStatuses()) {
                                if (csajm.getProtocol().equals("ERROR"))
                                {
                                    errorFound = true;
                                }
                                if (csajm.getProtocol().equals("COMPLETED"))
                                {
                                    completedFound = true;
                                }
                            }
                            if (errorFound && !completedFound)
                                error_count++;
                        }

//                        if (error_count > 0 || row_count != batchInfo.getSize()) {
//                            System.out.println(batchId + " - " + batchInfo.getSize() + "/" + row_count + " - " + error_count);
//                            sender.addJob(batchId, batchInfo);
//                            receiverStatusSingleton.batchReceived();
//                        }

                        fout.write((batchId + "," +
                                batchFull.getFileName() + "," +
                                batchInfo.getSize() + "," + row_count + "," + error_count + "\n").getBytes());
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Error in pending batches view: " + e.getMessage());
                    e.printStackTrace();
                    System.out.println("Retrying...");
                }
            }
            System.out.println("Done");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }      */
        //////////////////////////

        couchbaseClient.shutdown();
    }

    private class SenderThread extends Thread {
        private ReceiverStatusSingleton receiverStatusSingleton;

        public ReceiverStatusSingleton getReceiverStatusSingleton()
        {
            return receiverStatusSingleton;
        }

        public void setReceiverStatusSingleton(ReceiverStatusSingleton receiverStatusSingleton)
        {
            this.receiverStatusSingleton = receiverStatusSingleton;
        }

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

        private ConcurrentLinkedQueue<JobInfo> ids = new ConcurrentLinkedQueue<JobInfo>();

        private synchronized void addJob(long id, BatchInfo batchInfo) {
            ids.add(new JobInfo(id, batchInfo));
        }

        private synchronized JobInfo getNextJob() {
            if (ids.size() > 0)
                return ids.poll();
            return null;
        }

        public void run()
        {
            long sleepCounter = 0;
            while(true) {
                JobInfo nextJob;

                if (serviceFactory != null && serviceFactory.getEntityService().getQueueSize() > MAX_SYNC_QUEUE_SIZE) {
                    System.out.println("Can't send more files because of file limit or sync queue overload.");
                    try
                    {
                        sleep(10000L);
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
                            receiverStatusSingleton.batchStarted();
                            statusSingleton.addBatchStatus(nextJob.getBatchId(),
                                    new BatchStatusJModel(BatchStatuses.PROCESSING, null, new Date(),
                                            nextJob.getBatchInfo().getUserId()));
                        } else {
                            logger.error("Unknown batch file type: " + nextJob.getBatchInfo().getBatchType() +
                                    " in batch with id: " + nextJob.getBatchId());

                            statusSingleton.addBatchStatus(nextJob.getBatchId(),
                                    new BatchStatusJModel(BatchStatuses.ERROR, "Unknown batch file type: " +
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
        receiverStatusSingleton.batchReceived();

        IBatchService batchService = serviceFactory.getBatchService();

        Batch batch = new Batch(batchInfo.getRepDate());
        batch.setUserId(batchInfo.getUserId());
        long batchId = batchService.save(batch);

        Long cId = -1l;
        boolean haveError = false;

        if(batchInfo.getUserId() != 100500L) {
            List<Creditor> cList = serviceFactory.getUserService().getPortalUserCreditorList(batchInfo.getUserId());

            if (cList.size() > 0) {
                cId = cList.get(0).getId();
            } else {
                cId = -1L;
                statusSingleton.addBatchStatus(batchId,
                        new BatchStatusJModel(BatchStatuses.ERROR,
                                "Can't find user with id: " + batchInfo.getUserId(), new Date(), batchInfo.getUserId()));
                haveError = true;
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
                        String creditorRNN = batchInfo.getAdditionalParams().get("RNN");

                        for (Creditor creditor : creditors) {
                            if (creditor.getRNN() != null) {
                                if (creditor.getRNN().equals(creditorRNN)) {
                                    cId = creditor.getId();
                                    foundCreditor = true;
                                    break;
                                }
                            }
                        }

                        if (!foundCreditor) {
                            logger.error("Can't find creditor with code: " + creditorCode + " or BIN: " +
                                    creditorBIN + "or RNN: " + creditorRNN);

                            statusSingleton.addBatchStatus(batchId,
                                    new BatchStatusJModel(BatchStatuses.ERROR,
                                            "Can't find creditor with code: " + creditorCode + " or BIN: " +
                                            creditorBIN + "or RNN: " + creditorRNN, new Date(), batchInfo.getUserId()));

                            haveError = true;
                        }
                    }
                }
            }
        }

        BatchFullJModel batchFullJModel = new BatchFullJModel(batchId, filename, bytes, new Date(),
                batchInfo.getUserId(), cId);
        statusSingleton.startBatch(batchId, batchFullJModel, batchInfo);
        if (!haveError) {
            statusSingleton.addBatchStatus(batchId,
                    new BatchStatusJModel(BatchStatuses.WAITING, null, new Date(), batchInfo.getUserId()));

            sender.addJob(batchId, batchInfo);
        }
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

        try {
            ZipFile zipFile = new ZipFile(filename);
            ZipEntry manifestEntry = zipFile.getEntry("manifest.xml");

            if(manifestEntry == null) {
                int fileCount = 0;
                ZipEntry dataXmlFile = null;
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while(entries.hasMoreElements()) {
                    if(fileCount >= 1)
                        throw new UnsupportedOperationException("Zip file must contain exactly one file");

                    dataXmlFile = entries.nextElement();
                    fileCount++;
                }

                if(dataXmlFile == null)
                    throw new NullPointerException("Zip file contains corrupted xml file");

                if(userId == null)
                    userId = 100500L;

                batchInfo.setBatchType("2");
                batchInfo.setBatchName(dataXmlFile.getName());
                batchInfo.setUserId(userId);
                /*batchInfo.setSize(dataXmlFile.getSize());*/
//                Date date = DataTypeUtil.convertDateToFirstDay(new Date(), 0);
//                batchInfo.setRepDate(date);

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = null;

                try {
                    documentBuilder = documentBuilderFactory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }

                Document document = null;

                try {
                    document = documentBuilder.parse(zipFile.getInputStream(dataXmlFile));
                } catch (SAXException e) {
                    e.printStackTrace();
                }

                Date date = null;

                try {
                    date = new SimpleDateFormat("yyyy-MM-dd").parse(
                            document.getElementsByTagName("report_date").item(0).getTextContent());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                batchInfo.setRepDate(date);

                zipFile.close();
                saveData(batchInfo, filename, inputStreamToByte(new FileInputStream(filename)));
            } else {
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

                batchInfo.setUserId(userId == null ?
                        Long.parseLong(document.getElementsByTagName("userid").item(0).getTextContent()) : userId);

                batchInfo.setSize(Long.parseLong(document.getElementsByTagName("size").item(0).getTextContent()));
                Date date = null;

                try {
                    date = new SimpleDateFormat("dd.MM.yyyy").parse(
                            document.getElementsByTagName("date").item(0).getTextContent());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                batchInfo.setRepDate(date);
                zipFile.close();
                saveData(batchInfo, filename, inputStreamToByte(new FileInputStream(filename)));
            }
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
            HashMap<String, String> params = new HashMap<>();
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
            zipFile.close();

            saveData(batchInfo, filename, inputStreamToByte(new FileInputStream(filename)));
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
