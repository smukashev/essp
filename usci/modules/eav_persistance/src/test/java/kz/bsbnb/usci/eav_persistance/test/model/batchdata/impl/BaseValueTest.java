package kz.bsbnb.usci.eav_persistance.test.model.batchdata.impl;

import junit.framework.Assert;
import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_persistance.test.GenericTestCase;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IBatchDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static junit.framework.Assert.fail;

/**
 *  @author abukabayev
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class BaseValueTest  extends GenericTestCase
{

    private final Logger logger = LoggerFactory.getLogger(BatchRepositoryTest.class);

    @Autowired
    private IBatchDao batchDao;

    @Test
    public void testBatchIsNull() {

       boolean b = false;
       try{
            BaseValue bv = new BaseValue(null,1L,null);
       }
       catch (IllegalArgumentException e){
            b = true;
       }

       if (!b)
       fail("Accepts Null as a Batch");
    }

    @Test
    public void testBatchNoId() throws Exception {
         Batch batch = new Batch();
        boolean b = false;
        try{
            BaseValue bv = new BaseValue(batch,1L,null);
        }catch(IllegalArgumentException e){
            b = true;

        }

        if (!b)
        fail("Accepts batch with no id");
    }

    @Test
    public void testGetBatch(){

        Batch batch = new Batch();
        Long batchId = batchDao.save(batch);
        BaseValue bv = new BaseValue(batch,1L,null);

        Assert.assertEquals(batch,bv.getBatch());
    }

    @Test
    public void testEquals(){

        Batch batch = new Batch();
        Long batchId = batchDao.save(batch);
        BaseValue bv = new BaseValue(batch,1L,null);

        Assert.assertEquals(true,bv.equals(bv));


        BaseValue bv2 = new BaseValue(batch,1L,null);

        Assert.assertEquals(true,bv.equals(bv2));


        BaseValue bv4 = new BaseValue(batch,1L,"Value");

        Assert.assertEquals(false,bv.equals(bv4));

        Assert.assertEquals(false,bv.equals(null));


//        Batch batch2 = new Batch(new Timestamp(new Date().getTime()));
//        BaseValue bv3 = new BaseValue(batch2,1L,null);
//
//        Assert.assertEquals(false,bv.equals(bv3));
    }


}
