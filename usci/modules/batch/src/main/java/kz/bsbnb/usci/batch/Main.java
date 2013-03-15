package kz.bsbnb.usci.batch;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.apache.log4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;

/**
 *
 * @author k.tulbassiyev
 */
public class Main
{
    static Logger logger = Logger.getLogger(Main.class);

    public static void main(String args[])
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);

        if(!storage.testConnection())
        {
            logger.error("Can't connect to storage.");
            System.exit(1);
        }

        IBatchDao batchDao = ctx.getBean(IBatchDao.class);

        Batch batch = new Batch(new java.sql.Date(new Date().getTime()));

        long batchId = batchDao.save(batch);

        JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);

        Job job = ctx.getBean(Job.class);

        try
        {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addParameter("fileName", new JobParameter("/opt/xmls/test.xml"));
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

    }
}
