package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Created by a.tkachenko on 5/14/14.
 */

@Component
public class EntityProcessorListenerImpl implements IDaoListener
{
    private ArrayList<ShowCaseHolder> holders = new ArrayList<ShowCaseHolder>();

    @Autowired
    ShowCaseSingleton singleton;

    @Override
    public void applyToDBEnded(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IBaseEntity baseEntityApplied, IBaseEntityManager entityManager)
    {
//        for(ShowCaseHolder holder : holders) {
//            holder.process(baseEntitySaving, baseEntityLoaded, baseEntityApplied, entityManager);
//        }

        QueueEntry queueEntry = new QueueEntry()
                .setBaseEntityApplied(baseEntityApplied)
                .setBaseEntityLoaded(baseEntityLoaded)
                .setBaseEntitySaving(baseEntitySaving)
                .setEntityManager(entityManager);

        singleton.enqueue(queueEntry);
    }

    public void addShowCaseHolder(ShowCaseHolder holder) {
        holders.add(holder);
    }
}
