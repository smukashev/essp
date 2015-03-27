package com.bsbnb.usci.portlets.crosscheck.ui;

import com.bsbnb.usci.portlets.crosscheck.PortletEnvironmentFacade;
import org.junit.Before;
import java.util.MissingResourceException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class LocalizationTest {

    private String[] availableLanguages = new String[]{"ru", "kz", "en"};
    
    @Before
    public void setUp() {
        PortletEnvironmentFacade.set(new TestPortletEnvironmentFacade());
    }

    @Test
    public void testLocalization() {
        System.out.println("Localization test");
        for (String language : availableLanguages) {
            System.out.println("Language: " + language);
            PortletEnvironmentFacade.set(new TestPortletEnvironmentFacade(language));
            for (Localization localizationElement : Localization.values()) {
                try {
                    localizationElement.getValue();
                } catch (MissingResourceException mre) {
                    fail("Localization. Language: " + language + ". Key: " + mre.getKey());
                }
            }
        }
    }
}
