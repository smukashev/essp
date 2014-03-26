package com.bsbnb.creditregistry.portlets.protocol.data;

//import com.bsbnb.creditregistry.ejb.ref.exception.ResultInconsistentException;
//import com.bsbnb.creditregistry.ejb.ref.exception.ResultNotFoundException;
import java.util.Date;
import java.util.LinkedHashMap;
//import com.bsbnb.creditregistry.ejb.api.maintenance.InputFileBeanRemoteBusiness;
import java.util.ArrayList;
//import com.bsbnb.creditregistry.ejb.api.maintenance.InputInfoBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.ProtocolBeanRemoteBusiness;
import com.bsbnb.creditregistry.portlets.protocol.PortletEnvironmentFacade;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Link;
import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Protocol;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {
    private RmiProxyFactoryBean protocolBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean inputInfoBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean inputFileBeanRemoteBusinessFactoryBean;

    private ProtocolBeanRemoteBusiness protocolBusiness;
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputFileBeanRemoteBusiness inputFileBusiness;

    public BeanDataProvider() {
        initializeBeans();
    }

    private void initializeBeans() {
        protocolBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        protocolBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/protocolBeanRemoteBusiness");
        protocolBeanRemoteBusinessFactoryBean.setServiceInterface(ProtocolBeanRemoteBusiness.class);

        protocolBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        protocolBusiness = (ProtocolBeanRemoteBusiness) protocolBeanRemoteBusinessFactoryBean.getObject();

        //////////////////////////////

        inputInfoBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputInfoBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/inputInfoBeanRemoteBusiness");
        inputInfoBeanRemoteBusinessFactoryBean.setServiceInterface(InputInfoBeanRemoteBusiness.class);

        inputInfoBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputInfoBusiness = (InputInfoBeanRemoteBusiness) inputInfoBeanRemoteBusinessFactoryBean.getObject();
        if (inputInfoBusiness == null)
        {
            System.out.println("InputInfoBusiness is null!");
        }

        //////////////////////////////

        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        //////////////////////////////

        inputFileBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputFileBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/inputFileBeanRemoteBusiness");
        inputFileBeanRemoteBusinessFactoryBean.setServiceInterface(InputFileBeanRemoteBusiness.class);

        inputFileBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputFileBusiness = (InputFileBeanRemoteBusiness) inputFileBeanRemoteBusinessFactoryBean.getObject();
    }

    public List<Creditor> getCreditorsList() {
        return portalUserBusiness.getMainCreditorsInAlphabeticalOrder(PortletEnvironmentFacade.get().getUserID());
    }

    public List<ProtocolDisplayBean> getProtocolsByInputInfo(InputInfoDisplayBean inputInfo) {
        List<Protocol> protocols = protocolBusiness.getProtocolsBy_InputInfo(inputInfo.getInputInfo());
        System.out.println("Protocols count: " + protocols.size());
        List<ProtocolDisplayBean> result = new ArrayList<ProtocolDisplayBean>();
        for (Protocol protocol : protocols) {
            ProtocolDisplayBean pr = new ProtocolDisplayBean(protocol);
            if (protocol.getMessageType().getNameRu().equals("COMPLETED"))
                pr.setLink(new Link("Просмотр",
                        new ExternalResource("https://170.7.15.117/web/guest/31?entityId=" +
                        protocol.getMessage().getNameRu())));

            result.add(pr);
        }
        return result;
    }

    public List<InputInfoDisplayBean> getInputInfosByCreditors(List<Creditor> creditors, Date reportDate) {
        List<InputInfo> inputInfoList = inputInfoBusiness.getAllInputInfosBy_Creditors_By_RepDateSortedBy_Id_Desc(creditors,reportDate);
        List<InputInfoDisplayBean> result = new ArrayList<InputInfoDisplayBean>(inputInfoList.size());
        for (InputInfo inputInfo : inputInfoList) {
                result.add(new InputInfoDisplayBean(inputInfo,this));
        }
        return result;
    }

    public Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> getProtocolsByInputInfoGrouped(InputInfoDisplayBean inputInfo) {
        List<ProtocolDisplayBean> list = getProtocolsByInputInfo(inputInfo);
        Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> result = new LinkedHashMap<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>>(list.size());
        for (ProtocolDisplayBean protocol : list) {
            Map<String, List<ProtocolDisplayBean>> innerMap = null;
            SharedDisplayBean shared = new SharedDisplayBean(protocol.getType());
            if (result.containsKey(shared)) {
                innerMap = result.get(shared);
            } else {
                innerMap = new LinkedHashMap<String, List<ProtocolDisplayBean>>();
                result.put(shared, innerMap);
            }
            List<ProtocolDisplayBean> innerList = null;
            if (innerMap.containsKey(protocol.getDescription())) {
                innerList = innerMap.get(protocol.getDescription());
            } else {
                innerList = new ArrayList<ProtocolDisplayBean>();
                innerMap.put(protocol.getDescription(), innerList);
            }
            innerList.add(protocol);
        }
        return result;
    }

    public InputFile getFileByInputInfo(InputInfoDisplayBean inputInfo) {
        return inputFileBusiness.getInputFileByInputInfo(inputInfo.getInputInfo());
    }
}
