package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;

import java.util.Date;
import java.util.List;

public interface IRefProcessorDao {
    List<RefListItem> getRefsByMetaclass(long metaClassId);

    List<RefListItem> getRefsByMetaclassRaw(long metaClassId);

    RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis);

    RefColumnsResponse getRefColumns(long metaClassId);

    RefListResponse getRefListApprox(long metaClassId);
}
