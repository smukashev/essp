package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * @author k.tulbassiyev
 */
public class BatchService implements IBatchService
{
    @Autowired
    RmiProxyFactoryBean rmiProxyFactoryBean;

    @Override
    public long save(Batch batch)
    {
        throw new IllegalStateException();
    }
}
