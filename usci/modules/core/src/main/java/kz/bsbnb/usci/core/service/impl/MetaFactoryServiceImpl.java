package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public MetaClass getDisabledMetaClass(String name) {
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
