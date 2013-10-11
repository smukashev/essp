package kz.bsbnb.usci.receiver.repository.impl;

import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

/**
 * @author k.tulbassiyev
 */
@Repository
public class ServiceRepositoryImpl implements IServiceRepository {
    @Autowired
    @Qualifier(value = "remoteEntityService")
    private RmiProxyFactoryBean entityRmiService;

    @Autowired
    @Qualifier(value = "remoteBatchService")
    private RmiProxyFactoryBean batchRmiService;

    @Autowired
    @Qualifier(value = "remoteMetaFactoryService")
    private RmiProxyFactoryBean metaFactoryRmiService;

    @Autowired
    @Qualifier(value = "remoteUserService")
    private RmiProxyFactoryBean remoteUserService;

    private IEntityService entityService;
    private IBatchService batchService;
    private IMetaFactoryService metaFactoryService;
    private PortalUserBeanRemoteBusiness userService;

    @PostConstruct
    public void init() {
        entityService = (IEntityService) entityRmiService.getObject();
        batchService = (IBatchService) batchRmiService.getObject();
        metaFactoryService = (IMetaFactoryService) metaFactoryRmiService.getObject();
        userService = (PortalUserBeanRemoteBusiness) remoteUserService.getObject();
    }

    @Override
    public IEntityService getEntityService() {
        return entityService;
    }

    @Override
    public IBatchService getBatchService() {
        return batchService;
    }

    @Override
    public IMetaFactoryService getMetaFactoryService() {
        return metaFactoryService;
    }

    @Override
    public PortalUserBeanRemoteBusiness getUserService()
    {
        return userService;
    }
}

