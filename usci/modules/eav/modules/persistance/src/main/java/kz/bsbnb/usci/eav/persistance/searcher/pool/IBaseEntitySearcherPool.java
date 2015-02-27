package kz.bsbnb.usci.eav.persistance.searcher.pool;

import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.impl.ImprovedBaseEntityLocalSearcher;

public interface IBaseEntitySearcherPool
{
    public IBaseEntitySearcher getSearcher(String name);
    public ImprovedBaseEntityLocalSearcher getImprovedBaseEntityLocalSearcher();
}
