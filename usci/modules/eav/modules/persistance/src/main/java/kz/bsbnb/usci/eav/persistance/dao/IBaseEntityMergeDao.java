package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.manager.IBaseEntityMergeManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;

public interface IBaseEntityMergeDao {
    IBaseEntity merge(IBaseEntity baseEntityLeft, IBaseEntity baseEntityRight, IBaseEntityMergeManager mergeManager,
                      MergeResultChoice choice, boolean deleteUnused);

    enum MergeResultChoice {
        RIGHT,
        LEFT
    }
}
