package kz.bsbnb.usci.receiver.monitor;

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
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author abukabayev
 */

public class ZipFilesMonitor{
    private final Logger logger = LoggerFactory.getLogger(ZipFilesMonitor.class);

    //@Autowired
    //private ICouchbaseClientFactory clientFactory;

    @Autowired
    private IServiceRepository serviceFactory;

    @Autowired
    private StatusSingleton statusSingleton;

    @Autowired
    private JobLauncher jobLauncher;

    private Map<String,Job> jobs;

    //private static Gson gson = new Gson();

    public static final int ZIP_BUFFER_SIZE = 1024;

    public ZipFilesMonitor(Map<String, Job> jobs) {
        this.jobs = jobs;
    }

    public void saveData(BatchInfo batchInfo, String filename, byte[] bytes){
        IBatchService batchService = serviceFactory.getBatchService();

        Batch batch = new Batch(new java.sql.Date(new java.util.Date().getTime()));
        batch.setUserId(batchInfo.getUserId());
        long batchId = batchService.save(batch);

        List<Creditor> cList = serviceFactory.getUserService().getPortalUserCreditorList(batchInfo.getUserId());

        Long cId;

        if (cList.size() > 0) {
            cId = cList.get(0).getId();
        } else {
            cId = -1L;
        }

        BatchFullJModel batchFullJModel = new BatchFullJModel(batchId, filename, bytes, new Date(),
                batchInfo.getUserId(), cId);
        statusSingleton.startBatch(batchId, batchFullJModel, batchInfo);
        statusSingleton.addBatchStatus(batchId,
                new BatchStatusJModel(Global.BATCH_STATUS_WAITING, null, new Date(), batchInfo.getUserId()));
        statusSingleton.addBatchStatus(batchId,
                new BatchStatusJModel(Global.BATCH_STATUS_PROCESSING, null, new Date(), batchInfo.getUserId()));

        try {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addParameter("batchId", new JobParameter(batchId));
            jobParametersBuilder.addParameter("userId", new JobParameter(batchInfo.getUserId()));

            Job job = jobs.get(batchInfo.getBatchType());

            if (job != null) {
                jobLauncher.run(job, jobParametersBuilder.toJobParameters());
            } else {
                logger.error("Unknown batch file type: " + batchInfo.getBatchType() + " in batch with id: " + batchId);

                statusSingleton.addBatchStatus(batchId,
                        new BatchStatusJModel(Global.BATCH_STATUS_ERROR, "Unknown batch file type: " +
                                batchInfo.getBatchType(), new Date(), batchInfo.getUserId()));
            }
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
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

    public void readFiles(String filename){
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
            batchInfo.setUserId(Long.parseLong(document.getElementsByTagName("userid").item(0).getTextContent()));
            batchInfo.setSize(Long.parseLong(document.getElementsByTagName("size").item(0).getTextContent()));


            Date date = null;
            try {
                date = new SimpleDateFormat("dd.MM.yy").parse(document.getElementsByTagName("date").item(0).getTextContent());
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

        boolean valid = true;
        do {
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
