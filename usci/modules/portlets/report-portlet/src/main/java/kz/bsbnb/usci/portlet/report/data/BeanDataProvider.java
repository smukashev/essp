package kz.bsbnb.usci.portlet.report.data;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Link;
import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;
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

    private static final Logger logger = Logger.getLogger(BeanDataProvider.class);
    private static final String ENTITY_EDITOR_PAGE = "http://" + StaticRouter.getPortalUrl() +
            "/ru/web/guest/entity_editor";

    public BeanDataProvider() {
        initializeBeans();
    }

    private void initializeBeans() {
        try {
            protocolBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            protocolBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/protocolBeanRemoteBusiness");

            protocolBeanRemoteBusinessFactoryBean.setServiceInterface(ProtocolBeanRemoteBusiness.class);

            protocolBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            protocolBusiness = (ProtocolBeanRemoteBusiness) protocolBeanRemoteBusinessFactoryBean.getObject();

            //////////////////////////////

            inputInfoBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            inputInfoBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/inputInfoBeanRemoteBusiness");
            inputInfoBeanRemoteBusinessFactoryBean.setServiceInterface(InputInfoBeanRemoteBusiness.class);

            inputInfoBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            inputInfoBusiness = (InputInfoBeanRemoteBusiness) inputInfoBeanRemoteBusinessFactoryBean.getObject();
            if (inputInfoBusiness == null) {
                logger.info("InputInfoBusiness is null!");
            }

            //////////////////////////////

            portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/portalUserBeanRemoteBusiness");
            portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

            portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

            //////////////////////////////

            inputFileBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            inputFileBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/inputFileBeanRemoteBusiness");
            inputFileBeanRemoteBusinessFactoryBean.setServiceInterface(InputFileBeanRemoteBusiness.class);

            inputFileBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            inputFileBusiness = (InputFileBeanRemoteBusiness) inputFileBeanRemoteBusinessFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(Errors.compose(Errors.E286,e));
        }
    }

    public List<Creditor> getCreditorsList() {
        return portalUserBusiness.getMainCreditorsInAlphabeticalOrder(10196);
    }

    public List<ProtocolDisplayBean> getProtocolsByInputInfo(InputInfoDisplayBean inputInfo) {
        List<Protocol> protocols = protocolBusiness.getProtocolsBy_InputInfo(inputInfo.getInputInfo());
        logger.info("Protocols count: " + protocols.size());
        List<ProtocolDisplayBean> result = new ArrayList<ProtocolDisplayBean>();

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String sRepDate = df.format(inputInfo.getInputInfo().getReportDate());

        for (Protocol protocol : protocols) {
            ProtocolDisplayBean pr = new ProtocolDisplayBean(protocol);
            if (protocol.getMessageType().getNameRu().equals("COMPLETED"))
                pr.setLink(new Link("Просмотр",
                        new ExternalResource(ENTITY_EDITOR_PAGE + "?entityId=" +
                                protocol.getMessage().getNameRu() + "&repDate=" + sRepDate)));

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

    public Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> getProtocolsByInputInfoGrouped(InputInfoDisplayBean inputInfo) {
        List<ProtocolDisplayBean> list = getProtocolsByInputInfo(inputInfo);
        Map<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>> result = new LinkedHashMap<SharedDisplayBean, Map<String, List<ProtocolDisplayBean>>>(list.size());
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

    public InputFile getFileByInputInfo(InputInfoDisplayBean inputInfo) {
        return inputFileBusiness.getInputFileByInputInfo(inputInfo.getInputInfo());
    }

    public BatchFullJModel getBatchFullModel(BigInteger batchId) {
        return inputInfoBusiness.getBatchFullModel(batchId);
    }
}

