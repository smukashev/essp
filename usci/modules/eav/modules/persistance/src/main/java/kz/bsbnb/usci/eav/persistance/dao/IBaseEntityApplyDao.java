package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.EntityHolder;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;

public interface IBaseEntityApplyDao {
    IBaseEntity apply(long creditorId, IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded,
                      IBaseEntityManager baseEntityManager, EntityHolder entityHolder);

    IBaseEntity applyBaseEntityBasic(long creditorId, IBaseEntity baseEntitySaving,
                                     IBaseEntityManager baseEntityManager);

    IBaseEntity applyBaseEntityAdvanced(long creditorId, IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded,
                                        IBaseEntityManager baseEntityManager);

    void applyBaseValueBasic(long creditorId, IBaseEntity baseEntityApplied, IBaseValue baseValue,
                             IBaseEntityManager baseEntityManager);

    void applySimpleSet(long creditorId, IBaseEntity baseEntity, IBaseValue baseValueSaving,
                        IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager);

    void applyComplexSet(long creditorId, IBaseEntity baseEntity, IBaseValue baseValueSaving,
                         IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager);

    void applySimpleValue(long creditorId, IBaseEntity baseEntity, IBaseValue baseValueSaving,
                          IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager);

    void applyComplexValue(long creditorId, IBaseEntity baseEntity, IBaseValue baseValueSaving,
                           IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager);

    void applyToDb(IBaseEntityManager baseEntityManager);
}
