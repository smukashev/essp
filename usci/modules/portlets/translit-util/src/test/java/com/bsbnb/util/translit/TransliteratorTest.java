/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.util.translit;

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
public class TransliteratorTest {
    
    public TransliteratorTest() {
    }
    
    public static final String[] INPUTS = new String[]{"Юридические лица", "hello","o 1_2.3"};
    public static final String[] ANSWERS = new String[]{"Yuridicheskie_litsa", "hello", "o_1_2.3"};

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Test
    public void testTransliterate() {
        for(int inputIndex = 0; inputIndex < INPUTS.length; inputIndex++) {
            String input = INPUTS[inputIndex];
            String output = Transliterator.transliterate(input);
            String answer  = ANSWERS[inputIndex];
            assertEquals(answer, output);
        }
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testSomeMethod() {
    }
}
