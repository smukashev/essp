package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;

import java.sql.Timestamp;

public interface IMetaClassDao extends IDao<MetaClass>
{
	public MetaClass load(String className);
    public MetaClass load(String className, Timestamp timestamp);
}
