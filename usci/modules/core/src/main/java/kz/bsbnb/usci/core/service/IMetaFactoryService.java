package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.List;

/**
 * @author k.tulbassiyev
 */
public interface IMetaFactoryService {
    public BaseEntity getBaseEntity(String className);
    public BaseEntity getBaseEntity(MetaClass metaClass);
    public BaseSet getBaseSet(IMetaType meta);
    public List<BaseEntity> getBaseEntities();
}
