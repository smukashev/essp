package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.persistance.Persistable;

public interface IDao<T extends Persistable> {
	public T load(long id);
	public long save(T persistable);
	public void remove(T persistable);
}
