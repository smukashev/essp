package kz.bsbnb.usci.eav.storage.dao;

public interface IAdapterDao {
	public abstract void createStructure();
	public abstract void dropStructure();
	public abstract boolean testConnection();
}