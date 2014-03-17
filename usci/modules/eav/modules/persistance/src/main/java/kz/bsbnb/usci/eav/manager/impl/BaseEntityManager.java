package kz.bsbnb.usci.eav.manager.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
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
        CLASS_PRIORITY.add(BaseValue.class);
    }

    private Map<Class, List<IPersistable>> insertedObjects = new HashMap<Class, List<IPersistable>>();
    private Map<Class, List<IPersistable>> updatedObjects = new HashMap<Class, List<IPersistable>>();
    private Map<Class, List<IPersistable>> deletedObjects = new HashMap<Class, List<IPersistable>>();

    public void registerAsInserted(IPersistable persistableObject)
    {
        if (persistableObject == null)
        {
            throw new RuntimeException("Inserted object can not be null;");
        }

        Class objectClass = persistableObject.getClass();
        if (insertedObjects.containsKey(objectClass))
        {
            insertedObjects.get(objectClass).add(persistableObject);
        }
        else
        {
            List<IPersistable> objects =
                    new ArrayList<IPersistable>();
            objects.add(persistableObject);

            insertedObjects.put(objectClass, objects);
        }
    }

    @Override
    public List<IPersistable> getInsertedObjects(Class objectClass) {
        return insertedObjects.get(objectClass);
    }

    /*public void save()
    {
        for(Class objectClass : objectClasses)
        {
            if (insertedObjects.containsKey(objectClass))
            {
                Set<Object> objects = insertedObjects.get(objectClass);
                // TODO: save inserted objects
                if (objectClass == BaseEntity.class)
                {

                }
                else if ()
            }

            if (updatedObjects.containsKey(objectClass))
            {
                Set<Object> objects = updatedObjects.get(objectClass);
                // TODO: save updated objects
            }

            if (deletedObjects.containsKey(objectClass))
            {
                Set<Object> objects = deletedObjects.get(objectClass);
                // TODO: save deleted objects
            }
        }
    }*/

}
