package kz.bsbnb.usci.portlets.signing;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class SigningPortletEnvironment {

    private static final String BUNDLE_NAME = "content.Language";

    private final ResourceBundle bundle;

    public SigningPortletEnvironment(Locale locale) {
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }

    public String getResourceString(Localization localization) {
        return bundle.getString(localization.getKey());
    }
}
