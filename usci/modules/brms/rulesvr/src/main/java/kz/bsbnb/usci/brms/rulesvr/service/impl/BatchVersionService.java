package kz.bsbnb.usci.brms.rulesvr.service.impl;

import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
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
    public BatchVersion load(RulePackage batch, Date date) {
        return batchVersionDao.getBatchVersion(batch,date);
    }

    @Override
    public long save(RulePackage batch) {
        return batchVersionDao.saveBatchVersion(batch);
    }

    @Override
    public long save(RulePackage batch, Date date) {
        return batchVersionDao.saveBatchVersion(batch,date);
    }

    @Override
    public List<BatchVersion> getBatchVersions(RulePackage batch) {
        return batchVersionDao.getBatchVersions(batch);
    }

    @Override
    public void copyRule(Long ruleId, RulePackage batch, Date versionDate) {
        batchVersionDao.copyRule(ruleId,batch,versionDate);
    }

    @Override
    public BatchVersion getBatchVersion(String batchName, Date date) {
        return batchVersionDao.getBatchVersion(batchName,date);
    }
}
