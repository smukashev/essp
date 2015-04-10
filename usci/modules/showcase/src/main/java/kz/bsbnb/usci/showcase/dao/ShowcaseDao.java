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

    void generate(IBaseEntity entity, ShowcaseHolder showcaseHolder);

    ArrayList<ShowcaseHolder> getHolders();

    long save(ShowCase showCaseForSave);

    void remove(ShowCase showCase);

    ShowCase load(String name);

    ShowCase load(long id);

    void reloadCache();

    int deleteById(ShowcaseHolder holder, IBaseEntity e);

    ShowcaseHolder getHolderByClassName(String className);

    @Transactional
    List<Map<String, Object>> view(Long id, int offset, int limit, Date reportDate);

    Long insertBadEntity(long entityId, long scId, Date report_date, String strackTrace, String message);

}
