package com.bsbnb.creditregistry.portlets.queue;

import static com.bsbnb.creditregistry.portlets.queue.QueueApplication.log;
import com.bsbnb.creditregistry.portlets.queue.ui.Localization;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class QueuePortalEnvironmentFacade implements PortalEnvironmentFacade {
    private static final String BUNDLE_NAME = "content.Language";

    private User user;
    private boolean isAdmin = false;
    private Locale locale;
    private ResourceBundle bundle;
    private boolean isBankUser;

    public QueuePortalEnvironmentFacade(User user) {
        this.user = user;
        this.locale = user.getLocale();
        this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        try {
            for (Role role : user.getRoles()) {
                if ("administrator".equalsIgnoreCase(role.getName())||"QueueManager".equalsIgnoreCase(role.getName())) {
                    isAdmin = true;
                    break;
                } else if("BankUser".equalsIgnoreCase(role.getName())) {
                    isBankUser = true;
                }
            }
        } catch (SystemException se) {
            log.log(Level.WARNING, "Couldn't get user roles", se);
        }

    }

    @Override
    public long getUserId() {
        return user.getUserId();
    }

    @Override
    public String getString(String key) {
        return bundle.getString(key);
    }

    @Override
    public String getString(Localization key) {
        return getString(key.getKey());
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
        return isBankUser;
    }
}
