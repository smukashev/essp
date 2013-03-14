package kz.bsbnb.usci.eav_persistance.repository.impl;

import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_persistance.repository.IBatchRepository;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IBatchDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Caches crud operations with Batch objects. Uses permanent cache.
 *
 * @author a.motov
 */
@Component
@Scope(value = "singleton")
public class BatchRepository implements IBatchRepository
{
    @Autowired
    private IBatchDao postgreSQLBatchDaoImpl;

    private HashMap<Long, Batch> cache = new HashMap<Long, Batch>();

    /**
     * Retrieves Batch from Dao.
     *
     * @param batchId - id of Batch in Storage
     * @return
     */
    @Override
    public Batch getBatch(long batchId)
    {
        if (cache.containsKey(batchId))
            return cache.get(batchId);
        else
        {
            Batch batch = postgreSQLBatchDaoImpl.load(batchId);
            cache.put(batchId, batch);

            return batch;
        }
    }

    /**
     * Persists Batch using Dao. Always cache write through.
     *
     * @param batch - id of Batch in Storage
     * @return
     */
    @Override
    public Batch addBatch(Batch batch)
    {
        long batchId = batch.getId();

        if (batch.getId() < 1)
            batchId = postgreSQLBatchDaoImpl.save(batch);
        //else TODO: Add batch update here

        return getBatch(batchId);
    }
}
