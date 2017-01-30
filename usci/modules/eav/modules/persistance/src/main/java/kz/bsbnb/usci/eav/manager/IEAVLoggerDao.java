package kz.bsbnb.usci.eav.manager;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;


public interface IEAVLoggerDao {
    void log(IPersistable deletedObject);
}
