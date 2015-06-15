package com.bsbnb.creditregistry.portlets.queue;

import com.bsbnb.creditregistry.portlets.queue.ui.Localization;
import java.util.Locale;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface PortalEnvironmentFacade {

    public long getUserId();

    public String getString(String key);

    public String getString(Localization key);

    public Locale getLocale();

    public String getLocaleLanguage();

    public boolean isUserAdmin();

    public boolean isBankUser();
}
