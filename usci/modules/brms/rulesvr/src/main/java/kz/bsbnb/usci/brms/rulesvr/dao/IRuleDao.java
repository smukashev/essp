package kz.bsbnb.usci.brms.rulesvr.dao;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.model.impl.SimpleTrack;

import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */
public interface IRuleDao extends IDao{
    public long save(Rule rule,BatchVersion batchVersion);
    public List<Rule> load(BatchVersion batchVersion);
    public long update(Rule rule);
    public List<Rule> getAllRules();
    public List<SimpleTrack> getRuleTitles(Long packageId, Date repDate);
    public Rule getRule(Long ruleId);
    public boolean deleteRule(long ruleId, long batchVersionId);
    public long save(long ruleId, long BatchVersionId);
    public long createRule(String title);
    public boolean updateBody(Long ruleId, String body);
}
