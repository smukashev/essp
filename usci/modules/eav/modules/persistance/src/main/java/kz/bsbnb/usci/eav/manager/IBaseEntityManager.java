package kz.bsbnb.usci.eav.manager;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.List;

/**
 * Created by Alexandr.Motov on 09.03.14.
 */
public interface IBaseEntityManager {

    public void registerAsInserted(IPersistable persistableObject);

    public void registerAsUpdated(IPersistable persistableObject);

    public void registerAsDeleted(IPersistable persistableObject);

    public List<IPersistable> getInsertedObjects(Class objectClass);

    public List<IPersistable> getUpdatedObjects(Class objectClass);

    public List<IPersistable> getDeletedObjects(Class objectClass);

}
