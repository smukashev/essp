package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;

import java.util.Date;
import java.util.Set;

public interface IBaseEntityDao extends IPersistableDao {
    IBaseEntity load(long id);

    IBaseEntity load(long id, Date reportDate, Date actualReportDate);

    IMetaClass getMetaClass(long baseEntityId);

    boolean isUsed(long baseEntityId);

    boolean isUsed(long baseEntityId, long exceptContainingId);

    boolean deleteRecursive(long baseEntityId);

    boolean deleteRecursive(long baseEntityId, IMetaClass metaClass);

    Set<Long> getChildBaseEntityIds(long parentBaseEntityId);

    boolean isDeleted(long id);
}
