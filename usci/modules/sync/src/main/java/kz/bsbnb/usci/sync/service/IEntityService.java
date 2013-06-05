package kz.bsbnb.usci.sync.service;


import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.util.List;

/**
 * @author k.tulbassiyev
 */
public interface IEntityService {
    public void process(List<BaseEntity> entities);
    public void save(BaseEntity baseEntity);
    public BaseEntity search(BaseEntity baseEntity);
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad);
}
