package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;

/**
 * @author k.tulbassiyev
 */
public class MetaFactoryService implements IMetaFactoryService
{
    @Override
    public BaseEntity getBaseEntity(String className)
    {
        return null;
    }

    @Override
    public BaseEntity getBaseEntity(MetaClass metaClass)
    {
        return null;
    }

    @Override
    public BaseSet getBaseSet(IMetaType meta)
    {
        return null;
    }
}
