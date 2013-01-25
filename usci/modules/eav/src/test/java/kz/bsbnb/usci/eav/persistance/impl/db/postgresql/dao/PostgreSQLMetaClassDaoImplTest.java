/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import static org.junit.Assert.*;

import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClassArray;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValueArray;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 *
 * @author a.tkachenko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class PostgreSQLMetaClassDaoImplTest {
	
	@Autowired
	IStorage postgreSQLStorageImpl;
	@Autowired
	IMetaClassDao postgreSQLMetaClassDaoImpl;
	
	final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaClassDaoImpl.class);
	
	public PostgreSQLMetaClassDaoImplTest() {
    }
	
    @BeforeClass
    public static void setUp() throws Exception {
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    }

    @Test
    public void saveMetaClass() throws Exception {
        try {
            postgreSQLStorageImpl.initialize();

            logger.debug("Create metadata test");

            MetaClass metaCreate = new MetaClass("testClass");

            metaCreate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
            metaCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
            metaCreate.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaCreate.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaCreate.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));

            MetaValueArray metaValueArray = new MetaValueArray(DataTypes.STRING, false, false);
            metaCreate.setMemberType("testArray", metaValueArray);

            MetaClass metaClass = new MetaClass("innerClass", true, false);
            metaClass.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClass.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaCreate.setMemberType("testInnerClass", metaClass);

            MetaClass metaClassForArray = new MetaClass("innerClassForArray", true, false);
            metaClassForArray.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClassForArray.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            MetaClassArray metaClassArray = new MetaClassArray(metaClassForArray);
            metaCreate.setMemberType("testInnerClassArray", metaClassArray);

            long id;
            MetaClass loadClassNotExists;

            try
            {
                loadClassNotExists = postgreSQLMetaClassDaoImpl.load("testClass");
            }
            catch (IllegalArgumentException e)
            {
                logger.debug("Can't load not existing class, and that's ok");
                loadClassNotExists = null;
            }
            assertTrue(loadClassNotExists == null);

            id = postgreSQLMetaClassDaoImpl.save(metaCreate);

            MetaClass loadedByNameMetaCreate = postgreSQLMetaClassDaoImpl.load("testClass");
            MetaClass loadedByIdMetaCreate = postgreSQLMetaClassDaoImpl.load(id);

            assertTrue(metaCreate.equals(loadedByNameMetaCreate));
            assertTrue(metaCreate.equals(loadedByIdMetaCreate));
        }
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }

    @Test
    public void loadMetaClass() throws Exception {
        try {
        	postgreSQLStorageImpl.initialize();
	        
	        logger.debug("Create metadata test");

            long time = Calendar.getInstance().getTimeInMillis();

            MetaClass metaCreate = new MetaClass("testClass");
            metaCreate.setBeginDate(new Timestamp(time));
            metaCreate.setDisabled(false);
	        
	        metaCreate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
	        metaCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
	        metaCreate.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
	        metaCreate.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
	        metaCreate.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));

            MetaValueArray metaValueArray = new MetaValueArray(DataTypes.STRING, false, false);
            metaCreate.setMemberType("testArray", metaValueArray);

            MetaClass metaClass = new MetaClass("innerClass", true, false);
            metaClass.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClass.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            metaCreate.setMemberType("testInnerClass", metaClass);

            MetaClass metaClassForArray = new MetaClass("innerClassForArray", true, false);
            metaClassForArray.setMemberType("innerBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
            metaClassForArray.setMemberType("innerDouble", new MetaValue(DataTypes.DOUBLE, false, false));
            MetaClassArray metaClassArray = new MetaClassArray(metaClassForArray);
            metaCreate.setMemberType("testInnerClassArray", metaClassArray);
	        
            long id;

            MetaClass loadClassNotExists;

            try
            {
                loadClassNotExists = postgreSQLMetaClassDaoImpl.load("testClass");
            }
            catch (IllegalArgumentException e)
            {
                logger.debug("Class not found, but it's ok.");
                loadClassNotExists = null;
            }

            assertTrue(loadClassNotExists == null);

	        id = postgreSQLMetaClassDaoImpl.create(metaCreate);

            boolean cantCreateExistingClass = false;
            try
            {
                id = postgreSQLMetaClassDaoImpl.create(metaCreate);
            }
            catch(IllegalArgumentException e)
            {
                cantCreateExistingClass = true;
            }

            if(!cantCreateExistingClass)
            {
                fail("Existing class created again!");
            }
	        
	        MetaClass loadedByNameMetaCreate = postgreSQLMetaClassDaoImpl.load("testClass");
	        MetaClass loadedByIdMetaCreate = postgreSQLMetaClassDaoImpl.load(id);

	        assertTrue(metaCreate.equals(loadedByNameMetaCreate));
	        assertTrue(metaCreate.equals(loadedByIdMetaCreate));
        }
        finally
        {
        	postgreSQLStorageImpl.clear();
        }
    }
    
    /*@Test
    public void deleteFieldsMetaClass() throws Exception {
        try {
        	postgreSQLStorageImpl.initialize();
	        
	        logger.debug("Delete metadata attribute test");
	        
	        MetaClass metaDelete = new MetaClass("testClass");
	        
	        metaDelete.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
	        metaDelete.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
	        MetaValue t = new MetaValue(DataTypes.DOUBLE, false, false);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaDelete.setMemberType("testDouble", t);
	        //metaDelete.setMemberType("testBoolean", new Type(DataTypes.BOOLEAN));
	        t = new MetaValue(DataTypes.STRING, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaDelete.setMemberType("testString", t);
	        
	        MetaClass expResultDelete = new MetaClass("testClass");
	        
	        expResultDelete.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
	        expResultDelete.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
	        t = new MetaValue(DataTypes.DOUBLE, false, false);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultDelete.setMemberType("testDouble", t);
	        //expResultDelete.setMemberType("testBoolean", new Type(DataTypes.BOOLEAN));
	        t = new MetaValue(DataTypes.STRING, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultDelete.setMemberType("testString", t);
	        
	        postgreSQLMetaClassDaoImpl.saveMetaClass(metaDelete);
	        
	        MetaClass loadedMetaDelete = postgreSQLMetaClassDaoImpl.loadMetaClass("testClass");
	        
	        assertTrue(expResultDelete.equals(loadedMetaDelete));
        }
        catch(Exception e)
        {
        	fail("Exception: " + e.getMessage());
        }
        finally
        {
        	postgreSQLStorageImpl.clear();
        }
    }
    
    @Test
    public void updateFieldsMetaClass() throws Exception {
        System.out.println("loadMetaClass");
        try {
        	postgreSQLStorageImpl.initialize();
	        
	        logger.debug("Delete metadata attribute test");
	        
	        MetaClass metaUpdate = new MetaClass("testClass");
	        
	        metaUpdate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
	        metaUpdate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, true, false));
	        MetaValue t = new MetaValue(DataTypes.DOUBLE, false, true);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaUpdate.setMemberType("testDouble", t);
	        //metaDelete.setMemberType("testBoolean", new Type(DataTypes.BOOLEAN));
	        t = new MetaValue(DataTypes.INTEGER, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaUpdate.setMemberType("testString", t);
	        
	        MetaClass expResultUpdate = new MetaClass("testClass");
	        
	        expResultUpdate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
	        expResultUpdate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, true, false));
	        t = new MetaValue(DataTypes.DOUBLE, false, true);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultUpdate.setMemberType("testDouble", t);
	        //expResultDelete.setMemberType("testBoolean", new Type(DataTypes.BOOLEAN));
	        t = new MetaValue(DataTypes.INTEGER, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultUpdate.setMemberType("testString", new MetaValue(DataTypes.INTEGER, false, false));
	        
	        postgreSQLMetaClassDaoImpl.saveMetaClass(metaUpdate);
	        
	        MetaClass loadedMetaUpdate = postgreSQLMetaClassDaoImpl.loadMetaClass("testClass");
	        
	        assertTrue(expResultUpdate.equals(loadedMetaUpdate));
        }
        catch(Exception e)
        {
        	fail("Exception: " + e.getMessage());
        }
        finally
        {
        	postgreSQLStorageImpl.clear();
        }
    }*/
}
