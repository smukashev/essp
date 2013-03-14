package kz.bsbnb.usci.eav_persistance.factory.impl;

import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_persistance.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav_persistance.factory.IMetaFactory;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
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

    public void setMetaClassRepository(IMetaClassRepository metaClassRepository)
    {
        this.metaClassRepository = metaClassRepository;
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
