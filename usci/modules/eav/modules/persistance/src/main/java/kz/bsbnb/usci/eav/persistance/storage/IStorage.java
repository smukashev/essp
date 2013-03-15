package kz.bsbnb.usci.eav.persistance.storage;

import java.util.HashMap;

public interface IStorage
{
	public void initialize();
	public void clear();
	public void empty();
    public boolean isClean();
    public boolean testConnection();
    public HashMap<String, Long> tableCounts();
}