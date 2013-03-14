package kz.bsbnb.usci.eav_persistance.persistance.dao;

import kz.bsbnb.usci.eav_model.model.persistable.impl.Persistable;

/**
 * Common DAO interface.
 *
 * @param <T>
 */
public interface IDao<T extends Persistable>
{
	public T load(long id);
	public long save(T persistable);
	public void remove(T persistable);
}
