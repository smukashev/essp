package kz.bsbnb.usci.showcase.service.impl;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.model.stats.QueryEntry;
import kz.bsbnb.usci.showcase.dao.impl.ShowcaseDaoImpl;
import kz.bsbnb.usci.showcase.driver.ShowCaseJdbcTemplate;
import kz.bsbnb.usci.showcase.service.ShowcaseService;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ShowcaseServiceImpl implements ShowcaseService {
    @Autowired
    private ShowCaseJdbcTemplate jdbcTemplateSC;

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
    public String getUrlSc() {
        return ((BasicDataSource) showcaseDao.getDataSourceSc()).getUrl();
    }

    @Override
    public String getSchemaSc() {
        return ((BasicDataSource) showcaseDao.getDataSourceSc()).getUsername();
    }

    @Override
    public String getPasswordSc() {
        return ((BasicDataSource) showcaseDao.getDataSourceSc()).getPassword();
    }

    @Override
    public String getDriverSc() {
        return ((BasicDataSource) showcaseDao.getDataSourceSc()).getDriverClassName();
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
    public Map<String, QueryEntry> getSQLStats() {
        return jdbcTemplateSC.getSqlStats();
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
