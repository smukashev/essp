package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.job.AbstractDataJob;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusSingleton;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import java.util.Iterator;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public final class DataJob extends AbstractDataJob {
    @Autowired
    @Qualifier(value = "remoteEntityService")
    private RmiProxyFactoryBean rmiProxyFactoryBean;

    @Autowired
    BasicBaseEntityComparator comparator;

    private IEntityService entityService;
    private final Logger logger = Logger.getLogger(DataJob.class);

    private double avgTimePrev = 0;
    private double avgTimeCur = 0;
    private long entityCounter = 0;

    @Autowired
    protected StatusSingleton statusSingleton;

    @Override
    public void run() {
        System.out.println("Data Job Started.");
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();

        while(true) {
            try {
                if(entities.size() > 0 && entitiesInProcess.size() < MAX_THREAD) {
                    //System.out.println("Number of threads: " + entitiesInProcess.size());
                    processNewEntities();
                }

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
        final ProcessJob processJob = new ProcessJob(entityService, entity, statusSingleton);

        if(entity != null) {
            logger.debug("Starting job");
            entitiesInProcess.add(entity);
            processingJobs.add(processJob);

            processJob.start();
            skip_count = 0;
        } else {

        }
    }

    private synchronized BaseEntity getClearEntity() {
        Iterator<BaseEntity> iterator = entities.iterator();

        while(iterator.hasNext()) {
            BaseEntity entity = iterator.next();

            if(!isInProcess(entity)) {
                iterator.remove();
                return entity;
            } else {
                logger.debug("Entity in process.");
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
        List<String> interList = comparator.intersect(entity1, entity2);

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

        if (interList.size() > 0)
            return true;
        return false;
    }
}
