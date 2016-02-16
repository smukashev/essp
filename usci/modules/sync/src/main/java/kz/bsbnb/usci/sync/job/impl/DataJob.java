package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.sync.job.AbstractDataJob;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.tool.status.SyncStatusSingleton;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.*;
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

    protected final List<InProcessTester> entitiesInProcess = new ArrayList<>();

    private volatile BaseEntity currentEntity;
    private volatile boolean currentIntersection;

    ExecutorService executorService = Executors.newCachedThreadPool();

    private double avgTimePrev = 0;
    private double avgTimeCur = 0;
    private long entityCounter = 0;

    private int clearJobsIndex = 0;

    private class InProcessTester implements Callable<Boolean> {
        private BaseEntity myEntity;

        private InProcessTester(BaseEntity myEntity) {
            this.myEntity = myEntity;
        }

        public Boolean call() {
            try {
                for (BaseEntity myEntityKeyElement : myEntity.getKeyElements()) {
                    for (BaseEntity currentEntityKeyElement : currentEntity.getKeyElements()) {
                        if (myEntityKeyElement.equalsByKey(currentEntityKeyElement)) {
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

        public BaseEntity getMyEntity() {
            return myEntity;
        }
    }

    @Override
    public void run() {
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();
        actualCountJob = new ActualCountJob(batchService);
        actualCountJob.start();

        while(true) {
            try {
                if(entities.size() > 0 && entitiesInProcess.size() < currentThread)
                    processNewEntities();

                if(processingJobs.size() > 0)
                    removeDeadJobs();

                if(entities.size() == 0 && entitiesInProcess.size() == 0) {
                    skip_count++;
                    Thread.sleep(SLEEP_TIME_NORMAL);
                }

                if(skip_count > SKIP_TIME_MAX) {
                    Thread.sleep(SLEEP_TIME_LONG);
                    skip_count = 0;
                }

                syncStatusSingleton.put(entities.size(), entitiesInProcess.size(), currentThread, avgTimeCur);

                /* Debug */
                if(entitiesInProcess.size() != processingJobs.size())
                    throw new IllegalStateException("CRITICAL: EntitiesInProcess != ProcessJobs");
            }
            catch(NullPointerException ne) {
                ne.printStackTrace();
                System.exit(11);
            }
            catch(Exception e) {
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

                entityCounter++;
                if (entityCounter < WATCH_INTERVAL) {
                    avgTimeCur = (avgTimeCur * (entityCounter - 1)) / entityCounter +
                            processJob.getTimeSpent() / entityCounter;
                } else {
                    if (avgTimePrev > 0) {
                        double offset = avgTimeCur / avgTimePrev - 1;
                        if (autoChooseThreshold) {
                            if (offset > THRESHOLD) {
                                currentThread--;
                            } else {
                                currentThread++;
                            }

                            if (currentThread >= MAX_THREAD) {
                                currentThread = MAX_THREAD - 10;
                            }
                            if (currentThread < MIN_THREAD) {
                                currentThread = MIN_THREAD;
                            }
                        }

                        System.out.println("Threads: " + currentThread + ", avgCur: " +
                                avgTimeCur + ", counter: " + entityCounter);
                    }
                    entityCounter = 0;
                    avgTimePrev = avgTimeCur;
                }

                Iterator<InProcessTester> entityProcessIterator = entitiesInProcess.iterator();

                boolean found = false;

                while(entityProcessIterator.hasNext()) {
                    if(entity.hashCode() == entityProcessIterator.next().getMyEntity().hashCode()) {
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
        final ProcessJob processJob = new ProcessJob(entityService, entity, batchService);

        if(entity != null) {
            logger.debug("Starting job");
            entitiesInProcess.add(new InProcessTester(entity));
            processingJobs.add(processJob);
            actualCountJob.insertBatchId(entity.getBatchId());

            processJob.start();
            skip_count = 0;
        }
    }

    private synchronized BaseEntity getClearEntity() {
        if(clearJobsIndex >= entities.size())
            clearJobsIndex = 0;

        Iterator<BaseEntity> iterator = entities.listIterator(clearJobsIndex);
        while(iterator.hasNext()) {
            BaseEntity entity = iterator.next();

            entity.getKeyElements();

            if(!isInProcessWithThreads(entity)) {
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
