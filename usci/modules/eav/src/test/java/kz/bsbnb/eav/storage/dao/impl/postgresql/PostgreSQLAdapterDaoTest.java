/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.eav.storage.dao.impl.postgresql;

import static org.junit.Assert.*;

import kz.bsbnb.usci.eav.storage.dao.IAdapterDao;
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
public class PostgreSQLAdapterDaoTest {
	
	@Autowired
	IAdapterDao postgreSQLAdapterDaoImpl;
	
	final Logger logger = LoggerFactory.getLogger(PostgreSQLAdapterDaoTest.class);
	
	public PostgreSQLAdapterDaoTest() {
    }
	
    @BeforeClass
    public static void setUp() throws Exception {
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    }

    @Test
    public void connect() {
        if(postgreSQLAdapterDaoImpl == null)
        	fail("postgreSQLAdapterDaoImpl is null");
        
		assertEquals(postgreSQLAdapterDaoImpl.testConnection(), true);
       	postgreSQLAdapterDaoImpl.createStructure();
       	postgreSQLAdapterDaoImpl.dropStructure();
    }
    
    @Test
    public void createDropStructure() {
        if(postgreSQLAdapterDaoImpl == null)
        	fail("postgreSQLAdapterDaoImpl is null");
        
		postgreSQLAdapterDaoImpl.createStructure();
       	postgreSQLAdapterDaoImpl.dropStructure();
    }
}
