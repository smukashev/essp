package kz.bsbnb.usci.brms.rulesvr.dao;

import kz.bsbnb.usci.brms.rulesvr.model.IBatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;

import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */
public interface IBatchDao extends IDao{
    public Batch loadBatch(long id);
    public long save(Batch batch);
    public List<Batch> getAllBatches();
    public long getBatchVersionId(long batchId, Date repDate);
    public List<IBatchVersion> getBatchVersions(long batchId);
}
