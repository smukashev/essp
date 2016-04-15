package kz.bsbnb.usci.eav.rule;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */
public interface IRuleDao {
    long save(Rule rule, PackageVersion packageVersion);

    List<Rule> load(PackageVersion packageVersion);

    long update(Rule rule);

    List<Rule> getAllRules();

    List<SimpleTrack> getRuleTitles(Long packageId, Date repDate);

    Rule getRule(Rule rule);

    boolean deleteRule(long ruleId, RulePackage rulePackage);

    void save(long ruleId, long BatchVersionId);

    long createEmptyRule(String title);

    void updateBody(Rule rule);

    void copyExistingRule(long ruleId, long batchVersionId);

    long createCopy(long ruleId, String title);

    long createRule(Rule rule, PackageVersion packageVersion);

    void renameRule(long ruleId, String title);

    void clearAllRules();

    List<SimpleTrack> getRuleTitles(Long batchVersionId, Date reportDate, String searchText);

    List<PackageVersion> getPackageVersions(RulePackage rulePackage);

    void saveInPackage(Rule rule, PackageVersion packageVersion);

    RulePackage getPackage(String name);

    void insertHistory(Rule rule, Date closeDate);

    List<Rule> getRuleHistory(long ruleId);

    List<RulePackage> getRulePackages(Rule rule);

    void setDataSource(DataSource dataSource);
}
