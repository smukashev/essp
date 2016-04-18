package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.stats.QueryEntry;

import java.util.*;

public interface IEntityService {
    void process(BaseEntity mockEntity);

    BaseEntity load(long id);

    BaseEntity load(long id, Date date);

    RefColumnsResponse getRefColumns(long metaClassId);

    RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis);

    Map<String, QueryEntry> getSQLStats();
}
