package com.bsbnb.usci.portlets.protocol;

import com.liferay.portal.model.User;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 *
 * @author Aidar.Myrzahanov
 */
public class ProtocolPortletEnvironmentFacade extends PortletEnvironmentFacade{
    private static final String BUNDLE_NAME = "content.Language";
    private ResourceBundle bundle;
    private boolean isKazakh;
    private User user;
    private boolean isNB;

    public ProtocolPortletEnvironmentFacade(User user, boolean isNB) {
        this.user = user;
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("ru", "RU"));
        isKazakh = "kz".equals(user.getLocale().getLanguage());
        this.isNB = isNB;
    }

    @Override
    public String getResourceString(String key) {
        return bundle.getString(key);
    }

    @Override
    public long getUserID() {
        return user.getUserId();
    }

    @Override
    public boolean isLanguageKazakh() {
        return isKazakh;
    }

    @Override
    public boolean isNB() {
        return isNB;
    }

}
