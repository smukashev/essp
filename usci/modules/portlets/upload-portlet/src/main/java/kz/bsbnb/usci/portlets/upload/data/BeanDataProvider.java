package kz.bsbnb.usci.portlets.upload.data;

import kz.bsbnb.usci.core.service.IGlobalService;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.RemoteCreditorBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {
    private static final String UPLOADS_PATH_CONFIG_CODE = "UPLOADS_PATH";
    private static final String DEFAULT_UPLOADS_PATH = "D:\\portal_afn\\uploads\\";
    private static final String DIGITAL_SIGNING_SETTINGS = "DIGITAL_SIGNING_SETTINGS";
    private static final String DIGITAL_SIGNING_ORGANIZATIONS_IDS_CONFIG_CODE = "DIGITAL_SIGNING_ORGANIZATIONS_IDS";
    private static final String ORG_FIRST_DATE_SETTING = "ORG_FIRST_DATE_SETTING";
    private static final String DEFAULT_DATE_VALUE = "DEFAULT_DATE_VALUE";
    private static final String CREDITOR_DATES = "CREDITOR_DATES";

    private final Logger logger = Logger.getLogger(BeanDataProvider.class);

    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private RemoteCreditorBusiness creditorBusiness;
    private IGlobalService globalService;

    public BeanDataProvider() {
        initializeBeans();
    }

    private void initializeBeans() {
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

            RmiProxyFactoryBean globalServiceFactoryBean = new RmiProxyFactoryBean();
            globalServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1099/globalService");
            globalServiceFactoryBean.setServiceInterface(IGlobalService.class);
            globalServiceFactoryBean.afterPropertiesSet();
            globalService = (IGlobalService) globalServiceFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(Errors.compose(Errors.E286));
        }
    }

    @Override
    public String getUploadsPath() {
        return DEFAULT_UPLOADS_PATH;
    }

    @Override
    public void saveFile(long userId, boolean isSigning, Creditor creditor, String filename, byte[] content, String path) throws DatabaseException {
        // fixme!
        //InputInfoStatus status = isSigning ? InputInfoStatus.WAITING_FOR_SIGNATURE : InputInfoStatus.IN_QUEUE;
        //inputInfoBusiness.insert(userId, creditor, filename, status, path, hash(content));
    }

    @Override
    public List<Creditor> getUserCreditors(long userId) {
        return portalUserBusiness.getPortalUserCreditorList(userId);
    }

    @Override
    public List<Creditor> getOrganizations() {
        return creditorBusiness.findMainOfficeCreditors();
    }

    @Override
    public List<Integer> getIdsForOrganizationsUsingDigitalSigning() {
        ArrayList<Integer> result = new ArrayList<Integer>();
        try {
            String idsString = globalService.getValue(DIGITAL_SIGNING_SETTINGS, DIGITAL_SIGNING_ORGANIZATIONS_IDS_CONFIG_CODE);
            String[] ids = idsString.split(",");
            for (String id : ids) {
                result.add(Integer.parseInt(id));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        return result;
    }

    public void saveOrganizationsUsingDigitalSigning(List<Creditor> creditors) {
        StringBuilder idsStringBuilder = new StringBuilder("0");
        for (Creditor creditor : creditors) {
            idsStringBuilder.append(",");
            idsStringBuilder.append(creditor.getId());
        }
        try {
            globalService.update(DIGITAL_SIGNING_SETTINGS, DIGITAL_SIGNING_ORGANIZATIONS_IDS_CONFIG_CODE, idsStringBuilder.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    @Override
    public Map<Long, String> getOrganizationFirstDates() {
        Map<Long, String> firstDates = new HashMap<>();
        String defaultDate = globalService.getValue(ORG_FIRST_DATE_SETTING, DEFAULT_DATE_VALUE);
        List<Creditor> creditors = getOrganizations();

        try {
            String creditorDates = globalService.getValue(ORG_FIRST_DATE_SETTING, CREDITOR_DATES);
            String[] pairs = creditorDates.split(",");
            for(String pair: pairs) {
                String[] record = pair.split("=");
                Long creditorId = Long.parseLong(record[0]);
                String date = record[1];
                firstDates.put(creditorId, date);
            }

            for(Creditor creditor : creditors) {
                if(!firstDates.containsKey(creditor.getId()))
                    firstDates.put(creditor.getId(), defaultDate);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return firstDates;
    }

    @Override
    public Date getDefaultDate() throws ParseException {
        return new SimpleDateFormat("dd.MM.yyyy").parse(globalService.getValue(ORG_FIRST_DATE_SETTING, DEFAULT_DATE_VALUE));
    }

    @Override
    public void saveOrganizationFirstDates(String firstDateString) {
        globalService.update(ORG_FIRST_DATE_SETTING, CREDITOR_DATES, firstDateString);
    }
}
