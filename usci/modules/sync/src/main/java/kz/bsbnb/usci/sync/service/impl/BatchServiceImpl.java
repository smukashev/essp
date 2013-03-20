package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.core.Main;
import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author k.tulbassiyev
 */
@Service
public class BatchServiceImpl implements IBatchService
{
    @Autowired
    @Qualifier(value = "batchService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    kz.bsbnb.usci.core.service.IBatchService remoteBatchService;

    @PostConstruct
    public void init()
    {
        remoteBatchService = (kz.bsbnb.usci.core.service.IBatchService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public long save(Batch batch)
    {
        return remoteBatchService.save(batch);
    }

    @Override
    public Batch load(long batchId)
    {
        return remoteBatchService.load(batchId);
    }
}
