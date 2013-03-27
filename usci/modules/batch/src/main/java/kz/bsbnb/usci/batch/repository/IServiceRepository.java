package kz.bsbnb.usci.batch.repository;

import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;

/**
 * @author k.tulbassiyev
 */
public interface IServiceRepository {
    public IEntityService getEntityService();
    public IBatchService getBatchService();
    public IMetaFactoryService getMetaFactoryService();
}
