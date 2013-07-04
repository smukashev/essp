package kz.bsbnb.usci.brms.rulesvr.dao;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;

import java.util.List;

/**
 * @author abukabayev
 */
public interface IRuleDao extends IDao{
    public long save(Rule rule,BatchVersion batchVersion);
    public List<Rule> load(BatchVersion batchVersion);
    public long update(Rule rule);
    public List<Rule> getAllRules();

}
