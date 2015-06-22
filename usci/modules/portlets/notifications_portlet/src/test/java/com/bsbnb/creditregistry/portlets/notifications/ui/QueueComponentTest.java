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
public class QueueComponentTest extends NotificationsComponent {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public QueueComponentTest() {
        super(null, null);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDisplay() {
        for (Locale locale : MainLayoutTest.LOCALES) {
            TestPortalEnvironmentFacade environment = new TestPortalEnvironmentFacade(10000, locale, true);
            TestDataProvider dataProvider = new TestDataProvider();
            //NotificationsComponent queue = new NotificationsComponent(environment, dataProvider);
            //queue.attach();
        }
    }
}
