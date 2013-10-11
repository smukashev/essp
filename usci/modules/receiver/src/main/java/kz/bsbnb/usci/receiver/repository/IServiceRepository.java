package kz.bsbnb.usci.receiver.repository;

import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
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
    public PortalUserBeanRemoteBusiness getUserService();
}
