package kz.bsbnb.usci.batch;

import com.couchbase.client.CouchbaseClient;
import kz.bsbnb.usci.batch.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.batch.helper.impl.FileHelper;
import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav_persistance.persistance.storage.IStorage;
import org.apache.log4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

/**
 *
 * @author k.tulbassiyev
 */
public class Main
{
    private static Logger logger = Logger.getLogger(Main.class);
    private static final String FILE_PATH = "/opt/xmls/test.xml";

    public static void main(String args[])
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);

        if(!storage.testConnection())
        {
            logger.error("Can't connect to storage.");
            System.exit(1);
        }

        ICouchbaseClientFactory couchbaseClientFactory = ctx.getBean(ICouchbaseClientFactory.class);

        CouchbaseClient client = couchbaseClientFactory.getCouchbaseClient();

        FileHelper fileHelper = ctx.getBean(FileHelper.class);
        File file  = new File(FILE_PATH);
        byte bytes[] = fileHelper.getFileBytes(file);

        IBatchDao batchDao = ctx.getBean(IBatchDao.class);

        Batch batch = new Batch(new java.sql.Date(new java.util.Date().getTime()));

        long batchId = batchDao.save(batch);

        client.set(""+batchId, 0, bytes);

        JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);

        Job job = ctx.getBean(Job.class);

        try
        {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addParameter("fileName", new JobParameter(FILE_PATH));
            jobParametersBuilder.addParameter("batchId", new JobParameter(batchId));

            jobLauncher.run(job, jobParametersBuilder.toJobParameters());
        }
        catch (JobExecutionAlreadyRunningException e)
        {
            e.printStackTrace();
        }
        catch (JobRestartException e)
        {
            e.printStackTrace();
        }
        catch (JobInstanceAlreadyCompleteException e)
        {
            e.printStackTrace();
        }
        catch (JobParametersInvalidException e)
        {
            e.printStackTrace();
        }
        finally
        {
            client.shutdown();
        }
    }
}
