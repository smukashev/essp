package com.bsbnb.creditregistry.portlets.approval.ui;

import com.bsbnb.creditregistry.portlets.approval.PortletEnvironmentFacade;
import com.liferay.portal.model.User;
import kz.bsbnb.usci.eav.util.Errors;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TestPortletEnvironmentFacade implements PortletEnvironmentFacade {

    private Locale locale;
    private ResourceBundle bundle;
    
    private boolean isNbUser = false;
    private boolean isBankUser = false;

    public TestPortletEnvironmentFacade() {
        this("ru");
    }

    public TestPortletEnvironmentFacade(String language) {
        locale = new Locale("ru", "RU");
        bundle = ResourceBundle.getBundle("content.Language", locale);
    }

    @Override
    public String getResourceString(Localization localization) {
        return bundle.getString(localization.getKey());
    }

    @Override
    public long getUserID() {
        return 10169;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public boolean isLanguageKazakh() {
        return false;
    }

    @Override
    public boolean isNbUser() {
        return isNbUser;
    }

    @Override
    public boolean isBankUser() {
        return isBankUser;
    }
    
    public void setIsNbUser(boolean isNbUser) {
        this.isNbUser = isNbUser;
    }
    
    public void setIsBankUser(boolean isBankUser) {
        this.isBankUser = isBankUser;
    }

    public List<User> getPortalUsers() {
        throw new UnsupportedOperationException(Errors.compose(Errors.E206));
    }

    public String getUsername() {
        throw new UnsupportedOperationException(Errors.compose(Errors.E206));
    }

    public boolean isApprovalAuthority() {
        throw new UnsupportedOperationException(Errors.compose(Errors.E206));
    }

    public boolean isAdministrator() {
        throw new UnsupportedOperationException(Errors.compose(Errors.E206));
    }
}
