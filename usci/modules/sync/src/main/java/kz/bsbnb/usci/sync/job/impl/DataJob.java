package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.util.SetUtils;
import kz.bsbnb.usci.sync.job.AbstractDataJob;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.tool.status.SyncStatusSingleton;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
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

    private Map<Long, Map<String, Boolean> > documentOptimizer = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private double avgTimePrev = 0;
    private double avgTimeCur = 0;
    private long entityCounter = 0;
    private long executorServiceCnt = 0;
    private double avgTimeExectuor = 0;

    private int clearJobsIndex = 0;

    private class InProcessTester implements Callable<Boolean> {
        private final BaseEntity myEntity;

        private InProcessTester(BaseEntity myEntity) {
            this.myEntity = myEntity;
        }

        public Boolean call() {
            try {
                for (IBaseEntity myEntityKeyElement : myEntity.getKeyElements()) {
                    if(syncOptimization(myEntityKeyElement))
                        continue;

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

        private boolean syncOptimization(IBaseEntity myEntityKeyElement) {
            if(myEntityKeyElement.getMeta().getClassName().equals("document")) {
                if(myEntity.getMeta().getClassName().equals("credit") && currentEntity.getMeta().getClassName().equals("credit")) {
                    long creditorId = myEntity.getBaseValue("creditor").getCreditorId();
                    if(currentEntity.getBaseValue("creditor").getCreditorId() == creditorId) {
                        String documentKey = creditorId + "|" + myEntityKeyElement.getEl("no").toString() + "|" + myEntityKeyElement.getEl("doc_type.code").toString();
                        Map<String, Boolean> hm = documentOptimizer.get(myEntity.getBatchId());

                        if(hm != null && hm.containsKey(documentKey)) {
                            return true;
                        }
                    }
                }
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
                if (entities.size() > 0 && entitiesInProcess.size() < StaticRouter.getThreadLimit() && processNewEntities());

                if (processingJobs.size() > 0)
                    removeDeadJobs();

                setFinishStatusToBatches();

                if (entities.size() == 0 && entitiesInProcess.size() == 0) {
                    Thread.sleep(SLEEP_TIME_NORMAL);
                    skip_count++;
                }

                if (skip_count > SKIP_TIME_MAX) {
                    Thread.sleep(SLEEP_TIME_LONG);
                    skip_count = 0;
                }

                syncStatusSingleton.put(entities.size(), entitiesInProcess.size(), avgTimeCur);
                syncStatusSingleton.setExecutorStat(executorServiceCnt, avgTimeExectuor);
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

                syncOptimization(processJob, entity);

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

    private void syncOptimization(ProcessJob processJob, BaseEntity entity) {
        if(entity.getMeta().getClassName().equals("credit") && processJob.statusCode) {
            long creditorId = entity.getBaseValue("creditor").getCreditorId();
            Map<String,Boolean> docs;

            if(!documentOptimizer.containsKey(entity.getBatchId())) {
                docs = new ConcurrentHashMap<>();
                documentOptimizer.put(entity.getBatchId(), docs);
            }

            docs = documentOptimizer.get(entity.getBatchId());

            List<IBaseEntity> documents = ((List) entity.getEls("{get}subject.docs"));

            for (IBaseEntity document : documents) {
                String documentKey = creditorId + "|" + document.getEl("no").toString() + "|" + document.getEl("doc_type.code").toString();
                docs.put(documentKey, true);
            }

            documents = ((List) entity.getEls("{get}subject.organization_info.head.docs"));

            for (IBaseEntity document : documents) {
                String documentKey = creditorId + "|" + document.getEl("no").toString() + "|" + document.getEl("doc_type.code").toString();
                docs.put(documentKey, true);
            }
        }
    }

    private boolean processNewEntities() {
        final BaseEntity entity = getClearEntity();
        final ProcessJob processJob = new ProcessJob(entityService, entity);

        if (entity != null) {
            logger.debug("Starting job");
            entitiesInProcess.add(new InProcessTester(entity));
            processingJobs.add(processJob);
            actualCountJob.insertBatchId(entity.getBatchId());

            processJob.start();
            skip_count = 0;
            return  true;
        }

        return false;
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
            long t1 = System.currentTimeMillis();
            executorService.invokeAll(entitiesInProcess);
            long t2 = System.currentTimeMillis();
            executorServiceCnt ++;
            avgTimeExectuor = ((executorServiceCnt - 1) * avgTimeExectuor + (t2 - t1) ) / executorServiceCnt;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return currentIntersection;
    }

    private void setFinishStatusToBatches(){
        Set<Long> activeBatches = new HashSet<>();
        Set<Long> difference;

        synchronized (this) {
            for (BaseEntity entity : entities) {
                activeBatches.add(entity.getBatchId());
            }

            for (InProcessTester entitiesInProces : entitiesInProcess) {
                activeBatches.add(entitiesInProces.getMyEntity().getBatchId());
            }

            difference = SetUtils.difference(batches, activeBatches);

            if(difference.size() > 0) {
                finishedCreditors.addAll(difference);
            }

            for (Long batchId : difference) {
                batches.remove(batchId);
                documentOptimizer.remove(batchId);
            }
        }

        for (Long batchId : difference) {
            batchService.endBatch(batchId);
        }
    }
}
