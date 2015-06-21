package com.bsbnb.creditregistry.portlets.notifications;

import com.bsbnb.creditregistry.portlets.notifications.ui.Localization;
import java.util.Locale;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface PortalEnvironmentFacade {

    public long getUserId();

    public String getResourceString(String key);

    public String getResourceString(Localization key);

    public Locale getLocale();

    public String getLocaleLanguage();

    public boolean isUserAdmin();
}
