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
    public BaseEntity getBaseEntity(String className);
    public BaseEntity getBaseEntity(MetaClass metaClass);
    public BaseEntity getBaseEntity(String className, Date reportDate);
    public BaseEntity getBaseEntity(MetaClass metaClass, Date reportDate);
    public BaseSet getBaseSet(IMetaType meta);
    public List<BaseEntity> getBaseEntities();
    public List<MetaClass> getMetaClasses();
    public List<MetaClassName> getMetaClassesNames();
    public List<MetaClassName> getRefNames();
    public MetaClass getMetaClass(String name);
    public MetaClass getMetaClass(Long metaId);
    public boolean saveMetaClass(MetaClass meta);
    public boolean delMetaClass(String className);
}
