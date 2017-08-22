package com.bsbnb.creditregistry.portlets.notifications;

import com.bsbnb.creditregistry.portlets.notifications.ui.Localization;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import org.apache.log4j.Logger;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class NotificationsPortalEnvironmentFacade implements PortalEnvironmentFacade {
    private static final String BUNDLE_NAME = "content.Language";

    private User user;
    private boolean isAdmin = false;
    private Locale locale;
    private ResourceBundle bundle;
    public final Logger logger = Logger.getLogger(NotificationsPortalEnvironmentFacade.class);

    public NotificationsPortalEnvironmentFacade(User user) {
        this.user = user;
        if(user != null) {
            this.locale = user.getLocale();
            this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            try {
                for (Role role : user.getRoles()) {
                    if ("administrator".equalsIgnoreCase(role.getName())) {
                        isAdmin = true;
                        break;
                    }
                }
            } catch (SystemException se) {
                logger.warn("Couldn't get user roles", se);
            }
        }

    }

    public long getUserId() {
        return user.getUserId();
    }

    public String getResourceString(String key) {
        return bundle.getString(key);
    }

    public String getResourceString(Localization key) {
        return getResourceString(key.getKey());
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
