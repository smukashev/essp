package kz.bsbnb.usci.showcase.consumer;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.showcase.QueueEntry;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.model.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.showcase.dao.impl.CortegeDaoImpl;
import kz.bsbnb.usci.showcase.dao.impl.ShowcaseDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class ShowcaseMessageConsumer implements MessageListener {
    @Autowired
    private ShowcaseDaoImpl showcaseDao;

    @Autowired
    private CortegeDaoImpl cortegeDao;

    @Autowired
    private SQLQueriesStats sqlQueriesStats;

    private final ExecutorService exec = Executors.newCachedThreadPool();

    private final Logger logger = LoggerFactory.getLogger(ShowcaseMessageConsumer.class);

    private final static List<IBaseEntity> entities = new LinkedList<>();

    @Override
    @Transactional
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            long onMessageTime = System.currentTimeMillis();
            ObjectMessage om = (ObjectMessage) message;
            QueueEntry queueEntry;

            IBaseEntity currentEntity;

            try {
                queueEntry = (QueueEntry) om.getObject();
            } catch (JMSException jms) {
                jms.printStackTrace();
                return;
            }

            currentEntity = queueEntry.getBaseEntityApplied();

            if (currentEntity == null) {
                logger.error("Переданный объект пустой;");
                return;
            }

            try {
                final Map<ShowCase, Future> showCaseFutureMap = new HashMap<>();
                final List<ShowCase> showCases = showcaseDao.getShowCases();

                if (showCases.size() == 0)
                    throw new IllegalStateException(Errors.compose(Errors.E271));

                currentEntity.getKeyElements();

                boolean entityFound;
                int failCounter = 0;
                do {
                    entityFound = false;
                    synchronized (entities) {
                        for (IBaseEntity entity : entities) {
                            for (IBaseEntity keyEntity : entity.getKeyElements()) {
                                for (IBaseEntity currentKeyEntity : currentEntity.getKeyElements()) {
                                    if (keyEntity.getMeta().getId() == currentKeyEntity.getMeta().getId()
                                            && keyEntity.equalsByKey(currentKeyEntity)) {
                                        entityFound = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (!entityFound)
                            entities.add(currentEntity);
                    }

                    if (entityFound)
                        Thread.sleep(50);

                    if (entityFound && (failCounter %  1000) == 0) {
                        //throw new IllegalStateException(Errors.compose(Errors.E288));
                        System.out.println("failCounter = " + failCounter);
                    }
                } while(entityFound);

                boolean found = false;

                final String metaClassName = currentEntity.getMeta().getClassName();

                for (ShowCase showCase : showCases) {
                    if (showCase.getMeta().getClassName().equals(metaClassName)) {
                        Future future = exec.submit(new CortegeGenerator(currentEntity, showCase));
                        showCaseFutureMap.put(showCase, future);
                        found = true;
                    }
                }

                if (!found) {
                    for (ShowCase showCase : showCases) {
                        for (ShowCase childShowCase : showCase.getChildShowCases()) {
                            if (childShowCase.getMeta().getClassName().equals(metaClassName)) {
                                Future future = exec.submit(new CortegeGenerator(currentEntity, childShowCase));

                                showCaseFutureMap.put(childShowCase, future);
                                found = true;
                            }
                        }
                    }
                }

                if (found) {
                    for (Map.Entry<ShowCase, Future> entry : showCaseFutureMap.entrySet()) {
                        try {
                            entry.getValue().get(3600, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            logger.error("key={}, e={}",entry.getKey().toString(), e);
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error on message exception={}", e);
                throw new RuntimeException(e.getMessage());
            } finally {
                synchronized (entities) {
                    entities.remove(currentEntity);

                    sqlQueriesStats.put("java::onMessage", (System.currentTimeMillis() - onMessageTime));
                }
            }
        }
    }

    private class CortegeGenerator implements Runnable {
        private final IBaseEntity entity;
        private final ShowCase showCase;

        CortegeGenerator(IBaseEntity entity, ShowCase showCase) {
            this.entity = entity;
            this.showCase = showCase;
        }

        @Override
        public void run() {
            cortegeDao.generate(entity, showCase);
        }
    }
}
