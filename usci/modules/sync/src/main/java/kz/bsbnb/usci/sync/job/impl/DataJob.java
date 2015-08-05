package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.job.AbstractDataJob;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.tool.status.SyncStatusSingleton;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author k.tulbassiyev
 */
public final class DataJob extends AbstractDataJob {
    @Autowired
    @Qualifier(value = "remoteEntityService")
    private RmiProxyFactoryBean rmiProxyFactoryBean;

    @Autowired
    BasicBaseEntityComparator comparator;

    @Autowired
    SyncStatusSingleton syncStatusSingleton;

    private IEntityService entityService;

    @Autowired
    private IBatchService batchService;

    private final Logger logger = Logger.getLogger(DataJob.class);

    protected final List<InProcessTester> entitiesInProcess
            = Collections.synchronizedList(new ArrayList<InProcessTester>());

    BaseEntity currentEntity;
    boolean currentIntersection;

    ExecutorService service = Executors.newCachedThreadPool();

    private double avgTimePrev = 0;
    private double avgTimeCur = 0;
    private long entityCounter = 0;

    private int clearJobsIndex = 0;

    private class InProcessTester implements Callable<Boolean> {
        BaseEntity myEntity;

        private InProcessTester(BaseEntity myEntity) {
            this.myEntity = myEntity;
        }

        public Boolean call() {
            if(hasCrossLine(currentEntity, myEntity)) {
                currentIntersection = true;
                return true;
            }

            return false;
        }

        public BaseEntity getMyEntity() {
            return myEntity;
        }
    }

    private class InProcessContainer extends Thread {
        private BaseEntity entity;

        private InProcessContainer(BaseEntity entity) {
            this.entity = entity;
        }

        @Override
        public void run() {
            if(hasCrossLine(entity, currentEntity))
            currentIntersection = true;
        }

        public BaseEntity getEntity() {
            return entity;
        }

        public void setEntity(BaseEntity entity) {
            this.entity = entity;
        }
    }

    @Override
    public void run() {
        System.out.println("Data Job Started.");
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();

        while(true) {
            try {
                if(entities.size() > 0 && entitiesInProcess.size() < currentThread) {
                    //System.out.println("Number of threads: " + entitiesInProcess.size());
                    //long t1 = System.currentTimeMillis();
                    processNewEntities();
                    //System.out.println("New job in " + (System.currentTimeMillis() - t1));
                }

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

                //logger.debug("Queue size: " + entities.size());
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

                ////////////////
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

                        System.out.println("Threads: " + currentThread + ", avgPrev: " +
                                avgTimePrev + ", avgCur: " + avgTimeCur + ", offset: " + offset);
                    }
                    entityCounter = 0;
                    avgTimePrev = avgTimeCur;
                }
                ////////////////

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

            processJob.start();
            skip_count = 0;
        } else {

        }
    }

    private synchronized BaseEntity getClearEntity() {
        /*Iterator<BaseEntity> iterator = entities.iterator();

        int i = 0;
        while(iterator.hasNext()) {
            BaseEntity entity = iterator.next();

            if(!isInProcess(entity)) {
                iterator.remove();
                System.out.println("Watched entities " + (++i));
                return entity;
            } else {
                logger.debug("Entity in process.");
            }
            i++;
        }      */

        long t1 = System.currentTimeMillis();

        if(clearJobsIndex >= entities.size()) {
            clearJobsIndex = 0;
        }

        int i = 0;
        Iterator<BaseEntity> iterator = entities.listIterator(clearJobsIndex);
        while(iterator.hasNext()) {
            BaseEntity entity = iterator.next();

            if(!isInProcessWithThreads(entity)) {
                iterator.remove();
                //System.out.println("Watched entities " + (++i));
                //System.out.println("Time spent: " + (System.currentTimeMillis() - t1));
                return entity;
            } else {
                logger.debug("Entity in process.");
            }
            i++;
            clearJobsIndex++;
        }

        return null;
    }

    private boolean isInProcess(BaseEntity baseEntity) {
        for (InProcessTester entity : entitiesInProcess)
            if(hasCrossLine(baseEntity, entity.getMyEntity()))
                return true;

        return false;
    }

    private boolean isInProcessWithThreads(BaseEntity baseEntity) {
        currentEntity = baseEntity;
        currentIntersection = false;

        try {
            service.invokeAll(entitiesInProcess);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return currentIntersection;
    }

    private boolean hasCrossLine(BaseEntity entity1, BaseEntity entity2) {
        //List<String> interList = comparator.intersect(entity1, entity2);

        /*logger.debug("###################################################");
        logger.debug(entity1.toString());
        logger.debug("---------------------------------------------------");
        for (String str : interList) {
            logger.debug(str);
        }
        logger.debug("---------------------------------------------------");
        logger.debug(entity2.toString());
        logger.debug("###################################################");*/

        /*System.out.println("---------------------------------------------------");
        for (String str : interList) {
            System.out.println(str);
        }
        System.out.println("---------------------------------------------------");*/

        /*if (interList.size() < 1) {
            System.out.println("Error in comparator");
        } */

        //if (interList.size() > 0)
        if (comparator.hasIntersect(entity1, entity2))
            return true;
        return false;
    }
}
