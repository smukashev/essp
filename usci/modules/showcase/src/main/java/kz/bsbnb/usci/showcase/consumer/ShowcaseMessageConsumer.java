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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class ShowcaseMessageConsumer implements MessageListener {
    final static Logger logger = Logger.getLogger(ShowcaseMessageConsumer.class);

    @Autowired
    SQLQueriesStats stats;

    @Autowired
    ShowcaseDao showcaseDao;

    private ExecutorService exec = Executors.newCachedThreadPool();

    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    private static HashSet<SyncKey> syncSet = new HashSet<>();

    private class SyncKey {
        public Long entityId;
        public Long scId;

        public SyncKey(Long entityId, Long scId) {
            this.entityId = entityId;
            this.scId = scId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SyncKey syncKey = (SyncKey) o;

            if (!entityId.equals(syncKey.entityId)) return false;
            return scId.equals(syncKey.scId);

        }

        @Override
        public int hashCode() {
            int result = entityId.hashCode();
            result = 31 * result + scId.hashCode();
            return result;
        }
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            ObjectMessage om = (ObjectMessage) message;
            QueueEntry queueEntry;

            try {
                queueEntry = (QueueEntry) om.getObject();
            } catch(JMSException jms) {
                jms.printStackTrace();
                return;
            }

            Long scId = queueEntry.getScId();

            try {
                ArrayList<Future> futures = new ArrayList<>();
                List<ShowcaseHolder> holders = showcaseDao.getHolders();

                if (queueEntry.getBaseEntityApplied().getOperation() == OperationType.DELETE) {
                    ShowcaseHolder h = showcaseDao.getHolderByClassName(
                            queueEntry.getBaseEntityApplied().getMeta().getClassName());

                    showcaseDao.deleteById(h, queueEntry.getBaseEntityApplied());
                } else if (queueEntry.getBaseEntityApplied().getOperation() == OperationType.NEW) {
                    throw new UnsupportedOperationException("Operation new not supported in showcase");
                } else {
                    boolean found = false;

                    for (ShowcaseHolder holder : holders) {
                        if (!holder.getShowCaseMeta().getMeta().getClassName()
                                .equals(queueEntry.getBaseEntityApplied().getMeta().getClassName()))
                            continue;

                        if (scId == null || scId == 0L || scId == holder.getShowCaseMeta().getId()) {
                            Future future = exec.submit(new CarteageGenerator(queueEntry.getBaseEntityApplied(),
                                    holder));

                            futures.add(future);

                            found = true;
                        }
                    }

                    if(!found)
                        System.err.println("MetaClass " + queueEntry.getBaseEntityApplied().getMeta().getClassName() +
                                " couldn't find matching ShowCase");

                    for (Future f : futures)
                        f.get();

                    futures.removeAll(futures);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());

                StringBuilder sb = new StringBuilder();

                for(StackTraceElement s : e.getStackTrace())
                    sb.append(s.toString());

                showcaseDao.insertBadEntity(queueEntry.getBaseEntityApplied().getId(), scId,
                        queueEntry.getBaseEntityApplied().getReportDate(), sb.toString(), e.getMessage());
            }
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
            SyncKey syncKey = new SyncKey(entity.getId(), holder.getShowCaseMeta().getId());

            try {
                while(true) {
                    lock.readLock().lock();
                    if(!syncSet.contains(syncKey)) {
                        lock.readLock().unlock();
                        break;
                    }

                    lock.readLock().unlock();
                    Thread.sleep(50);
                }

                lock.writeLock().lock();
                syncSet.add(syncKey);
                lock.writeLock().unlock();

                showcaseDao.generate(entity, holder);
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                lock.writeLock().lock();
                syncSet.remove(syncKey);
                lock.writeLock().unlock();
            }
        }
    }
}
