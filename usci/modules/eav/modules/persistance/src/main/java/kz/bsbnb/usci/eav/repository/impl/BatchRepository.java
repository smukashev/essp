package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class BatchRepository implements IBatchRepository {
    @Autowired
    private IBatchDao batchDao;

    @Override
    public Batch getBatch(long batchId) {
        return batchDao.load(batchId);
    }

    @Override
    public Batch addBatch(Batch batch) {
        if (batch.getId() < 1)
            batchDao.save(batch);

        return batch;
    }
}
