/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import static org.junit.Assert.*;

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
    public void loadMetaClass() throws Exception {
        try {
        	postgreSQLStorageImpl.initialize();
	        
	        logger.debug("Create metadata test");
	        
	        MetaClass metaCreate = new MetaClass("testClass");
	        
	        metaCreate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
	        metaCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
	        metaCreate.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
	        metaCreate.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
	        metaCreate.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));
	        
	        MetaClass expResultCreate = new MetaClass("testClass");
	        
	        expResultCreate.setMemberType("testDate", new MetaValue(DataTypes.DATE, false, false));
	        expResultCreate.setMemberType("testInteger", new MetaValue(DataTypes.INTEGER, false, false));
	        expResultCreate.setMemberType("testDouble", new MetaValue(DataTypes.DOUBLE, false, false));
	        expResultCreate.setMemberType("testBoolean", new MetaValue(DataTypes.BOOLEAN, false, false));
	        expResultCreate.setMemberType("testString", new MetaValue(DataTypes.STRING, false, false));
	        
	        long id = postgreSQLMetaClassDaoImpl.save(metaCreate);
	        
	        MetaClass loadedByNameMetaCreate = postgreSQLMetaClassDaoImpl.load("testClass");
	        //MetaClass loadedByIdMetaCreate = postgreSQLMetaClassDaoImpl.load(id);

	        assertTrue(expResultCreate.equals(loadedByNameMetaCreate));
	        //assertTrue(expResultCreate.equals(loadedByIdMetaCreate));
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
