package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.EntityStatuses;

public interface IBatchService {

    long save(Batch batch);

    Batch load(long batchId);

    long uploadBatch(Batch batch);

    void addBatchStatus(long batchId, long statusId, String description);

    void addBatchStatus(long batchId, BatchStatuses batchStatus, String description);

    void addEntityStatus(long batchId, long entityId, long statusId, Long index, String description);

    void addEntityStatus(long batchId, long entityId, EntityStatuses entityStatus, Long index, String description);

    void endBatch(long batchId);

}
