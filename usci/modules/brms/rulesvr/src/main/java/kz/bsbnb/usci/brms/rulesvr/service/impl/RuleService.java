package kz.bsbnb.usci.brms.rulesvr.service.impl;

import kz.bsbnb.usci.brms.rulemodel.model.IPackageVersion;
import kz.bsbnb.usci.brms.rulemodel.model.impl.PackageVersion;
import kz.bsbnb.usci.brms.rulemodel.model.impl.Rule;
import kz.bsbnb.usci.brms.rulemodel.model.impl.RulePackage;
import kz.bsbnb.usci.brms.rulemodel.service.IRuleService;
import kz.bsbnb.usci.brms.rulesvr.dao.IPackageDao;
import kz.bsbnb.usci.brms.rulesvr.dao.IRuleDao;
import kz.bsbnb.usci.brms.rulesvr.rulesingleton.RulesSingleton;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

//import kz.bsbnb.usci.brms.rulesvr.service.ListenerSingleton;

/**
 * @author abukabayev
 */

@Service
public class RuleService implements IRuleService {

    @Autowired
    private IRuleDao ruleDao;

    @Autowired
    private IPackageDao batchDao;

    //@Autowired
    //private IBatchVersionDao batchVersionDao;

//    @Autowired
//    private ListenerSingleton listenerSingleton;


    @Autowired
    private RulesSingleton rulesSingleton;

    @Override
    public long save(Rule rule, PackageVersion packageVersion) {

//        Batch batch = batchDao.loadBatch(batchVersion.getPackageId());
//
//        listenerSingleton.callListeners(batchVersion.getId(),batchVersion.getReport_date(), batch.getName());
        long id = ruleDao.save(rule, packageVersion);

//        listenerSingleton.callListeners(batchVersion.getId(), batchVersion.getReport_date(), batch.getName());

        return id;
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
        //long batchVersionId = batchDao.getBatchVersionId(packageId, repDate);
        Map m = new HashMap();
        //m.put("batchVersionId",batchVersionId);
        m.put("data",ruleDao.getRuleTitles(packageId,repDate));
        return m;
    }

    @Override
    public Map getRuleTitles(Long packageId, Date reportDate, String searchText) {
        if(searchText == null || searchText.length() < 1)
            throw new IllegalArgumentException(Errors.getMessage(Errors.E270));

        //long batchVersionId = batchDao.getBatchVersionId(packageId, repDate);
        Map m = new HashMap();
        //m.put("batchVersionId", batchVersionId);
        m.put("data",ruleDao.getRuleTitles(packageId, reportDate, searchText));
        return m;
    }

    @Override
    public Rule getRule(Long ruleId) {
        return ruleDao.getRule(ruleId);
    }

    @Override
    public boolean deleteRule(long ruleId, long batchVersionId) {
        return ruleDao.deleteRule(ruleId, batchVersionId);
    }

    @Override
    public long saveEmptyRule(String title, long batchVersionId) {
        long ruleId = ruleDao.createEmptyRule(title);
        ruleDao.save(ruleId, batchVersionId);
        return ruleId;
    }

    @Override
    public void updateBody(Long ruleId, String body) {
        ruleDao.updateBody(ruleId,body);
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
    public long createNewRuleInBatch(Rule rule, RulePackage rulePackage) {
        long ruleId = ruleDao.createRule(rule);
        rule.setId(ruleId);
        //ruleDao.save(ruleId, packageVersion.getId());
        ruleDao.saveInPackage(rule, rulePackage);
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
        List<String> list = new java.util.ArrayList<String>();
        for(String s : entity.getValidationErrors())
            list.add(s);
        return list;
    }

    /*@Override
    public String getRulePackageName(String pkgName, Date repDate) {
        return rulesSingleton.getRulePackageName(pkgName, repDate);
    }*/

    @Override
    public String getRuleErrors(String rule) {
        return rulesSingleton.getRuleErrors(rule);
    }

    @Override
    public String getPackageErrorsOnRuleUpdate(String ruleBody, Long ruleId, String pkgName, Date repDate) {
        return rulesSingleton.getPackageErrorsOnRuleUpdate(ruleBody, ruleId, pkgName, repDate, false, false, true);
    }

    @Override
    public String getPackageErrorsOnRuleActivate(String ruleBody, Long ruleId, String pkgName, Date repDate, boolean ruleEdited) {
        return rulesSingleton.getPackageErrorsOnRuleUpdate(ruleBody, ruleId, pkgName, repDate, true, false, ruleEdited);
    }

    @Override
    public String getPackageErrorsOnRuleDisable(Long ruleId, String pkgName, Date repDate) {
        return rulesSingleton.getPackageErrorsOnRuleUpdate("", ruleId, pkgName, repDate, false, true, false);
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


    //    public ListenerSingleton getListenerSingleton() {
//        return listenerSingleton;
//    }
//
//    public void setListenerSingleton(ListenerSingleton listenerSingleton) {
//        this.listenerSingleton = listenerSingleton;
//    }
}
