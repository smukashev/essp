package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.Batch;

/**
 * @author k.tulbassiyev
 */
public interface IBatchService {
    public long save(Batch batch);
    public Batch load(long batchId);
}
