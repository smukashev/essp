package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.Date;
import java.util.List;

public interface IMetaClassDao extends IDao<MetaClass>
{
	public MetaClass load(String className);
    public MetaClass load(String className, Date beginDate);
    public List<MetaClass> loadAll();
    public long save(MetaClass meta);
}
