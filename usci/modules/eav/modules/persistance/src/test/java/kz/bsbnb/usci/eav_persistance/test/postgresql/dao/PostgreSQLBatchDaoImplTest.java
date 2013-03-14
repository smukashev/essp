package kz.bsbnb.usci.eav_persistance.test.postgresql.dao;

import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav_persistance.postgresql.dao.PostgreSQLBatchDaoImpl;
import kz.bsbnb.usci.eav_persistance.test.GenericTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Date;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author a.motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class PostgreSQLBatchDaoImplTest  extends GenericTestCase
{

    @Autowired
    IBatchDao postgreSQLBatchDaoImpl;

	private final Logger logger = LoggerFactory.getLogger(PostgreSQLBatchDaoImpl.class);

	public PostgreSQLBatchDaoImplTest() {
    }

    @Test
    public void saveAndLoadBatch() throws Exception {
        logger.debug("Create and load batch test");

        Batch batchCreate = new Batch(new Date(System.currentTimeMillis()));
        Batch loadBatchNotExists;

        try
        {
            loadBatchNotExists = postgreSQLBatchDaoImpl.load(1000);
        }
        catch (IllegalArgumentException e)
        {
            logger.debug("Can't load not existing batch, and that's ok");
            loadBatchNotExists = null;
        }
        assertTrue(loadBatchNotExists == null);

        long batchId = postgreSQLBatchDaoImpl.save(batchCreate);
        Batch batchLoadedById = postgreSQLBatchDaoImpl.load(batchId);
        assertTrue(batchCreate.equals(batchLoadedById));
    }
    
    @Test
    public void deleteBatch() throws Exception {
        logger.debug("Delete batch test.");

        Batch batchCreate = new Batch(new Date(System.currentTimeMillis()));
        postgreSQLBatchDaoImpl.save(batchCreate);

        try
        {
            Batch dbBatch = postgreSQLBatchDaoImpl.load(batchCreate.getId());
        }
        catch(IllegalArgumentException e)
        {
            fail(String.format("Can't load Batch: %s", e.getMessage()));
        }

        postgreSQLBatchDaoImpl.remove(batchCreate);

        try
        {
            postgreSQLBatchDaoImpl.load(batchCreate.getId());
            fail("Loaded removed batch");
        }
        catch(IllegalArgumentException e)
        {
        }
    }

}
