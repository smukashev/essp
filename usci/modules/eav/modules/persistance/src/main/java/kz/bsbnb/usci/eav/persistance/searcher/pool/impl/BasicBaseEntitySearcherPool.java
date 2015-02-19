package kz.bsbnb.usci.eav.persistance.searcher.pool.impl;

import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.impl.CreditSearcher;
import kz.bsbnb.usci.eav.persistance.searcher.impl.DocumentSearcher;
import kz.bsbnb.usci.eav.persistance.searcher.impl.ImprovedBaseEntityLocalSearcher;
import kz.bsbnb.usci.eav.persistance.searcher.impl.ImprovedBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.pool.IBaseEntitySearcherPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Repository
public class BasicBaseEntitySearcherPool implements IBaseEntitySearcherPool {
    private HashMap<String, IBaseEntitySearcher> searchersByName = new HashMap<String, IBaseEntitySearcher>();

    @Autowired
    private ImprovedBaseEntitySearcher baseEntitySearcher;

    @Autowired
    private ImprovedBaseEntityLocalSearcher baseEntityLocalSearcher;

    @Autowired
    private DocumentSearcher documentSearcher;

    @Autowired
    private CreditSearcher creditSearcher;

    @PostConstruct
    public void init() {
        addSearcher(documentSearcher.getClassName(), documentSearcher);
        addSearcher(creditSearcher.getClassName(), creditSearcher);
    }

    @Override
    public IBaseEntitySearcher getSearcher(String name) {
        IBaseEntitySearcher searcher = searchersByName.get(name);
        if (searcher == null)
            return baseEntityLocalSearcher;
            //return baseEntitySearcher;
        return searcher;
    }

    public void addSearcher(String name, IBaseEntitySearcher searcher) {
        searchersByName.put(name, searcher);
    }
}
