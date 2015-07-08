package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.stats.QueryEntry;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface IEntityService {
    BaseEntity load(long id);

    BaseEntity load(long id, Date date);

    void process(BaseEntity baseEntity);

    BaseEntity search(BaseEntity baseEntity);

    BaseEntity prepare(BaseEntity baseEntity);

    BaseEntity getActualBaseEntity(BaseEntity baseEntity);

    void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad);

    List<Long> getEntityIDsByMetaclass(long id);

    List<RefListItem> getRefsByMetaclass(long metaClassId);

    RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis);

    HashMap<String, QueryEntry> getSQLStats();

    void clearSQLStats();

    void remove(long id);

    void removeAllByMetaClass(IMetaClass metaClass);

    Set<Long> getChildBaseEntityIds(long parentBaseEntityIds);

    RefColumnsResponse getRefColumns(long metaClassId);
}
