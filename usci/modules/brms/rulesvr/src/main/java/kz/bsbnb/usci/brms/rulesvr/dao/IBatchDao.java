package kz.bsbnb.usci.brms.rulesvr.dao;

import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;

import java.util.List;

/**
 * @author abukabayev
 */
public interface IBatchDao extends IDao{
    public Batch loadBatch(long id);
    public long save(Batch batch);
    public List<Batch> getAllBatches();
}
