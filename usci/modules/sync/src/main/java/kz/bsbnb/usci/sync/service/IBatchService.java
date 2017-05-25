package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EntityStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author k.tulbassiyev
 */
public interface IBatchService {

    long save(Batch batch);

    Batch getBatch(long batchId);

    long uploadBatch(Batch batch);

    Long addBatchStatus(BatchStatus batchStatus);

    Long addEntityStatus(EntityStatus entityStatus);

    void endBatch(long batchId);

    List<EntityStatus> getEntityStatusList(long batchId);

    List<BatchStatus> getBatchStatusList(long batchId);

    List<Batch> getPendingBatchList();

    List<Batch> getBatchListToSign(long userId);

    void signBatch(long batchId, String sign, String signInfo, Date signTime);

    List<Batch> getAll(Date repDate);

    boolean incrementActualCounts(Map<Long, Long> batchesToUpdate);

    boolean clearActualCount(long batchId);
}
