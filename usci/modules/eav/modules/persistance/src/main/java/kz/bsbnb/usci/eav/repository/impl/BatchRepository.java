package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Scope(value = "singleton")
public class BatchRepository implements IBatchRepository {
    @Autowired
    private IBatchDao batchDao;

    private HashMap<Long, Batch> cache = new HashMap<>();

    @Override
    public Batch getBatch(long batchId) {
        if (cache.containsKey(batchId))
            return cache.get(batchId);
        else {
            Batch batch = batchDao.load(batchId);
            cache.put(batchId, batch);

            return batch;
        }
    }

    @Override
    public Batch addBatch(Batch batch) {
        if (batch.getId() < 1) {
            Long batchId = batchDao.save(batch);
            batch.setId(batchId);
            cache.put(batchId, batch);
        }

        return batch;
    }

    @Override
    public synchronized void clearCache() {
        cache.clear();
    }
}
