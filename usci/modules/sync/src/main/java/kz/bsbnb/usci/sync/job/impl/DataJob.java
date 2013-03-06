package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.sync.job.AbstractJob;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public class DataJob extends AbstractJob
{
    @Autowired
    RmiProxyFactoryBean rmiProxyFactoryBean;

    IEntityService entityService;

    private final List<BaseEntity> entities = Collections.synchronizedList(new ArrayList<BaseEntity>());
    private final List<BaseEntity> entitiesInProcess = Collections.synchronizedList(new ArrayList<BaseEntity>());
    private final List<ProcessJob> processJobs = new ArrayList<ProcessJob>();
    private final List<ProcessJob> waitingJobs = new ArrayList<ProcessJob>();

    private final int SLEEP_TIME_NORMAL = 1000;
    private final int SLEEP_TIME_LONG = 5000;
    private final int SKIP_TIME_MAX = 10;
    private final int MAX_THREAD = 30;

    private int skip_count = 0;

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
                //

                if(entities.size() > 0 && entitiesInProcess.size() < MAX_THREAD)
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
                    {
                        Thread.sleep(SLEEP_TIME_NORMAL);
                    }
                }
                else
                {
                    if(++skip_count > SKIP_TIME_MAX)
                        Thread.sleep(SLEEP_TIME_LONG);

                    Thread.sleep(SLEEP_TIME_NORMAL);
                }

                //

                if(waitingJobs.size() > 0 && processJobs.size() < MAX_THREAD)
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
                }

                //

                if(processJobs.size() > 0)
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

                // for debug; in production will be deleted
                if(entitiesInProcess.size() != (processJobs.size() + waitingJobs.size()))
                    throw new IllegalStateException("CRITICAL: Sizes are not equal [" + entitiesInProcess.size() + ", "
                            + (processJobs.size() + waitingJobs.size()));

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    public BaseEntity getClearEntity()
    {
        Iterator<BaseEntity> iterator = entities.iterator();

        while(iterator.hasNext())
        {
            BaseEntity entity = iterator.next();

            if(isClear(entity))
            {
                iterator.remove();
                return entity;
            }
        }

        return null;
    }

    public boolean isClear(BaseEntity baseEntity)
    {
        for (BaseEntity entity : entitiesInProcess)
            if(hasCrossLine(baseEntity, entity))
                return false;

        return true;
    }

    public boolean hasCrossLine(BaseEntity entity1, BaseEntity entity2)
    {
        // todo: implement
        /*if(entity1.equals(entity2))
            return true;*/

        return false;
    }

    public void addAll(List<BaseEntity> entities)
    {
        this.entities.addAll(entities);
    }
}
