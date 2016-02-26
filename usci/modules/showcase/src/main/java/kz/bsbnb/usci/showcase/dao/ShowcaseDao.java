package kz.bsbnb.usci.showcase.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.showcase.ShowCase;

import java.util.Date;
import java.util.List;

public interface ShowcaseDao {
    void createTables(ShowCase ShowCase);

    void generate(IBaseEntity entity, ShowCase ShowCase);

    List<ShowCase> getShowCases();

    ShowCase load(String name);

    ShowCase load(long id);

    void reloadCache();

    int deleteById(ShowCase holder, IBaseEntity e);

    ShowCase getHolderByClassName(String className);

    Long insertBadEntity(Long entityId, Long scId, Date report_date, String stackTrace, String message);

    void closeEntities(Long scId, IBaseEntity entity, List<ShowCase> holders);

    long insert(ShowCase showCase);
}
