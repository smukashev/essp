package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;

/**
 *
 */
public interface IBaseEntityDao extends IDao<BaseEntity> {

    BaseEntity load(BaseEntity baseEntity, boolean eager);

    //BaseEntity load(long id, boolean eager);

}
