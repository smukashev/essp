package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

public interface IDao<T extends Persistable> {
    T load(long id);

    long save(T persistable);

    void remove(T persistable);
}
