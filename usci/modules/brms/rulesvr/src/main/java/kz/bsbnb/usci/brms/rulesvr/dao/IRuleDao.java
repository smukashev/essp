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
    public void save(long ruleId, long BatchVersionId);
    public long createEmptyRule(String title);
    public void updateBody(Long ruleId, String body);
    public void copyExistingRule(long ruleId, long batchVersionId);
    public long createCopy(long ruleId, String title);
    public long createRule(Rule rule);
    public void renameRule(long ruleId, String title);
    public boolean activateRule(String ruleBody, long ruleId);
    public boolean activateRule(long ruleId);
    public boolean disableRule(long ruleId);
    public void clearAllRules();
}
