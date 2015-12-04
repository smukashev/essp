package kz.bsbnb.usci.eav.manager;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.List;

public interface IBaseEntityManager {
    void registerAsInserted(IPersistable persistableObject);

    void registerAsUpdated(IPersistable persistableObject);

    void registerAsDeleted(IPersistable persistableObject);

    void registerProcessedBaseEntity(IBaseEntity processedBaseEntity);

    List<IPersistable> getInsertedObjects(Class objectClass);

    List<IPersistable> getUpdatedObjects(Class objectClass);

    List<IPersistable> getDeletedObjects(Class objectClass);

    IBaseEntity getProcessed(IBaseEntity baseEntity);
}
