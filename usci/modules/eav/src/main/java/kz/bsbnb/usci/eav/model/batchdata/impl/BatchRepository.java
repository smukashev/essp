package kz.bsbnb.usci.eav.model.batchdata.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBatchRepository;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

/**
 * @author a.motov
 */
@Component
@Scope(value = "singleton")
public class BatchRepository implements IBatchRepository
{
    @Autowired
    private IBatchDao postgreSQLBatchDaoImpl;

    private HashMap<Long, Batch> cache = new HashMap<Long, Batch>();

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

    @Override
    public Batch addBatch(Batch batch)
    {
        long batchId = batch.getId();

        if (batch.getId() < 1)
            batchId = postgreSQLBatchDaoImpl.save(batch);

        //TODO: Здесь можно сразу же обновить или создать новый метод только для обновления
        return getBatch(batchId);
    }
}
