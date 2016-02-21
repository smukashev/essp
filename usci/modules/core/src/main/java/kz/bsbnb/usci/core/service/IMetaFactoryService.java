package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.List;

public interface IMetaFactoryService {
    List<MetaClass> getMetaClasses();

    List<MetaClassName> getMetaClassesNames();

    List<MetaClassName> getRefNames();

    MetaClass getMetaClass(String name);

    MetaClass getDisabledMetaClass(String name);

    MetaClass getMetaClass(Long metaId);

    boolean saveMetaClass(MetaClass meta);

    boolean delMetaClass(String className);
}
