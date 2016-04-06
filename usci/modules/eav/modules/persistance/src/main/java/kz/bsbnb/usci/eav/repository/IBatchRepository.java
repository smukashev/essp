package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.Batch;

public interface IBatchRepository {
    Batch getBatch(long batchId);

    Batch addBatch(Batch batch);
}
