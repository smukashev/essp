package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;

import java.util.Date;
import java.util.Set;

/**
 *
 */
public interface IBaseEntityDao extends IPersistableDao {

    public IBaseEntity load(long id);

    public IBaseEntity load(long id, Date reportDate, Date actualReportDate);

    public IMetaClass getMetaClass(long baseEntityId);

    public boolean isUsed(long baseEntityId);

    public boolean deleteRecursive(long baseEntityId);

    public boolean deleteRecursive(long baseEntityId, IMetaClass metaClass);

    public long getRandomBaseEntityId(long metaClassId);

    public long getRandomBaseEntityId(IMetaClass metaClass);

    public Set<Long> getChildBaseEntityIds(long parentBaseEntityId);

    public boolean isDeleted(long id);

}
