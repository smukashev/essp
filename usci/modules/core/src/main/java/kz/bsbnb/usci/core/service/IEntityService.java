package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.stats.QueryEntry;

import java.util.*;

public interface IEntityService {
    BaseEntity load(long id);

    BaseEntity load(long id, Date date);

    void process(BaseEntity baseEntity);

    BaseEntity search(BaseEntity baseEntity);

    BaseEntity prepare(BaseEntity baseEntity, long creditorId);

    BaseEntity getActualBaseEntity(BaseEntity baseEntity);

    List<Long> getEntityIDsByMetaclass(long id);

    List<RefListItem> getRefsByMetaclass(long metaClassId);

    RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis);

    Map<String, QueryEntry> getSQLStats();

    void remove(long id);

    Set<Long> getChildBaseEntityIds(long parentBaseEntityIds);

    RefColumnsResponse getRefColumns(long metaClassId);
}
