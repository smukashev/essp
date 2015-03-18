package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.CoreShowcaseService;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.showcase.QueueEntry;
import kz.bsbnb.usci.eav.showcase.ShowcaseMessageProducer;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.*;

@Service
public class CoreShowcaseServiceImpl implements CoreShowcaseService {
    private final Logger logger = LoggerFactory.getLogger(CoreShowcaseServiceImpl.class);
    @Autowired
    protected IBaseEntityProcessorDao baseEntityProcessorDao;
    @Autowired
    protected ShowcaseMessageProducer producer;
    private Map<String, QueueViewMBean> queueViewBeanCache = new HashMap<String, QueueViewMBean>();
    private Map<Long, Thread> SCThreads = new HashMap<Long, Thread>();

    private static final int ENTITY_COUNT_PER_LOAD = 10;
    private static final int ENTITY_COUNT_PER_CLEAR = 10;
    private static final int SC_HISTORY_THREADS_COUNT = 10;
    private static final int SLEEP_TIME_MILLIS = 1000;

    private Map<Integer, Thread> scHistoryThreads = new HashMap<>();

    @Override
    public void start(String metaName, Long id, Date reportDate) {
        baseEntityProcessorDao.populate(metaName, id, reportDate);
        Thread t = new Thread(new Sender(id, reportDate));
        SCThreads.put(id, t);
        t.start();
    }

    @Override
    public void startLoadHistory(boolean populate, Long creditorId) {
        if (populate) {
            if (creditorId != null) {
                baseEntityProcessorDao.populateSC(creditorId);
            } else {
                baseEntityProcessorDao.populateSC();
            }
        }

        IdSupplier idSupplier = new IdSupplier();

        for (int i = 0; i < SC_HISTORY_THREADS_COUNT; i++) {
            Thread t = new Thread(new HistorySender(i, idSupplier));
            scHistoryThreads.put(i, t);
            t.start();
        }
    }

    @Override
    public void stopHistory() {
        for (Thread t : scHistoryThreads.values()) {
            t.interrupt();
        }
        scHistoryThreads.clear();
    }

    @Override
    public void pause(Long id){
        if(SCThreads.containsKey(id))
            SCThreads.get(id).interrupt();
    }

    @Override
    public void resume(final Long id) {
        Thread t = new Thread(new Sender(id));
        SCThreads.put(id, t);
        t.start();
    }

    @Override
    public void stop(Long id) {
        SCThreads.get(id).interrupt();
        baseEntityProcessorDao.removeShowcaseId(id);
        SCThreads.remove(id);
    }

    @Override
    public List<Long> listLoading() {
        List<Long> list = new ArrayList<Long>();

        for (Long id : SCThreads.keySet())
            list.add(id);

        return list;
    }

    private QueueViewMBean getShowcaseQueue() {
        JMXServiceURL url;

        if (queueViewBeanCache.containsKey("showcaseQueue"))
            return queueViewBeanCache.get("showcaseQueue");

        try {
            url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1094/jmxrmi");
            JMXConnector jmxc = JMXConnectorFactory.connect(url);
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();

            ObjectName activeMQ = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost");
            BrokerViewMBean mbean = MBeanServerInvocationHandler
                    .newProxyInstance(conn, activeMQ, BrokerViewMBean.class, true);

            for (ObjectName name : mbean.getQueues()) {
                QueueViewMBean queueMbean =
                        MBeanServerInvocationHandler.newProxyInstance(conn, name, QueueViewMBean.class, true);

                if (queueMbean.getName().equals("showcaseQueue")) {
                    queueViewBeanCache.put("showcaseQueue", queueMbean);
                    return queueMbean;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private class IdSupplier {
        private List<Long> idsToProcess;
        private List<Long> processedIds;
        private Long maxIdToProcess;

        public IdSupplier() {
            idsToProcess = new ArrayList<>();
            processedIds = new ArrayList<>();
            maxIdToProcess = -1L;
        }

        public synchronized Long supply() {
            if (idsToProcess.isEmpty()) {
                List<Long> entityIds = baseEntityProcessorDao.getSCEntityIds(ENTITY_COUNT_PER_LOAD, maxIdToProcess);
                idsToProcess.addAll(entityIds);
            }

            if (idsToProcess.isEmpty()) {
                return null;
            }

            maxIdToProcess = idsToProcess.get(idsToProcess.size() - 1);

            return idsToProcess.isEmpty() ? null : idsToProcess.remove(0);
        }

        public synchronized void done(Long entityId) {
            processedIds.add(entityId);

            if (processedIds.size() == ENTITY_COUNT_PER_CLEAR) {
                baseEntityProcessorDao.removeSCEntityIds(processedIds);
                processedIds.clear();
            }
        }

        public void clearProcessedLeft() {
            if (!processedIds.isEmpty()) {
                baseEntityProcessorDao.removeSCEntityIds(processedIds);
                processedIds.clear();
            }
        }
    }

    private class HistorySender implements Runnable {
        private int index;
        private IdSupplier idSupplier;

        public HistorySender(int index, IdSupplier idSupplier) {
            this.index = index;
            this.idSupplier = idSupplier;
        }

        @Override
        public void run() {
            while (true) {
                Long entityId = idSupplier.supply();

                logger.info("STARTED LOADING (entityId = %s, threadIndex = %s)", entityId, index);
                System.out.printf("STARTED LOADING (entityId = %s, threadIndex = %s)\n", entityId, index);

                if (entityId == null) {
                    onFinish(false);
                    return;
                }

                List<Date> reportDates = baseEntityProcessorDao.getEntityReportDates(entityId);

                for (Date reportDate : reportDates) {
                    QueueEntry entry = new QueueEntry()
                            .setBaseEntityApplied(baseEntityProcessorDao.loadByReportDate(entityId, reportDate));

                    try {
                        producer.produce(entry);
                    } catch (InterruptedException e) {
                        // do nothing, finish loading
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                idSupplier.done(entityId);

                logger.info("FINISHED LOADING (entityId = %s, threadIndex = %s)", entityId, index);
                System.out.printf("FINISHED LOADING (entityId = %s, threadIndex = %s)\n", entityId, index);

                if (Thread.interrupted()) {
                    onFinish(true);
                    return;
                }

                try {
                    Thread.sleep(SLEEP_TIME_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void onFinish(boolean afterInterrupt) {
            logger.info("FINISH LOADING ENTITIES HISTORY TO SHOWCASE (threadIndex = %s, afterInterrupt = %s)", index, afterInterrupt);
            System.out.printf("FINISH LOADING ENTITIES HISTORY TO SHOWCASE (threadIndex = %s, afterInterrupt = %s)\n", index, afterInterrupt);

            scHistoryThreads.remove(index);

            if (scHistoryThreads.isEmpty()) {
                idSupplier.clearProcessedLeft();
            }
        }
    }

    private class Sender implements Runnable {
        final Long scId;
        Date reportDate;

        public Sender(Long id) {
            this.scId = id;
        }

        public Sender(Long id, Date reportDate) {
            this.scId = id;
            this.reportDate = reportDate;
        }

        @Override
        public void run() {
            QueueViewMBean queueMbean = getShowcaseQueue();

            if (queueMbean == null) {
                logger.error("Can't get ShowcaseQueue, queueMBean is null!");
                throw new NullPointerException();
            }

            while (true) {
                if (queueMbean.getQueueSize() == 0) {
                    List<Long> list = baseEntityProcessorDao.getSCEntityIds(scId);

                    if (list.size() == 0) {
                        logger.info("Done loading entities for showcase %s, reportDate %s", scId, reportDate);
                        SCThreads.remove(scId);
                        return;
                    }
                    for (Long id : list) {
                        QueueEntry entry = new QueueEntry().setBaseEntityApplied(baseEntityProcessorDao.load(id))
                                .setScId(scId);
                        try {
                            producer.produce(entry);
                        } catch (InterruptedException e) {
                            //do nothing, finish loading
                            logger.error("Producer interrupted!");
                            logger.error(e.getMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    baseEntityProcessorDao.removeSCEntityIds(list, scId);
                    if (Thread.interrupted()) return;
                } else {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
    }
}
