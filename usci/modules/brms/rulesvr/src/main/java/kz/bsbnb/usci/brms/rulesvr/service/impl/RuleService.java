package kz.bsbnb.usci.brms.rulesvr.service.impl;

import kz.bsbnb.usci.brms.rulesvr.dao.IBatchDao;
import kz.bsbnb.usci.brms.rulesvr.dao.IBatchVersionDao;
import kz.bsbnb.usci.brms.rulesvr.dao.IRuleDao;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.model.impl.SimpleTrack;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import kz.bsbnb.usci.brms.rulesvr.service.ListenerSingleton;

/**
 * @author abukabayev
 */

@Service
public class RuleService implements IRuleService {

    @Autowired
    private IRuleDao ruleDao;

    @Autowired
    private IBatchDao batchDao;

    @Autowired
    private IBatchVersionDao batchVersionDao;

//    @Autowired
//    private ListenerSingleton listenerSingleton;

    @Override
    public long save(Rule rule, BatchVersion batchVersion) {

//        Batch batch = batchDao.loadBatch(batchVersion.getPackage_id());
//
//        listenerSingleton.callListeners(batchVersion.getId(),batchVersion.getReport_date(), batch.getName());
        long id = ruleDao.save(rule,batchVersion);

//        listenerSingleton.callListeners(batchVersion.getId(), batchVersion.getReport_date(), batch.getName());

        return id;
    }

    @Override
    public List<Rule> load(BatchVersion batchVersion) {
        return ruleDao.load(batchVersion);
    }

    @Override
    public void update(Rule rule) {
        ruleDao.update(rule);
    }

    @Override
    public List<Rule> getAllRules() {
        return ruleDao.getAllRules();
    }

    @Override
    public Map getRuleTitles(Long packageId, Date repDate) {
        long batchVersionId = batchDao.getBatchVersionId(packageId, repDate);
        Map m = new HashMap();
        m.put("batchVersionId",batchVersionId);
        m.put("data",ruleDao.getRuleTitles(packageId,repDate));
        return m;
    }

    @Override
    public Rule getRule(Long ruleId) {
        return ruleDao.getRule(ruleId);
    }

    @Override
    public boolean deleteRule(long ruleId, long batchVersionId) {
        return ruleDao.deleteRule(ruleId,batchVersionId);
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
    public long createNewRuleInBatch(Rule rule, BatchVersion batchVersion) {
          long ruleId = ruleDao.createRule(rule);
          ruleDao.save(ruleId, batchVersion.getId());
          return ruleId;
    }

    @Override
    public void renameRule(long ruleId, String title) {
        ruleDao.renameRule(ruleId,title);
    }

    //    public ListenerSingleton getListenerSingleton() {
//        return listenerSingleton;
//    }
//
//    public void setListenerSingleton(ListenerSingleton listenerSingleton) {
//        this.listenerSingleton = listenerSingleton;
//    }
}
