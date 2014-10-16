package com.bsbnb.creditregistry.portlets.approval.ui;

import com.bsbnb.creditregistry.portlets.approval.PortletEnvironmentFacade;
import java.util.MissingResourceException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class LocalizationTest {

    private String[] availableLanguages = new String[]{"ru", "kz", "en"};

    @Test
    public void testLocalization() {
        System.out.println("Localization test");
        for (String language : availableLanguages) {
            System.out.println("Language: " + language);
            PortletEnvironmentFacade testEnvironment = new TestPortletEnvironmentFacade(language);
            for (Localization localizationElement : Localization.values()) {
                try {
                    String localizationString = testEnvironment.getResourceString(localizationElement);
                    System.out.println("Element: " + localizationElement.getKey());
                    System.out.println("Value: " + localizationString);
                } catch (MissingResourceException mre) {
                    System.out.println("Key: " + mre.getKey());
                    fail("Localization. Language: " + language + ". Key: " + mre.getKey());
                }
            }
        }
    }
}
