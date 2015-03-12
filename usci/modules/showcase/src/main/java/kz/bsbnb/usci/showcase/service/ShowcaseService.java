package kz.bsbnb.usci.showcase.service;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.showcase.ShowcaseHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by almaz on 7/3/14.
 */
public interface ShowcaseService {
    public void add(ShowCase showCase);
    public List<ShowcaseHolder> list();
    public ShowCase load(String name);
    public void startLoad(String name, Date reportDate);
    void startLoadHistory(boolean populate);
    void stopLoadHistory();
    public HashMap<String, QueryEntry> getSQLStats();
    public void reloadCash();


    void stopLoad(String name);
    void pauseLoad(String name);
    void resumeLoad(String name);
    List<String> listLoading();

    ShowCase load(Long id);
    List<Map<String, Object>> view(Long id, int offset, int limit, Date reportDate);
}
