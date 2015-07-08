package kz.bsbnb.usci.core.service.form;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.util.Pair;

import java.util.HashMap;
import java.util.List;

public interface ISearcherFormService {
    List<String[]> getMetaClasses(long userId);
    String getDom(Long userId, String search, IMetaClass metaClass);
    List<BaseEntity> search(String searchClassName, HashMap<String,String> parameters, MetaClass metaClass);
}
