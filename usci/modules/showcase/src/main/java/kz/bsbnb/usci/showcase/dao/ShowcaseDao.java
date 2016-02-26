package kz.bsbnb.usci.showcase.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.showcase.ShowcaseHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface ShowcaseDao {
    void createTables(ShowcaseHolder showcaseHolder);

    void generate(IBaseEntity entity, ShowcaseHolder showcaseHolder);

    ArrayList<ShowcaseHolder> getHolders();

    ShowCase load(String name);

    ShowCase load(long id);

    void reloadCache();

    int deleteById(ShowcaseHolder holder, IBaseEntity e);

    ShowcaseHolder getHolderByClassName(String className);

    Long insertBadEntity(Long entityId, Long scId, Date report_date, String strackTrace, String message);

    void closeEntities(Long scId, IBaseEntity entity, List<ShowcaseHolder> holders);

    long insert(ShowCase showCase);
}
