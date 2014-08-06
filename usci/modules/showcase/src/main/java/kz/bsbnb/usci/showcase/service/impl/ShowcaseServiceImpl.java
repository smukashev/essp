package kz.bsbnb.usci.showcase.service.impl;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import kz.bsbnb.usci.showcase.dao.ShowcaseDao;
import kz.bsbnb.usci.showcase.service.CoreShowcaseService;
import kz.bsbnb.usci.showcase.service.ShowcaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by almaz on 7/3/14.
 */

@Service
public class ShowcaseServiceImpl implements ShowcaseService{

    @Autowired
    private ShowcaseDao showcaseDao;

    @Autowired
    @Qualifier(value="remoteCoreShowcaseService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    @Autowired
    SQLQueriesStats stats;

    @Override
    public void add(ShowCase showCase){
        ShowcaseHolder scHolder = new ShowcaseHolder();
        long id = showcaseDao.save(showCase);
        scHolder.setShowCaseMeta(showCase);
        showcaseDao.createTables(scHolder);
    }


    public void startLoad(String name, Date reportDate){
        CoreShowcaseService coreShowcaseService = (CoreShowcaseService) rmiProxyFactoryBean.getObject();
        Long id = showcaseDao.load(name).getId();
        coreShowcaseService.start("credit", id, reportDate);
    }

    @Override
    public List<ShowcaseHolder> list(){
        List<ShowcaseHolder> list = showcaseDao.getHolders();
        return list;
    }

    @Override
    public ShowCase load(String name){
        ShowCase showcase = showcaseDao.load(name);
        return showcase;
    }

    @Override
    public HashMap<String, QueryEntry> getSQLStats() {
        return stats.getStats();
    }
}
