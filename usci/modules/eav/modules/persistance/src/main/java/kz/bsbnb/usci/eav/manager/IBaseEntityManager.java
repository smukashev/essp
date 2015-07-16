package kz.bsbnb.usci.eav.manager;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.List;
import java.util.Set;

public interface IBaseEntityManager {

    void registerAsInserted(IPersistable persistableObject);

    void registerAsUpdated(IPersistable persistableObject);

    void registerAsDeleted(IPersistable persistableObject);

    void registerUnusedBaseEntity(IBaseEntity unusedBaseEntity);

    void registerProcessedBaseEntity(IBaseEntity processedBaseEntity);

    List<IPersistable> getInsertedObjects(Class objectClass);

    List<IPersistable> getUpdatedObjects(Class objectClass);

    List<IPersistable> getDeletedObjects(Class objectClass);

    Set<IBaseEntity> getUnusedBaseEntities();

    IBaseEntity getProcessed(IBaseEntity baseEntity);

}
