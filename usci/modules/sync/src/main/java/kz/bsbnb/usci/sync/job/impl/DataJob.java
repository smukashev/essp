package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.sync.job.AbstractDataJob;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.Iterator;

/**
 * @author k.tulbassiyev
 */
public class DataJob extends AbstractDataJob
{
    @Autowired
    RmiProxyFactoryBean rmiProxyFactoryBean;

    @Autowired
    BasicBaseEntitySearcherPool basicBaseEntitySearcherPool;

    IEntityService entityService;

    private final Logger logger = Logger.getLogger(DataJob.class);

    @Override
    public void run()
    {
        logger.info("DataJob has been executed");

        entityService = (IEntityService) rmiProxyFactoryBean.getObject();

        while(true)
        {
            try
            {
                if(entities.size() > 0 && entitiesInProcess.size() < MAX_THREAD)
                    processNewEntities();
                else
                {
                    if(++skip_count > SKIP_TIME_MAX)
                        Thread.sleep(SLEEP_TIME_LONG);

                    Thread.sleep(SLEEP_TIME_NORMAL);
                }

                if(waitingJobs.size() > 0 && processJobs.size() < MAX_THREAD)
                    processWaitingJobs();

                if(processJobs.size() > 0)
                    removeDeadJobs();

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void processNewEntities() throws InterruptedException
    {
        final BaseEntity entity = getClearEntity();

        if(entity != null)
        {
            entitiesInProcess.add(entity);

            final ProcessJob processJob = new ProcessJob(entityService, entity);

            if(processJobs.size() < MAX_THREAD)
            {
                processJobs.add(processJob);
                processJob.start();
            }
            else
                waitingJobs.add(processJob);
        }
        else
            Thread.sleep(SLEEP_TIME_NORMAL);

        skip_count = 0;
    }

    private void processWaitingJobs()
    {
        Iterator<ProcessJob> iterator = waitingJobs.iterator();

        while(iterator.hasNext())
        {
            final ProcessJob waitingJob = iterator.next();

            if(processJobs.size() < MAX_THREAD && isClear(waitingJob.getBaseEntity()))
            {
                iterator.remove();

                processJobs.add(waitingJob);
                entitiesInProcess.add(waitingJob.getBaseEntity());

                waitingJob.start();
            }
        }

        skip_count = 0;
    }

    private void removeDeadJobs()
    {
        Iterator<ProcessJob> processJobIterator = processJobs.iterator();

        while(processJobIterator.hasNext())
        {
            ProcessJob processJob = processJobIterator.next();

            if(!processJob.isAlive())
            {
                BaseEntity entity = processJob.getBaseEntity();

                Iterator<BaseEntity> entityProcessIterator = entitiesInProcess.iterator();

                boolean found = false;

                while(entityProcessIterator.hasNext())
                {
                    if(entity.hashCode() == entityProcessIterator.next().hashCode())
                    {
                        entityProcessIterator.remove();
                        found = true;
                        break;
                    }
                }

                if(!found)
                    throw new IllegalStateException("CRITICAL: Entity not found.");

                processJobIterator.remove();
            }
        }
    }
}
