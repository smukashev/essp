package kz.bsbnb.usci.eav.storage.dao;

import kz.bsbnb.usci.eav.model.metadata.MetaData;

public interface IMetaDataDao {
	public abstract MetaData loadMetaData(String metaClassName);
	public abstract boolean saveMetaData(MetaData meta);
}
