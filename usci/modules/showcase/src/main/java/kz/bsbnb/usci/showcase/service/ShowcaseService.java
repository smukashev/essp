package kz.bsbnb.usci.showcase.service;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.showcase.ShowcaseHolder;

import java.util.*;

public interface ShowcaseService {
    long add(ShowCase showCase);

    List<ShowcaseHolder> list();

    ShowCase load(String name);

    void startLoad(String name, Date reportDate, boolean doPopulate);

    void startLoadHistory(boolean populate, Queue<Long> creditorIds);

    void stopLoadHistory();

    HashMap<String, QueryEntry> getSQLStats();

    void reloadCash();

    void stopLoad(String name);

    void pauseLoad(String name);

    void resumeLoad(String name);

    List<String> listLoading();

    ShowCase load(Long id);

    List<Map<String, Object>> view(Long id, int offset, int limit, Date reportDate);
}
