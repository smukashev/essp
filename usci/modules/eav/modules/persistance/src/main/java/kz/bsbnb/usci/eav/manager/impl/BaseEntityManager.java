package kz.bsbnb.usci.eav.manager.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.*;

/**
 * Created by Alexandr.Motov on 09.03.14.
 */
public class BaseEntityManager implements IBaseEntityManager {

    public static List<Class> CLASS_PRIORITY = new ArrayList<Class>();
    static
    {
        CLASS_PRIORITY.add(BaseEntity.class);
        CLASS_PRIORITY.add(BaseEntityReportDate.class);

        CLASS_PRIORITY.add(BaseSet.class);

        CLASS_PRIORITY.add(BaseEntityBooleanValue.class);
        CLASS_PRIORITY.add(BaseEntityDateValue.class);
        CLASS_PRIORITY.add(BaseEntityDoubleValue.class);
        CLASS_PRIORITY.add(BaseEntityIntegerValue.class);
        CLASS_PRIORITY.add(BaseEntityStringValue.class);
        CLASS_PRIORITY.add(BaseEntityComplexValue.class);

        CLASS_PRIORITY.add(BaseEntitySimpleSet.class);
        CLASS_PRIORITY.add(BaseEntityComplexSet.class);

        CLASS_PRIORITY.add(BaseSetBooleanValue.class);
        CLASS_PRIORITY.add(BaseSetDateValue.class);
        CLASS_PRIORITY.add(BaseSetDoubleValue.class);
        CLASS_PRIORITY.add(BaseSetIntegerValue.class);
        CLASS_PRIORITY.add(BaseSetStringValue.class);
        CLASS_PRIORITY.add(BaseSetComplexValue.class);
    }

    private Map<Class, List<IPersistable>> insertedObjects = new HashMap<Class, List<IPersistable>>();
    private Map<Class, List<IPersistable>> updatedObjects = new HashMap<Class, List<IPersistable>>();
    private Map<Class, List<IPersistable>> deletedObjects = new HashMap<Class, List<IPersistable>>();

    private Set<IBaseEntity> unusedBaseEntities = new HashSet<IBaseEntity>();

    public void registerAsInserted(IPersistable insertedObject)
    {
        if (insertedObject == null)
        {
            throw new RuntimeException("Inserted object can not be null;");
        }

        Class objectClass = insertedObject.getClass();
        if (insertedObjects.containsKey(objectClass))
        {
            insertedObjects.get(objectClass).add(insertedObject);
        }
        else
        {
            List<IPersistable> objects = new ArrayList<IPersistable>();
            objects.add(insertedObject);

            insertedObjects.put(objectClass, objects);
        }
    }

    public void registerAsUpdated(IPersistable updatedObject)
    {
        if (updatedObject == null)
        {
            throw new RuntimeException("Updated object can not be null;");
        }

        Class objectClass = updatedObject.getClass();
        if (updatedObjects.containsKey(objectClass))
        {
            updatedObjects.get(objectClass).add(updatedObject);
        }
        else
        {
            List<IPersistable> objects = new ArrayList<IPersistable>();
            objects.add(updatedObject);

            updatedObjects.put(objectClass, objects);
        }
    }

    public void registerAsDeleted(IPersistable deletedObject)
    {
        if (deletedObject == null)
        {
            throw new RuntimeException("Deleted object can not be null;");
        }

        Class objectClass = deletedObject.getClass();
        if (deletedObjects.containsKey(objectClass))
        {
            deletedObjects.get(objectClass).add(deletedObject);
        }
        else
        {
            List<IPersistable> objects = new ArrayList<IPersistable>();
            objects.add(deletedObject);

            deletedObjects.put(objectClass, objects);
        }
    }

    @Override
    public void registerUnusedBaseEntity(IBaseEntity unusedBaseEntity)
    {
        if (unusedBaseEntity == null)
        {
            throw new RuntimeException("Unused instance of BaseEntity can not be null;");
        }

        unusedBaseEntities.add(unusedBaseEntity);
    }

    @Override
    public List<IPersistable> getInsertedObjects(Class objectClass) {
        return insertedObjects.get(objectClass);
    }

    @Override
    public List<IPersistable> getUpdatedObjects(Class objectClass) {
        return updatedObjects.get(objectClass);
    }

    @Override
    public List<IPersistable> getDeletedObjects(Class objectClass) {
        return deletedObjects.get(objectClass);
    }

    @Override
    public Set<IBaseEntity> getUnusedBaseEntities() {
        return unusedBaseEntities;
    }

}
