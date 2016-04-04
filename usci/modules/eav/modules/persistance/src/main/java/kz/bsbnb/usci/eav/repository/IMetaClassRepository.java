package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.List;


public interface IMetaClassRepository {
    MetaClass getMetaClass(String className);

    MetaClass getDisabledMetaClass(String className);

    MetaClass getMetaClass(long id);

    List<MetaClass> getMetaClasses();

    void saveMetaClass(MetaClass meta);

    void resetCache();

    List<MetaClassName> getMetaClassesNames();

    List<MetaClassName> getRefNames();

    boolean delMetaClass(String className);
}
