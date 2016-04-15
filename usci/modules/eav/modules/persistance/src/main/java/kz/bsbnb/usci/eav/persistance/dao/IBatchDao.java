package kz.bsbnb.usci.eav.persistance.dao;


import kz.bsbnb.usci.eav.model.Batch;

import java.util.Date;
import java.util.List;

public interface IBatchDao {

    Batch load(long id);

    long save(Batch batch);

    List<Batch> getPendingBatchList();

    List<Batch> getBatchListToSign(long creditorId);

    List<Batch> getAll(Date repDate);

    void incrementActualCount(long batchId, long count);

    void clearActualCount(long batchId);
}
