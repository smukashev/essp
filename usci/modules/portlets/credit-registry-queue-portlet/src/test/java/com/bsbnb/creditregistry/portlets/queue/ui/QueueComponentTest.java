package com.bsbnb.creditregistry.portlets.queue.ui;

import com.vaadin.Application;
import com.vaadin.ui.Window;
import java.util.Arrays;
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
public class QueueComponentTest extends QueueComponent {

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
        Application app = new Application() {

            @Override
            public void init() {

            }
        };
        Window window = new Window("");
        app.setMainWindow(window);
        for (Locale locale : MainLayoutTest.LOCALES) {
            for (boolean isAdminValue : Arrays.asList(true, false)) {
                TestPortalEnvironmentFacade environment = new TestPortalEnvironmentFacade(10000, locale, isAdminValue);
                TestDataProvider dataProvider = new TestDataProvider();
                QueueComponent queue = new QueueComponent(environment, dataProvider);

                window.addComponent(queue);
                queue.loadTable();
            }
        }
    }
}
