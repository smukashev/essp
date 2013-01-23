/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;

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
public class PostgreSQLStorageImplTest {
	
	@Autowired
	IStorage postgreSQLStorageImpl;
	
	final Logger logger = LoggerFactory.getLogger(PostgreSQLStorageImplTest.class);
	
	public PostgreSQLStorageImplTest() {
    }
	
    @BeforeClass
    public static void setUp() throws Exception {
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    }

    @Test
    public void connect() {
        if(postgreSQLStorageImpl == null)
        	fail("postgreSQLAdapterDaoImpl is null");
        
		assertEquals(postgreSQLStorageImpl.testConnection(), true);
    }
    
    @Test
    public void createDropStructure() {
        if(postgreSQLStorageImpl == null)
        	fail("postgreSQLAdapterDaoImpl is null");

        logger.debug("DB created");
        postgreSQLStorageImpl.initialize();
        logger.debug("DB cleared");
        //TODO: ADD IT BACK
        postgreSQLStorageImpl.clear();
    }
}
