package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityProcessorListenerImpl implements IDaoListener {
    @Autowired
    ShowcaseMessageProducer producer;

    @Autowired
    SQLQueriesStats stats;

    @Override
    public void applyToDBEnded(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded,
                               IBaseEntity baseEntityApplied, IBaseEntityManager entityManager) {
        long t1 = System.currentTimeMillis();

        final QueueEntry queueEntry = new QueueEntry()
                .setBaseEntityApplied(baseEntityApplied)
                .setBaseEntityLoaded(baseEntityLoaded);
                /*.setBaseEntitySaving(baseEntitySaving)
                .setEntityManager(entityManager);*/

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    producer.produce(queueEntry);
                } catch (Exception e) {
                    throw new RuntimeException(Errors.E181+"|" + (e.getMessage().length() > 255
                            ? e.getMessage().substring(0, 255) : e.getMessage()));
                }
            }
        }).start();

        stats.put("producer.produce(queueEntry)", (System.currentTimeMillis() - t1));
    }
}
