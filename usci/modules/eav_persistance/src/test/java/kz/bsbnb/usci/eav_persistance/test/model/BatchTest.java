package kz.bsbnb.usci.eav_persistance.test.model;

import junit.framework.Assert;
import kz.bsbnb.usci.eav_persistance.test.GenericTestCase;
import kz.bsbnb.usci.eav_model.model.Batch;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

/**
 *  @author abukabayev
 */
public class BatchTest  extends GenericTestCase
{
    @Test
    public void testGetReceipt() throws Exception {
        Timestamp tt = new Timestamp(new Date().getTime());
         Batch batch = new Batch(tt, new java.sql.Date(new Date().getTime()));


        Assert.assertEquals(tt,batch.getReceiptDate());
    }

    @Test
    public void testEquals() throws Exception {
        Timestamp tt = new Timestamp(new Date().getTime());
        Batch batch = new Batch(tt, new java.sql.Date(new Date().getTime()));
        Batch batch2 = new Batch(tt, new java.sql.Date(new Date().getTime()));

        Assert.assertTrue(batch.equals(batch));
        Assert.assertTrue(batch.equals(batch2));

        Thread.sleep(50);

        Batch batch3 = new Batch(new java.sql.Date(System.currentTimeMillis()));

        Assert.assertFalse(batch.equals(batch3));

    }
}
