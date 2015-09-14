package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.Date;
import java.util.List;

public interface IMetaClassDao extends IDao<MetaClass> {
    MetaClass load(String className);

    MetaClass loadDisabled(String className);

    MetaClass load(String className, Date beginDate);

    List<MetaClass> loadAll();

    long save(MetaClass meta);

    List<MetaClassName> getMetaClassesNames();

    List<MetaClassName> getRefNames();

    void remove(MetaClass metaClass);
}
