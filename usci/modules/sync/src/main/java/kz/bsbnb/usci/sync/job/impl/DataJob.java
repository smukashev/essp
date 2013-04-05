package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.job.AbstractDataJob;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import java.util.Iterator;

/**
 * @author k.tulbassiyev
 */
public final class DataJob extends AbstractDataJob {
    @Autowired
    @Qualifier(value = "remoteEntityService")
    private RmiProxyFactoryBean rmiProxyFactoryBean;

    /*@Autowired
    private BasicBaseEntitySearcherPool basicBaseEntitySearcherPool;*/

    private IEntityService entityService;
    private final Logger logger = Logger.getLogger(DataJob.class);

    @Override
    public void run() {
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();

        while(true) {
            try {
                if(entities.size() > 0 && entitiesInProcess.size() < MAX_THREAD)
                    processNewEntities();

                if(processingJobs.size() > 0)
                    removeDeadJobs();

                if(entities.size() == 0 && entitiesInProcess.size() == 0) {
                    skip_count++;
                    Thread.sleep(SLEEP_TIME_NORMAL);
                }

                if(skip_count > SKIP_TIME_MAX)
                    Thread.sleep(SLEEP_TIME_LONG);

                /* Debug */
                if(entitiesInProcess.size() != processingJobs.size())
                    throw new IllegalStateException("CRITICAL: EntitiesInProcess != ProcessJobs");

            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeDeadJobs() {
        Iterator<ProcessJob> processJobIterator = processingJobs.iterator();

        while(processJobIterator.hasNext()) {
            ProcessJob processJob = processJobIterator.next();

            if(!processJob.isAlive()) {
                BaseEntity entity = processJob.getBaseEntity();

                Iterator<BaseEntity> entityProcessIterator = entitiesInProcess.iterator();

                boolean found = false;

                while(entityProcessIterator.hasNext()) {
                    if(entity.hashCode() == entityProcessIterator.next().hashCode()) {
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

    private void processNewEntities() {
        final BaseEntity entity = getClearEntity();
        final ProcessJob processJob = new ProcessJob(entityService, entity);

        if(entity != null) {
            entitiesInProcess.add(entity);
            processingJobs.add(processJob);

            processJob.start();
            skip_count = 0;
        } else {

        }
    }

    private BaseEntity getClearEntity() {
        Iterator<BaseEntity> iterator = entities.iterator();

        while(iterator.hasNext()) {
            BaseEntity entity = iterator.next();

            if(isInProcess(entity)) {
                iterator.remove();
                return entity;
            }
        }

        return null;
    }

    private boolean isInProcess(BaseEntity baseEntity) {
        for (BaseEntity entity : entitiesInProcess)
            if(hasCrossLine(baseEntity, entity))
                return true;

        return false;
    }

    private boolean hasCrossLine(BaseEntity entity1, BaseEntity entity2) {
        // todo: implement
        return false;
    }
}
