package kz.bsbnb.usci.sync.service;


import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.tool.status.SyncStatus;

import java.util.Date;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public interface IEntityService {
    void process(List<BaseEntity> entities);

    BaseEntity load(long id);

    BaseEntity load(long id, Date date);

    void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad);

    BaseEntity search(BaseEntity baseEntity);

    List<Long> getEntityIDsByMetaclass(long id);

    List<RefListItem> getRefsByMetaclass(long metaClassId);

    int getQueueSize();

    void setThreadsCount(int threadsCount, boolean allowAutoIncrement);

    SyncStatus getStatus();

    void remove(long id);
}
