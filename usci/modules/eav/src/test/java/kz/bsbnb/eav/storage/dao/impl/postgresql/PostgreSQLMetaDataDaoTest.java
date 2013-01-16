/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.eav.storage.dao.impl.postgresql;

import static org.junit.Assert.*;

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
    public void testLoadMetaData() throws Exception {
        System.out.println("loadMetaData");
        try {
        	postgreSQLAdapterDaoImpl.createStructure();
	        
	        //---------------- Create metadata
	        logger.debug("Create metadata test");
	        
	        MetaData metaCreate = new MetaData("testClass");
	        
	        metaCreate.setType("testDate", new Type(DataTypes.DATE, false, false));
	        metaCreate.setType("testInteger", new Type(DataTypes.INTEGER, false, false));
	        metaCreate.setType("testDouble", new Type(DataTypes.DOUBLE, false, false));
	        metaCreate.setType("testBoolean", new Type(DataTypes.BOOLEAN, false, false));
	        metaCreate.setType("testString", new Type(DataTypes.STRING, false, false));
	        
	        MetaData expResultCreate = new MetaData("testClass");
	        
	        expResultCreate.setType("testDate", new Type(DataTypes.DATE, false, false));
	        expResultCreate.setType("testInteger", new Type(DataTypes.INTEGER, false, false));
	        expResultCreate.setType("testDouble", new Type(DataTypes.DOUBLE, false, false));
	        expResultCreate.setType("testBoolean", new Type(DataTypes.BOOLEAN, false, false));
	        expResultCreate.setType("testString", new Type(DataTypes.STRING, false, false));
	        
	        postgreSQLMetaDataDaoImpl.saveMetaData(metaCreate);
	        
	        MetaData loadedMetaCreate = postgreSQLMetaDataDaoImpl.loadMetaData("testClass");
	        
	        //-----------------------------------------
	        //---------------- Delete metadata attribute
	        logger.debug("Delete metadata attribute test");
	        
	        MetaData metaDelete = new MetaData("testClass");
	        
	        metaDelete.setType("testDate", new Type(DataTypes.DATE, false, false));
	        metaDelete.setType("testInteger", new Type(DataTypes.INTEGER, false, false));
	        metaDelete.setType("testDouble", new Type(DataTypes.DOUBLE, false, false));
	        //metaDelete.setType("testBoolean", new Type(DataTypes.BOOLEAN));
	        metaDelete.setType("testString", new Type(DataTypes.STRING, false, false));
	        
	        MetaData expResultDelete = new MetaData("testClass");
	        
	        expResultDelete.setType("testDate", new Type(DataTypes.DATE, false, false));
	        expResultDelete.setType("testInteger", new Type(DataTypes.INTEGER, false, false));
	        expResultDelete.setType("testDouble", new Type(DataTypes.DOUBLE, false, false));
	        //expResultDelete.setType("testBoolean", new Type(DataTypes.BOOLEAN));
	        expResultDelete.setType("testString", new Type(DataTypes.STRING, false, false));
	        
	        postgreSQLMetaDataDaoImpl.saveMetaData(metaDelete);
	        
	        MetaData loadedMetaDelete = postgreSQLMetaDataDaoImpl.loadMetaData("testClass");
	        
	        //-----------------------------------------
	        //---------------- Update metadata attribute
	        logger.debug("Delete metadata attribute test");
	        
	        MetaData metaUpdate = new MetaData("testClass");
	        
	        metaUpdate.setType("testDate", new Type(DataTypes.DATE, false, false));
	        metaUpdate.setType("testInteger", new Type(DataTypes.INTEGER, true, false));
	        metaUpdate.setType("testDouble", new Type(DataTypes.DOUBLE, false, true));
	        //metaDelete.setType("testBoolean", new Type(DataTypes.BOOLEAN));
	        metaUpdate.setType("testString", new Type(DataTypes.INTEGER, false, false));
	        
	        MetaData expResultUpdate = new MetaData("testClass");
	        
	        expResultUpdate.setType("testDate", new Type(DataTypes.DATE, false, false));
	        expResultUpdate.setType("testInteger", new Type(DataTypes.INTEGER, true, false));
	        expResultUpdate.setType("testDouble", new Type(DataTypes.DOUBLE, false, true));
	        //expResultDelete.setType("testBoolean", new Type(DataTypes.BOOLEAN));
	        expResultUpdate.setType("testString", new Type(DataTypes.INTEGER, false, false));
	        
	        postgreSQLMetaDataDaoImpl.saveMetaData(metaUpdate);
	        
	        MetaData loadedMetaUpdate = postgreSQLMetaDataDaoImpl.loadMetaData("testClass");
	        
	        //-----------------------------------------
	        assertTrue(expResultCreate.equals(loadedMetaCreate));
	        assertTrue(expResultDelete.equals(loadedMetaDelete));
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
