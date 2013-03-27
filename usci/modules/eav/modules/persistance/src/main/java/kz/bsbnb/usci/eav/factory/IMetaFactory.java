package kz.bsbnb.usci.eav.factory;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

/**
 * @author k.tulbassiyev
 */
public interface IMetaFactory {
    public BaseEntity getBaseEntity(String className);
    public BaseEntity getBaseEntity(MetaClass metaClass);
    public BaseSet getBaseSet(IMetaType meta);
}
