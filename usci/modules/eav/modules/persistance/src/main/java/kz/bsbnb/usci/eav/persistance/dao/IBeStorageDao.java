package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

import java.util.Date;

/**
 *
 */
public interface IBeStorageDao {

    public IBaseEntity getBaseEntity(long id, Date reportDate, boolean withClosedValues);

    public IBaseEntity getBaseEntity(long id, Date reportDate);

    public IBaseEntity getBaseEntity(long id, boolean withClosedValues);

    public IBaseEntity getBaseEntity(long id);

    public void clean();

}
