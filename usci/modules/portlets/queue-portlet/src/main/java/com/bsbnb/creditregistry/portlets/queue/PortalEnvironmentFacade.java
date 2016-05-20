package com.bsbnb.creditregistry.portlets.queue;

import com.bsbnb.creditregistry.portlets.queue.ui.Localization;
import java.util.Locale;

/**
 *
 * @author Aidar.Myrzahanov
 */
public abstract class PortalEnvironmentFacade {

    private static PortalEnvironmentFacade instance;

    public static void set(PortalEnvironmentFacade provider) {
        instance = provider;
    }

    public static PortalEnvironmentFacade get() {
        return instance;
    }

    public abstract String getResourceString(String key);

    public abstract long getUserId();

    public abstract String getString(String key);

    public abstract String getString(Localization key);

    public abstract  Locale getLocale();

    public abstract String getLocaleLanguage();

    public abstract boolean isUserAdmin();

    public abstract boolean isBankUser();
}
