package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.sync.job.AbstractJob;
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
@Component
public class DataJob extends AbstractJob
{
    private List<BaseEntity> entities = Collections.synchronizedList(new ArrayList<BaseEntity>());
    private List<BaseEntity> entitiesInProcess = Collections.synchronizedList(new ArrayList<BaseEntity>());

    private List<ProcessJob> processJobs = new ArrayList<ProcessJob>();
    private List<ProcessJob> waitingJobs = new ArrayList<ProcessJob>();

    private final int SLEEP_TIME_NORMAL = 1000;
    private final int SLEEP_TIME_LONG = 5000;
    private final int SKIP_TIME_MAX = 10;

    private final int MAX_THREAD = 30;

    private int skip_count = 0;

    @Autowired
    RmiProxyFactoryBean rmiProxyFactoryBean;

    IEntityService entityService;

    @PostConstruct
    public void init()
    {
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();

        run();
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                //

                if(entities.size() > 0)
                {
                    BaseEntity entity = getClearEntity();

                    if(entity != null)
                    {
                        entitiesInProcess.add(entity);

                        ProcessJob processJob = new ProcessJob(entityService, entity);

                        if(processJobs.size() < MAX_THREAD)
                        {
                            processJobs.add(processJob);

                            processJob.start();
                        }
                        else
                        {
                            waitingJobs.add(processJob);
                        }

                    }
                    else
                        Thread.sleep(SLEEP_TIME_NORMAL);
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
                    for (ProcessJob waitingJob : waitingJobs)
                    {
                        if(isClear(waitingJob.getBaseEntity()))
                        {
                            processJobs.add(waitingJob);
                            entitiesInProcess.add(waitingJob.getBaseEntity());
                            waitingJob.start();
                        }
                    }

                }

                // for debug; in production will be deleted
                if(entitiesInProcess.size() != processJobs.size())
                    throw new IllegalStateException("CRITICAL: Sizes are not equal");

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
                return true;

        return false;
    }

    public boolean hasCrossLine(BaseEntity entity1, BaseEntity entity2)
    {
        // todo: implement
        if(entity1.equals(entity2))
            return true;

        return false;
    }

    public void addAll(List<BaseEntity> entities)
    {
        this.entities.addAll(entities);
    }
}
