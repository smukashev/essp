package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.manager.IBaseEntityMergeManager;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
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
    long search(IBaseEntity baseEntity);

    List<Long> search(long metaClassId);

    List<Long> search(String className);

    IBaseEntity loadByMaxReportDate(long id, Date reportDate);

    IBaseEntity loadByMaxReportDate(long id, Date reportDate, boolean caching);

    IBaseEntity loadByMinReportDate(long id, Date reportDate);

    IBaseEntity loadByMinReportDate(long id, Date reportDate, boolean caching);

    IBaseEntity loadByReportDate(long id, Date actualReportDate, boolean caching);

    IBaseEntity loadByReportDate(long id, Date actualReportDate);

    IBaseEntity load(long id);

    IBaseEntity load(long id, boolean caching);

    IBaseEntity load(long id, Date maxReportDate, Date actualReportDate);

    IBaseEntity load(long id, Date maxReportDate, Date actualReportDate, boolean caching);

    IBaseEntity prepare(IBaseEntity baseEntity);

    IBaseEntity process(IBaseEntity baseEntity);

    List<Long> getEntityIDsByMetaclass(long metaClassId);

    List<RefListItem> getRefsByMetaclass(long metaClassId);

    List<RefListItem> getRefsByMetaclassRaw(long metaClassId);

    List<BaseEntity> getEntityByMetaclass(MetaClass meta);

    boolean isApproved(long id);

    int batchCount(long id, String className);

    boolean remove(long baseEntityId);

    long getRandomBaseEntityId(long metaClassId);

    long getRandomBaseEntityId(IMetaClass metaClass);

    Set<Long> getChildBaseEntityIds(long parentBaseEntityIds);

    IDaoListener getApplyListener();

    void setApplyListener(IDaoListener applyListener);

    List<Date> getEntityReportDates(Long entityId);

    enum MergeResultChoice
    {
        RIGHT,
        LEFT
    }

    public IBaseEntity merge(IBaseEntity baseEntityLeft, IBaseEntity baseEntityRight,
                             IBaseEntityMergeManager mergeManager, MergeResultChoice choice, boolean deleteUnused);

    void populate(String metaName, Long id, Date reportDate);

    void populateSC(Long creditorId);

    void populateSC();

    List<Long> getSCEntityIds(Long id);

    List<Long[]> getSCEntityIds(int limit, Long prevMaxId);

    void removeSCEntityIds(List<Long> list, Long id);

    void removeSCEntityIds(List<Long> entityIds);

    void removeShowcaseId(Long id);

    List<Long> getShowcaseIdsToLoad();

    List<Long> getNewTableIds(Long id);

    void removeNewTableIds(List<Long> list, Long id);

}
