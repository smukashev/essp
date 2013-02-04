package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author a.motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class PostgreSQLBatchDaoImplTest {

	@Autowired
    IStorage postgreSQLStorageImpl;
    @Autowired
    IBatchDao postgreSQLBatchDaoImpl;

	private final Logger logger = LoggerFactory.getLogger(PostgreSQLBatchDaoImpl.class);

	public PostgreSQLBatchDaoImplTest() {
    }

    @Test
    public void saveAndLoadBatch() throws Exception {
        try {
            postgreSQLStorageImpl.initialize();

            logger.debug("Create and load batch test");

            Batch batchCreate = new Batch();
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
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }
    
    @Test
    public void deleteBatch() throws Exception {
        try {
            postgreSQLStorageImpl.initialize();

            logger.debug("Delete batch test.");

            Batch batchCreate = new Batch();
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
        finally
        {
            postgreSQLStorageImpl.clear();
        }
    }

}
