package kz.bsbnb.usci.showcase.service;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.showcase.ShowcaseHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ShowcaseService {
    long add(ShowCase showCase);

    List<ShowcaseHolder> list();

    ShowCase load(String name);

    HashMap<String, QueryEntry> getSQLStats();

    void reloadCash();

    ShowCase load(Long id);

    List<Map<String, Object>> view(Long id, int offset, int limit, Date reportDate);
}
