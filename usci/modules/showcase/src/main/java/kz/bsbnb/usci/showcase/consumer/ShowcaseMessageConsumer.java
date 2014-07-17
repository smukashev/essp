package kz.bsbnb.usci.showcase.consumer;

import javax.jms.*;

import kz.bsbnb.usci.eav.showcase.QueueEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import kz.bsbnb.usci.showcase.dao.ShowcaseDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by almaz on 6/25/14.
 */
@Component
public class ShowcaseMessageConsumer implements MessageListener{

    @Autowired
    SQLQueriesStats stats;

    @Autowired
    ShowcaseDao showcaseDao;

    final static Logger logger = Logger.getLogger(ShowcaseMessageConsumer.class);

    private int numOfMessages = 0;

    @Override
    public void onMessage(Message message) {
        try {
            numOfMessages++;
            if (message instanceof ObjectMessage) {
                ObjectMessage om = (ObjectMessage) message;
                QueueEntry queueEntry = (QueueEntry) om.getObject();
                Long scId = queueEntry.getScId();
                for(ShowcaseHolder holder : showcaseDao.getHolders()) {
                    if(scId == null || scId == holder.getShowCaseMeta().getId()){
                        long t1 = System.currentTimeMillis();
                        showcaseDao.dbCarteageGenerate(queueEntry.getBaseEntityApplied(), holder);
                        long t2 = System.currentTimeMillis() - t1;
                        stats.put("showcaseGenerate(per credit)", t2);//sqlstat rmi://127.0.0.1:1099/entityService
                    }
                }
                message.acknowledge();
            }
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
