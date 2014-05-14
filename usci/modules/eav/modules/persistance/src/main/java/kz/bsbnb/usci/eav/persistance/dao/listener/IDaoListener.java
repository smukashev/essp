package kz.bsbnb.usci.eav.persistance.dao.listener;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;

/**
 * Created by a.tkachenko on 5/14/14.
 */
public interface IDaoListener
{
    public void applyToDBEnded(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IBaseEntity baseEntityApplied,
                               IBaseEntityManager entityManager);
}
