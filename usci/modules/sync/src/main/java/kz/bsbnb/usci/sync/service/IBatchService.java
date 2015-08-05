package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.EntityStatuses;

import java.util.List;

/**
 * @author k.tulbassiyev
 */
public interface IBatchService {

    long save(Batch batch);

    Batch getBatch(long batchId);

    long uploadBatch(Batch batch);

    void addBatchStatus(long batchId, long statusId);

    void addBatchStatus(long batchId, long statusId, String description);

    void addBatchStatus(long batchId, BatchStatuses batchStatus);

    void addBatchStatus(long batchId, BatchStatuses batchStatus, String description);

    void addBatchStatus(BatchStatus batchStatus);

    void addEntityStatus(long batchId, long entityId, long statusId, Long index);

    void addEntityStatus(long batchId, long entityId, long statusId, Long index, String description);

    void addEntityStatus(long batchId, long entityId, EntityStatuses entityStatus, Long index);

    void addEntityStatus(long batchId, long entityId, EntityStatuses entityStatus, Long index, String description);

    void addEntityStatus(EntityStatus entityStatus);

    void endBatch(long batchId);

    List<EntityStatus> getEntityStatusList(long batchId);

}
