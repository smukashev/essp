package kz.bsbnb.usci.showcase.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.showcase.ShowcaseHolder;

import java.util.ArrayList;

/**
 * Created by almaz on 7/2/14.
 */
public interface ShowcaseDao {

    public void createTables(ShowcaseHolder showcaseHolder);
    public void dbCarteageGenerate(IBaseEntity entity, ShowcaseHolder showcaseHolder);
    public ArrayList<ShowcaseHolder> getHolders();
    public long save(ShowCase showCaseForSave);
    public void remove(ShowCase showCase);
    public ShowCase load(String name);
}
