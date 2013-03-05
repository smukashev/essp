package kz.bsbnb.usci.eav.model.batchdata.impl;

import junit.framework.Assert;
import kz.bsbnb.usci.eav.GenericTestCase;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBatchRepository;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.fail;

/**
 *  @author abukabayev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class BatchRepositoryTest  extends GenericTestCase
{

    private final Logger logger = LoggerFactory.getLogger(BatchRepositoryTest.class);

    @Autowired
    private IBatchDao batchDao;

    @Autowired
    IBatchRepository batchRepository;

    @Test
    public void testAddBatch() throws Exception {

        Batch batchStored = new Batch(new Timestamp(new Date().getTime()));

        Long batchId = batchDao.save(batchStored);

        Batch batchRepo = batchRepository.addBatch(batchStored);

        Batch batchGet = batchRepository.getBatch(batchId);

        Assert.assertEquals(batchRepo, batchGet);

        try{
            Batch batchNotStored = batchRepository.addBatch(new Batch());
        }catch (Exception e){
            fail("Batch with no Id is not added");
        }
    }
}
