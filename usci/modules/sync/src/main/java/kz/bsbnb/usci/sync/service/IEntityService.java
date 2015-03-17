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
    public void process(List<BaseEntity> entities);
    public BaseEntity load(long id);
    public BaseEntity load(long id, Date date);
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad);
    public BaseEntity search(BaseEntity baseEntity);
    public List<Long> getEntityIDsByMetaclass(long id);
    public List<RefListItem> getRefsByMetaclass(long metaClassId);
    public int getQueueSize();
    public void setThreadsCount(int threadsCount, boolean allowAutoIncrement);

    public SyncStatus getStatus();
    public void remove(long id);
}
