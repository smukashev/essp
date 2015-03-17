package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.IBaseEntityMergeManager;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;

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

    public IBaseEntity loadByMaxReportDate(long id, Date reportDate, boolean caching);

    public IBaseEntity loadByMinReportDate(long id, Date reportDate);

    public IBaseEntity loadByMinReportDate(long id, Date reportDate, boolean caching);

    public IBaseEntity loadByReportDate(long id, Date actualReportDate, boolean caching);

    public IBaseEntity loadByReportDate(long id, Date actualReportDate);

    public IBaseEntity load(long id);

    public IBaseEntity load(long id, boolean caching);

    public IBaseEntity load(long id, Date maxReportDate, Date actualReportDate);

    public IBaseEntity load(long id, Date maxReportDate, Date actualReportDate, boolean caching);

    public IBaseEntity prepare(IBaseEntity baseEntity);

    public IBaseEntity process(IBaseEntity baseEntity);

    public List<Long> getEntityIDsByMetaclass(long metaClassId);

    public List<RefListItem> getRefsByMetaclass(long metaClassId);

    public List<RefListItem> getRefsByMetaclassRaw(long metaClassId);

    public List<BaseEntity> getEntityByMetaclass(MetaClass meta);

    public boolean isApproved(long id);

    public int batchCount(long id, String className);

    public boolean remove(long baseEntityId);

    public long getRandomBaseEntityId(long metaClassId);

    public long getRandomBaseEntityId(IMetaClass metaClass);

    public Set<Long> getChildBaseEntityIds(long parentBaseEntityIds);

    public IDaoListener getApplyListener();

    public void setApplyListener(IDaoListener applyListener);

    List<Date> getEntityReportDates(Long entityId);

    enum MergeResultChoice
    {
        RIGHT,
        LEFT
    }

    public IBaseEntity merge(IBaseEntity baseEntityLeft, IBaseEntity baseEntityRight,
                             IBaseEntityMergeManager mergeManager, MergeResultChoice choice);

    public void populate(String metaName, Long id, Date reportDate);

    void populateSC(Long creditorId);

    void populateSC();

    public List<Long> getSCEntityIds(Long id);

    public List<Long> getSCEntityIds(int limit);

    public void removeSCEntityIds(List<Long> list, Long id);

    void removeSCEntityIds(List<Long> entityIds);

    public void removeShowcaseId(Long id);

    public List<Long> getShowcaseIdsToLoad();

    public List<Long> getNewTableIds(Long id);

    public void removeNewTableIds(List<Long> list, Long id);

}
