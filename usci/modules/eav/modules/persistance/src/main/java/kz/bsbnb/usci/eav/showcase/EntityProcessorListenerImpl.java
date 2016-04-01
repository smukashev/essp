package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityProcessorListenerImpl implements IDaoListener {
    @Autowired
    private ShowcaseMessageProducer producer;

    @Autowired
    private SQLQueriesStats stats;

    @Override
    public void applyToDBEnded(IBaseEntity baseEntityApplied) {
        final long t1 = System.currentTimeMillis();

        final QueueEntry queueEntry = new QueueEntry(baseEntityApplied);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    producer.produce(queueEntry);
                } catch (Exception e) {
                    throw new RuntimeException(Errors.getMessage(Errors.E181, e.getMessage()));
                }
            }
        }).start();

        stats.put("producer.produce(queueEntry)", (System.currentTimeMillis() - t1));
    }
}
