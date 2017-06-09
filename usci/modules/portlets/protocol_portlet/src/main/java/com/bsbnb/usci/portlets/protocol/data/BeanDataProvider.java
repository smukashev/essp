package com.bsbnb.usci.portlets.protocol.data;

import com.bsbnb.usci.portlets.protocol.PortletEnvironmentFacade;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Link;
import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {
    private ProtocolBeanRemoteBusiness protocolBusiness;
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private boolean isNb;

    private static final String ENTITY_EDITOR_PAGE = "http://" + StaticRouter.getPortalUrl() + ":" +
            StaticRouter.getPortalPort() + "/entity_editor";
    public final Logger logger = Logger.getLogger(BeanDataProvider.class);

    public BeanDataProvider() {
        initializeBeans();
    }

    private void initializeBeans() {
        try {
            RmiProxyFactoryBean protocolBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            protocolBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/protocolBeanRemoteBusiness");
            protocolBeanRemoteBusinessFactoryBean.setServiceInterface(ProtocolBeanRemoteBusiness.class);

            protocolBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            protocolBusiness = (ProtocolBeanRemoteBusiness) protocolBeanRemoteBusinessFactoryBean.getObject();

            RmiProxyFactoryBean inputInfoBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            inputInfoBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/inputInfoBeanRemoteBusiness");
            inputInfoBeanRemoteBusinessFactoryBean.setServiceInterface(InputInfoBeanRemoteBusiness.class);

            inputInfoBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            inputInfoBusiness = (InputInfoBeanRemoteBusiness) inputInfoBeanRemoteBusinessFactoryBean.getObject();

            RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/portalUserBeanRemoteBusiness");
            portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

            portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(Errors.getError(Errors.E286));
        }
    }

    public List<Creditor> getCreditorsList() {
        return portalUserBusiness.getMainCreditorsInAlphabeticalOrder(PortletEnvironmentFacade.get().getUserID());
    }

    public List<ProtocolDisplayBean> getProtocolsByInputInfo(InputInfoDisplayBean inputInfo) {
        List<Protocol> protocols = protocolBusiness.getProtocolsBy_InputInfo(inputInfo.getInputInfo());
        List<ProtocolDisplayBean> result = new ArrayList<>();

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Date reportDate = inputInfo.getInputInfo().getReportDate();
        String sRepDate = (reportDate == null) ? "" : df.format(reportDate);

        for (Protocol protocol : protocols) {
            ProtocolDisplayBean pr = new ProtocolDisplayBean(protocol);
            if (protocol.getMessageType().getCode().equals("COMPLETED"))
                pr.setLink(new Link("Просмотр",
                        new ExternalResource(ENTITY_EDITOR_PAGE + "?entityId=" +
                        protocol.getNote() + "&repDate=" + sRepDate)));

            result.add(pr);
        }
        return result;
    }

    public String cutGodMode(String filename) {
        for (String tmpStr : StaticRouter.getGODModes()) {
            if (filename.indexOf(tmpStr) >= 0) {
                filename = filename.substring(0, filename.indexOf(tmpStr)) + filename.substring(filename.indexOf(tmpStr) + tmpStr.length());
            }
        }

        return filename;
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

    @Override
    public int countFiles(List<Creditor> selectedCreditors, Date date) {
        return inputInfoBusiness.countInputInfos(selectedCreditors,date);
    }

    @Override
    public List<InputInfoDisplayBean> loadFiles(List<Creditor> creditors, Date reportDate, int firstIndex, int count) {
        List<InputInfo> inputInfoList = inputInfoBusiness.getAllInputInfos(creditors, reportDate, firstIndex, count);
        List<InputInfoDisplayBean> result = new ArrayList<>(inputInfoList.size());
        for (InputInfo inputInfo : inputInfoList) {
            if (inputInfo != null && inputInfo.getCreditor() != null && !StaticRouter.isDEVILMode(inputInfo.getFileName())) {
                if (StaticRouter.isGODMode(inputInfo.getFileName())) {
                    inputInfo.setFileName(cutGodMode(inputInfo.getFileName()));
                }
                result.add(new InputInfoDisplayBean(inputInfo, this));
            }
        }
        return result;
    }


    @Override
    public List<ProtocolDisplayBean> getProtocolStatisticsByInputInfo(InputInfoDisplayBean inputInfo)
    {
        List<Protocol> protocols = protocolBusiness.getProtocolStatisticsBy_InputInfo(inputInfo.getInputInfo());
        List<ProtocolDisplayBean> result = new ArrayList<>();

        for (Protocol protocol : protocols) {
            result.add(new ProtocolDisplayBean(protocol));
        }

        return result;
    }

    @Override
    public void setNb(boolean nb) {
        isNb = nb;
    }

    public boolean isNb() {
        return isNb;
    }

    @Override
    public String getSignatureInformation(InputInfoDisplayBean ii) {
        return inputInfoBusiness.getSignatureInfo(ii.getInputInfo());
    }
}
