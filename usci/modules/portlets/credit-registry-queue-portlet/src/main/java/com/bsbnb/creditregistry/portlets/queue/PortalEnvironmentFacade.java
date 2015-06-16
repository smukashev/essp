package com.bsbnb.creditregistry.portlets.queue;

import com.bsbnb.creditregistry.portlets.queue.ui.Localization;
import java.util.Locale;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface PortalEnvironmentFacade {

    long getUserId();

    String getString(String key);

    String getString(Localization key);

    Locale getLocale();

    String getLocaleLanguage();

    boolean isUserAdmin();

    boolean isBankUser();
}
