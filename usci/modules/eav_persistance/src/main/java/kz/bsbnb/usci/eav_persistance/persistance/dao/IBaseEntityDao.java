package kz.bsbnb.usci.eav_persistance.persistance.dao;

import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;

/**
 *
 */
public interface IBaseEntityDao extends IDao<BaseEntity>
{
    BaseEntity load(BaseEntity baseEntity, boolean eager);

    //BaseEntity load(long id, boolean eager);

}
