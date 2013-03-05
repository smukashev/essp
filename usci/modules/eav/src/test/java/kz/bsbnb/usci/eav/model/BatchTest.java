package kz.bsbnb.usci.eav.model;

import junit.framework.Assert;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

/**
 *  @author abukabayev
 */
public class BatchTest {
    @Test
    public void testGetReceipt() throws Exception {
        Timestamp tt = new Timestamp(new Date().getTime());
         Batch batch = new Batch(tt);


        Assert.assertEquals(tt,batch.getReceiptDate());
    }

    @Test
    public void testEquals() throws Exception {
        Timestamp tt = new Timestamp(new Date().getTime());
        Batch batch = new Batch(tt);
        Batch batch2 = new Batch(tt);

        Assert.assertTrue(batch.equals(batch));
        Assert.assertTrue(batch.equals(batch2));

        Thread.sleep(50);

        Batch batch3 = new Batch();

        Assert.assertFalse(batch.equals(batch3));

    }
}
