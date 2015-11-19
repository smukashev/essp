package kz.bsbnb.usci.receiver.monitor;

import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.ReportStatus;
import kz.bsbnb.usci.receiver.queue.JobInfo;
import kz.bsbnb.usci.receiver.queue.JobLauncherQueue;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.tool.status.ReceiverStatusSingleton;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
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
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
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

public class ZipFilesMonitor {
    private final Logger logger = LoggerFactory.getLogger(ZipFilesMonitor.class);

    @Autowired
    private IServiceRepository serviceFactory;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private ReceiverStatusSingleton receiverStatusSingleton;

    @Autowired
    private JobLauncherQueue jobLauncherQueue;

    private IBatchService batchService;

    private Map<String, Job> jobs;

    private List<Creditor> creditors;

    SenderThread sender;

    public static final int ZIP_BUFFER_SIZE = 1024;
    public static final int MAX_SYNC_QUEUE_SIZE = 256;

    private static final long WAIT_TIMEOUT = 360; //in 10 sec units

    public ZipFilesMonitor(Map<String, Job> jobs) {
        this.jobs = jobs;
    }

    public boolean restartBatch(long batchId) {
        try {
            Batch batch = batchService.getBatch(batchId);
            BatchInfo batchInfo = new BatchInfo(batch);
            System.out.println(batchId + " - restarted");
            //sender.addJob(batchId, batchInfo);
            jobLauncherQueue.addJob(batchId, batchInfo);
            receiverStatusSingleton.batchReceived();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @PostConstruct
    public void init() {
        batchService = serviceFactory.getBatchService();
        sender = new SenderThread();
        sender.start();
        sender.setReceiverStatusSingleton(receiverStatusSingleton);
        creditors = serviceFactory.getRemoteCreditorBusiness().findMainOfficeCreditors();
        System.out.println("Найдено " + creditors.size() + " кредиторов;");

        IBatchService batchService = serviceFactory.getBatchService();

        List<Batch> pendingBatchList = batchService.getPendingBatchList();

        if (pendingBatchList.size() > 0) {
            System.out.println("Найдены не законченные батчи: " + pendingBatchList.size());

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

            System.out.println("-------------------------------------------------------------------------");

            for (Batch b : pendingBatchList)
                System.out.println(b.getId() + ", " + b.getFileName() + ", " +
                        sdf.format(b.getRepDate()));

            System.out.println("-------------------------------------------------------------------------");

            for (Batch batch : pendingBatchList) {
                try {
                    //sender.addJob(batch.getId(), new BatchInfo(batch));
                    jobLauncherQueue.addJob(batch.getId(), new BatchInfo(batch));
                    receiverStatusSingleton.batchReceived();
                    System.out.println("Перезагрузка батча : " + batch.getId() + " - " + batch.getFileName());
                } catch (Exception e) {
                    System.out.println("Error in pending batches view: " + e.getMessage());
                    System.out.println("Retrying...");
                }
            }
        }
    }

    private class SenderThread extends Thread {
        private ReceiverStatusSingleton receiverStatusSingleton;

        public ReceiverStatusSingleton getReceiverStatusSingleton() {
            return receiverStatusSingleton;
        }

        public void setReceiverStatusSingleton(ReceiverStatusSingleton receiverStatusSingleton) {
            this.receiverStatusSingleton = receiverStatusSingleton;
        }

        public void run() {
            long sleepCounter = 0;
            while (true) {
                JobInfo nextJob;

                if (serviceFactory != null && serviceFactory.getEntityService().getQueueSize() > MAX_SYNC_QUEUE_SIZE) {
                    System.out.println("Can't send more files because of file limit or sync queue overload.");
                    try {
                        sleep(10000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sleepCounter++;
                    if (sleepCounter > WAIT_TIMEOUT) {
                        throw new IllegalStateException("Sync timeout in reader.");
                    }
                    continue;
                }
                sleepCounter = 0;

                if ((nextJob = jobLauncherQueue.getNextJob()) != null) {
                    System.out.println("Отправка батча на обработку : " + nextJob.getBatchId());

                    try {
                        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
                        jobParametersBuilder.addParameter("batchId", new JobParameter(nextJob.getBatchId()));

                        jobParametersBuilder.addParameter("userId",
                                new JobParameter(nextJob.getBatchInfo().getUserId()));

                        jobParametersBuilder.addParameter("reportId",
                                new JobParameter(nextJob.getBatchInfo().getReportId()));

                        jobParametersBuilder.addParameter("actualCount",
                                new JobParameter(nextJob.getBatchInfo().getActualCount()));

                        Job job = jobs.get(nextJob.getBatchInfo().getBatchType());

                        if (job != null) {
                            jobLauncher.run(job, jobParametersBuilder.toJobParameters());
                            receiverStatusSingleton.batchStarted();
                            batchService.clearActualCount(nextJob.getBatchId());
                            batchService.addBatchStatus(new BatchStatus()
                                            .setBatchId(nextJob.getBatchId())
                                            .setStatus(BatchStatuses.PROCESSING)
                                            .setReceiptDate(new Date()));
                        } else {
                            logger.error("Unknown batch file type: " + nextJob.getBatchInfo().getBatchType() +
                                    " in batch with id: " + nextJob.getBatchId());

                            batchService.addBatchStatus(new BatchStatus()
                                            .setBatchId(nextJob.getBatchId())
                                            .setStatus(BatchStatuses.ERROR)
                                            .setDescription("Unknown batch file type: " +
                                                    nextJob.getBatchInfo().getBatchType())
                                            .setReceiptDate(new Date()));
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
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.debug("No files to send");
                }
            }
        }
    }

    public void saveData(BatchInfo batchInfo, String filename, byte[] bytes, boolean isNB){
        // TODO: fix hardcoded settings
        receiverStatusSingleton.batchReceived();

        IBatchService batchService = serviceFactory.getBatchService();

        Batch batch = new Batch();
        batch.setUserId(batchInfo.getUserId());
        batch.setFileName(filename);
        batch.setContent(bytes);
        batch.setRepDate(batchInfo.getRepDate());
        batch.setReceiptDate(new Date());
        batch.setBatchType(batchInfo.getBatchType());
        batch.setTotalCount(batchInfo.getSize());

        long batchId = batchService.save(batch);
        batch.setId(batchId);

        Long cId = -1l;
        boolean haveError = false;

        if (batchInfo.getUserId() != 100500L) {
            List<Creditor> cList = serviceFactory.getUserService().getPortalUserCreditorList(batchInfo.getUserId());

            if (isNB) {
                cId = 0L;
            } else if (cList.size() == 1) {
                cId = cList.get(0).getId();
            } else {
                cId = -1L;

                batchService.addBatchStatus(new BatchStatus()
                                .setBatchId(batchId)
                                .setStatus(BatchStatuses.ERROR)
                                .setDescription("Can't find creditor for user with id: " + batchInfo.getUserId())
                                .setReceiptDate(new Date())
                );

                haveError = true;
            }
        } else {
            if (batchInfo.getAdditionalParams() != null && batchInfo.getAdditionalParams().size() > 0) {
                String docType = batchInfo.getAdditionalParams().get("DOC_TYPE");
                String docValue = batchInfo.getAdditionalParams().get("DOC_VALUE");

                String code = batchInfo.getAdditionalParams().get("CODE");
                String bin = batchInfo.getAdditionalParams().get("BIN");
                String bik = batchInfo.getAdditionalParams().get("BIK");
                String rnn = batchInfo.getAdditionalParams().get("RNN");


                if (docType == null) docType = "";
                if (docValue == null) docValue = "";

                boolean foundCreditor = false;

                for (Creditor creditor : creditors) {
                    if (creditor.getBIK() != null && docType.equals("15") &&
                            creditor.getBIK().equals(docValue)) {
                        cId = creditor.getId();
                        foundCreditor = true;
                        break;

                    }

                    if (creditor.getBIN() != null && docType.equals("07") &&
                            creditor.getBIN().equals(docValue)) {
                        cId = creditor.getId();
                        foundCreditor = true;
                        break;

                    }

                    if (creditor.getRNN() != null && docType.equals("11") &&
                            creditor.getRNN().equals(docValue)) {
                        cId = creditor.getId();
                        foundCreditor = true;
                        break;
                    }

                    if (code != null && code.length() > 0 && creditor.getCode() != null
                            && creditor.getCode().length() > 0 && code.equals(creditor.getCode())) {
                        cId = creditor.getId();
                        foundCreditor = true;
                        break;
                    }

                    if (bin != null && bin.length() > 0 && creditor.getBIN() != null
                            && creditor.getBIN().length() > 0 && bin.equals(creditor.getBIN())) {
                        cId = creditor.getId();
                        foundCreditor = true;
                        break;
                    }

                    if (bik != null && bik.length() > 0 && creditor.getBIK() != null
                            && creditor.getBIK().length() > 0 && bik.equals(creditor.getBIK())) {
                        cId = creditor.getId();
                        foundCreditor = true;
                        break;
                    }

                    if (rnn != null && rnn.length() > 0 && creditor.getRNN() != null
                            && creditor.getRNN().length() > 0 && rnn.equals(creditor.getRNN())) {
                        cId = creditor.getId();
                        foundCreditor = true;
                        break;
                    }
                }

                if (!foundCreditor) {
                    logger.error("Can't find creditor: " + docType +
                            ", " + docValue);

                    batchService.addBatchStatus(new BatchStatus()
                                    .setBatchId(batchId)
                                    .setStatus(BatchStatuses.ERROR)
                                    .setDescription("Кредитор не найден")
                                    .setReceiptDate(new Date())
                    );

                    haveError = true;
                }
            }
        }


        if (!haveError && !checkAndFillEavReport(cId, batchInfo, batchId)) {
            haveError = true;
        }

        batch.setCreditorId(cId);
        batch.setReportId(batchInfo.getReportId());
        batchService.uploadBatch(batch);

        if (!haveError) {
            batchService.addBatchStatus(new BatchStatus()
                            .setBatchId(batchId)
                            .setStatus(BatchStatuses.WAITING)
                            .setReceiptDate(new Date()));

            batchInfo.setContentSize(batch.getContent().length);
            batchInfo.setCreditorId(batch.getCreditorId());
            batchInfo.setReceiptDate(batch.getReceiptDate());
            //sender.addJob(batchId, batchInfo);
            jobLauncherQueue.addJob(batchId, batchInfo);
        }
    }

    private boolean checkAndFillEavReport(long creditorId, BatchInfo batchInfo, long batchId) {
        ReportBeanRemoteBusiness reportBeanRemoteBusiness = serviceFactory.getReportBeanRemoteBusinessService();

        Report existing = reportBeanRemoteBusiness.getReport(creditorId, batchInfo.getRepDate());

        if (existing != null) {
            if (ReportStatus.COMPLETED.code().equals(existing.getStatus().getCode())) {
                String errMsg = "Отчет со статусом 'Завершен' уже существует для кредитора = "
                        + creditorId + ", отчетная дата = " + batchInfo.getRepDate();
                logger.error(errMsg);

                batchService.addBatchStatus(new BatchStatus()
                                .setBatchId(batchId)
                                .setStatus(BatchStatuses.ERROR)
                                .setDescription(errMsg)
                                .setReceiptDate(new Date())
                );
                return false;
            }
        } else {
            // FIXME: 18.11.15
        }


        EavGlobal inProgress = serviceFactory.getGlobalService().getGlobal(ReportStatus.IN_PROGRESS);

        if (existing != null) {
            existing.setStatusId(inProgress.getId());
            existing.setTotalCount(batchInfo.getTotalCount());
            existing.setActualCount(batchInfo.getActualCount());
            existing.setEndDate(new Date());

            PortalUserBeanRemoteBusiness userService = serviceFactory.getUserService();
            PortalUser portalUser = userService.getUser(batchInfo.getUserId());
            if (portalUser != null)
                reportBeanRemoteBusiness.updateReport(existing, portalUser.getScreenName());
            else
                reportBeanRemoteBusiness.updateReport(existing, "Unknown");

            batchInfo.setReportId(existing.getId());
        } else {
            Report report = new Report();
            {
                Creditor creditor = new Creditor();
                creditor.setId(creditorId);
                report.setCreditor(creditor);
            }
            report.setStatusId(inProgress.getId());
            report.setTotalCount(batchInfo.getTotalCount());
            report.setActualCount(batchInfo.getActualCount());
            report.setReportDate(batchInfo.getRepDate());
            report.setBeginningDate(new Date());
            report.setEndDate(new Date());

            PortalUserBeanRemoteBusiness userService = serviceFactory.getUserService();
            PortalUser portalUser = userService.getUser(batchInfo.getUserId());
            Long reportId;
            if (portalUser != null)
                reportId = reportBeanRemoteBusiness.insert(report, portalUser.getScreenName());
            else
                reportId = reportBeanRemoteBusiness.insert(report, "Unknown");
            batchInfo.setReportId(reportId);
        }

        return true;
    }

    public byte[] inputStreamToByte(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        in.close();

        return buffer.toByteArray();
    }

    public void readFiles(String filename) {
        readFiles(filename, null);
    }

    public void readFiles(String filename, Long userId) {
        readFiles(filename, userId, false);
    }

    public void readFiles(String filename, Long userId, boolean isNB) {
        BatchInfo batchInfo = new BatchInfo();

        try {
            ZipFile zipFile = new ZipFile(filename);
            ZipEntry manifestEntry = zipFile.getEntry("manifest.xml");
            
            if(manifestEntry == null) { // credit-registry
                // this is fix for zip extract for file names with non latin chars
                String batchName = null;
                ZipArchiveInputStream zis = null;
                byte[] extractedBytes = null;

                try {
                    zis = new ZipArchiveInputStream(new FileInputStream(filename));
                    ZipArchiveEntry zipArchiveEntry = null;

                    while ((zipArchiveEntry = zis.getNextZipEntry()) != null) {
                        ByteArrayOutputStream baos = null;
                        try {
                            int size;
                            byte[] buffer = new byte[ZIP_BUFFER_SIZE];

                            baos = new ByteArrayOutputStream();

                            while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                                baos.write(buffer, 0, size);
                            }
                            batchName = zipArchiveEntry.getName();
                            extractedBytes = baos.toByteArray();
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

                if (extractedBytes == null)
                    throw new IOException("ZIP file does not contain any files.");

                if (userId == null)
                    userId = 100500L;

                batchInfo.setBatchType("2");
                batchInfo.setBatchName(parseFileNameFromPath(filename));
                batchInfo.setUserId(userId);

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = null;

                try {
                    documentBuilder = documentBuilderFactory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }

                Document document = null;

                try {
                    // TODO: fix OutOfMemory
                    document = documentBuilder.parse(new ByteArrayInputStream(extractedBytes));
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

                String actualCreditCount = document.getElementsByTagName("actual_credit_count").item(0).
                        getTextContent();

                batchInfo.setSize(Long.parseLong(actualCreditCount));
                batchInfo.setActualCount(Integer.parseInt(actualCreditCount));
                batchInfo.setTotalCount(0);

                try {
                    Element infoElement = (Element) document.getElementsByTagName("info").item(0);
                    Element creditorElement = (Element) infoElement.getElementsByTagName("creditor").item(0);
                    Element codeElement = (Element) creditorElement.getElementsByTagName("code").item(0);
                    String code = null;

                    if (codeElement != null)
                        code = codeElement.getTextContent();

                    if (code != null && code.length() > 0) {
                        batchInfo.addParam("CODE", code.replaceAll("\\s+", ""));
                    } else {
                        NamedNodeMap map = document.getElementsByTagName("doc").item(0).getAttributes();
                        Node n = map.getNamedItem("doc_type");
                        String docType = n.getTextContent();

                        String docValue = document.getElementsByTagName("doc").item(0).getTextContent();

                        if (docType != null && docValue != null &&
                                docType.length() > 0 && docValue.length() > 0) {
                            batchInfo.addParam("DOC_TYPE", docType.replaceAll("\\s+", ""));
                            batchInfo.addParam("DOC_VALUE", docValue.replaceAll("\\s+", ""));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                zipFile.close();
                saveData(batchInfo, filename, inputStreamToByte(new FileInputStream(filename)), isNB);
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


                batchInfo.setBatchType(document.getElementsByTagName("type").item(0).getTextContent().
                        replaceAll("\\s+", ""));

                /*batchInfo.setBatchName(document.getElementsByTagName("name").item(0).getTextContent().
                        replaceAll("\\s+", ""));*/
                batchInfo.setBatchName(parseFileNameFromPath(filename));

                batchInfo.setUserId(userId == null ?
                        Long.parseLong(document.getElementsByTagName("userid").item(0).getTextContent().
                                replaceAll("\\s+", "")) : userId);

                int actualCreditCount = Integer.parseInt(document.getElementsByTagName("size").item(0).
                        getTextContent().replaceAll("\\s+", ""));

                batchInfo.setSize((long) actualCreditCount);
                batchInfo.setActualCount(actualCreditCount);
                batchInfo.setTotalCount(0);

                Date date = null;

                try {
                    date = new SimpleDateFormat("dd.MM.yyyy").parse(
                            document.getElementsByTagName("date").item(0).getTextContent().replaceAll("\\s+", ""));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                batchInfo.setRepDate(date);

                NodeList propertiesList = document.getElementsByTagName("properties");

                for (int i = 0; i < propertiesList.getLength(); i++) {
                    Node propertiesNode = propertiesList.item(i);

                    if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element propertiesElement = (Element) propertiesNode;

                        NodeList propertyList = propertiesElement.getElementsByTagName("property");

                        for (int j = 0; j < propertyList.getLength(); j++) {
                            Node propertyNode = propertyList.item(j);

                            if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element propertyElement = (Element) propertyNode;

                                String name = propertyElement.getElementsByTagName("name").item(0).getTextContent()
                                        .replaceAll("\\s+", "");
                                String value = propertyElement.getElementsByTagName("value").item(0).getTextContent()
                                        .replaceAll("\\s+", "");

                                batchInfo.addParam(name, value);
                            }
                        }
                    }
                }


                zipFile.close();
                saveData(batchInfo, filename, inputStreamToByte(new FileInputStream(filename)), isNB);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readFilesWithoutUser(String filename) {
        BatchInfo batchInfo = new BatchInfo();
        try {

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
                // TODO: Out of memory
                document = documentBuilder.parse(inManifest);
            } catch (SAXException e) {
                e.printStackTrace();
            }


            batchInfo.setBatchType(document.getElementsByTagName("type").item(0).getTextContent()
                    .replaceAll("\\s+", ""));

            /*batchInfo.setBatchName(document.getElementsByTagName("name").item(0).getTextContent()
                    .replaceAll("\\s+", ""));*/
            batchInfo.setBatchName(parseFileNameFromPath(filename));

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
                        name = curChild.getTextContent().replaceAll("\\s+", "");
                    }
                    if (curChild.getNodeName().equals("value")) {
                        value = curChild.getTextContent().replaceAll("\\s+", "");
                    }
                }

                params.put(name, value);
            }

            batchInfo.setAdditionalParams(params);
            batchInfo.setSize(Long.parseLong(document.getElementsByTagName("size").item(0).getTextContent()));


            Date date = null;
            try {
                date = new SimpleDateFormat("dd.MM.yyyy").parse(document.getElementsByTagName("date").
                        item(0).getTextContent().replaceAll("\\s+", ""));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            batchInfo.setRepDate(date);
            zipFile.close();

            saveData(batchInfo, filename, inputStreamToByte(new FileInputStream(filename)), false);
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
            while (entityService.getQueueSize() > MAX_SYNC_QUEUE_SIZE) {
                Thread.sleep(1000);

                sleepCounter++;
                if (sleepCounter > WAIT_TIMEOUT) {
                    logger.error("Sync timeout in reader.");
                    // throw new IllegalStateException("Sync timeout in reader.");
                }
            }
            sleepCounter = 0;

            WatchKey watchKey = watchService.take();

            for (WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                    String fileName = event.context().toString();
                    System.out.println("Поступил батч : " + fileName);

                    Thread.sleep(1000);

                    readFiles(path + "/" + fileName);
                }
            }
            valid = watchKey.reset();

        } while (valid);

    }

    private String parseFileNameFromPath(String fileName){
        return fileName.substring(fileName.lastIndexOf('/') + 1);

    }

    public JobLauncherQueue getJobLauncherQueue() {
        return jobLauncherQueue;
    }
}
