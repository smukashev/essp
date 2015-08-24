package com.bsbnb.usci.portlets.crosscheck;

import com.liferay.portal.model.User;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CrossCheckPortletEnvironmentFacade extends PortletEnvironmentFacade{
    private static final String BUNDLE_NAME = "content.Language";
    private ResourceBundle bundle;
    private User user;
    private String businessRulesUrl;
    private Date repDate;
    private String CreditorId;
    
    public CrossCheckPortletEnvironmentFacade(User user, String businessRulesUrl, Date repDate, String CreditorId) {
        this.user = user;
        this.businessRulesUrl = businessRulesUrl;
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, user==null ? new Locale("ru", "RU") : user.getLocale());
        this.repDate=repDate;
        this.CreditorId=CreditorId;
    }

    @Override
    public String getCurrentLanguage() {
        return user==null ? "" : user.getLocale().getLanguage();
    }

    @Override
    public String getResourceString(String key) {
        return bundle.getString(key);
    }

    @Override
    public long getUserID() {
        return user==null ? 0 : user.getUserId();
    }

    @Override
    public String getBusinessRulesUrl() {
        return businessRulesUrl;
    }

    public Date getRepDate() {
        return repDate;
    }

    public String getCreditorId() {
        return CreditorId;
    }
}
