package kz.bsbnb.usci.eav.manager;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.List;
import java.util.Set;

/**
 * Created by Alexandr.Motov on 09.03.14.
 */
public interface IBaseEntityManager {

    public void registerAsInserted(IPersistable persistableObject);

    public void registerAsUpdated(IPersistable persistableObject);

    public void registerAsDeleted(IPersistable persistableObject);

    public void registerUnusedBaseEntity(IBaseEntity unusedBaseEntity);

    public void registerProcessedBaseEntity(IBaseEntity processedBaseEntity);

    public List<IPersistable> getInsertedObjects(Class objectClass);

    public List<IPersistable> getUpdatedObjects(Class objectClass);

    public List<IPersistable> getDeletedObjects(Class objectClass);

    public Set<IBaseEntity> getUnusedBaseEntities();

    public IBaseEntity getProcessed(IBaseEntity baseEntity);

}
