package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

/**
 *
 */
public interface IBaseEntityDao extends IDao<BaseEntity>
{

    public BaseEntity search(BaseEntity baseEntity);

    public BaseEntity update(BaseEntity baseEntity);

    public BaseEntity update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad);

}
