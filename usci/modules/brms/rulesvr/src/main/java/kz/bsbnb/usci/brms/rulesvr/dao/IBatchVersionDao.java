package kz.bsbnb.usci.brms.rulesvr.dao;

import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
import kz.bsbnb.usci.brms.rulemodel.model.impl.BatchVersion;

import java.util.List;
import java.util.Date;

/**
 * @author abukabayev
 */
public interface IBatchVersionDao extends IDao {
    long saveBatchVersion(RulePackage batch);
    long saveBatchVersion(RulePackage batch, Date date);
    BatchVersion getBatchVersion(RulePackage batch);
    BatchVersion getBatchVersion(RulePackage batch, Date date);
    List<BatchVersion> getBatchVersions(RulePackage batch);
    void copyRule(Long ruleId, RulePackage batch, Date versionDate);
    BatchVersion getBatchVersion(String name, Date repdate);
    long insertBatchVersion(long packageId, Date date);
}
