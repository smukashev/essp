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

    List<Batch> getBatchListToSign(long userId);

    void signBatch(long batchId, String sign);

    List<Batch> getAll(Date repDate);

    Map<String, String> getEntityStatusParams(long entityStatusId);

    void addEntityStatusParams(long entityStatusId, Map<String, String> params);

}
