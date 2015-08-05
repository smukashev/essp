package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.EntityStatuses;

import java.util.List;

public interface IBatchService {

    long save(Batch batch);

    Batch getBatch(long batchId);

    long uploadBatch(Batch batch);

    void addBatchStatus(long batchId, long statusId, String description);

    void addBatchStatus(long batchId, BatchStatuses batchStatus, String description);

    void addBatchStatus(BatchStatus batchStatus);

    void addEntityStatus(long batchId, long entityId, long statusId, Long index, String description);

    void addEntityStatus(long batchId, long entityId, EntityStatuses entityStatus, Long index, String description);

    void endBatch(long batchId);

    void addEntityStatus(EntityStatus entityStatus);

    List<EntityStatus> getEntityStatusList(long batchId);

}
