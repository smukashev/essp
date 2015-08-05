package kz.bsbnb.usci.eav.persistance.searcher.pool;

import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;

public interface IBaseEntitySearcherPool {
    IBaseEntitySearcher getSearcher(String name);
}
