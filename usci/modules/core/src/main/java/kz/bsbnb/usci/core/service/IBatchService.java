package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EntityStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IBatchService {

    long save(Batch batch);

    Batch getBatch(long batchId);

    long uploadBatch(Batch batch);

    Long addBatchStatus(BatchStatus batchStatus);

    void endBatch(long batchId);

    Long addEntityStatus(EntityStatus entityStatus);

    List<EntityStatus> getEntityStatusList(long batchId);

    List<BatchStatus> getBatchStatusList(long batchId);

    List<Batch> getPendingBatchList();

    List<Batch> getBatchListToSign(long creditorId);

    void signBatch(long batchId, String sign);

    List<Batch> getAll(Date repDate);

    boolean incrementActualCounts(Map<Long, Long> batchesToUpdate);

    boolean clearActualCount(long batchId);

    List<Batch> getMaintenanceBatches(Date reportDate);
}
