package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Service
public class MetaFactoryServiceImpl implements IMetaFactoryService {
    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Override
    public BaseEntity getBaseEntity(String className) {
        // TODO: Implement generation of the reporting date.
        return new BaseEntity(metaClassRepository.getMetaClass(className), new Date());
    }

    @Override
    public BaseEntity getBaseEntity(MetaClass metaClass) {
        // TODO: Implement generation of the reporting date.
        return new BaseEntity(metaClass, new Date());
    }

    @Override
    public BaseSet getBaseSet(IMetaType meta) {
        return new BaseSet(meta);
    }

    @Override
    public List<BaseEntity> getBaseEntities() {
        List<BaseEntity> baseEntityList = new ArrayList<BaseEntity>();

        for (MetaClass metaClass : metaClassRepository.getMetaClasses()){
            baseEntityList.add(new BaseEntity(metaClass,new Date()));
        }
        return baseEntityList;
    }
}
