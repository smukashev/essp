package kz.bsbnb.usci.eav.model.metadata.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.metadata.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.metadata.IMetaFactory;
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
}
