package kz.bsbnb.usci.eav.test.postgresql.storage;

import kz.bsbnb.usci.eav.postgresql.storage.PostgreSQLStorageImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author a.tkachenko
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles({"postgres"})
public class PostgreSQLStorageImplTest {
	
	@Autowired
    PostgreSQLStorageImpl postgreSQLStorageImpl;
	
	private final Logger logger = LoggerFactory.getLogger(PostgreSQLStorageImplTest.class);
	
	public PostgreSQLStorageImplTest() {
    }

    @Test
    public void connect() {
        if(postgreSQLStorageImpl == null)
        	fail("postgreSQLAdapterDaoImpl is null");
        
		assertEquals(postgreSQLStorageImpl.testConnection(), true);
    }
    
    @Test
    public void createDropStructure() {
        try
        {
            if(postgreSQLStorageImpl == null)
                fail("postgreSQLAdapterDaoImpl is null");

            logger.debug("DB created");
            postgreSQLStorageImpl.initialize();
            logger.debug("DB cleared");
        }
        finally {
            postgreSQLStorageImpl.clear();
        }
    }
}
