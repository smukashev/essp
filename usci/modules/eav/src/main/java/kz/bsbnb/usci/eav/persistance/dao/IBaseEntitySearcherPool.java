package kz.bsbnb.usci.eav.persistance.dao;

import java.util.HashMap;

public interface IBaseEntitySearcherPool
{
    public IBaseEntitySearcher getSearcher(String name);
}
