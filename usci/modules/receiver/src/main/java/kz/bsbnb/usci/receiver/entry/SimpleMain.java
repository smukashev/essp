package kz.bsbnb.usci.receiver.entry;

import com.couchbase.client.CouchbaseClient;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
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

    public static void main(String args[]) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextSimple.xml");

        IServiceRepository serviceFactory = ctx.getBean(IServiceRepository.class);
        IBatchService batchService = serviceFactory.getBatchService();

        ICouchbaseClientFactory couchbaseClientFactory = ctx.getBean(ICouchbaseClientFactory.class);
        CouchbaseClient client = couchbaseClientFactory.getCouchbaseClient();

        FileHelper fileHelper = ctx.getBean(FileHelper.class);
        File file  = new File(FILE_PATH);
        byte bytes[] = fileHelper.getFileBytes(file);

        Batch batch = new Batch(new java.sql.Date(new java.util.Date().getTime()));
        long batchId = batchService.save(batch);

        client.set("batch:"+batchId, 0, bytes);

        JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
        Job job = ctx.getBean(Job.class);

        try {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addParameter("batchId", new JobParameter(batchId));

            jobLauncher.run(job, jobParametersBuilder.toJobParameters());
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
