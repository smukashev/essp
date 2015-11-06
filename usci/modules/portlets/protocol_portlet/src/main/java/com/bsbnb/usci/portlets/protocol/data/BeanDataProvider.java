package com.bsbnb.usci.portlets.protocol.data;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import com.bsbnb.usci.portlets.protocol.PortletEnvironmentFacade;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Link;
import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {

    private ProtocolBeanRemoteBusiness protocolBusiness;
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputFileBeanRemoteBusiness inputFileBusiness;

    private static final String ENTITY_EDITOR_PAGE = "http://localhost:8081/entity_editor";

    public BeanDataProvider() {
        initializeBeans();
    }

    private void initializeBeans() {
        RmiProxyFactoryBean protocolBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        protocolBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/protocolBeanRemoteBusiness");
        protocolBeanRemoteBusinessFactoryBean.setServiceInterface(ProtocolBeanRemoteBusiness.class);

        protocolBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        protocolBusiness = (ProtocolBeanRemoteBusiness) protocolBeanRemoteBusinessFactoryBean.getObject();

        RmiProxyFactoryBean inputInfoBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputInfoBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/inputInfoBeanRemoteBusiness");
        inputInfoBeanRemoteBusinessFactoryBean.setServiceInterface(InputInfoBeanRemoteBusiness.class);

        inputInfoBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputInfoBusiness = (InputInfoBeanRemoteBusiness) inputInfoBeanRemoteBusinessFactoryBean.getObject();

        RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        RmiProxyFactoryBean inputFileBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
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
        List<ProtocolDisplayBean> result = new ArrayList<>();

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String sRepDate = df.format(inputInfo.getInputInfo().getReportDate());

        for (Protocol protocol : protocols) {
            ProtocolDisplayBean pr = new ProtocolDisplayBean(protocol);
            if (protocol.getMessageType().getNameRu().equals("COMPLETED"))
                pr.setLink(new Link("Просмотр",
                        new ExternalResource(ENTITY_EDITOR_PAGE + "?entityId=" +
                        protocol.getNote() + "&repDate=" + sRepDate)));

            result.add(pr);
        }
        return result;
    }

    public List<InputInfoDisplayBean> getInputInfosByCreditors(List<Creditor> creditors, Date reportDate) {
        List<InputInfo> inputInfoList = inputInfoBusiness.getAllInputInfos(creditors,reportDate);
        List<InputInfoDisplayBean> result = new ArrayList<>(inputInfoList.size());
        for (InputInfo inputInfo : inputInfoList) {
            if(inputInfo != null && inputInfo.getCreditor() != null)
                result.add(new InputInfoDisplayBean(inputInfo,this));
        }
        return result;
    }

    public Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> getProtocolsByInputInfoGrouped(
            InputInfoDisplayBean inputInfo) {
        List<ProtocolDisplayBean> list = getProtocolsByInputInfo(inputInfo);
        Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> result = new LinkedHashMap<>(list.size());
        for (ProtocolDisplayBean protocol : list) {
            Map<String, List<ProtocolDisplayBean>> innerMap;
            SharedDisplayBean shared = new SharedDisplayBean(protocol.getType());
            if (result.containsKey(shared)) {
                innerMap = result.get(shared);
            } else {
                innerMap = new LinkedHashMap<>();
                result.put(shared, innerMap);
            }
            List<ProtocolDisplayBean> innerList;
            if (innerMap.containsKey(protocol.getDescription())) {
                innerList = innerMap.get(protocol.getDescription());
            } else {
                innerList = new ArrayList<>();
                innerMap.put(protocol.getDescription(), innerList);
            }
            innerList.add(protocol);
        }
        return result;
    }

    public BatchFullJModel getBatchFullModel(BigInteger batchId) {
        return inputInfoBusiness.getBatchFullModel(batchId);
    }
}
