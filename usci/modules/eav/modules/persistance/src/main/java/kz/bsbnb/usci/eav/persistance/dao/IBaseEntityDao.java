package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

/**
 *
 */
public interface IBaseEntityDao extends IDao<BaseEntity>
{

    public void update(BaseEntity baseEntity);

}
