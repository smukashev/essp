package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.sql.Timestamp;
import java.util.List;

public interface IMetaClassDao extends IDao<MetaClass>
{
	public MetaClass load(String className);
    public MetaClass load(String className, Timestamp timestamp);
    public List<MetaClass> loadAll();
}
