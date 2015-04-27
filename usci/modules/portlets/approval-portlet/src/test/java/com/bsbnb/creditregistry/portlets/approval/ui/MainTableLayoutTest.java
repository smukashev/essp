package com.bsbnb.creditregistry.portlets.approval.ui;

import com.bsbnb.creditregistry.portlets.approval.PortletEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.approval.data.DataProvider;
import com.bsbnb.creditregistry.portlets.approval.ui.MainLayout;
import java.util.*;

import kz.bsbnb.usci.cr.model.Creditor;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class MainTableLayoutTest {

    private String[] availableLanguages = new String[]{"ru", "kz", "en"};

    public MainTableLayoutTest() {
    }

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

    /**
     * Test of attach method, of class ProtocolLayout.
     */
    @Test
    public void testAttach() {
        System.out.println("attach");
        for (String language : availableLanguages) {
            TestPortletEnvironmentFacade testEnvironment = new TestPortletEnvironmentFacade(language);
            testEnvironment.setIsNbUser(true);
            DataProvider provider = EasyMock.createMock(DataProvider.class);
            Creditor onlyCreditor = new Creditor();
            onlyCreditor.setName("Super-bank");
            List<Creditor> creditors = Arrays.asList(new Creditor[]{onlyCreditor});
            EasyMock.expect(provider.getCreditorsList(testEnvironment.getUserID())).andReturn(creditors);
            EasyMock.replay(provider);
            MainLayout instance = new MainLayout(provider, testEnvironment);
            instance.attach();
        }
    }
}
