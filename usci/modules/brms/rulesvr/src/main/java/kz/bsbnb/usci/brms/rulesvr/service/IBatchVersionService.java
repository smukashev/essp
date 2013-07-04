package kz.bsbnb.usci.brms.rulesvr.service;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;

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
}
