package kz.bsbnb.usci.brms.rulemodel.service;

import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.List;

/**
 * @author abukabayev
 */
public interface IPackageService {
    public long save(RulePackage batch);
    public RulePackage load(long id);
    public List<RulePackage> getAllPackages();
    public List<Pair> getBatchVersions(Long batchId);
}
