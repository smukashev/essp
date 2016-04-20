package com.bsbnb.creditregistry.portlets.notifications.ui;

import com.bsbnb.creditregistry.portlets.notifications.PortalEnvironmentFacade;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TestPortalEnvironmentFacade implements PortalEnvironmentFacade {

    private static final String BUNDLE_NAME = "content.Language";

    private long userId;
    private Locale locale;
    private final boolean isAdmin;
    private final ResourceBundle bundle;

    public TestPortalEnvironmentFacade(long userId, Locale locale, boolean isAdmin) {
        this.userId = userId;
        this.locale = locale;
        this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("ru", "RU"));
        this.isAdmin = isAdmin;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public long getUserId() {
        return userId;
    }

    public String getResourceString(String key) {
        return bundle.getString(key);
    }

    public String getResourceString(Localization key) {
        return bundle.getString(key.getKey());
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLocaleLanguage() {
        return locale.getLanguage();
    }

    public boolean isUserAdmin() {
        return isAdmin;
    }
}
