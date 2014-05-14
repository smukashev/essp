package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;

import java.util.ArrayList;

/**
 * Created by a.tkachenko on 5/14/14.
 */
public class EntityProcessorListenerImpl implements IDaoListener
{
    private ArrayList<ShowCaseHolder> holders = new ArrayList<ShowCaseHolder>();

    @Override
    public void applyToDBEnded(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IBaseEntity baseEntityApplied, IBaseEntityManager entityManager)
    {
        for(ShowCaseHolder holder : holders) {
            holder.process(baseEntitySaving, baseEntityLoaded, baseEntityApplied, entityManager);
        }
    }

    public void addShowCaseHolder(ShowCaseHolder holder) {
        holders.add(holder);
    }
}
