package kz.bsbnb.usci.sync.service;


import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.util.List;

/**
 * @author k.tulbassiyev
 */
public interface IEntityService {
    public void process(List<BaseEntity> entities);
    public BaseEntity load(long id);
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad);
    public BaseEntity search(BaseEntity baseEntity);
    public List<Long> getEntityIDsByMetaclass(long id);
    public List<RefListItem> getRefsByMetaclass(long metaClassId);
    public int getQueueSize();
}
