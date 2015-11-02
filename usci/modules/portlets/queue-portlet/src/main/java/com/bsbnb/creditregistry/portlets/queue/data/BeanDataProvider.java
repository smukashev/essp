package com.bsbnb.creditregistry.portlets.queue.data;

import com.bsbnb.creditregistry.portlets.queue.thread.ConfigurationException;
import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.util.QueueOrderType;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

//import com.bsbnb.creditregistry.ejb.api.maintenance.InputInfoBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.ref.business.remote.IRemoteSharedBusiness;
//import com.bsbnb.creditregistry.ejb.ref.exception.ResultInconsistentException;
//import com.bsbnb.creditregistry.ejb.ref.exception.ResultNotFoundException;


import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;

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
    private RmiProxyFactoryBean globalServiceFactoryBean;
    private RmiProxyFactoryBean batchProcessServiceFactoryBean;

    private RemoteCreditorBusiness creditorBusiness;
    private ProtocolBeanRemoteBusiness protocolBusiness;
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputFileBeanRemoteBusiness inputFileBusiness;
    //private IRemoteSharedBusiness sharedBusiness;
    private IGlobalService globalService;
    private IBatchProcessService batchProcessService;



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

        globalServiceFactoryBean = new RmiProxyFactoryBean();
        globalServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/globalService");
        globalServiceFactoryBean.setServiceInterface(IGlobalService.class);

        globalServiceFactoryBean.afterPropertiesSet();
        globalService = (IGlobalService)globalServiceFactoryBean.getObject();

        batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
        batchProcessServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchProcessService");
        batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);

        batchProcessServiceFactoryBean.afterPropertiesSet();
        batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
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
        return convertInputInfo(queueList);
    }

    /*
    @Override
    public void rejectInputInfo(int inputInfoId) throws InputInfoNotInQueueException {

    }*/

    @Override
    public String getConfig(String type, String code) throws ConfigurationException {
        String value = globalService.getValue(type, code);
        return value;
    }

    @Override
    public void saveConfig(EavGlobal global) throws ConfigurationException {
        globalService.updateValue(global);
        batchProcessService.reloadJobLauncherConfig();
    }

    @Override
    public List<QueueFileInfo> getPreviewQueue(List<Creditor> creditors, List<Integer> selectedCreditorIds, QueueOrderType selectedOrder) {
        Set<Long> creditorsSet = new HashSet<>(selectedCreditorIds.size());
        for(Integer i : selectedCreditorIds) {
            creditorsSet.add(i.longValue());
        }
        List<InputInfo> infos = batchProcessService.getQueueListPreview(creditors, creditorsSet, selectedOrder);
        return convertInputInfo(infos);
    }

    List<QueueFileInfo> convertInputInfo(List<InputInfo> inputInfoList){
        List<QueueFileInfo> queue = new ArrayList<>();
        int i=0;
        for (InputInfo ii : inputInfoList) {
            QueueFileInfo file = new QueueFileInfo();
            file.setRownum(++i);
            file.setInputInfoId(ii.getId().intValue());
            file.setUserId(ii.getUserId().intValue());
            file.setProtocolCount(ii.getActualCount());
            file.setCreditorId(ii.getCreditor().getId().intValue());
            file.setCreditorName(ii.getCreditor().getName());
            // file.setStatusCode("AAAAA");
            try {
                file.setStatus(ii.getStatus().getNameRu());
            } catch (Exception e) {
                file.setStatus("В очереди");
            }
            file.setFilePath(ii.getFileName());
            file.setReceiverDate(ii.getReceiverDate());
            file.setFilename(ii.getFileName());
            //file.setFilename(new File(ii.getFileName()).getName());
            queue.add(file);
        }

        return queue;
    }
}
