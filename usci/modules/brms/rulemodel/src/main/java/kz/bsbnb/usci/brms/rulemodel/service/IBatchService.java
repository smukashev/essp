package kz.bsbnb.usci.brms.rulemodel.service;

import kz.bsbnb.usci.brms.rulemodel.model.impl.Batch;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.List;

/**
 * @author abukabayev
 */
public interface IBatchService {
    public long save(Batch batch);
    public Batch load(long id);
    public List<Batch> getAllBatches();
    public List<Pair> getBatchVersions(Long batchId);
}