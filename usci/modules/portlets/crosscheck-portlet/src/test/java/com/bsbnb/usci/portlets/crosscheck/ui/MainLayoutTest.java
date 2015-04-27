package com.bsbnb.usci.portlets.crosscheck.ui;

import com.bsbnb.usci.portlets.crosscheck.PortletEnvironmentFacade;
import com.bsbnb.usci.portlets.crosscheck.data.DataProvider;
import com.bsbnb.usci.portlets.crosscheck.dm.Creditor;
import com.bsbnb.usci.portlets.crosscheck.dm.CrossCheck;
import com.bsbnb.usci.portlets.crosscheck.dm.SubjectType;
import com.vaadin.ui.Window;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class MainLayoutTest extends CrossCheckLayout {

    private List<Creditor> sampleCreditors;
    private String[] availableLanguages = new String[]{"ru", "kz", "en"};
    private List<CrossCheck> sampleCrossChecks;
    private static TestPortletEnvironmentFacade facade = new TestPortletEnvironmentFacade(); 
    

    public MainLayoutTest() {
        super("WORK", facade, null);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        int creditorsCount = 10;
        PortletEnvironmentFacade.set(new TestPortletEnvironmentFacade());
        Creditor[] creditors = new Creditor[creditorsCount];
        for (int creditorIndex = 0; creditorIndex < creditorsCount; creditorIndex++) {
            Creditor creditor = new Creditor();
            creditor.setId(BigInteger.valueOf(creditorIndex+1));
            creditor.setName("Creditor "+(creditorIndex+1));
            SubjectType type = new SubjectType();
            type.setNameRu("Type "+(creditorIndex+1));
            creditor.setSubjectType(type);
            creditors[creditorIndex] = creditor;
        }
        sampleCreditors = Arrays.asList(creditors);
        int inputInfosCount = 20;
        sampleCrossChecks = new ArrayList<CrossCheck>(inputInfosCount);
        for (int i = 0; i < inputInfosCount; i++) {
            CrossCheck displayBean = new CrossCheck();
            sampleCrossChecks.add(displayBean);
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of attach method, of class CrossCheckLayout.
     */
    @Test
    public void testLocalization() {
        System.out.println("Localization testing");
        for (String language : availableLanguages) {

            System.out.println("Language: " + language);
            PortletEnvironmentFacade.set(new TestPortletEnvironmentFacade(language));
            DataProvider providerMock = EasyMock.createMock(DataProvider.class);
            EasyMock.expect(providerMock.getCreditorsList()).andReturn(sampleCreditors);
            EasyMock.replay(providerMock);
            CrossCheckLayout instance = new CrossCheckLayout("WORK", facade, providerMock);
            Window window = new Window();
            window.addComponent(instance);
            window.attach();
            EasyMock.verify(providerMock);
        }
    }

}
