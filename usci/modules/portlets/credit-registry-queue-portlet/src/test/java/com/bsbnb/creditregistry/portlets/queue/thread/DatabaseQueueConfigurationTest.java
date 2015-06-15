package com.bsbnb.creditregistry.portlets.queue.thread;

import com.bsbnb.creditregistry.portlets.queue.thread.logic.QueueOrderType;
import com.bsbnb.creditregistry.portlets.queue.ui.TestDataProvider;
import com.vaadin.ui.ComboBox;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class DatabaseQueueConfigurationTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public DatabaseQueueConfigurationTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testQueueOrderAccess() {
        TestDataProvider dataProvider = new TestDataProvider();
        dataProvider.setQueueOrder(TestDataProvider.MINIMUM_WEIGHT_ORDER_VALUE);
        DatabaseQueueConfiguration config = new DatabaseQueueConfiguration(dataProvider);
        assertEquals(QueueOrderType.MINIMAL_WEIGHT_FIRST, config.getOrderType());
        ComboBox orderBox = new ComboBox();
        QueueOrderType[] orders = QueueOrderType.values();
        int counter = 1;
        for (QueueOrderType order : orders) {
            orderBox.addItem(order);
            orderBox.setItemCaption(order, "Caption" + counter);
            counter++;
        }
        orderBox.setImmediate(true);
        orderBox.setNullSelectionAllowed(false);
        orderBox.setWidth("400px");
        orderBox.setValue(config.getOrderType());
        assertEquals(QueueOrderType.MINIMAL_WEIGHT_FIRST, orderBox.getValue());
    }
}
