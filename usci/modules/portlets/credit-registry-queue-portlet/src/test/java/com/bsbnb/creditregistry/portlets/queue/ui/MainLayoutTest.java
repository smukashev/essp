package com.bsbnb.creditregistry.portlets.queue.ui;

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
public class MainLayoutTest {

    static final Locale[] LOCALES = new Locale[]{
        Locale.US,
        new Locale("ru", "RU"),
        new Locale("kz", "KZ")
    };

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

        public MainLayoutTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAttach() {
        for (Locale locale : LOCALES) {
            for (int i = 0; i < 2; i++) {
                TestPortalEnvironmentFacade environment = new TestPortalEnvironmentFacade(10169, locale, i == 0);
                TestDataProvider dataProvider = new TestDataProvider();
                MainLayout layout = new MainLayout(environment, dataProvider);
                layout.attach();
            }
        }

    }
}
