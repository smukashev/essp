package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;

public interface IMetaClassDao extends IDao<MetaClass> {
	public MetaClass load(String className);
}
