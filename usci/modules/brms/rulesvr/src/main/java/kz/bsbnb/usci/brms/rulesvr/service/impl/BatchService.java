package kz.bsbnb.usci.brms.rulesvr.service.impl;

import kz.bsbnb.usci.brms.rulesvr.dao.IBatchDao;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author abukabayev
 */
@Service
public class BatchService implements IBatchService
{

    @Autowired
    private IBatchDao batchDao;

    @Override
    public long save(Batch batch) {
        return batchDao.save(batch);
    }

    @Override
    public Batch load(long id) {
//        System.out.println(id);
        Batch batch = batchDao.loadBatch(id);
//        System.out.println(batch.getName()+" "+batch.getRepoDate());
        return batch;
    }

    @Override
    public List<Batch> getAllBatches() {
        return batchDao.getAllBatches();
    }
}
