package kz.bsbnb.usci.eav.persistance.dao;


import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.Batch;

import java.util.Date;
import java.util.List;

public interface IBatchDao {

    Batch load(long id);

    long save(Batch batch);

    List<Batch> getPendingBatchList();

    List<Batch> getBatchListToSign(long creditorId);

    List<Batch> getAll(Date repDate);

    List<Batch> getAll(Date repDate, List<Creditor> creditorsList);

    List<Batch> getAll(Date repDate, List<Creditor> creditorsList, int firstIndex, int count);

    void incrementActualCount(long batchId, long count);

    void clearActualCount(long batchId);

    List<Batch> getMaintenanceBatches(Date reportDate);

    void approveMaintenance(List<Long> approvedBatchIds);

    void declineMaintenance(List<Long> declinedBatchIds);

    int getBatchCount(List<Creditor> creditors, Date reportDate);

}
