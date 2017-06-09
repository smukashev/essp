package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;
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

    List<EntityStatus> getEntityStatusList(long batchId, int firstIndex, int count);

    List<BatchStatus> getBatchStatusList(long batchId);


    List<BatchStatus> getBatchStatuses(List<Long> batchIds);

    List<Batch> getPendingBatchList();

    List<Batch> getBatchListToSign(long creditorId);

    void signBatch(long batchId, String sign, String signInfo, Date signTime);

    List<Batch> getAll(Date repDate);

    List<Batch> getAll(Date repDate, List<Creditor> creditorsList);

    List<Batch> getAll(Date repDate, List<Creditor> creditorsList, int firstIndex, int count);

    boolean incrementActualCounts(Map<Long, Long> batchesToUpdate);

    boolean clearActualCount(long batchId);

    List<Batch> getMaintenanceBatches(Date reportDate);

    void approveMaintenance(List<Long> approvedBatchIds);

    void declineMaintenance(List<Long> declinedBatchIds);

    int getBatchCount(List<Creditor> creditors, Date reportDate);

    int getEntityStatusCount(long batchId);

    int getErrorEntityStatusCount(Batch batch);

    String getSignatureInfo(long batchId);
}
