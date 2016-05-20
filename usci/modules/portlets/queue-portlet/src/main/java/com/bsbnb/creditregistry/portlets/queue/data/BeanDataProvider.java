package com.bsbnb.creditregistry.portlets.queue.data;

import com.bsbnb.creditregistry.portlets.queue.thread.ConfigurationException;
import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.util.QueueOrderType;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import org.apache.log4j.Logger;
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
    private RemoteCreditorBusiness creditorBusiness;
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private IGlobalService globalService;
    private IBatchProcessService batchProcessService;
    public final Logger logger = Logger.getLogger(BeanDataProvider.class);

    public BeanDataProvider() {
        try {
            RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/portalUserBeanRemoteBusiness");
            portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

            portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

            RmiProxyFactoryBean remoteCreditorBusinessFactoryBean = new RmiProxyFactoryBean();
            remoteCreditorBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/remoteCreditorBusiness");
            remoteCreditorBusinessFactoryBean.setServiceInterface(RemoteCreditorBusiness.class);

            remoteCreditorBusinessFactoryBean.afterPropertiesSet();
            creditorBusiness = (RemoteCreditorBusiness) remoteCreditorBusinessFactoryBean.getObject();

            RmiProxyFactoryBean protocolBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            protocolBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/protocolBeanRemoteBusiness");
            protocolBeanRemoteBusinessFactoryBean.setServiceInterface(ProtocolBeanRemoteBusiness.class);

            protocolBeanRemoteBusinessFactoryBean.afterPropertiesSet();

            RmiProxyFactoryBean inputInfoBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            inputInfoBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/inputInfoBeanRemoteBusiness");
            inputInfoBeanRemoteBusinessFactoryBean.setServiceInterface(InputInfoBeanRemoteBusiness.class);

            inputInfoBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            inputInfoBusiness = (InputInfoBeanRemoteBusiness) inputInfoBeanRemoteBusinessFactoryBean.getObject();

            portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/portalUserBeanRemoteBusiness");
            portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

            portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

            RmiProxyFactoryBean inputFileBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            inputFileBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/inputFileBeanRemoteBusiness");
            inputFileBeanRemoteBusinessFactoryBean.setServiceInterface(InputFileBeanRemoteBusiness.class);

            inputFileBeanRemoteBusinessFactoryBean.afterPropertiesSet();

            RmiProxyFactoryBean globalServiceFactoryBean = new RmiProxyFactoryBean();
            globalServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1099/globalService");
            globalServiceFactoryBean.setServiceInterface(IGlobalService.class);

            globalServiceFactoryBean.afterPropertiesSet();
            globalService = (IGlobalService) globalServiceFactoryBean.getObject();

            RmiProxyFactoryBean batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
            batchProcessServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1097/batchProcessService");
            batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);

            batchProcessServiceFactoryBean.afterPropertiesSet();
            batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(Errors.getError(Errors.E286));
        }
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

    @Override
    public String getConfig(String type, String code) throws ConfigurationException {
        return globalService.getValue(type, code);
    }

    @Override
    public void saveConfig(EavGlobal global) throws ConfigurationException {
        globalService.updateValue(global);
        batchProcessService.reloadJobLauncherConfig();
    }

    @Override
    public List<QueueFileInfo> getPreviewQueue(List<Creditor> creditors, List<Integer> selectedCreditorIds,
                                               QueueOrderType selectedOrder) {
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

            try {
                file.setStatus(ii.getStatus().getNameRu());
            } catch (Exception e) {
                file.setStatus("В очереди");
            }

            file.setFilePath(ii.getFileName());
            file.setReceiverDate(ii.getReceiverDate());
            file.setFilename(ii.getFileName() == null ? "" : new File(ii.getFileName()).getName());
            queue.add(file);
        }

        return queue;
    }

    @Override
    public BatchFullJModel getBatchFullModel(BigInteger batchId) {
        return inputInfoBusiness.getBatchFullModel(batchId);
    }

    @Override
    public List<InputInfoDisplayBean> getMaintenanceInfo(List<Creditor> creditors, Date reportDate) {
        List<InputInfo> inputInfoList = inputInfoBusiness.getMaintenanceInfo(creditors,reportDate);

        List<InputInfoDisplayBean> result = new ArrayList<>(inputInfoList.size());
        for (InputInfo inputInfo : inputInfoList) {
            if(inputInfo != null && inputInfo.getCreditor() != null)
                result.add(new InputInfoDisplayBean(inputInfo,this));
        }
        return result;
    }
}
