package kz.bsbnb.usci.batch.factory;

import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;

/**
 * @author k.tulbassiyev
 */
public interface IServiceFactory
{
    public IEntityService getEntityService();
    public IBatchService getBatchService();
    public IMetaFactoryService getMetaFactoryService();
}
