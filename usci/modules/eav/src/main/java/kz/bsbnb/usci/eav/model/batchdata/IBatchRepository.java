package kz.bsbnb.usci.eav.model.batchdata;

import kz.bsbnb.usci.eav.model.Batch;

/**
 * Caches crud operations with Batch objects.
 *
 * @author a.motov
 */
public interface IBatchRepository
{
    /**
     * Retrieves Batch from Dao.
     *
     * @param batchId - id of Batch in Storage
     * @return
     */
    public Batch getBatch(long batchId);

    /**
     * Persists Batch using Dao.
     *
     * @param batch - id of Batch in Storage
     * @return
     */
    public Batch addBatch(Batch batch);

}
