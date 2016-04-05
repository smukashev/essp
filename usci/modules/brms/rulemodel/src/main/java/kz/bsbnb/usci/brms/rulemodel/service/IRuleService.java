package kz.bsbnb.usci.brms.rulemodel.service;

import kz.bsbnb.usci.brms.rulemodel.model.impl.PackageVersion;
import kz.bsbnb.usci.brms.rulemodel.model.impl.Rule;
import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author abukabayev
 */
public interface IRuleService {
    long save(Rule rule,PackageVersion packageVersion);
    List<Rule> load(PackageVersion packageVersion);
    void update(Rule rule);
    RulePackage getPackage(String name);
    List<Pair> getPackageVersions(RulePackage rulePackage);

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

    Map getRuleTitles(Long packageId, Date repDate, String searchText);

    /**
     * Retrieves single rule by ruleId
     *
     * @param ruleId
     * @return
     */
    public Rule getRule(Long ruleId);


    public boolean deleteRule(long ruleId, RulePackage rulePackage);

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
    public long createNewRuleInPackage(Rule rule, PackageVersion packageVersion);

    public void renameRule(long ruleId, String title);

    public long insertBatchVersion(long packageId, Date date);
    /**
     * ============================
     * RuleSingleton interface
     * ============================
     */
    public void reloadCache();

    public List<String> runRules(BaseEntity entity, String pkgName, Date repDate);

    //public List<String> runRules(BaseEntity entity, String pkgName, Date repDate);

    //public String getRulePackageName(String pkgName, Date repDate);

    public String getRuleErrors(String rule);

    public String getPackageErrorsOnRuleUpdate(String ruleBody, Long ruleId, String pkgName, Date repDate);

    public String getPackageErrorsOnRuleActivate(String ruleBody, Long ruleId, String pkgName, Date repDate, boolean ruleEdited);

    public String getPackageErrorsOnRuleDisable(Long ruleId, String pkgName, Date repDate);

    public boolean activateRule(String ruleBody, Long ruleId);

    public boolean activateRule(Long ruleId);

    public boolean disableRule(Long ruleId);

    String getPackageErrorsOnRuleInsert(PackageVersion packageVersion, String title, String ruleBody);

    /**
     * =============================
     *   Developer tools
     * =============================
     */
    public void clearAllRules();

}
