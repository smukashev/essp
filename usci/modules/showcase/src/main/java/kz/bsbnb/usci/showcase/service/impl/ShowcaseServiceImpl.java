package kz.bsbnb.usci.showcase.service.impl;

import kz.bsbnb.usci.core.service.CoreShowcaseService;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import kz.bsbnb.usci.showcase.dao.ShowcaseDao;
import kz.bsbnb.usci.showcase.service.ShowcaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShowcaseServiceImpl implements ShowcaseService {

    @Autowired
    @Qualifier(value = "remoteCoreShowcaseService")
    RmiProxyFactoryBean rmiProxyFactoryBean;
    @Autowired
    SQLQueriesStats stats;
    @Autowired
    private ShowcaseDao showcaseDao;

    @Override
    public long add(ShowCase showCase) {
        ShowcaseHolder scHolder = new ShowcaseHolder();
        long id = showcaseDao.save(showCase);
        scHolder.setShowCaseMeta(showCase);
        showcaseDao.createTables(scHolder);
        return id;
    }

    @Override
    public void startLoad(String name, Date reportDate) {
        CoreShowcaseService coreShowcaseService = (CoreShowcaseService) rmiProxyFactoryBean.getObject();

        Long id;
        try {
            id = showcaseDao.load(name).getId();
        } catch(Exception e) {
            id = 0L;
        }

        coreShowcaseService.start("credit", id, reportDate);
    }

    @Override
    public void startLoadHistory(boolean populate, Queue<Long> creditorIds) {
        CoreShowcaseService coreShowcaseService = (CoreShowcaseService) rmiProxyFactoryBean.getObject();
        coreShowcaseService.startLoadHistory(populate, creditorIds);
    }

    @Override
    public void stopLoadHistory() {
        CoreShowcaseService coreShowcaseService = (CoreShowcaseService) rmiProxyFactoryBean.getObject();
        coreShowcaseService.stopHistory();
    }

    @Override
    public List<ShowcaseHolder> list(){
        List<ShowcaseHolder> list = showcaseDao.getHolders();
        return list;
    }

    @Override
    public ShowCase load(String name) {
        ShowCase showcase = showcaseDao.load(name);
        return showcase;
    }

    @Override
    public HashMap<String, QueryEntry> getSQLStats() {
        return stats.getStats();
    }

    @Override
    public void reloadCash() {
        showcaseDao.reloadCache();
    }

    @Override
    public void stopLoad(String name) {
        CoreShowcaseService coreShowcaseService = (CoreShowcaseService) rmiProxyFactoryBean.getObject();
        Long id = showcaseDao.load(name).getId();
        coreShowcaseService.stop(id);
    }

    @Override
    public void pauseLoad(String name) {
        CoreShowcaseService coreShowcaseService = (CoreShowcaseService) rmiProxyFactoryBean.getObject();
        Long id = showcaseDao.load(name).getId();
        coreShowcaseService.pause(id);
    }

    @Override
    public void resumeLoad(String name) {
        CoreShowcaseService coreShowcaseService = (CoreShowcaseService) rmiProxyFactoryBean.getObject();
        Long id = showcaseDao.load(name).getId();
        coreShowcaseService.resume(id);
    }

    @Override
    public List<String> listLoading() {
        CoreShowcaseService coreShowcaseService = (CoreShowcaseService) rmiProxyFactoryBean.getObject();
        List<String> list = new ArrayList<String>();

        for (Long id : coreShowcaseService.listLoading())
            list.add(showcaseDao.load(id).getName());

        return list;
    }

    @Override
    public ShowCase load(Long id) {
        return showcaseDao.load(id);
    }

    @Override
    public List<Map<String, Object>> view(Long id, int offset, int limit, Date reportDate) {
        return showcaseDao.view(id, offset, limit, reportDate);
    }
}
