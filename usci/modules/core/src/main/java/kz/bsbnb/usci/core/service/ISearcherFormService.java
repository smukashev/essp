package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.List;

/**
 * Created by Bauyrzhan.Makhambeto on 19/02/2015.
 */
public interface ISearcherFormService {
    public List<Pair> getMetaClasses(long userId);
    public String getDom(long userId, IMetaClass metaClass);
}
