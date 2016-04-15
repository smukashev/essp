package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IPackageService;
import kz.bsbnb.usci.eav.rule.IPackageDao;
import kz.bsbnb.usci.eav.rule.RulePackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author abukabayev
 */
@Service
public class PackageService implements IPackageService {
    @Autowired
    private IPackageDao batchDao;

    public PackageService() {
        super();
    }

    @Override
    public long save(RulePackage batch) {
        return batchDao.save(batch);
    }

    @Override
    public RulePackage load(long id) {
        RulePackage batch = batchDao.loadBatch(id);
        return batch;
    }

    @Override
    public List<RulePackage> getAllPackages() {
        return batchDao.getAllPackages();
    }
}
