package com.bsbnb.creditregistry.portlets.queue.ui;

import com.bsbnb.creditregistry.portlets.queue.PortalEnvironmentFacade;
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
        this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        this.isAdmin = isAdmin;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public String getString(String key) {
        return bundle.getString(key);
    }

    @Override
    public String getString(Localization key) {
        return bundle.getString(key.getKey());
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getLocaleLanguage() {
        return locale.getLanguage();
    }

    @Override
    public boolean isUserAdmin() {
        return isAdmin;
    }

    @Override
    public boolean isBankUser() {
        return !isAdmin;
    }
}
