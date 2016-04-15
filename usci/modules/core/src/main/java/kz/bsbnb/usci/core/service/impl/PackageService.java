package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IPackageService;
import kz.bsbnb.usci.eav.rule.IPackageDao;
import kz.bsbnb.usci.eav.rule.RulePackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackageService implements IPackageService {
    @Autowired
    private IPackageDao packageDao;

    public PackageService() {
        super();
    }

    @Override
    public long save(RulePackage batch) {
        return packageDao.save(batch);
    }

    @Override
    public RulePackage load(long id) {
        RulePackage batch = packageDao.loadBatch(id);
        return batch;
    }

    @Override
    public List<RulePackage> getAllPackages() {
        return packageDao.getAllPackages();
    }
}
