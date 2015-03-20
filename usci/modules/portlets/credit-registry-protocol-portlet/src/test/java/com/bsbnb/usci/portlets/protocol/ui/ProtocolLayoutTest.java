package com.bsbnb.usci.portlets.protocol.ui;

import com.bsbnb.usci.portlets.protocol.data.InputInfoDisplayBean;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
import com.bsbnb.usci.portlets.protocol.PortletEnvironmentFacade;
import com.bsbnb.usci.portlets.protocol.data.DataProvider;
import kz.bsbnb.usci.cr.model.Creditor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.*;
import org.easymock.EasyMock;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ProtocolLayoutTest {

    private String[] availableLanguages = new String[]{"ru", "kz", "en"};

    public ProtocolLayoutTest() {
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
            PortletEnvironmentFacade.set(new TestPortletEnvironmentFacade(language));
            DataProvider provider = EasyMock.createMock(DataProvider.class);
            Creditor onlyCreditor = new Creditor();
            onlyCreditor.setName("Super-bank");
            List<Creditor> creditors = Arrays.asList(new Creditor[]{onlyCreditor});
            EasyMock.expect(provider.getCreditorsList()).andReturn(creditors);
            EasyMock.expect(provider.getInputInfosByCreditors(creditors,null)).andReturn(new ArrayList<InputInfoDisplayBean>());
            EasyMock.replay(provider);
            ProtocolLayout instance = new ProtocolLayout(provider);
            instance.attach();
        }
    }
}
