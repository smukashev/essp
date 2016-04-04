package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.sync.job.AbstractDataJob;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.tool.status.SyncStatusSingleton;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author k.tulbassiyev
 */
public final class DataJob extends AbstractDataJob {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier(value = "remoteEntityService")
    private RmiProxyFactoryBean rmiProxyFactoryBean;

    @Autowired
    private SyncStatusSingleton syncStatusSingleton;

    private IEntityService entityService;

    @Autowired
    private IBatchService batchService;

    private ActualCountJob actualCountJob;

    private final Logger logger = Logger.getLogger(DataJob.class);

    private final List<InProcessTester> entitiesInProcess = new ArrayList<>();

    private volatile BaseEntity currentEntity;
    private volatile boolean currentIntersection;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private double avgTimePrev = 0;
    private double avgTimeCur = 0;
    private long entityCounter = 0;

    private int clearJobsIndex = 0;

    private class InProcessTester implements Callable<Boolean> {
        private final BaseEntity myEntity;

        private InProcessTester(BaseEntity myEntity) {
            this.myEntity = myEntity;
        }

        public Boolean call() {
            try {
                for (IBaseEntity myEntityKeyElement : myEntity.getKeyElements()) {
                    for (IBaseEntity currentEntityKeyElement : currentEntity.getKeyElements()) {
                        if (myEntityKeyElement.getMeta().getId() == currentEntityKeyElement.getMeta().getId() &&
                                myEntityKeyElement.equalsByKey(currentEntityKeyElement)) {
                            currentIntersection = true;
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        BaseEntity getMyEntity() {
            return myEntity;
        }
    }

    @Override
    public void run() {
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();
        actualCountJob = new ActualCountJob(batchService);
        actualCountJob.start();

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                if (entities.size() > 0 && entitiesInProcess.size() < THREAD_MAX_LIMIT)
                    processNewEntities();

                if (processingJobs.size() > 0)
                    removeDeadJobs();

                if (entities.size() == 0 && entitiesInProcess.size() == 0) {
                    Thread.sleep(SLEEP_TIME_NORMAL);
                    skip_count++;
                }

                if (skip_count > SKIP_TIME_MAX) {
                    Thread.sleep(SLEEP_TIME_LONG);
                    skip_count = 0;
                }

                syncStatusSingleton.put(entities.size(), entitiesInProcess.size(), avgTimeCur);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeDeadJobs() {
        Iterator<ProcessJob> processJobIterator = processingJobs.iterator();

        while (processJobIterator.hasNext()) {
            ProcessJob processJob = processJobIterator.next();

            if (!processJob.isAlive()) {
                BaseEntity entity = processJob.getBaseEntity();

                entityCounter++;
                if (entityCounter < STAT_INTERVAL) {
                    avgTimeCur = (avgTimeCur * (entityCounter - 1)) / entityCounter +
                            processJob.getTimeSpent() / entityCounter;
                } else {
                    if (avgTimePrev > 0)
                        System.out.println("Скорость обработки: " + avgTimeCur);

                    entityCounter = 0;
                    avgTimePrev = avgTimeCur;
                }

                Iterator<InProcessTester> entityProcessIterator = entitiesInProcess.iterator();

                boolean found = false;

                while (entityProcessIterator.hasNext()) {
                    if (entity.hashCode() == entityProcessIterator.next().getMyEntity().hashCode()) {
                        entityProcessIterator.remove();
                        found = true;
                        break;
                    }
                }

                if (!found)
                    throw new IllegalStateException(Errors.compose(Errors.E280));

                processJobIterator.remove();
            }
        }
    }

    private void processNewEntities() {
        final BaseEntity entity = getClearEntity();
        final ProcessJob processJob = new ProcessJob(entityService, entity);

        if (entity != null) {
            logger.debug("Starting job");
            entitiesInProcess.add(new InProcessTester(entity));
            processingJobs.add(processJob);
            actualCountJob.insertBatchId(entity.getBatchId());

            processJob.start();
            skip_count = 0;
        }
    }

    private synchronized BaseEntity getClearEntity() {
        if (clearJobsIndex >= entities.size())
            clearJobsIndex = 0;

        Iterator<BaseEntity> iterator = entities.listIterator(clearJobsIndex);
        while (iterator.hasNext()) {
            BaseEntity entity = iterator.next();

            entity.getKeyElements();

            if (!isInProcessWithThreads(entity)) {
                iterator.remove();
                return entity;
            }

            clearJobsIndex++;
        }

        return null;
    }

    private boolean isInProcessWithThreads(BaseEntity baseEntity) {
        currentEntity = baseEntity;
        currentIntersection = false;

        try {
            executorService.invokeAll(entitiesInProcess);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return currentIntersection;
    }
}
