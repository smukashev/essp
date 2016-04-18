package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.rule.PackageVersion;
import kz.bsbnb.usci.eav.rule.Rule;
import kz.bsbnb.usci.eav.rule.RulePackage;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IRuleService {
    long save(Rule rule, PackageVersion packageVersion);

    List<Rule> load(PackageVersion packageVersion);

    void update(Rule rule);

    RulePackage getPackage(String name);

    List<Pair> getPackageVersions(RulePackage rulePackage);

    /**
     * Select * from rules
     *
     * @return list of rules
     */
    List<Rule> getAllRules();

    /**
     * Retrieves rules(<i>id</i>, <i>title <b>as name</b></i>) and <b>batchVersionId</b> given <b>packageId</b> and <b>date</b><br/>
     * Automatically defines its date (i.e the latest version of batch among all dates <= <b>repDate</b>)
     *
     * @return map of : <br/> <b>data</b> - array of rules <br/><b>batchVersionId</b> - batchVersionId
     */
    Map getRuleTitles(Long packageId, Date repDate);

    Map getRuleTitles(Long packageId, Date repDate, String searchText);

    /**
     * Retrieves single rule by ruleId
     *
     * @param rule
     * @return
     */
    Rule getRule(Rule rule);


    boolean deleteRule(long ruleId, RulePackage rulePackage);

    /**
     * Creates new rule with given title and empty body <br/>
     *
     * @param title
     * @param batchVersionId
     * @return generated id of rule
     */
    long saveEmptyRule(String title, long batchVersionId);


    /**
     * Sets new body to rule with given <b>ruleId</b>
     *
     * @param rule - rule id <br/>
     */
    void updateBody(Rule rule);

    /**
     * Copy rule to batch version
     */
    void copyExistingRule(long ruleId, long batchVersionId);

    /**
     * Copy body of rule as a new rule into the batch
     *
     * @param ruleId         lookup id for rule body
     * @param batchVersionId what package to add new rule
     * @return id of created rule
     */
    long createCopy(long ruleId, String title, long batchVersionId);

    /**
     * Create new rule with given title and body into batchVersion
     *
     * @return id of created rule
     */
    long createNewRuleInPackage(Rule rule, PackageVersion packageVersion);

    void renameRule(long ruleId, String title);

    long insertBatchVersion(long packageId, Date date);

    /**
     * ============================
     * RuleSingleton interface
     * ============================
     */
    void reloadCache();

    List<String> runRules(BaseEntity entity, String pkgName, Date repDate);

    String getRuleErrors(String rule);

    String getPackageErrorsOnRuleUpdate(Rule rule, PackageVersion packageVersion);

    String getPackageErrorsOnRuleActivate(String ruleBody, Long ruleId, String pkgName, Date repDate, boolean ruleEdited);

    String getPackageErrorsOnRuleDisable(Long ruleId, String pkgName, Date repDate);

    boolean activateRule(String ruleBody, Long ruleId);

    boolean activateRule(Long ruleId);

    boolean disableRule(Long ruleId);

    String getPackageErrorsOnRuleInsert(PackageVersion packageVersion, String title, String ruleBody);

    boolean insertHistory(Rule rule);

    List<Rule> getRuleHistory(long ruleId);

    String getPackageErrorsOnRuleDelete(Rule rule);

    /**
     * =============================
     * Developer tools
     * =============================
     */
    void clearAllRules();
}
