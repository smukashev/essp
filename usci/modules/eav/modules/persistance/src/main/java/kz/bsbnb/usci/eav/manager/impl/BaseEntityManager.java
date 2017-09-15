package kz.bsbnb.usci.eav.manager.impl;

import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.IEAVLoggerDao;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.output.BaseToShortTool;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BaseEntityManager implements IBaseEntityManager {

    public static final List<Class<? extends IPersistable>> CLASS_PRIORITY = new ArrayList<>();

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

    private IEAVLoggerDao deleteLogger;
    private List<String> history = new ArrayList<>();

    private void history(String operation, IPersistable persistable) {
        if (persistable == null) return;
        StringBuffer buffer = new StringBuffer(operation.toUpperCase() + ": ");
        if (false)
            BaseToShortTool.print(buffer, persistable);
        else
            buffer.append(persistable.toString());
        history.add(buffer.toString());
    }

    private void registerEntity(Map<Class, List<IPersistable>> objects, IPersistable persistable) {
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
            throw new RuntimeException(Errors.compose(Errors.E54));

        history("inserted", insertedObject);
        registerEntity(insertedObjects, insertedObject);

    }

    @Override
    public void registerAsUpdated(IPersistable updatedObject) {

        if (updatedObject == null)
            throw new RuntimeException(Errors.compose(Errors.E55));

        history("updated", updatedObject);
        registerEntity(updatedObjects, updatedObject);

    }

    @Override
    public void registerAsDeleted(IPersistable deletedObject) {

        if (deletedObject == null)
            throw new RuntimeException(Errors.compose(Errors.E53));

        history("deleted", deletedObject);

        List<IPersistable> objList = deletedObjects.get(deletedObject.getClass());

        if (objList != null && deletedObject.getId() > 0) {
            for (IPersistable iPersistable : objList) {
                if (iPersistable.getId() > 0 && iPersistable.getId() == deletedObject.getId())
                    return;
            }
        }

        try {
            if (StaticRouter.isDeleteLogEnabled()) {
                deleteLogger.log(deletedObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
    public List<String> getHistory() {
        return history;
    }

    @Override
    public void addHistory(String age) {
        history.add(age);
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
        if (!baseEntity.getMeta().isSearchable())
            return null;

        List<IBaseEntity> entityList = processedEntities.get(baseEntity.getMeta().getClassName());

        if (entityList == null)
            return null;

        for (IBaseEntity currentBaseEntity : entityList)
            if (baseEntity.equalsByKey(currentBaseEntity))
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

    @Override
    public void setDeleteLogger(IEAVLoggerDao deleteLogger) {
        this.deleteLogger = deleteLogger;
    }
}
