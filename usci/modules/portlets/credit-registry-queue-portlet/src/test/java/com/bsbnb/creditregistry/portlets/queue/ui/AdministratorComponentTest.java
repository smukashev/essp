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
public class AdministratorComponentTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

        public AdministratorComponentTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInitialization() {
        for (Locale locale : MainLayoutTest.LOCALES) {
            TestPortalEnvironmentFacade testEnvironment = new TestPortalEnvironmentFacade(10000, locale, true);
            AdministratorComponent adminComponent = new AdministratorComponent(testEnvironment, new TestDataProvider());
            //QueueHandler.createNewThread();
            adminComponent.attach();
            adminComponent.initializeUI();
            //QueueHandler.get().stop();
        }
    }
}
