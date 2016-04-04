package kz.bsbnb.usci.eav.persistance.dao.listener;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

public interface IDaoListener {
    void applyToDBEnded(IBaseEntity baseEntityApplied);
}
