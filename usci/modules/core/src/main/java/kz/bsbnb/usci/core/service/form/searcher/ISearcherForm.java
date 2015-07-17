package kz.bsbnb.usci.core.service.form.searcher;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Bauyrzhan.Makhambeto on 01/07/2015.
 */
public interface ISearcherForm {
    List<Pair> getMetaClasses(long userId);
    String getDom(long userId, IMetaClass metaClass, String prefix);
    List<BaseEntity> search(HashMap<String,String> parameters, MetaClass metaClass, String prefix);
}
