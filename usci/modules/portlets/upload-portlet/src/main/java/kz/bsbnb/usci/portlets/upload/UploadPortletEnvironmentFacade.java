package kz.bsbnb.usci.portlets.upload;

import java.util.ResourceBundle;

import com.liferay.portal.model.Role;
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
    private boolean usingDigitalSign = false;
    private boolean isNB = false;
    
    public UploadPortletEnvironmentFacade(User user, boolean isNB) {
        this.user = user;
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, user.getLocale());
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

    public boolean isUsingDigitalSign() {
        return usingDigitalSign;
    }

    public void setUsingDigitalSign(boolean value) {
        usingDigitalSign = value;
    }

    @Override
    public boolean isNB() {
        return isNB;
    }
}
