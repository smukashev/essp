package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.Batch;

public interface IBatchService {
    long save(Batch batch);
    Batch load(long batchId);
}
