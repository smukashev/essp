package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.core.Main;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author k.tulbassiyev
 */
@Service
public class MetaFactoryServiceImpl implements IMetaFactoryService
{
    @Autowired
    @Qualifier(value = "metaFactoryService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    kz.bsbnb.usci.core.service.IMetaFactoryService remoteMetaFactoryService;

    @PostConstruct
    public void init()
    {
        remoteMetaFactoryService = (kz.bsbnb.usci.core.service.IMetaFactoryService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public BaseEntity getBaseEntity(String className)
    {
        return remoteMetaFactoryService.getBaseEntity(className);
    }

    @Override
    public BaseEntity getBaseEntity(MetaClass metaClass)
    {
        return remoteMetaFactoryService.getBaseEntity(metaClass);
    }

    @Override
    public BaseSet getBaseSet(IMetaType meta)
    {
        return remoteMetaFactoryService.getBaseSet(meta);
    }
}
