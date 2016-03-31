package com.bsbnb.creditregistry.portlets.approval;

import com.bsbnb.creditregistry.portlets.approval.ui.Localization;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ApprovalPortletEnvironmentFacade implements PortletEnvironmentFacade {

    private static final String BUNDLE_NAME = "content.Language";
    private final ResourceBundle bundle;
    private final boolean isKazakh;
    private final User user;
    private boolean isBankUser;
    private boolean isNbUser;
    private boolean isApprovalAuthority;
    private boolean isAdministrator;
    private final Logger logger = org.apache.log4j.Logger.getLogger(ApprovalPortletEnvironmentFacade.class);

    public ApprovalPortletEnvironmentFacade(User user) {
        this.user = user;
        readUserProperties(user);
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, user.getLocale());
        isKazakh = "kz".equals(user.getLocale().getLanguage());
    }

    private void readUserProperties(User user) {
        isBankUser = false;
        isNbUser = false;
        isApprovalAuthority = false;
        isAdministrator = false;
        try {
            for (Role role : user.getRoles()) {
                if ("BankUser".equalsIgnoreCase(role.getName())) {
                    isBankUser = true;
                }
                if("ApprovingAuthority".equalsIgnoreCase(role.getName())) {
                    isApprovalAuthority = true;
                }
                if("Administrator".equalsIgnoreCase(role.getName())) {
                    isAdministrator = true;
                }
            }
            isNbUser = ExpandoValueLocalServiceUtil.getData(user.getCompanyId(), User.class.getName(),
                    ExpandoTableConstants.DEFAULT_TABLE_NAME, "isNb", user.getPrimaryKey(), false);
        } catch (PortalException pe) {
            logger.error(null,pe);
        } catch (SystemException se) {
            logger.error(null,se);
        }
    }

    @Override
    public String getResourceString(Localization localization) {
        return bundle.getString(localization.getKey());
    }

    @Override
    public long getUserID() {
        return user.getUserId();
    }

    @Override
    public boolean isLanguageKazakh() {
        return isKazakh;
    }

    @Override
    public boolean isNbUser() {
        return isNbUser;
    }

    @Override
    public boolean isBankUser() {
        return isBankUser;
    }
    
    @Override
    public boolean isApprovalAuthority() {
        return isApprovalAuthority;
    }
    
    @Override
    public boolean isAdministrator() {
        return isAdministrator;
    }

    @Override
    public List<User> getPortalUsers() {
        try {
            return UserLocalServiceUtil.getUsers(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
        } catch (SystemException se) {
            logger.error(null,se);
        }
        return null;
    }

    public String getUsername() {
        return user.getFullName();
    }
}
