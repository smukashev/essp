package kz.bsbnb.usci.eav.persistance.searcher.pool.impl;

import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.impl.CreditSearcher;
import kz.bsbnb.usci.eav.persistance.searcher.impl.DocumentSearcher;
import kz.bsbnb.usci.eav.persistance.searcher.impl.ImprovedBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.pool.IBaseEntitySearcherPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class BasicBaseEntitySearcherPool implements IBaseEntitySearcherPool {
    private HashMap<String, IBaseEntitySearcher> searchersByName = new HashMap<>();

    @Autowired
    private ImprovedBaseEntitySearcher baseEntitySearcher;

    @Autowired
    private CreditSearcher creditSearcher;

    @Autowired
    private DocumentSearcher documentSearcher;

    public BasicBaseEntitySearcherPool() {
        searchersByName.put(creditSearcher.getClassName(), creditSearcher);
        searchersByName.put(documentSearcher.getClassName(), documentSearcher);
    }

    @Override
    public IBaseEntitySearcher getSearcher(String name) {
        IBaseEntitySearcher searcher = searchersByName.get(name);

        if (searcher == null)
            return baseEntitySearcher;

        return searcher;
    }
}
