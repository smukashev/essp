package kz.bsbnb.usci.brms.rulesvr.service;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;

import java.util.List;

/**
 * @author abukabayev
 */
public interface IRuleService {
    public long save(Rule rule,BatchVersion batchVersion);
    public List<Rule> load(BatchVersion batchVersion);
    public void update(Rule rule);
    public List<Rule> getAllRules();
}
