package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.List;

/**
 * Created by Bauyrzhan.Makhambeto on 05/03/2015.
 */
public interface ISearcherFormService {
    public List<Pair> getMetaClasses(long userId);
    public String getDom(long userId, IMetaClass metaClass);
}
