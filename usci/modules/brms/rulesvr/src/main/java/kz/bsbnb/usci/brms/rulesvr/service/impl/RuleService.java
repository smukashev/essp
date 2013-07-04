package kz.bsbnb.usci.brms.rulesvr.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import kz.bsbnb.usci.brms.rulesvr.dao.IRuleDao;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;

import java.util.List;

/**
 * @author abukabayev
 */

@Service
public class RuleService implements IRuleService {

    @Autowired
    private IRuleDao ruleDao;

    @Override
    public long save(Rule rule, BatchVersion batchVersion) {

        return ruleDao.save(rule,batchVersion);
    }

    @Override
    public List<Rule> load(BatchVersion batchVersion) {
        return ruleDao.load(batchVersion);
    }

    @Override
    public void update(Rule rule) {
        ruleDao.update(rule);
    }

    @Override
    public List<Rule> getAllRules() {
        return ruleDao.getAllRules();
    }
}
