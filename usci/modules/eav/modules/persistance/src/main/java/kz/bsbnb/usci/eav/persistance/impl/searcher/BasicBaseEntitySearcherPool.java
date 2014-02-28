package kz.bsbnb.usci.eav.persistance.impl.searcher;

import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcherPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class BasicBaseEntitySearcherPool implements IBaseEntitySearcherPool
{
    private HashMap<String, IBaseEntitySearcher> searchersByName = new HashMap<String, IBaseEntitySearcher>();

    @Autowired
    private ImprovedBaseEntitySearcher baseEntitySearcher;

    @Override
    public IBaseEntitySearcher getSearcher(String name)
    {
        IBaseEntitySearcher searcher = searchersByName.get(name);
        if(searcher == null)
            return baseEntitySearcher;
        return searcher;
    }

    public void addSearcher(String name, IBaseEntitySearcher searcher)
    {
        searchersByName.put(name, searcher);
    }
}
