package kz.bsbnb.usci.showcase.consumer;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.showcase.QueueEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import kz.bsbnb.usci.showcase.dao.ShowcaseDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class ShowcaseMessageConsumer implements MessageListener {

    final static Logger logger = Logger.getLogger(ShowcaseMessageConsumer.class);
    @Autowired
    SQLQueriesStats stats;
    @Autowired
    ShowcaseDao showcaseDao;
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

                if (queueEntry.getBaseEntityApplied().getOperation() == OperationType.DELETE) {
                    ShowcaseHolder h = showcaseDao.getHolderByClassName(
                            queueEntry.getBaseEntityApplied().getMeta().getClassName());

                    showcaseDao.deleteById(h, queueEntry.getBaseEntityApplied());
                    message.acknowledge();
                } else if (queueEntry.getBaseEntityApplied().getOperation() == OperationType.NEW) {
                    throw new UnsupportedOperationException("Operation new not supported in showcase");
                } else {
                    boolean found = false;
                    for (ShowcaseHolder holder : holders) {
                        if (!holder.getShowCaseMeta().getMeta().getClassName()
                                .equals(queueEntry.getBaseEntityApplied().getMeta().getClassName()))
                            continue;

                        if (scId == null || scId == holder.getShowCaseMeta().getId()) {
                            Future future = exec.submit(new CarteageGenerator(queueEntry.getBaseEntityApplied(),
                                    holder));

                            futures.add(future);

                            found = true;
                        }
                    }

                    if(!found)
                        logger.warn("MetaClass " + queueEntry.getBaseEntityApplied().getMeta().getClassName() +
                            " couldn't find matching ShowCase");

                    for (Future f : futures) {
                        try {
                            f.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }

                    futures.removeAll(futures);

                    long t4 = System.currentTimeMillis() - t3;
                    stats.put("message", t4);

                    message.acknowledge();
                }
            }
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private class CarteageGenerator implements Runnable {
        private IBaseEntity entity;
        private ShowcaseHolder holder;

        public CarteageGenerator(IBaseEntity entity, ShowcaseHolder holder) {
            this.entity = entity;
            this.holder = holder;
        }

        @Override
        public void run() {
            long t1 = System.currentTimeMillis();

            try {
                showcaseDao.generate(entity, holder);
            } catch(Exception e) {
                // TODO: SC_LOGS
                //
            }

            long t2 = System.currentTimeMillis() - t1;
            stats.put("showcase", t2);
        }
    }
}
