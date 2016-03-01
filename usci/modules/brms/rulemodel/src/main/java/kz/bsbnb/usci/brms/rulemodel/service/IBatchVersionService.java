package kz.bsbnb.usci.brms.rulemodel.service;


import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
import kz.bsbnb.usci.brms.rulemodel.model.impl.BatchVersion;

import java.util.List;
import java.util.Date;

/**
 * @author abukabayev
 */
public interface IBatchVersionService {
    BatchVersion load(RulePackage batch, Date date);
    long save(RulePackage batch);
    long save(RulePackage batch, Date date);
    List<BatchVersion> getBatchVersions(RulePackage batch);
    void copyRule(Long ruleId, RulePackage batch, Date versionDate);
    BatchVersion getBatchVersion(String batchName, Date date);
}
