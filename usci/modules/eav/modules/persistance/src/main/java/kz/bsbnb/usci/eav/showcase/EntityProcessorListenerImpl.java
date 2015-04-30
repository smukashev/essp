package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import java.util.ArrayList;

@Component
public class EntityProcessorListenerImpl implements IDaoListener {
    @Autowired
    ShowcaseMessageProducer producer;

    @Override
    public void applyToDBEnded(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded,
                               IBaseEntity baseEntityApplied, IBaseEntityManager entityManager) {

        QueueEntry queueEntry = new QueueEntry()
                .setBaseEntityApplied(baseEntityApplied)
                .setBaseEntityLoaded(baseEntityLoaded)
                .setBaseEntitySaving(baseEntitySaving)
                .setEntityManager(entityManager);

        try {
            producer.produce(queueEntry);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
