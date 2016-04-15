package kz.bsbnb.usci.core.service;


import kz.bsbnb.usci.eav.rule.RulePackage;

import java.util.List;

public interface IPackageService {
    long save(RulePackage batch);

    RulePackage load(long id);

    List<RulePackage> getAllPackages();
}
