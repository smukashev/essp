package kz.bsbnb.usci.eav.manager;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.List;

/**
 * Created by Alexandr.Motov on 09.03.14.
 */
public interface IBaseEntityManager {

    public void registerAsInserted(IPersistable persistableObject);

    public List<IPersistable> getInsertedObjects(Class objectClass);

}
