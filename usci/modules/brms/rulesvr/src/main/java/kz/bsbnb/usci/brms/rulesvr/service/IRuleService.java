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
     * Retrieves rules(<i>id</i>, <i>title <b>as name</b></i>) given <b>packageId</b> and <b>date</b><br/>
     * Automatically defines its date (i.e the latest version of batch among all dates <= <b>repDate</b>)
     *
     * @param packageId
     * @param repDate
     * @return
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

    public long saveRule(String title, long batchVersionId);

}
