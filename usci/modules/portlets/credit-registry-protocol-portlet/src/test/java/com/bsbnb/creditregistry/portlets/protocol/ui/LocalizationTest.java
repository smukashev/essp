package com.bsbnb.creditregistry.portlets.protocol.ui;

import com.bsbnb.creditregistry.portlets.protocol.PortletEnvironmentFacade;
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
            for (Localization localizationElement : Localization.values()) {
                try {
                    localizationElement.getValue();
                } catch (MissingResourceException mre) {
                    System.out.println("Key: "+mre.getKey());
                    fail("Localization. Language: " + language + ". Key: " + mre.getKey());
                }
            }
        }
    }
}
