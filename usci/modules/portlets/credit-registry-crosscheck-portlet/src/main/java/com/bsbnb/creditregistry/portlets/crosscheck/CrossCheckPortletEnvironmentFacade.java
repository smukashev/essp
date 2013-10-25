package com.bsbnb.creditregistry.portlets.crosscheck;

import java.util.Locale;
import java.util.ResourceBundle;

import com.liferay.portal.model.User;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CrossCheckPortletEnvironmentFacade extends PortletEnvironmentFacade{
    private static final String BUNDLE_NAME = "content.Language";
    private ResourceBundle bundle;
    private User user;
    private String businessRulesUrl;
    
    public CrossCheckPortletEnvironmentFacade(User user, String businessRulesUrl) {
        this.user = user;
        this.businessRulesUrl = businessRulesUrl;
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, user==null ? new Locale("ru", "RU") : user.getLocale());
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
    
}
