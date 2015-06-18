package com.bsbnb.creditregistry.portlets.queue.data;

import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.math.BigDecimal;
import java.util.*;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {

    private RmiProxyFactoryBean protocolBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean inputInfoBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean inputFileBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean remoteCreditorBusinessFactoryBean;

    private RemoteCreditorBusiness creditorBusiness;
    private ProtocolBeanRemoteBusiness protocolBusiness;
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputFileBeanRemoteBusiness inputFileBusiness;

    public BeanDataProvider() {
        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        remoteCreditorBusinessFactoryBean = new RmiProxyFactoryBean();
        remoteCreditorBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/remoteCreditorBusiness");
        remoteCreditorBusinessFactoryBean.setServiceInterface(RemoteCreditorBusiness.class);

        remoteCreditorBusinessFactoryBean.afterPropertiesSet();
        creditorBusiness = (RemoteCreditorBusiness) remoteCreditorBusinessFactoryBean.getObject();

        protocolBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        protocolBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/protocolBeanRemoteBusiness");
        protocolBeanRemoteBusinessFactoryBean.setServiceInterface(ProtocolBeanRemoteBusiness.class);

        protocolBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        protocolBusiness = (ProtocolBeanRemoteBusiness) protocolBeanRemoteBusinessFactoryBean.getObject();

        inputInfoBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputInfoBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/inputInfoBeanRemoteBusiness");
        inputInfoBeanRemoteBusinessFactoryBean.setServiceInterface(InputInfoBeanRemoteBusiness.class);

        inputInfoBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputInfoBusiness = (InputInfoBeanRemoteBusiness) inputInfoBeanRemoteBusinessFactoryBean.getObject();

        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        inputFileBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputFileBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/inputFileBeanRemoteBusiness");
        inputFileBeanRemoteBusinessFactoryBean.setServiceInterface(InputFileBeanRemoteBusiness.class);

        inputFileBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputFileBusiness = (InputFileBeanRemoteBusiness) inputFileBeanRemoteBusinessFactoryBean.getObject();
    }

    @Override
    public List<Creditor> getCreditors(long userId, boolean isUserAdmin) {
        if (!isUserAdmin) {
            return portalUserBusiness.getPortalUserCreditorList(userId);
        } else {
            return creditorBusiness.findMainOfficeCreditors();
        }
    }

    @Override
    public List<QueueFileInfo> getQueue(List<Creditor> creditors) {
        List<InputInfo> queueList = inputInfoBusiness.getPendingBatches(creditors);

        List<QueueFileInfo> queue = new ArrayList<>();

        for (InputInfo ii : queueList) {
            QueueFileInfo file = new QueueFileInfo();
            file.setRownum(0);
            file.setInputInfoId(ii.getId().intValue());
            file.setUserId(ii.getUserId().intValue());
            file.setProtocolCount(0);
            file.setCreditorId(ii.getCreditor().getId().intValue());
            file.setCreditorName(ii.getCreditor().getName());
            // file.setStatusCode("AAAAA");
            file.setStatus(ii.getStatus().getNameRu());
            file.setFilePath(ii.getFileName());
            file.setReceiverDate(ii.getReceiverDate());
            file.setFilename(ii.getFileName());
            queue.add(file);
        }

        return queue;
    }
}
