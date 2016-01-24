package kz.bsbnb.usci.brms.rulemodel.service;


import kz.bsbnb.usci.brms.rulemodel.model.impl.Batch;
import kz.bsbnb.usci.brms.rulemodel.model.impl.BatchVersion;

import java.util.List;
import java.util.Date;

/**
 * @author abukabayev
 */
public interface IBatchVersionService {
    public BatchVersion load(Batch batch,Date date);
    public long save(Batch batch);
    public long save(Batch batch,Date date);
    public List<BatchVersion> getBatchVersions(Batch batch);
    public void copyRule(Long ruleId,Batch batch,Date versionDate);
    public BatchVersion getBatchVersion(String batchName, Date date);
}
