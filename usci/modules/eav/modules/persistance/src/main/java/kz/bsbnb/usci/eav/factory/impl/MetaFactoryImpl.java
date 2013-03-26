package kz.bsbnb.usci.eav.factory.impl;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.factory.IMetaFactory;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author k.tulbassiyev
 */
@Repository
public class MetaFactoryImpl implements IMetaFactory
{
    @Autowired
    private IMetaClassRepository metaClassRepository;

    public BaseEntity getBaseEntity(String className)
    {
        return new BaseEntity(metaClassRepository.getMetaClass(className));
    }

    @Override
    public BaseSet getBaseSet(IMetaType meta)
    {
        return new BaseSet(meta);
    }

    @Override
    public BaseEntity getBaseEntity(MetaClass metaClass)
    {
        return new BaseEntity(metaClass);
    }
}
