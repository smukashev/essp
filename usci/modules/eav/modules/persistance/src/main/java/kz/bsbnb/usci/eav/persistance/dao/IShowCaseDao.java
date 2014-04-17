package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.showcase.ShowCase;

public interface IShowCaseDao extends IDao<ShowCase>
{
    @Override
    ShowCase load(long id);

    @Override
    long save(ShowCase showCase);

    @Override
    void remove(ShowCase showCase);
}
