package kz.bsbnb.usci.showcase.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ShowcaseDao {

    void createTables(ShowcaseHolder showcaseHolder);

    void generate(IBaseEntity entity, IBaseEntity entityLoaded, ShowcaseHolder showcaseHolder);

    ArrayList<ShowcaseHolder> getHolders();

    long save(ShowCase showCaseForSave);

    void remove(ShowCase showCase);

    ShowCase load(String name);

    ShowCase load(long id);

    void reloadCache();

    int deleteById(ShowcaseHolder holder, IBaseEntity e);

    ShowcaseHolder getHolderByClassName(String className);

    Long insertBadEntity(Long entityId, Long scId, Date report_date, String strackTrace, String message);

    void closeEntities(Long scId, IBaseEntity entity, List<ShowcaseHolder> holders);

    @Transactional
    List<Map<String, Object>> view(Long id, int offset, int limit, Date reportDate);
}
