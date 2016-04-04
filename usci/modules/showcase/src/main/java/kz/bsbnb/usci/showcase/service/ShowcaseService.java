package kz.bsbnb.usci.showcase.service;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.stats.QueryEntry;

import java.util.List;
import java.util.Map;

public interface ShowcaseService {
    long add(ShowCase showCase);

    List<ShowCase> list();

    ShowCase load(String name);

    Map<String, QueryEntry> getSQLStats();

    void reloadCash();

    ShowCase load(Long id);

    String getUrlSc();

    String getSchemaSc();

    String getPasswordSc();

    String getDriverSc();
}
