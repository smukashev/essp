package kz.bsbnb.usci.brms.rulesvr.dao;

import kz.bsbnb.usci.brms.rulemodel.model.impl.Batch;
import kz.bsbnb.usci.brms.rulemodel.model.impl.BatchVersion;

import java.util.List;
import java.util.Date;

/**
 * @author abukabayev
 */
public interface IBatchVersionDao extends IDao {
    public long saveBatchVersion(Batch batch);
    public long saveBatchVersion(Batch batch,Date date);
    public BatchVersion getBatchVersion(Batch batch);
    public BatchVersion getBatchVersion(Batch batch,Date date);
    public List<BatchVersion> getBatchVersions(Batch batch);
    public void copyRule(Long ruleId,Batch batch,Date versionDate);
    public BatchVersion getBatchVersion(String name, Date repdate);
    public long insertBatchVersion(long packageId, Date date);
}
