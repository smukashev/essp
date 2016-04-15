package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.eav.rule.impl.RulesSingleton;
import kz.bsbnb.usci.core.service.IRuleService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.rule.*;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author abukabayev
 */

@Service
public class RuleServiceImpl implements IRuleService {
    @Autowired
    private IRuleDao ruleDao;

    @Autowired
    private RulesSingleton rulesSingleton;

    @Override
    public long save(Rule rule, PackageVersion packageVersion) {
        return ruleDao.save(rule, packageVersion);
    }

    @Override
    public List<Rule> load(PackageVersion packageVersion) {
        return ruleDao.load(packageVersion);
    }

    @Override
    public void update(Rule rule) {
        ruleDao.update(rule);
    }

    @Override
    public RulePackage getPackage(String name) {
        return ruleDao.getPackage(name);
    }

    @Override
    public List<Rule> getAllRules() {
        return ruleDao.getAllRules();
    }

    @Override
    public Map getRuleTitles(Long packageId, Date repDate) {
        Map m = new HashMap();
        m.put("data",ruleDao.getRuleTitles(packageId,repDate));
        return m;
    }

    @Override
    public Map getRuleTitles(Long packageId, Date reportDate, String searchText) {
        if(searchText == null || searchText.length() < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E270));

        Map m = new HashMap();
        m.put("data",ruleDao.getRuleTitles(packageId, reportDate, searchText));
        return m;
    }

    @Override
    public Rule getRule(Rule rule) {
        return ruleDao.getRule(rule);
    }

    @Override
    public boolean deleteRule(long ruleId, RulePackage rulePackage) {
        return ruleDao.deleteRule(ruleId, rulePackage);
    }

    @Override
    public long saveEmptyRule(String title, long batchVersionId) {
        long ruleId = ruleDao.createEmptyRule(title);
        ruleDao.save(ruleId, batchVersionId);
        return ruleId;
    }

    @Override
    public void updateBody(Rule rule) {
        ruleDao.updateBody(rule);
    }

    @Override
    public void copyExistingRule(long ruleId, long batchVersionId) {
        ruleDao.copyExistingRule(ruleId, batchVersionId);
    }

    @Override
    public long createCopy(long ruleId, String title, long batchVersionId) {
        long newRuleId = ruleDao.createCopy(ruleId, title);
        ruleDao.copyExistingRule(newRuleId, batchVersionId);
        return newRuleId;
    }

    @Override
    public long createNewRuleInPackage(Rule rule, PackageVersion packageVersion) {
        long ruleId = ruleDao.createRule(rule, packageVersion);
        rule.setId(ruleId);
        //ruleDao.save(ruleId, packageVersion.getId());
        ruleDao.saveInPackage(rule, packageVersion);
        return ruleId;
    }

    @Override
    public void renameRule(long ruleId, String title) {
        ruleDao.renameRule(ruleId,title);
    }

    @Override
    public long insertBatchVersion(long packageId, Date date) {
        //return batchVersionDao.insertBatchVersion(packageId, date);
        return -1L;
    }

    @Override
    public void reloadCache() {
        rulesSingleton.reloadCache();
    }

    @Override
    public List<String> runRules(BaseEntity entity, String pkgName, Date repDate) {
        rulesSingleton.runRules(entity,pkgName,repDate);
        List<String> list = new ArrayList<>();
        for(String s : entity.getValidationErrors())
            list.add(s);
        return list;
    }

    @Override
    public String getRuleErrors(String rule) {
        return rulesSingleton.getRuleErrors(rule);
    }

    @Override
    public String getPackageErrorsOnRuleUpdate(Rule rule, PackageVersion packageVersion) {
        return rulesSingleton.getPackageErrorsOnRuleUpdate(rule, packageVersion);
    }

    @Override
    public String getPackageErrorsOnRuleActivate(String ruleBody, Long ruleId, String pkgName, Date repDate, boolean ruleEdited) {
        //return rulesSingleton.getPackageErrorsOnRuleUpdate(ruleBody, ruleId);
        return null;
    }

    @Override
    public String getPackageErrorsOnRuleDisable(Long ruleId, String pkgName, Date repDate) {
        //return rulesSingleton.getPackageErrorsOnRuleUpdate("", ruleId);
        return null;
    }

    @Override
    public boolean activateRule(String ruleBody, Long ruleId) {
        //return ruleDao.activateRule(ruleBody, ruleId);
        return true;
    }

    @Override
    public boolean activateRule(Long ruleId) {
        //return ruleDao.activateRule(ruleId);
        return true;
    }

    @Override
    public boolean disableRule(Long ruleId) {
        //return ruleDao.disableRule(ruleId);
        return true;
    }

    @Override
    public void clearAllRules() {
        ruleDao.clearAllRules();
    }

    @Override
    public List<Pair> getPackageVersions(RulePackage rulePackage) {
        List<PackageVersion> versions = ruleDao.getPackageVersions(rulePackage);

        List<Pair> ret = new LinkedList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        long id = 1L;
        for(PackageVersion packageVersion: versions) {
            Pair p = new Pair(id++, sdf.format(packageVersion.getReportDate()));
            ret.add(p);
        }

        return ret;
    }

    @Override
    public String getPackageErrorsOnRuleInsert(PackageVersion packageVersion, String title, String ruleBody) {
        return rulesSingleton.getPackageErrorsOnRuleInsert(packageVersion, title, ruleBody);
    }

    @Override
    public boolean insertHistory(Rule rule) {
        Rule ruleInDb = ruleDao.getRule(rule);

        if(ruleInDb.getOpenDate().compareTo(rule.getOpenDate()) >=0 )
            throw new RuntimeException("Дата должна быть позднее");

        ruleDao.insertHistory(ruleInDb, ruleInDb.getOpenDate());
        ruleDao.update(rule);

        return true;
    }

    @Override
    public List<Rule> getRuleHistory(long ruleId) {
        return ruleDao.getRuleHistory(ruleId);
    }

    @Override
    public String getPackageErrorsOnRuleDelete(Rule rule) {
        return rulesSingleton.getPackageErrorsOnRuleDelete(rule);
    }
}
