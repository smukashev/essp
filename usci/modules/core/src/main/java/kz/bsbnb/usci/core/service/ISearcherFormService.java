package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.List;

public interface ISearcherFormService {
    List<Pair> getMetaClasses(long userId);
    String getDom(long userId, IMetaClass metaClass);
}
