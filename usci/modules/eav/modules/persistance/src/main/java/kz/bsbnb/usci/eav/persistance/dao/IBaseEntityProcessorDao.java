package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
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
public interface IBaseEntityProcessorDao
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

    public IBaseEntity loadByMaxReportDate(long id, Date reportDate);

    public IBaseEntity load(long id);

    public IBaseEntity load(long id, boolean caching);

    public IBaseEntity load(long id, Date reportDate);

    public IBaseEntity load(long id, Date reportDate, boolean caching);

    public IBaseEntity prepare(IBaseEntity baseEntity);

    public IBaseEntity process(IBaseEntity baseEntity);

    public Set<Date> getAvailableReportDates(long baseEntityId);

    public Date getMinReportDate(long baseEntityId);

    public Date getMaxReportDate(long baseEntityId);

    public Date getMaxReportDate(long baseEntityId, Date reportDate);

    public List<Long> getEntityIDsByMetaclass(long metaClassId);

    public List<RefListItem> getRefsByMetaclass(long metaClassId);

    public List<BaseEntity> getEntityByMetaclass(MetaClass meta);

    public boolean isApproved(long id);

    public int batchCount(long id, String className);


    public void loadSimpleSetValues(IBaseSet baseSet, Date baseEntityReportDate);

}
