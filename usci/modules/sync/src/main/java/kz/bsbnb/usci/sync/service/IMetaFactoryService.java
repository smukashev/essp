package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public interface IMetaFactoryService {
    List<MetaClass> getMetaClasses();

    List<MetaClassName> getMetaClassesNames();

    List<MetaClassName> getRefNames();

    MetaClass getMetaClass(String name);

    MetaClass getDisabledMetaClass(String name);

    MetaClass getMetaClass(Long metaId);

    boolean saveMetaClass(MetaClass meta) throws FileNotFoundException;

    boolean delMetaClass(String className);
}
