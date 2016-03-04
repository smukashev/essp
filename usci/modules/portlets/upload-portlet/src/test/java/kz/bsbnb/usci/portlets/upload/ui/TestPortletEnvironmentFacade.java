/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.portlets.upload.ui;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.portlets.upload.PortletEnvironmentFacade;
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
        throw new UnsupportedOperationException(Errors.getMessage(Errors.E206));
    }

    public void setUsingDigitalSign(boolean value) {
        throw new UnsupportedOperationException(Errors.getMessage(Errors.E206));
    }

    @Override
    public boolean isNB() {
        return false;
    }
}
