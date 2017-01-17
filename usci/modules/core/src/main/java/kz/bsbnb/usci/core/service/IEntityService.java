package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.stats.QueryEntry;

import java.util.*;

public interface IEntityService {
    void process(BaseEntity mockEntity);

    BaseEntity load(long id);

    BaseEntity load(long id, Date date);

    RefColumnsResponse getRefColumns(long metaClassId);

    RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis);

    Map<String, QueryEntry> getSQLStats();

    RefListResponse getRefListApprox(long metaClassId);

    BaseEntity loadForDisplay(long entityId, Date reportDate);

    List<String> getValidationErrors(IBaseEntity baseEntity);

    Date getPreviousReportDate(long entityId, Date reportDate);

    String getStatus();
}
