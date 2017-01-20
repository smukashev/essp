package kz.bsbnb.usci.receiver.repository;

import kz.bsbnb.usci.core.service.IGlobalService;
import kz.bsbnb.usci.core.service.MailMessageBeanCommonBusiness;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.RemoteCreditorBusiness;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import kz.bsbnb.usci.sync.service.ReportBeanRemoteBusiness;

/**
 * @author k.tulbassiyev
 */
public interface IServiceRepository {
    IEntityService getEntityService();

    IBatchService getBatchService();

    IMetaFactoryService getMetaFactoryService();

    PortalUserBeanRemoteBusiness getUserService();

    RemoteCreditorBusiness getRemoteCreditorBusiness();

    ReportBeanRemoteBusiness getReportBeanRemoteBusinessService();

    MailMessageBeanCommonBusiness getMailMessageBeanCommonBusiness();

    IGlobalService getGlobalService();

    void batchFinishedInReader(Long batchId);
}
