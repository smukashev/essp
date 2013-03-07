package kz.bsbnb.usci.eav_persistance.persistance.dao;

import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;

import java.sql.Timestamp;

public interface IMetaClassDao extends IDao<MetaClass>
{
	public MetaClass load(String className);
    public MetaClass load(String className, Timestamp timestamp);
}
