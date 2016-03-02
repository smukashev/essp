package kz.bsbnb.usci.brms.rulesvr.dao;

import kz.bsbnb.usci.brms.rulemodel.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulemodel.model.impl.Rule;
import kz.bsbnb.usci.brms.rulemodel.model.impl.SimpleTrack;

import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */
public interface IRuleDao extends IDao{
    long save(Rule rule,BatchVersion batchVersion);
    //List<Rule> load(BatchVersion batchVersion);
    List<Rule> load(String packageName, Date reportDate);
    long update(Rule rule);
    List<Rule> getAllRules();
    List<SimpleTrack> getRuleTitles(Long packageId, Date repDate);
    Rule getRule(Long ruleId);
    boolean deleteRule(long ruleId, long batchVersionId);
    void save(long ruleId, long BatchVersionId);
    long createEmptyRule(String title);
    void updateBody(Long ruleId, String body);
    void copyExistingRule(long ruleId, long batchVersionId);
    long createCopy(long ruleId, String title);
    long createRule(Rule rule);
    void renameRule(long ruleId, String title);
    void clearAllRules();
    List<SimpleTrack> getRuleTitles(Long batchVersionId, String searchText);
}
