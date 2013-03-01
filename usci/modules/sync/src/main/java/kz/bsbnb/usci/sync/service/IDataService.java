package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.BaseEntity;

import java.util.List;

/**
 * @author k.tulbassiyev
 */
public interface IDataService
{
    public void process(List<BaseEntity> entities);
}
