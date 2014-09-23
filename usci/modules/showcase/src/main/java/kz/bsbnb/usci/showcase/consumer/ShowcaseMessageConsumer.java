package kz.bsbnb.usci.showcase.consumer;

import javax.annotation.PostConstruct;
import javax.jms.*;
import javax.xml.transform.Result;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.showcase.QueueEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import kz.bsbnb.usci.showcase.dao.ShowcaseDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private Random r = new Random();
    private ExecutorService exec = Executors.newCachedThreadPool();

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                long t3 = System.currentTimeMillis();

                ObjectMessage om = (ObjectMessage) message;
                QueueEntry queueEntry = (QueueEntry) om.getObject();
                Long scId = queueEntry.getScId();
                ArrayList<Future> futures = new ArrayList<Future>();
                List<ShowcaseHolder> holders = showcaseDao.getHolders();

                for(ShowcaseHolder holder : holders) {

                    if(!holder.getShowCaseMeta().getMeta().getClassName()
                            .equals(queueEntry.getBaseEntityApplied().getMeta().getClassName()))
                        continue;

                    //showcaseDao.generate(queueEntry.getBaseEntityApplied(), holder);
                    if(scId == null || scId == holder.getShowCaseMeta().getId()){
                        Future future = exec.submit(new CarteageGenerator(queueEntry.getBaseEntityApplied(), holder));
                        futures.add(future);
                    }
                }

                for(Future f : futures){
                    try {
                        f.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                futures.removeAll(futures);

                long t4 = System.currentTimeMillis() - t3;
                stats.put("message", t4);

                message.acknowledge();
            }
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private class CarteageGenerator implements Runnable{

        private IBaseEntity entity;
        private ShowcaseHolder holder;

        public CarteageGenerator(IBaseEntity entity, ShowcaseHolder holder){
            this.entity = entity;
            this.holder = holder;
        }

        @Override
        public void run() {
            long t1 = System.currentTimeMillis();
            //showcaseDao.dbCarteageGenerate(entity, holder);
            showcaseDao.generate(entity,holder);
            long t2 = System.currentTimeMillis() - t1;
            //stats.put("showcase " + holder.getShowCaseMeta().getName(), t2);
            stats.put("showcase", t2);
            System.out.print("." + (r.nextInt(50) == 25 ? "\n" : ""));
        }
    }
}
