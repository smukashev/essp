package kz.bsbnb.usci.showcase.service.impl;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.showcase.dao.impl.ShowcaseDaoImpl;
import kz.bsbnb.usci.showcase.service.ShowcaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ShowcaseServiceImpl implements ShowcaseService {
    @Autowired
    SQLQueriesStats stats;

    @Autowired
    private ShowcaseDaoImpl showcaseDao;

    @Override
    public long add(ShowCase showCase) {
        long id = showcaseDao.insert(showCase);
        showCase.setId(id);
        showcaseDao.createTables(showCase);
        return id;
    }

    @Override
    public List<ShowCase> list() {
        return showcaseDao.getShowCases();
    }

    @Override
    public ShowCase load(String name) {
        return showcaseDao.load(name);
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
}
