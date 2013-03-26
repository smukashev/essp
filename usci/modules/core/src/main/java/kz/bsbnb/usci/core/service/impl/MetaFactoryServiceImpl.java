package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author k.tulbassiyev
 */
@Service
public class MetaFactoryServiceImpl implements IMetaFactoryService
{
    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Override
    public BaseEntity getBaseEntity(String className)
    {
        return new BaseEntity(metaClassRepository.getMetaClass(className));
    }

    @Override
    public BaseEntity getBaseEntity(MetaClass metaClass)
    {
        return new BaseEntity(metaClass);
    }

    @Override
    public BaseSet getBaseSet(IMetaType meta)
    {
        return new BaseSet(meta);
    }
}
