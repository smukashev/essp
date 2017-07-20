/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.vaadin.filterableselector;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
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
public class LocalizationTest {

    private List<Locale> locales = Arrays.asList(new Locale("ru"), new Locale("kz"), new Locale("en"));

    public LocalizationTest() {
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
     * Test of setLocale method, of class Localization.
     */
    @Test
    public void testSetLocale() throws IOException {
        System.out.println("setLocale");
        Enumeration<URL> urls = Localization.class.getClassLoader().getResources("content/Language_kk.properties");
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            System.out.println("URL: " + url.getFile());
        }
        for (Locale locale : locales) {
            Localization.setLocale(locale);
            for (Localization localization : Localization.values()) {
                System.out.println(String.format("Language: %s. Key: %s", locale.getLanguage(), localization.getKey()));
                localization.getValue();
                localization.getKey();
            }
        }

    }
}
