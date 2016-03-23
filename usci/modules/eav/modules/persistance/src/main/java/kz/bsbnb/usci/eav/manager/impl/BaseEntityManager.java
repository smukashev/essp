package kz.bsbnb.usci.eav.manager.impl;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.*;

public class BaseEntityManager implements IBaseEntityManager {

    public final static List<Class> CLASS_PRIORITY = new ArrayList<>();

    static {
        CLASS_PRIORITY.add(BaseEntity.class);
        CLASS_PRIORITY.add(BaseEntityReportDate.class);

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

    private Map<Class, List<IPersistable>> insertedObjects = new HashMap<>();
    private Map<Class, List<IPersistable>> updatedObjects = new HashMap<>();
    private Map<Class, List<IPersistable>> deletedObjects = new HashMap<>();

    private Map<Long, IBaseEntity> optimizerEntities = new HashMap<>();

    private HashMap<String, List<IBaseEntity>> processedEntities = new HashMap<>();

    private Long creditorId;

    private void registerEntity (Map<Class, List<IPersistable>> objects, IPersistable persistable) {
        Class objectClass = persistable.getClass();

        if (objects.containsKey(objectClass)) {
            objects.get(objectClass).add(persistable);
        } else {
            List<IPersistable> objList = new ArrayList<>();
            objList.add(persistable);

            objects.put(objectClass, objList);
        }
    }

    @Override
    public void registerAsInserted(IPersistable insertedObject) {
        if (insertedObject == null)
            throw new RuntimeException(Errors.getMessage(Errors.E54));

        registerEntity(insertedObjects, insertedObject);
    }

    @Override
    public void registerAsUpdated(IPersistable updatedObject) {
        if (updatedObject == null)
            throw new RuntimeException(Errors.getMessage(Errors.E55));

        registerEntity(updatedObjects, updatedObject);
    }

    @Override
    public void registerAsDeleted(IPersistable deletedObject) {
        if (deletedObject == null)
            throw new RuntimeException(Errors.getMessage(Errors.E53));

        registerEntity(deletedObjects, deletedObject);
    }

    @Override
    public void registerProcessedBaseEntity(IBaseEntity processedBaseEntity) {
        List<IBaseEntity> entityList = processedEntities.get(processedBaseEntity.getMeta().getClassName());

        if (entityList == null)
            entityList = new ArrayList<>();

        entityList.add(processedBaseEntity);
        processedEntities.put(processedBaseEntity.getMeta().getClassName(), entityList);
    }


    @Override
    public void addOptimizerEntity(IBaseEntity entity) {
        optimizerEntities.put(entity.getId(), entity);
    }

    @Override
    public Map<Long, IBaseEntity> getOptimizerEntities() {
        return optimizerEntities;
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
    public IBaseEntity getProcessed(IBaseEntity baseEntity) {
        List<IBaseEntity> entityList = processedEntities.get(baseEntity.getMeta().getClassName());

        if (entityList == null)
            return null;

        BasicBaseEntityComparator comparator = new BasicBaseEntityComparator();

        for (IBaseEntity currentBaseEntity : entityList)
            if (comparator.compare((BaseEntity) baseEntity, (BaseEntity) currentBaseEntity))
                return currentBaseEntity;

        return null;
    }

    @Override
    public void registerCreditorId(Long creditorId) {
        this.creditorId = creditorId;
    }

    @Override
    public Long getCreditorId() {
        return creditorId;
    }
}
