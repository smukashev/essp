package kz.bsbnb.usci.eav.persistance.dao;

public interface IBaseEntitySearcherPool
{
    public IBaseEntitySearcher getSearcher(String name);
}
