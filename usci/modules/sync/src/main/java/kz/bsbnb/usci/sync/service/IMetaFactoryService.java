package kz.bsbnb.usci.sync.service;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.Date;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public interface IMetaFactoryService {
    BaseEntity getBaseEntity(String className);

    BaseEntity getBaseEntity(MetaClass metaClass);

    BaseEntity getBaseEntity(String className, Date reportDate);

    BaseEntity getBaseEntity(MetaClass metaClass, Date reportDate);

    BaseSet getBaseSet(IMetaType meta);

    List<BaseEntity> getBaseEntities();

    List<MetaClass> getMetaClasses();

    List<MetaClassName> getMetaClassesNames();

    List<MetaClassName> getRefNames();

    MetaClass getMetaClass(String name);

    MetaClass getDisabledMetaClass(String name);

    MetaClass getMetaClass(Long metaId);

    boolean saveMetaClass(MetaClass meta);

    boolean delMetaClass(String className);
}
