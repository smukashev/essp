package com.bsbnb.creditregistry.portlets.administration.data;

import java.util.ArrayList;
import java.util.List;

//import com.bsbnb.creditregistry.dm.maintenance.PortalUser;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.ref.business.remote.IRemoteCreditorBusiness;
import static com.bsbnb.creditregistry.portlets.administration.AdministrationApplication.log;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.RemoteCreditorBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {
    private RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean remoteCreditorBusinessFactoryBean;

    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private RemoteCreditorBusiness creditorBusiness;

    public BeanDataProvider() {
        initializeBeans();
        synchronizePortalUsersWithDatabase();
    }

    private void initializeBeans() {
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
    }

    public List<Creditor> getAllCreditors() {
        List<Creditor> mainOfficeCreditors = creditorBusiness.findMainOfficeCreditors();
        log.info("Main office creditors count: "+mainOfficeCreditors.size());
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
                if(o1==null || o2==null) {
                    return 0;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return creditors;
    }

    private void synchronizePortalUsersWithDatabase() {
        try {
            List<User> liferayUserList = UserLocalServiceUtil.getUsers(0, UserLocalServiceUtil.getUsersCount());
            List<PortalUser> portalUserList = new ArrayList<PortalUser>(liferayUserList.size());
            for (User liferayUser : liferayUserList) {
                portalUserList.add(PortalUser.convert(liferayUser));
            }
            portalUserBusiness.synchronize(portalUserList);
        } catch (SystemException se) {
            log.error("Failed to retrieve users", se);
        } catch (Exception e) {
            log.error("Failed to synchronize portal users with database", e);
        }
    }
}
