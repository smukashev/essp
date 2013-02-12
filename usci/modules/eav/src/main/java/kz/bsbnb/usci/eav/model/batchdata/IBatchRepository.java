package kz.bsbnb.usci.eav.model.batchdata;

import kz.bsbnb.usci.eav.model.Batch;

/**
 * @author a.motov
 */
public interface IBatchRepository {

    public Batch getBatch(long batchId);

    public Batch addBatch(Batch batch);

}
