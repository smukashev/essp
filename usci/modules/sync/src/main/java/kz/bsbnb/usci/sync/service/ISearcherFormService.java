package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.List;

/**
 *  @author Bauyrzhan.Makhambetov.
 */
public interface ISearcherFormService {
    List<Pair> getMetaClasses(long userId);
    String getDom(long userId, IMetaClass metaClass);
}
