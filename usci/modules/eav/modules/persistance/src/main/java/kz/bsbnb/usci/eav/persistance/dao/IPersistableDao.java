package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;

public interface IPersistableDao {
    long insert(IPersistable persistable);

    void update(IPersistable persistable);

    void delete(IPersistable persistable);
}
