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

/**
 * Created by almaz on 6/27/14.
 */
@Service
public class CoreShowcaseServiceImpl implements CoreShowcaseService {

    @Autowired
    protected IBaseEntityProcessorDao baseEntityProcessorDao;
    @Autowired
    protected ShowcaseMessageProducer producer;
    private Map<String, QueueViewMBean> queueViewBeanCache = new HashMap<String, QueueViewMBean>();
    private final Logger logger = LoggerFactory.getLogger(CoreShowcaseServiceImpl.class);

    private Map<Long, Thread> SCThreads = new HashMap<Long, Thread>();

    @Override
    public void start(String metaName, Long id, Date reportDate){
        baseEntityProcessorDao.populate(metaName, id, reportDate);
        Thread t = new Thread(new Sender(id));
        SCThreads.put(id, t);
        t.start();
    }

    @Override
    public void pause(Long id){
        if(SCThreads.containsKey(id))
            SCThreads.get(id).interrupt();
    }

    @Override
    public void resume(Long id){
        Thread t = new Thread(new Sender(id));
        SCThreads.put(id, t);
        t.start();
    }

    @Override
    public void stop(Long id){
        SCThreads.get(id).interrupt();
        baseEntityProcessorDao.removeShowcaseId(id);
        SCThreads.remove(id);
    }

    @Override
    public List<Long> listLoading(){
        List list = new ArrayList<Long>();
        for(Long id : SCThreads.keySet()){
            list.add(id);
        }
        return list;
    }

    private QueueViewMBean getShowcaseQueue(){
        JMXServiceURL url;

        if(queueViewBeanCache.containsKey("showcaseQueue"))
            return queueViewBeanCache.get("showcaseQueue");

        try {
            url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1094/jmxrmi");
            JMXConnector jmxc = JMXConnectorFactory.connect(url);
            MBeanServerConnection conn = jmxc.getMBeanServerConnection();

            ObjectName activeMQ = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost");
            BrokerViewMBean mbean = (BrokerViewMBean) MBeanServerInvocationHandler
                    .newProxyInstance(conn, activeMQ, BrokerViewMBean.class, true);

            for (ObjectName name : mbean.getQueues()) {
                QueueViewMBean queueMbean = (QueueViewMBean)
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

    private class Sender implements Runnable {
        Long scId;

        public Sender(Long id){
            this.scId = id;
        }

        @Override
        public void run() {
            QueueViewMBean queueMbean = getShowcaseQueue();

            if(queueMbean == null) {
                logger.error("Can't get ShowcaseQueue, queueMBean is null!");
                throw new NullPointerException();
            }

            while(true){
                if(queueMbean.getQueueSize() == 0){
                    List<Long> list = baseEntityProcessorDao.getSCEntityIds(scId);
                    if(list.size() == 0){
                        logger.info("Done loading entities for showcase %s, reportDate %s");
                        SCThreads.remove(scId);
                        return;
                    }
                    for(Long id : list){
                        QueueEntry entry = new QueueEntry().setBaseEntityApplied(baseEntityProcessorDao.load(id))
                                .setScId(scId);
                        try {
                            producer.produce(entry);
                        } catch (InterruptedException e) {
                            //do nothing, finish loading
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    baseEntityProcessorDao.removeSCEntityIds(list, scId);
                    if(Thread.interrupted()) return;
                } else try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
}
