package com.bsbnb.creditregistry.portlets.administration.data;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.RemoteCreditorBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private RemoteCreditorBusiness creditorBusiness;
    private final Logger logger = Logger.getLogger(BeanDataProvider.class);

    public BeanDataProvider() {
        initializeBeans();
        try{
            synchronizePortalUsersWithDatabase();
        }catch (Exception e){
            logger.error(Errors.decompose(e.toString()));
        }
    }

    private void initializeBeans() {
        try {
            RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/portalUserBeanRemoteBusiness");
            portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

            portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

            RmiProxyFactoryBean remoteCreditorBusinessFactoryBean = new RmiProxyFactoryBean();
            remoteCreditorBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/remoteCreditorBusiness");
            remoteCreditorBusinessFactoryBean.setServiceInterface(RemoteCreditorBusiness.class);

            remoteCreditorBusinessFactoryBean.afterPropertiesSet();
            creditorBusiness = (RemoteCreditorBusiness) remoteCreditorBusinessFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(Errors.getError(Errors.E286));
        }
    }

    public List<Creditor> getAllCreditors() {
        List<Creditor> mainOfficeCreditors = creditorBusiness.findMainOfficeCreditors();
        logger.info("Main office creditors count: " + mainOfficeCreditors.size());
        return mainOfficeCreditors;
    }

    public void addUserCreditor(User user, Creditor creditor) {
        if (!portalUserBusiness.hasPortalUserCreditor(user.getUserId(), creditor.getId())) {
            portalUserBusiness.setPortalUserCreditors(user.getUserId(), creditor.getId());
        }
    }

    public void removeUserCreditor(User user, Creditor creditor) {
        if (portalUserBusiness.hasPortalUserCreditor(user.getUserId(), creditor.getId())) {
            portalUserBusiness.unsetPortalUserCreditors(user.getUserId(), creditor.getId());
        }
    }

    public List<Creditor> getUsersCreditors(User user) {
        List<Creditor> creditors = portalUserBusiness.getPortalUserCreditorList(user.getUserId());
        Collections.sort(creditors, new Comparator<Creditor>() {

            public int compare(Creditor o1, Creditor o2) {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return creditors;
    }

    private PortalUser convert(User liferayUser) throws IllegalArgumentException {
        if (liferayUser == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E200));

        PortalUser portalUser = new PortalUser();
        portalUser.setUserId(liferayUser.getUserId());
        portalUser.setEmailAddress(liferayUser.getEmailAddress());
        portalUser.setModifiedDate(liferayUser.getModifiedDate());
        portalUser.setFirstName(liferayUser.getFirstName());
        portalUser.setLastName(liferayUser.getLastName());
        portalUser.setMiddleName(liferayUser.getMiddleName());
        portalUser.setScreenName(liferayUser.getScreenName());
        portalUser.setActive(liferayUser.isActive());

        return portalUser;
    }

    private void synchronizePortalUsersWithDatabase() {
        try {
            List<User> liferayUserList = UserLocalServiceUtil.getUsers(0, UserLocalServiceUtil.getUsersCount());
            List<PortalUser> portalUserList = new ArrayList<>(liferayUserList.size());
            for (User liferayUser : liferayUserList) {
                portalUserList.add(convert(liferayUser));
            }
            portalUserBusiness.synchronize(portalUserList);
        } catch (SystemException se) {
            logger.error("Failed to retrieve users", se);
        } catch (Exception e) {
            logger.error("Failed to synchronize portal users with database", e);
        }
    }
}
