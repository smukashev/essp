package kz.bsbnb.usci.eav.factory.impl;

import kz.bsbnb.usci.eav.factory.IMetaFactory;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author k.tulbassiyev
 */
@Repository
public class MetaFactoryImpl implements IMetaFactory {
    @Autowired
    private IMetaClassRepository metaClassRepository;

    public BaseEntity getBaseEntity(String className) {
        // TODO: Implement generation of the reporting date.
        return new BaseEntity(metaClassRepository.getMetaClass(className), new Date());
    }

    @Override
    public BaseSet getBaseSet(IMetaType meta) {
        return new BaseSet(meta);
    }

    @Override
    public BaseEntity getBaseEntity(MetaClass metaClass) {
        // TODO: Implement generation of the reporting date.
        return new BaseEntity(metaClass, new Date());
    }
}
