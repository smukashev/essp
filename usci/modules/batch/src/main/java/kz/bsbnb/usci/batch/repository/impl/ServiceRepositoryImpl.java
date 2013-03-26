package kz.bsbnb.usci.batch.repository.impl;

import kz.bsbnb.usci.batch.repository.IServiceRepository;
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

    private IEntityService entityService;
    private IBatchService batchService;
    private IMetaFactoryService metaFactoryService;

    @PostConstruct
    public void init() {
        entityService = (IEntityService) entityRmiService.getObject();
        batchService = (IBatchService) batchRmiService.getObject();
        metaFactoryService = (IMetaFactoryService) metaFactoryRmiService.getObject();
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
}
