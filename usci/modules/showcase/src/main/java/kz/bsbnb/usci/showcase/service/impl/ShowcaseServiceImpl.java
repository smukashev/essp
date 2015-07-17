package kz.bsbnb.usci.showcase.service.impl;

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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShowcaseServiceImpl implements ShowcaseService {
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
    public List<ShowcaseHolder> list() {
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
    public ShowCase load(Long id) {
        return showcaseDao.load(id);
    }

    @Override
    public List<Map<String, Object>> view(Long id, int offset, int limit, Date reportDate) {
        return showcaseDao.view(id, offset, limit, reportDate);
    }
}
