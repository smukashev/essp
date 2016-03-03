package kz.bsbnb.usci.showcase.consumer;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.showcase.QueueEntry;
import kz.bsbnb.usci.eav.showcase.ShowCase;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class ShowcaseMessageConsumer implements MessageListener {
    @Autowired
    private ShowcaseDaoImpl showcaseDao;

    @Autowired
    private CortegeDaoImpl cortegeDao;

    private final ExecutorService exec = Executors.newCachedThreadPool();

    private final Logger logger = LoggerFactory.getLogger(ShowcaseDaoImpl.class);

    @Override
    @Transactional
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {
            ObjectMessage om = (ObjectMessage) message;
            QueueEntry queueEntry;

            try {
                queueEntry = (QueueEntry) om.getObject();
            } catch (JMSException jms) {
                jms.printStackTrace();
                return;
            }

            if (queueEntry.getBaseEntityApplied() == null) {
                logger.error("Переданный объект пустой;");
                return;
            }

            Long scId = queueEntry.getScId();

            try {
                ArrayList<Future> futures = new ArrayList<>();
                List<ShowCase> showCases = showcaseDao.getShowCases();

                if (showCases.size() == 0)
                    throw new IllegalStateException("Необходимо создать витрины;");

                OperationType operationType;

                if (queueEntry.getBaseEntityApplied().getOperation() != null) {
                    operationType = queueEntry.getBaseEntityApplied().getOperation();
                } else {
                    operationType = OperationType.INSERT;
                }

                if (operationType == OperationType.DELETE) {
                    ShowCase showCase = showcaseDao.getHolderByClassName(
                            queueEntry.getBaseEntityApplied().getMeta().getClassName());

                    cortegeDao.deleteById(showCase, queueEntry.getBaseEntityApplied());
                } else if (operationType == OperationType.CLOSE) {
                    cortegeDao.closeEntities(scId, queueEntry.getBaseEntityApplied(), showCases);
                } else {
                    boolean found = false;

                    final String metaClassName = queueEntry.getBaseEntityApplied().getMeta().getClassName();

                    for (ShowCase showCase : showCases) {
                        if (showCase.getMeta().getClassName().equals(metaClassName)) {
                            if (scId == null || scId == 0L || scId == showCase.getId()) {
                                Future future = exec.submit(
                                        new CortegeGenerator(queueEntry.getBaseEntityApplied(), showCase));

                                futures.add(future);

                                found = true;
                            }
                        }
                    }

                    if (!found) {
                        for (ShowCase showCase : showCases) {
                            for (ShowCase childShowCase : showCase.getChildShowCases()) {
                                if (childShowCase.getMeta().getClassName().equals(metaClassName)) {
                                    Future future = exec.submit(
                                            new CortegeGenerator(queueEntry.getBaseEntityApplied(), childShowCase));

                                    futures.add(future);

                                    found = true;
                                }
                            }
                        }
                    }

                    if (found) {
                        for (Future f : futures) f.get();
                    } else {
                        logger.error("Для мета класа  " + metaClassName + " нет существующих витрин;");
                    }

                    futures.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private class CortegeGenerator implements Runnable {
        private IBaseEntity entity;
        private ShowCase showCase;

        public CortegeGenerator(IBaseEntity entity, ShowCase showCase) {
            this.entity = entity;
            this.showCase = showCase;
        }

        @Override
        public void run() {
            cortegeDao.generate(entity, showCase);
        }
    }
}
