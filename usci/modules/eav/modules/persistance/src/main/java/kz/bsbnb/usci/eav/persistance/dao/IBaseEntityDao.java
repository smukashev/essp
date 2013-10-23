package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * @author a.motov
 * @since 1.0
 * @version 1.0
 */
public interface IBaseEntityDao
{

    /**
     * Search BaseEntity on key fields in the DB. In case
     * if the search found more than one instance, it will
     * return the first in the list. If the search has not
     * been found a single instance, it returns a null value.
     *
     * @param baseEntity instance of the BaseEntity for search
     * @return obtained instance of the BaseEntity by the search.
     * @since 1.0
     */
    public long search(IBaseEntity baseEntity);

    public List<Long> search(long metaClassId);

    public List<Long> search(String className);

    public IBaseEntity load(long id);

    public IBaseEntity load(long id, boolean closed);

    public BaseEntity load(long id, Date reportDate);

    public BaseEntity load(long id, Date reportDate, boolean closed);

    public IBaseEntity prepare(IBaseEntity baseEntity);

    public IBaseEntity apply(IBaseEntity baseEntity);

    public IBaseEntity process(IBaseEntity baseEntity);

    public IBaseEntity saveOrUpdate(IBaseEntity baseEntity);

    public IBaseEntity update(IBaseEntity baseEntityForSave, IBaseEntity baseEntityLoaded);

    public boolean isUsed(long baseEntityId);

    public Set<Date> getAvailableReportDates(long baseEntityId);

    public Date getMinReportDate(long baseEntityId);

    public Date getMaxReportDate(long baseEntityId);

    public Date getMaxReportDate(long baseEntityId, Date reportDate);

    public IBaseEntity save(IBaseEntity baseEntity);

    public void remove(IBaseEntity baseEntity);

    public List<Long> getEntityIDsByMetaclass(long metaClassId);

    public List<BaseEntity> getEntityByMetaclass(MetaClass meta);

    public boolean isApproved(long id);
    public int batchCount(long id, String className);
}
