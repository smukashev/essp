package kz.bsbnb.usci.eav.persistance.storage;

public interface IStorage {

	public void initialize();

	public void clear();

	public void empty();

	public boolean testConnection();
}