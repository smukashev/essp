package kz.bsbnb.usci.brms.rulesvr.dao;

import kz.bsbnb.usci.brms.rulemodel.model.IBatchVersion;
import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;

import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */
public interface IBatchDao extends IDao {
    RulePackage loadBatch(long id);
    long save(RulePackage batch);
    List<RulePackage> getAllPackages();
    long getBatchVersionId(long batchId, Date repDate);
    List<IBatchVersion> getBatchVersions(long batchId);
}
