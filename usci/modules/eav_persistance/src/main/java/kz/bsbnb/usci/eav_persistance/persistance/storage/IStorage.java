package kz.bsbnb.usci.eav_persistance.persistance.storage;

public interface IStorage
{
	public void initialize();
	public void clear();
	public void empty();
    public boolean isClean();
    public boolean testConnection();
}