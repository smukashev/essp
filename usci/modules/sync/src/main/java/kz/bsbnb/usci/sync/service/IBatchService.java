package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.Batch;

/**
 * @author k.tulbassiyev
 */
public interface IBatchService {
    long save(Batch batch);

    Batch load(long batchId);
}
