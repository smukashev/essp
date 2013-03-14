package kz.bsbnb.usci.eav_persistance.persistance.dao;

public interface IBaseEntitySearcherPool
{
    public IBaseEntitySearcher getSearcher(String name);
}
