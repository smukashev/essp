package kz.bsbnb.usci.receiver.entry;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.receiver.singleton.StatusSingleton;
import kz.bsbnb.usci.sync.service.IBatchService;
import net.spy.memcached.internal.OperationFuture;
import org.apache.log4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.io.File;

/**
 * Development entry point
 *
 * @author k.tulbassiyev
 */
public class SimpleMain {
    private static Logger logger = Logger.getLogger(SimpleMain.class);
    private static final String FILE_PATH = "/opt/xmls/test.xml";
    private static Gson gson = new Gson();

    public static void main(String args[]) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextSimple.xml");

        IServiceRepository serviceFactory = ctx.getBean(IServiceRepository.class);
        IBatchService batchService = serviceFactory.getBatchService();

        ICouchbaseClientFactory couchbaseClientFactory = ctx.getBean(ICouchbaseClientFactory.class);
        CouchbaseClient client = couchbaseClientFactory.getCouchbaseClient();

        StatusSingleton statusSingleton = ctx.getBean(StatusSingleton.class);

        FileHelper fileHelper = ctx.getBean(FileHelper.class);
        File file  = new File(FILE_PATH);
        byte bytes[] = fileHelper.getFileBytes(file);

        Batch batch = new Batch(new java.sql.Date(new java.util.Date().getTime()));
        long batchId = batchService.save(batch);

        BatchFullJModel batchFullJModel = new BatchFullJModel(batchId, FILE_PATH, bytes);
        statusSingleton.startBatch(batchId);
        statusSingleton.addBatchStatus(batchId, new BatchStatusJModel(Global.BATCH_STATUS_PROCESSING, null));
        OperationFuture<Boolean> result = client.set("batch:" + batchId, 0, gson.toJson(batchFullJModel));

        while(true) if(result.isDone()) break; // must be completed

        JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
        Job batchJob = ctx.getBean("batchJob", Job.class);

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
}
