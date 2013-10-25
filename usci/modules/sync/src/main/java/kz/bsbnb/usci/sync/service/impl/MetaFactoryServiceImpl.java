package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Service
public class MetaFactoryServiceImpl implements IMetaFactoryService {
    @Autowired
    @Qualifier(value = "remoteMetaFactoryService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    kz.bsbnb.usci.core.service.IMetaFactoryService remoteMetaFactoryService;

    @PostConstruct
    public void init() {
        remoteMetaFactoryService =
                (kz.bsbnb.usci.core.service.IMetaFactoryService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public BaseEntity getBaseEntity(String className) {
        return remoteMetaFactoryService.getBaseEntity(className);
    }

    @Override
    public BaseEntity getBaseEntity(MetaClass metaClass) {
        return remoteMetaFactoryService.getBaseEntity(metaClass);
    }

    @Override
    public BaseSet getBaseSet(IMetaType meta) {
        return remoteMetaFactoryService.getBaseSet(meta);
    }

    @Override
    public List<BaseEntity> getBaseEntities() {
        return remoteMetaFactoryService.getBaseEntities();
    }

    @Override
    public List<MetaClass> getMetaClasses()
    {
        return remoteMetaFactoryService.getMetaClasses();
    }

    @Override
    public List<MetaClassName> getMetaClassesNames()
    {
        return remoteMetaFactoryService.getMetaClassesNames();
    }

    @Override
    public MetaClass getMetaClass(String name)
    {
        return remoteMetaFactoryService.getMetaClass(name);
    }

    @Override
    public boolean saveMetaClass(MetaClass meta)
    {
        return remoteMetaFactoryService.saveMetaClass(meta);
    }

    @Override
    public boolean delMetaClass(String className)
    {
        return remoteMetaFactoryService.delMetaClass(className);
    }


}
