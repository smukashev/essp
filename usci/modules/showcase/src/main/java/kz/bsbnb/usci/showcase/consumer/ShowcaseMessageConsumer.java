package kz.bsbnb.usci.showcase.consumer;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.showcase.QueueEntry;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.showcase.dao.impl.CortegeDaoImpl;
import kz.bsbnb.usci.showcase.dao.impl.ShowcaseDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.*;
import java.util.concurrent.*;

@Component
public class ShowcaseMessageConsumer implements MessageListener, InitializingBean{
    @Autowired
    private ShowcaseDaoImpl showcaseDao;

    @Autowired
    private CortegeDaoImpl cortegeDao;

    private final ExecutorService exec = Executors.newCachedThreadPool();

    private final Logger logger = LoggerFactory.getLogger(ShowcaseDaoImpl.class);

    private static List<IBaseEntity> entities = new ArrayList<>();

    ExecutorService executorService = Executors.newCachedThreadPool();

    long start = 0L;
    private static Integer counter = 0;

    @Override
    public void afterPropertiesSet() throws Exception {
        start = System.currentTimeMillis();
    }

    @Override
    @Transactional
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage) {

            if(counter > 250){
                System.out.println("time 250 : "+(System.currentTimeMillis()-start));
                counter = 0;
                start = System.currentTimeMillis();
            }

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

            queueEntry.getBaseEntityApplied().getKeyElements();

            try {
                Map<ShowCase, Future> showCaseFutureMap = new HashMap<>();
                List<ShowCase> showCases = showcaseDao.getShowCases();

                if (showCases.size() == 0)
                    throw new IllegalStateException(Errors.getMessage(Errors.E271));

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
                    cortegeDao.closeEntities(queueEntry.getBaseEntityApplied(), showCases);
                } else {
                    boolean found = false;

                    final String metaClassName = queueEntry.getBaseEntityApplied().getMeta().getClassName();

                    for (ShowCase showCase : showCases) {
                        if (showCase.getMeta().getClassName().equals(metaClassName)) {
                            Future future = exec.submit(
                                    new CortegeGenerator(queueEntry.getBaseEntityApplied(), showCase));

                            showCaseFutureMap.put(showCase, future);
                            found = true;
                        }
                    }

                    if (!found) {
                        for (ShowCase showCase : showCases) {
                            for (ShowCase childShowCase : showCase.getChildShowCases()) {
                                if (childShowCase.getMeta().getClassName().equals(metaClassName)) {
                                    Future future = exec.submit(
                                            new CortegeGenerator(queueEntry.getBaseEntityApplied(), childShowCase));

                                    showCaseFutureMap.put(childShowCase, future);
                                    found = true;
                                }
                            }
                        }
                    }

                    if (found) {
                        //System.out.println(entities.size());
                        waitShowcase(queueEntry.getBaseEntityApplied());
                        entities.add(queueEntry.getBaseEntityApplied());

                        for (Map.Entry<ShowCase, Future> entry : showCaseFutureMap.entrySet()) {
                            try {
                                entry.getValue().get(60, TimeUnit.SECONDS);
                            } catch (Exception e) {
                                System.err.println(entry.getKey().toString());
                                throw e;
                            }
                        }
                    } else {
                        logger.error("Для мета класа  " + metaClassName + " нет существующих витрин;");
                    }

                    synchronized (counter){
                        counter++;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            } finally {
                synchronized (entities) {
                    entities.remove(queueEntry.getBaseEntityApplied());
                }
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

    private void waitShowcase(IBaseEntity entity) {
        while (true) {
            boolean found = false;

            List<InProcessTester> entitiesInProcess = new ArrayList<>();
            synchronized (entities) {
                for (IBaseEntity processingEntity : entities) {
                    entitiesInProcess.add(new InProcessTester(entity, processingEntity));
                }
            }

            List<Future<Boolean>> futures = null;
            try {
                futures = executorService.invokeAll(entitiesInProcess);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(Future<Boolean> future : futures){
                try {
                    if(future.get()){
                        found = true;
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            if (found) {
                try {
                    Thread.currentThread().sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }

    private class InProcessTester implements Callable<Boolean> {
        private IBaseEntity thisEntity;
        private IBaseEntity thatEntity;

        private InProcessTester(IBaseEntity thisEntity,IBaseEntity thatEntity) {
            this.thisEntity = thisEntity;
            this.thatEntity = thatEntity;
        }

        public Boolean call() {
            try {
                for (IBaseEntity keyEntity : thisEntity.getKeyElements()) {
                    for (IBaseEntity keyProcessingEntity : thatEntity.getKeyElements()) {
                        if (keyEntity.equalsByKey(keyProcessingEntity)) {
                            return  true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

    }


}
