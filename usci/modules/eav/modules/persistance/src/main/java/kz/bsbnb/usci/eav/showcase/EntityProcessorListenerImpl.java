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

/**
 * Created by a.tkachenko on 5/14/14.
 */

@Component
public class EntityProcessorListenerImpl implements IDaoListener
{
    final static Logger logger = Logger.getLogger(EntityProcessorListenerImpl.class);
    //private ArrayList<ShowCaseHolder> holders = new ArrayList<ShowCaseHolder>();

    @Autowired
    ShowcaseMessageProducer producer;

    @Override
    public void applyToDBEnded(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IBaseEntity baseEntityApplied, IBaseEntityManager entityManager)
    {
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

    //public void addShowCaseHolder(ShowCaseHolder holder) {
    //    holders.add(holder);
    //}
}
