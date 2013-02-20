package kz.bsbnb.usci.eav.model.metadata;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.BaseSet;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;

/**
 * @author k.tulbassiyev
 */
public interface IMetaFactory
{
    public BaseEntity getBaseEntity(String className);
    public BaseEntity getBaseEntity(MetaClass metaClass);
    public BaseSet getBaseSet(IMetaType meta);
}
