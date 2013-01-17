/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.eav.storage.dao.impl.postgresql;

import static org.junit.Assert.*;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.MetaData;
import kz.bsbnb.usci.eav.model.metadata.Type;
import kz.bsbnb.usci.eav.storage.dao.IAdapterDao;
import kz.bsbnb.usci.eav.storage.dao.IMetaDataDao;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author a.tkachenko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class PostgreSQLMetaDataDaoTest {
	
	@Autowired
	IAdapterDao postgreSQLAdapterDaoImpl;
	@Autowired
	IMetaDataDao postgreSQLMetaDataDaoImpl;
	
	final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaDataDaoTest.class);
	
	public PostgreSQLMetaDataDaoTest() {
    }
	
    @BeforeClass
    public static void setUp() throws Exception {
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    }

    @Test
    public void loadMetaData() throws Exception {
        try {
        	postgreSQLAdapterDaoImpl.createStructure();
	        
	        logger.debug("Create metadata test");
	        
	        MetaData metaCreate = new MetaData("testClass");
	        
	        metaCreate.setType("testDate", new Type(DataTypes.DATE, false, false));
	        metaCreate.setType("testInteger", new Type(DataTypes.INTEGER, false, false));
	        metaCreate.setType("testDouble", new Type(DataTypes.DOUBLE, false, false));
	        Type t = new Type(DataTypes.BOOLEAN, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaCreate.setType("testBoolean", t);
	        t = new Type(DataTypes.STRING, false, false);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaCreate.setType("testString", t);
	        
	        MetaData expResultCreate = new MetaData("testClass");
	        
	        expResultCreate.setType("testDate", new Type(DataTypes.DATE, false, false));
	        expResultCreate.setType("testInteger", new Type(DataTypes.INTEGER, false, false));
	        expResultCreate.setType("testDouble", new Type(DataTypes.DOUBLE, false, false));
	        t = new Type(DataTypes.BOOLEAN, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultCreate.setType("testBoolean", t);
	        t = new Type(DataTypes.STRING, false, false);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultCreate.setType("testString", t);
	        
	        postgreSQLMetaDataDaoImpl.saveMetaData(metaCreate);
	        
	        MetaData loadedMetaCreate = postgreSQLMetaDataDaoImpl.loadMetaData("testClass");

	        assertTrue(expResultCreate.equals(loadedMetaCreate));
        }
        catch(Exception e)
        {
        	fail("Exception: " + e.getMessage());
        }
        finally
        {
        	postgreSQLAdapterDaoImpl.dropStructure();
        }
    }
    
    @Test
    public void deleteFieldsMetaData() throws Exception {
        try {
        	postgreSQLAdapterDaoImpl.createStructure();
	        
	        logger.debug("Delete metadata attribute test");
	        
	        MetaData metaDelete = new MetaData("testClass");
	        
	        metaDelete.setType("testDate", new Type(DataTypes.DATE, false, false));
	        metaDelete.setType("testInteger", new Type(DataTypes.INTEGER, false, false));
	        Type t = new Type(DataTypes.DOUBLE, false, false);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaDelete.setType("testDouble", t);
	        //metaDelete.setType("testBoolean", new Type(DataTypes.BOOLEAN));
	        t = new Type(DataTypes.STRING, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaDelete.setType("testString", t);
	        
	        MetaData expResultDelete = new MetaData("testClass");
	        
	        expResultDelete.setType("testDate", new Type(DataTypes.DATE, false, false));
	        expResultDelete.setType("testInteger", new Type(DataTypes.INTEGER, false, false));
	        t = new Type(DataTypes.DOUBLE, false, false);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultDelete.setType("testDouble", t);
	        //expResultDelete.setType("testBoolean", new Type(DataTypes.BOOLEAN));
	        t = new Type(DataTypes.STRING, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultDelete.setType("testString", t);
	        
	        postgreSQLMetaDataDaoImpl.saveMetaData(metaDelete);
	        
	        MetaData loadedMetaDelete = postgreSQLMetaDataDaoImpl.loadMetaData("testClass");
	        
	        assertTrue(expResultDelete.equals(loadedMetaDelete));
        }
        catch(Exception e)
        {
        	fail("Exception: " + e.getMessage());
        }
        finally
        {
        	postgreSQLAdapterDaoImpl.dropStructure();
        }
    }
    
    @Test
    public void updateFieldsMetaData() throws Exception {
        System.out.println("loadMetaData");
        try {
        	postgreSQLAdapterDaoImpl.createStructure();
	        
	        logger.debug("Delete metadata attribute test");
	        
	        MetaData metaUpdate = new MetaData("testClass");
	        
	        metaUpdate.setType("testDate", new Type(DataTypes.DATE, false, false));
	        metaUpdate.setType("testInteger", new Type(DataTypes.INTEGER, true, false));
	        Type t = new Type(DataTypes.DOUBLE, false, true);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaUpdate.setType("testDouble", t);
	        //metaDelete.setType("testBoolean", new Type(DataTypes.BOOLEAN));
	        t = new Type(DataTypes.INTEGER, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        metaUpdate.setType("testString", t);
	        
	        MetaData expResultUpdate = new MetaData("testClass");
	        
	        expResultUpdate.setType("testDate", new Type(DataTypes.DATE, false, false));
	        expResultUpdate.setType("testInteger", new Type(DataTypes.INTEGER, true, false));
	        t = new Type(DataTypes.DOUBLE, false, true);
	        t.setArray(true);
	        t.setArrayKeyType(ComplexKeyTypes.ANY);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultUpdate.setType("testDouble", t);
	        //expResultDelete.setType("testBoolean", new Type(DataTypes.BOOLEAN));
	        t = new Type(DataTypes.INTEGER, false, false);
	        t.setArray(true);
	        t.setComplexKeyType(ComplexKeyTypes.ANY);
	        expResultUpdate.setType("testString", new Type(DataTypes.INTEGER, false, false));
	        
	        postgreSQLMetaDataDaoImpl.saveMetaData(metaUpdate);
	        
	        MetaData loadedMetaUpdate = postgreSQLMetaDataDaoImpl.loadMetaData("testClass");
	        
	        assertTrue(expResultUpdate.equals(loadedMetaUpdate));
        }
        catch(Exception e)
        {
        	fail("Exception: " + e.getMessage());
        }
        finally
        {
        	postgreSQLAdapterDaoImpl.dropStructure();
        }
    }
}
