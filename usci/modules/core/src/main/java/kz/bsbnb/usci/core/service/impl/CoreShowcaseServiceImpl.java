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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public void start(String metaName, Long id, Date reportDate){

        baseEntityProcessorDao.populate(metaName, id, reportDate);
        //TODO: restart on failure
        new Thread(new Sender(id)).start();

    }

    private QueueViewMBean getShowcaseQueue(){
        JMXServiceURL url = null;
        if(queueViewBeanCache.containsKey("showcaseQueue"))return queueViewBeanCache.get("showcaseQueue");
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

    private class Sender implements Runnable{

        Long scId;

        public Sender(Long id){
            this.scId = id;
        }

        @Override
        public void run() {
            QueueViewMBean queueMbean = getShowcaseQueue();
            while(true){
                if(queueMbean.getQueueSize() == 0){
                    List<Long> list = baseEntityProcessorDao.getNewTableIds(scId);
                    if(list.size() == 0){
                        logger.info("Done loading entities for showcase %s, reportDate %s");
                        return;
                    }
                    for(Long id : list){
                        QueueEntry entry = new QueueEntry().setBaseEntityApplied(baseEntityProcessorDao.load(id))
                                .setScId(scId);
                        try {
                            producer.produce(entry);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    baseEntityProcessorDao.removeNewTableIds(list, scId);
                } else try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
