/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.eav.model;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.Type;

/**
 *
 * @author a.tkachenko
 */
public class BaseEntityTest {
    
    public BaseEntityTest() {
    }
    
    @BeforeClass
    public static void setUp() throws Exception {
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    }
    
    @Test
    public void typesCount()
    {
    	assertEquals(DataTypes.values().length, 6);
    }
    
    @Test
    public void getComplex() {
    	String name = "testClass";
        BaseEntity instance = new BaseEntity(name);
        BaseEntity expResult;
        
        expResult = new BaseEntity("testClass");
        expResult.getMeta().setType("testField1", new Type(DataTypes.DATE, false, false));
        expResult.getMeta().setType("testField2", new Type(DataTypes.INTEGER, false, false));
        
        instance.getMeta().setType("testComplex", new Type(DataTypes.COMPLEX, false, false));
        instance.getMeta().setType("testNotComplex", new Type(DataTypes.INTEGER, false, false));
        instance.set("testComplex", expResult);
        
        
        BaseEntity result = instance.getComplex("testComplex");
        assertEquals(expResult, result);
        
        boolean pass = false;
        try {
            instance.getComplex("unknownName");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Complex with unknown name");
        }
        
        pass = false;
        try {
            instance.getComplex("testNotComplex");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Complex with illegal type");
        }
    }

    @Test
    public void getDate() {
        String name = "testClass";
        BaseEntity instance = new BaseEntity(name);
        Date expResult;
        
        expResult = new Date();
        instance.getMeta().setType("testDate", new Type(DataTypes.DATE, false, false));
        instance.getMeta().setType("testNotDate", new Type(DataTypes.INTEGER, false, false));
        instance.set("testDate", expResult);
        
        
        Date result = instance.getDate("testDate");
        assertEquals(expResult, result);
        
        boolean pass = false;
        try {
            instance.getDate("unknownName");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Date with unknown name");
        }
        
        pass = false;
        try {
            instance.getDate("testNotDate");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Date with illegal type");
        }
    }
    
    @Test
    public void getInteger() {
        String name = "testClass";
        BaseEntity instance = new BaseEntity(name);
        Integer expResult;
        
        expResult = new Integer(7);
        instance.getMeta().setType("testInteger", new Type(DataTypes.INTEGER, false, false));
        instance.getMeta().setType("testNotInteger", new Type(DataTypes.DATE, false, false));
        instance.set("testInteger", expResult);
        
        
        Integer result = instance.getInteger("testInteger");
        assertEquals(expResult, result);
        
        boolean pass = false;
        try {
            instance.getInteger("unknownName");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Integer with unknown name");
        }
        
        pass = false;
        try {
            instance.getInteger("testNotInteger");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Integer with illegal type");
        }
    }
    
    @Test
    public void getString() {
        String name = "testClass";
        BaseEntity instance = new BaseEntity(name);
        String expResult;
        
        expResult = "testString";
        instance.getMeta().setType("testString", new Type(DataTypes.STRING, false, false));
        instance.getMeta().setType("testNotString", new Type(DataTypes.INTEGER, false, false));
        instance.set("testString", expResult);
        
        
        String result = instance.getString("testString");
        assertEquals(expResult, result);
        
        boolean pass = false;
        try {
            instance.getDate("unknownName");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives String with unknown name");
        }
        
        pass = false;
        try {
            instance.getDate("testNotDate");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives String with illegal type");
        }
    }
    
    @Test
    public void getDouble() {
        String name = "testClass";
        BaseEntity instance = new BaseEntity(name);
        Double expResult;
        
        expResult = new Double(1.);
        instance.getMeta().setType("testDouble", new Type(DataTypes.DOUBLE, false, false));
        instance.getMeta().setType("testNotDouble", new Type(DataTypes.INTEGER, false, false));
        instance.set("testDouble", expResult);
        
        
        Double result = instance.getDouble("testDouble");
        assertEquals(expResult, result);
        
        boolean pass = false;
        try {
            instance.getDate("unknownName");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Double with unknown name");
        }
        
        pass = false;
        try {
            instance.getDate("testNotDate");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Double with illegal type");
        }
    }
    
    @Test
    public void getBoolean() {
        String name = "testClass";
        BaseEntity instance = new BaseEntity(name);
        Boolean expResult;
        
        expResult = true;
        instance.getMeta().setType("testBoolean", new Type(DataTypes.BOOLEAN, false, false));
        instance.getMeta().setType("testNotBoolean", new Type(DataTypes.INTEGER, false, false));
        instance.set("testBoolean", expResult);
        
        
        Boolean result = instance.getBoolean("testBoolean");
        assertEquals(expResult, result);
        
        boolean pass = false;
        try {
            instance.getBoolean("unknownName");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Boolean with unknown name");
        }
        
        pass = false;
        try {
            instance.getDate("testNotDate");
        }
        catch(IllegalArgumentException e)
        {
            pass = true;
        }
        
        if(!pass) {
            fail("Gives Boolean with illegal type");
        }
    }
}
