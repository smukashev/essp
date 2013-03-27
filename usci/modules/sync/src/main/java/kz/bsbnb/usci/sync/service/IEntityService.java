package kz.bsbnb.usci.sync.service;


import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.util.List;

/**
 * @author k.tulbassiyev
 */
public interface IEntityService {
    public void process(List<BaseEntity> entities);
}
