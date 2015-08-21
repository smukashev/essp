package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;

import java.util.HashMap;
import java.util.List;

/**
 *  @author Bauyrzhan.Makhambetov.
 */
public interface ISearcherFormService {
    List<String[]> getMetaClasses(long userId);
    String getDom(Long userId, String search, IMetaClass metaClass, String prefix);
    ISearchResult search(String searchClassName, HashMap<String,String> parameters, MetaClass metaClass, String prefix);
}
