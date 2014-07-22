package kz.bsbnb.usci.showcase.service;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.showcase.ShowcaseHolder;

import java.util.Date;
import java.util.List;

/**
 * Created by almaz on 7/3/14.
 */
public interface ShowcaseService {
    public void add(ShowCase showCase);
    public List<ShowcaseHolder> list();
    public ShowCase load(String name);
    public void startLoad(String name, Date reportDate);
}
