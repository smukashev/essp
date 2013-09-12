package kz.bsbnb.usci.receiver.monitor;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.entry.BatchInfo;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.receiver.singleton.StatusSingleton;
import kz.bsbnb.usci.sync.service.IBatchService;
import net.spy.memcached.internal.OperationFuture;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author abukabayev
 */
@Component
public class ZipFilesMonitor{
    @Autowired
    private ICouchbaseClientFactory clientFactory;

    @Autowired
    private IServiceRepository serviceFactory;

    @Autowired
    private StatusSingleton statusSingleton;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier(value = "batchJob")
    private Job batchJob;

    private static Gson gson = new Gson();


    public void saveData(BatchInfo batchInfo,String filename,byte[] bytes){
        IBatchService batchService = serviceFactory.getBatchService();

        CouchbaseClient client = clientFactory.getCouchbaseClient();

        System.out.println(clientFactory);

        Batch batch = new Batch(new java.sql.Date(new java.util.Date().getTime()));
        long batchId = batchService.save(batch);

        BatchFullJModel batchFullJModel = new BatchFullJModel(batchId, filename, bytes, new Date());
        statusSingleton.startBatch(batchId);
        statusSingleton.addBatchStatus(batchId,
                new BatchStatusJModel(Global.BATCH_STATUS_PROCESSING, null, new Date()));

        OperationFuture<Boolean> result = client.set("batch:" + batchId, 0, gson.toJson(batchFullJModel));

        while(true) if(result.isDone()) break; // must be completed

        OperationFuture<Boolean> result1 = client.set("manifest:" + batchId, 0, gson.toJson(batchInfo));

        while(true) if(result1.isDone()) break; // must be completed


        try {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addParameter("batchId", new JobParameter(batchId));

            jobLauncher.run(batchJob, jobParametersBuilder.toJobParameters());
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        } finally {
            client.shutdown();
        }

    }

    public void readFiles(String filename){
        BatchInfo batchInfo = new BatchInfo();
        try{

            ZipFile zipFile = new ZipFile(filename);

            ZipEntry manifestEntry = zipFile.getEntry("manifest.xml");
            ZipEntry dataEntry = zipFile.getEntry("data.xml");

            InputStream inManifest = zipFile.getInputStream(manifestEntry);
            InputStream inData = zipFile.getInputStream(dataEntry);

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

            saveData(batchInfo,filename,new byte[12]);


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

                    readFiles(path+"/"+fileName);



                }
            }
            valid = watchKey.reset();

        } while (valid);

    }
}
