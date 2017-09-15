package kz.bsbnb.usci.eav.manager;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.List;
import java.util.Map;

public interface IBaseEntityManager {
    void addOptimizerEntity(IBaseEntity entity);

    Map<Long, IBaseEntity> getOptimizerEntities();

    List<String> getHistory();

    void addHistory(String age);

    void registerAsInserted(IPersistable persistableObject);

    void registerAsUpdated(IPersistable persistableObject);

    void registerAsDeleted(IPersistable persistableObject);

    void registerProcessedBaseEntity(IBaseEntity processedBaseEntity);

    List<IPersistable> getInsertedObjects(Class objectClass);

    List<IPersistable> getUpdatedObjects(Class objectClass);

    List<IPersistable> getDeletedObjects(Class objectClass);

    IBaseEntity getProcessed(IBaseEntity baseEntity);

    void registerCreditorId(Long creditorId);

    Long getCreditorId();

    void setDeleteLogger(IEAVLoggerDao deleteLogger);
}
