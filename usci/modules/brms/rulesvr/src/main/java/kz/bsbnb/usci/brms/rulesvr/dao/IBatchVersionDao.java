package kz.bsbnb.usci.brms.rulesvr.dao;

import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
import kz.bsbnb.usci.brms.rulemodel.model.impl.PackageVersion;

import java.util.List;
import java.util.Date;

/**
 * @author abukabayev
 */
public interface IBatchVersionDao extends IDao {
    long saveBatchVersion(RulePackage batch);
    long saveBatchVersion(RulePackage batch, Date date);
    PackageVersion getBatchVersion(RulePackage batch);
    PackageVersion getBatchVersion(RulePackage batch, Date date);
    List<PackageVersion> getBatchVersions(RulePackage batch);
    void copyRule(Long ruleId, RulePackage batch, Date versionDate);
    PackageVersion getBatchVersion(String name, Date repdate);
    long insertBatchVersion(long packageId, Date date);
}
