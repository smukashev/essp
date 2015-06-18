package kz.bsbnb.usci.brms.rulesvr.service.impl;

import kz.bsbnb.usci.brms.rulesvr.dao.IBatchDao;
import kz.bsbnb.usci.brms.rulesvr.model.IBatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import kz.bsbnb.usci.brms.rulesvr.model.impl.SimpleTrack;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import kz.bsbnb.usci.eav.util.Pair;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * @author abukabayev
 */
@Service
public class BatchService implements IBatchService
{
    @Autowired
    private IBatchDao batchDao;

    public BatchService() {
        super();
    }

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

    @Override
    public List<Pair> getBatchVersions(Long batchId) {
        List<IBatchVersion> list = batchDao.getBatchVersions(batchId);
        List<Pair> ret = new LinkedList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        for(IBatchVersion batchVersion: list) {
            Pair p = new Pair(batchVersion.getId(), sdf.format(batchVersion.getOpenDate()));
            ret.add(p);
        }

        return ret;
    }
}
