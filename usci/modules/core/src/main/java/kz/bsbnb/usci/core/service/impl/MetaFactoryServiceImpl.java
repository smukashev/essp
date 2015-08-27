package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MetaFactoryServiceImpl implements IMetaFactoryService {
    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Override
    @Deprecated
    public BaseEntity getBaseEntity(String className) {
        // TODO: remove
        return new BaseEntity(metaClassRepository.getMetaClass(className), new Date());
    }

    @Override
    @Deprecated
    public BaseEntity getBaseEntity(MetaClass metaClass) {
        // TODO: remove
        return new BaseEntity(metaClass, new Date());
    }

    @Override
    public BaseEntity getBaseEntity(String className, Date reportDate) {
        return new BaseEntity(metaClassRepository.getMetaClass(className), reportDate);
    }

    @Override
    public BaseEntity getBaseEntity(MetaClass metaClass, Date reportDate) {
        return new BaseEntity(metaClass, reportDate);
    }

    @Override
    public BaseSet getBaseSet(IMetaType meta) {
        return new BaseSet(meta);
    }

    @Override
    public List<BaseEntity> getBaseEntities() {
        List<BaseEntity> baseEntityList = new ArrayList<BaseEntity>();

        for (MetaClass metaClass : metaClassRepository.getMetaClasses()) {
            baseEntityList.add(new BaseEntity(metaClass, new Date()));
        }

        return baseEntityList;

    }

    @Override
    public List<MetaClass> getMetaClasses() {
        return metaClassRepository.getMetaClasses();
    }

    @Override
    public List<MetaClassName> getMetaClassesNames() {
        return metaClassRepository.getMetaClassesNames();
    }

    @Override
    public List<MetaClassName> getRefNames() {
        return metaClassRepository.getRefNames();
    }

    @Override
    public MetaClass getMetaClass(String name) {
        return metaClassRepository.getMetaClass(name);
    }

    @Override
    public MetaClass getDisabledMetaClass(String name)
    {
        return metaClassRepository.getDisabledMetaClass(name);
    }
    @Override
    public MetaClass getMetaClass(Long metaId) {
        return metaClassRepository.getMetaClass(metaId);
    }

    @Override
    public boolean saveMetaClass(MetaClass meta) {
        metaClassRepository.saveMetaClass(meta);
        return true;
    }

    @Override
    public boolean delMetaClass(String className) {
        return metaClassRepository.delMetaClass(className);
    }

}
