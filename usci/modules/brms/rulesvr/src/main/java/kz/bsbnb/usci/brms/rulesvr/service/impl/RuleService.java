package kz.bsbnb.usci.brms.rulesvr.service.impl;

import kz.bsbnb.usci.brms.rulesvr.dao.IBatchDao;
import kz.bsbnb.usci.brms.rulesvr.dao.IRuleDao;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.model.impl.SimpleTrack;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

//    @Autowired
//    private ListenerSingleton listenerSingleton;

    @Override
    public long save(Rule rule, BatchVersion batchVersion) {

//        Batch batch = batchDao.loadBatch(batchVersion.getPackage_id());
//
//        listenerSingleton.callListeners(batchVersion.getId(),batchVersion.getRepDate(), batch.getName());
        long id = ruleDao.save(rule,batchVersion);

//        listenerSingleton.callListeners(batchVersion.getId(), batchVersion.getRepDate(), batch.getName());

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
    public List<SimpleTrack> getRuleTitles(Long packageId, Date repDate) {
        return ruleDao.getRuleTitles(packageId, repDate);
    }

    @Override
    public Rule getRule(Long ruleId) {
        return ruleDao.getRule(ruleId);
    }

    //    public ListenerSingleton getListenerSingleton() {
//        return listenerSingleton;
//    }
//
//    public void setListenerSingleton(ListenerSingleton listenerSingleton) {
//        this.listenerSingleton = listenerSingleton;
//    }
}
