package kz.bsbnb.usci.brms.rulesvr.service.impl;

import kz.bsbnb.usci.brms.rulemodel.model.impl.Batch;
import kz.bsbnb.usci.brms.rulemodel.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulemodel.service.IBatchVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import kz.bsbnb.usci.brms.rulesvr.dao.IBatchVersionDao;

import java.util.List;
import java.util.Date;

/**
 * @author abukabayev
 */

@Service
public class BatchVersionService implements IBatchVersionService {

    @Autowired
    private IBatchVersionDao batchVersionDao;

    @Override
    public BatchVersion load(Batch batch,Date date) {
        return batchVersionDao.getBatchVersion(batch,date);
    }

    @Override
    public long save(Batch batch) {
        return batchVersionDao.saveBatchVersion(batch);
    }

    @Override
    public long save(Batch batch, Date date) {
        return batchVersionDao.saveBatchVersion(batch,date);
    }

    @Override
    public List<BatchVersion> getBatchVersions(Batch batch) {
        return batchVersionDao.getBatchVersions(batch);
    }

    @Override
    public void copyRule(Long ruleId, Batch batch, Date versionDate) {
        batchVersionDao.copyRule(ruleId,batch,versionDate);
    }

    @Override
    public BatchVersion getBatchVersion(String batchName, Date date) {
        return batchVersionDao.getBatchVersion(batchName,date);
    }
}
