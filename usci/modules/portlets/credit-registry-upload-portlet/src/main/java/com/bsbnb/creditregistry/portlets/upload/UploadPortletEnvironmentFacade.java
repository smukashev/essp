package com.bsbnb.creditregistry.portlets.upload;

import java.util.ResourceBundle;

import com.liferay.portal.model.User;


/**
 *
 * @author Aidar.Myrzahanov
 */
public class UploadPortletEnvironmentFacade implements PortletEnvironmentFacade{
    private static final String BUNDLE_NAME = "content.Language";
    private ResourceBundle bundle;
    private boolean isKazakh;
    private User user;
    
    public UploadPortletEnvironmentFacade(User user) {
        this.user = user;
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, user.getLocale());
        isKazakh = "kz".equals(user.getLocale().getLanguage());
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
    
}
