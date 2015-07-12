package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.EntityHolder;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;

public interface IBaseEntityApplyDao {
    IBaseEntity apply(IBaseEntity baseEntityForSave, IBaseEntityManager baseEntityManager);

    IBaseEntity apply(IBaseEntity baseEntityForSave, IBaseEntityManager baseEntityManager, EntityHolder entityHolder);

    IBaseEntity applyBaseEntityBasic(IBaseEntity baseEntitySaving, IBaseEntityManager baseEntityManager);

    IBaseEntity applyBaseEntityAdvanced(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded,
                                        IBaseEntityManager baseEntityManager);

    void applyBaseValueBasic(IBaseEntity baseEntityApplied, IBaseValue baseValue,
                             IBaseEntityManager baseEntityManager);

    void applySimpleSet(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                        IBaseEntityManager baseEntityManager);

    void applyComplexSet(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                         IBaseEntityManager baseEntityManager);

    void applySimpleValue(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                          IBaseEntityManager baseEntityManager);

    void applyComplexValue(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                           IBaseEntityManager baseEntityManager);

    void applyToDb(IBaseEntityManager baseEntityManager);
}
