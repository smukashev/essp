/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.creditregistry.portlets.upload.ui;

import com.bsbnb.creditregistry.portlets.upload.PortletEnvironmentFacade;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TestPortletEnvironmentFacade implements PortletEnvironmentFacade {

    private ResourceBundle bundle;

    public TestPortletEnvironmentFacade(Locale userLocale) {
        bundle = ResourceBundle.getBundle("content.Language", userLocale);
    }

    public String getResourceString(String key) {
        return bundle.getString(key);
    }

    public long getUserID() {
        return 0;
    }

    public boolean isLanguageKazakh() {
        return true;
    }

    public boolean isUsingDigitalSign() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setUsingDigitalSign(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
