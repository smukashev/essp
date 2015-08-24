package com.bsbnb.usci.portlets.crosscheck.ui;

import com.bsbnb.usci.portlets.crosscheck.PortletEnvironmentFacade;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TestPortletEnvironmentFacade extends PortletEnvironmentFacade {
    
    private Locale locale;
    private ResourceBundle bundle;
    
    public TestPortletEnvironmentFacade() {
        this("ru");
    }
    
    public TestPortletEnvironmentFacade(String language) {
        locale = new Locale(language);
        bundle = ResourceBundle.getBundle("content.Language", locale);
    }

    @Override
    public String getCurrentLanguage() {
        return locale.getLanguage();
    }

    @Override
    public String getResourceString(String key) {
        return bundle.getString(key);
    }

    @Override
    public long getUserID() {
        return 10169;
    }
    
    @Override
    public String getBusinessRulesUrl() {
        return "dummyUrl";
    }

    public Date getRepDate() { return new Date(); }

    public String getCreditorId() { return "10196";}
}
