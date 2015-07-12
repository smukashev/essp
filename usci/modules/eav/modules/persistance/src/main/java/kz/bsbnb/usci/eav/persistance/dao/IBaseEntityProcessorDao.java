package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface IBaseEntityProcessorDao {
    long search(IBaseEntity baseEntity);

    List<Long> search(long metaClassId);

    List<Long> search(String className);

    IBaseEntity prepare(IBaseEntity baseEntity);

    IBaseEntity process(IBaseEntity baseEntity);

    List<Long> getEntityIDsByMetaclass(long metaClassId);

    List<RefListItem> getRefsByMetaclass(long metaClassId);

    List<RefListItem> getRefsByMetaclassRaw(long metaClassId);

    RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis);

    List<BaseEntity> getEntityByMetaclass(MetaClass meta);

    boolean isApproved(long id);

    boolean remove(long baseEntityId);

    long getRandomBaseEntityId(long metaClassId);

    long getRandomBaseEntityId(IMetaClass metaClass);

    Set<Long> getChildBaseEntityIds(long parentBaseEntityIds);

    List<Date> getEntityReportDates(Long entityId);

    RefColumnsResponse getRefColumns(long metaClassId);

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
