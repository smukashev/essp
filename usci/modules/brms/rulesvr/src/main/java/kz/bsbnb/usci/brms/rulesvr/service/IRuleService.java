package kz.bsbnb.usci.brms.rulesvr.service;

import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.model.impl.SimpleTrack;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author abukabayev
 */
public interface IRuleService {
    public long save(Rule rule,BatchVersion batchVersion);
    public List<Rule> load(BatchVersion batchVersion);
    public void update(Rule rule);

    /**
     * Select * from rules
     * @return  list of rules
     */
    public List<Rule> getAllRules();

    /**
     * Retrieves rules(<i>id</i>, <i>title <b>as name</b></i>) and <b>batchVersionId</b> given <b>packageId</b> and <b>date</b><br/>
     * Automatically defines its date (i.e the latest version of batch among all dates <= <b>repDate</b>)
     *
     * @return map of : <br/> <b>data</b> - array of rules <br/><b>batchVersionId</b> - batchVersionId
     */
    public Map getRuleTitles(Long packageId, Date repDate);

    /**
     * Retrieves single rule by ruleId
     *
     * @param ruleId
     * @return
     */
    public Rule getRule(Long ruleId);


    public boolean deleteRule(long ruleId, long batchVersionId );

    /**
     * Creates new rule with given title and empty body <br/>
     *
     * @param title
     * @param batchVersionId
     * @return generated id of rule
     */
    public long saveEmptyRule(String title, long batchVersionId);


    /**
     * Sets new body to rule with given <b>ruleId</b>
     *
     * @param ruleId - rule id <br/>
     * @param body - new body of rule <br/>
     */
    public void updateBody(Long ruleId, String body);

    /**
     * Copy rule to batch version
     */
    public void copyExistingRule(long ruleId, long batchVersionId);

    /**
     * Copy body of rule as a new rule into the batch
     *
     * @param ruleId lookup id for rule body
     * @param batchVersionId what package to add new rule
     * @return id of created rule
     */
    public long createCopy(long ruleId, String title,  long batchVersionId);

    /**
     * Create new rule with given title and body into batchVersion
     * @return id of created rule
     */
    public long createNewRuleInBatch(Rule rule, BatchVersion batchVersion);

}
