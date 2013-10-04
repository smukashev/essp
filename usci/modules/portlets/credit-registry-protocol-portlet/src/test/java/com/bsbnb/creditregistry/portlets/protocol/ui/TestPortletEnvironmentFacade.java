package com.bsbnb.creditregistry.portlets.protocol.ui;

import com.bsbnb.creditregistry.portlets.protocol.PortletEnvironmentFacade;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TestPortletEnvironmentFacade extends PortletEnvironmentFacade{
    
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
    public String getResourceString(String key) {
        return bundle.getString(key);
    }

    @Override
    public long getUserID() {
        return 10169;
    }

    @Override
    public boolean isLanguageKazakh() {
        return false;
    }
    
}
