package com.bsbnb.creditregistry.portlets.notifications.ui;

import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class LocalizationTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLocalization() {
        for (Locale locale : MainLayoutTest.LOCALES) {
            TestPortalEnvironmentFacade environment = new TestPortalEnvironmentFacade(1, locale, true);
            for (Localization localization : Localization.values()) {
                environment.getResourceString(localization);
            }
        }
    }
}
