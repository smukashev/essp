package kz.bsbnb.usci.showcase.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by almaz on 7/2/14.
 */
public interface ShowcaseDao {

    public void createTables(ShowcaseHolder showcaseHolder);
    public void generate(IBaseEntity entity, ShowcaseHolder showcaseHolder);
    public ArrayList<ShowcaseHolder> getHolders();
    public long save(ShowCase showCaseForSave);
    public void remove(ShowCase showCase);
    public ShowCase load(String name);
    public ShowCase load(long id);
    public void reloadCache();
    public int deleteById(ShowcaseHolder holder, IBaseEntity e);
    public ShowcaseHolder getHolderByClassName(String className);
    @Transactional
    List<Map<String, Object>> view(Long id, int offset, int limit, Date reportDate);
}
