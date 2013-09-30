package com.bsbnb.vaadin.filterableselector;

import java.util.Set;
import java.util.List;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CreditorSelectorTest {

    private class CreditorImpl{

        public String name;
        public BigInteger id;
        public String subjectType;

        public CreditorImpl() {
        }

        public CreditorImpl(String name, BigInteger id, String subjectType) {
            this.name = name;
            this.id = id;
            this.subjectType = subjectType;
        }

        public String getName() {
            return name;
        }

        public BigInteger getId() {
            return id;
        }

        public String getType() {
            return subjectType;
        }
    }
    
    private ArrayList<CreditorImpl> emptyCreditorsList = new ArrayList<CreditorImpl>();
    private ArrayList<CreditorImpl> singleCreditorsList = new ArrayList<CreditorImpl>(Arrays.asList(new CreditorImpl("OLOLO Creditor", BigInteger.ONE, "The best")));
    private ArrayList<CreditorImpl> severalCreditorsList = new ArrayList<CreditorImpl>(Arrays.asList(
            new CreditorImpl("OLOLO Creditor", BigInteger.ONE, "The best"),
            new CreditorImpl("Second Creditor", BigInteger.TEN, "Another type"),
            new CreditorImpl("Elit Stroy Finans", new BigInteger("57"), "Elit stroy subj type")));
    private List<ArrayList<CreditorImpl>> creditorsLists = Arrays.asList(emptyCreditorsList, singleCreditorsList, severalCreditorsList);
    

    public CreditorSelectorTest() {
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
     * Test of attach method, of class CreditorSelector.
     */
    @Test
    public void testAttach() {
        System.out.println("attach");
            for (ArrayList<CreditorImpl> creditorsList : creditorsLists) {
                FilterableSelect<CreditorImpl> instance = new FilterableSelect<CreditorImpl>(creditorsList, new Selector<CreditorImpl>() {

                @Override
                public String getCaption(CreditorImpl item) {
                    return item.getName();
                }

                @Override
                public Object getValue(CreditorImpl item) {
                    return item.getId();
                }

                @Override
                public String getType(CreditorImpl item) {
                    return item.getType();
                }
            });
                instance.attach();
            }
    }

    /**
     * Test of hasCreditors method, of class CreditorSelector.
     */
    @Test
    public void testHasCreditors() {
        System.out.println("hasCreditors");
        for (ArrayList<CreditorImpl> creditorsList : creditorsLists) {
            FilterableSelect<CreditorImpl> instance = new FilterableSelect<CreditorImpl>(creditorsList,new Selector<CreditorImpl>() {

                @Override
                public String getCaption(CreditorImpl item) {
                    return item.getName();
                }

                @Override
                public Object getValue(CreditorImpl item) {
                    return item.getId();
                }

                @Override
                public String getType(CreditorImpl item) {
                    return item.getType();
                }
            });
            instance.attach();
            boolean expectedResult = !creditorsList.isEmpty();
            boolean actualResult = instance.hasElements();
            assertEquals(expectedResult, actualResult);
        }
    }
    
    @Test
    public void testContainsElement() {
        FilterableSelect<CreditorImpl> instance = new FilterableSelect<CreditorImpl>(severalCreditorsList,new Selector<CreditorImpl>() {

                @Override
                public String getCaption(CreditorImpl item) {
                    return item.getName();
                }

                @Override
                public Object getValue(CreditorImpl item) {
                    return item.getId();
                }

                @Override
                public String getType(CreditorImpl item) {
                    return item.getType();
                }
            });
        instance.attach();
        assertEquals(instance.containsElement(BigInteger.ONE), true);
        assertEquals(instance.containsElement(BigInteger.TEN), true);
        assertEquals(instance.containsElement(BigInteger.ZERO), false);
        assertEquals(instance.containsElement(new BigInteger("112")), false);
        assertEquals(instance.containsElement(new BigInteger("57")), true);
    }
    
    @Test
    public void selectCreditors() {
        FilterableSelect<CreditorImpl> instance = new FilterableSelect<CreditorImpl>(severalCreditorsList,new Selector<CreditorImpl>() {

                @Override
                public String getCaption(CreditorImpl item) {
                    return item.getName();
                }

                @Override
                public Object getValue(CreditorImpl item) {
                    return item.getId();
                }

                @Override
                public String getType(CreditorImpl item) {
                    return item.getType();
                }
            });
        instance.attach();
        instance.selectElements(new Object[]{new BigInteger("57"), new BigInteger("12"),BigInteger.ONE});
        instance.getSelectedElements(new SelectionCallback<CreditorSelectorTest.CreditorImpl>() {

            @Override
            public void selected(List<CreditorImpl> selectedItems) {
                Set<BigInteger> ids = new HashSet<BigInteger>();
                for(CreditorImpl creditor : selectedItems) {
                    ids.add(creditor.getId());
                }
                assertArrayEquals(new BigInteger[]{BigInteger.ONE,new BigInteger("57")}, ids.toArray(new BigInteger[0]));
            }
        });
    }
}
