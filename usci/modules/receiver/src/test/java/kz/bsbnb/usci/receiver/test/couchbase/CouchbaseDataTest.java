package kz.bsbnb.usci.receiver.test.couchbase;

import com.couchbase.client.CouchbaseClient;
import junit.framework.Assert;
import net.spy.memcached.internal.OperationFuture;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * @author abukabayev
 */
public class CouchbaseDataTest {
    private CouchbaseClient client;
    private static final String MODE_PRODUCTION = "production";
    private static final String MODE_DEVELOPMENT = "development";
    private static final String BUCKET_NAME = "test";
    private static final String FILE_PATH = "/opt/xmls/test.xml";

    private Logger logger = Logger.getLogger(CouchbaseDataTest.class);

    @Before
    public void setUp() throws Exception {
        System.setProperty("viewmode", MODE_DEVELOPMENT);

        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {

            client = new CouchbaseClient(nodes, BUCKET_NAME, "");
        } catch (Exception e) {
            logger.info("Error connecting to Couchbase: " + e.getMessage());
            System.exit(1);
        }
    }

    @Test
    public void testGetSet() throws Exception {
        OperationFuture<Boolean> setOp = client.set("test_key",0,"test_value");

        try {
            if (!setOp.get().booleanValue()) {
                logger.info("Set failed: " + setOp.getStatus().getMessage());
                System.exit(1);
            }
        } catch (InterruptedException e) {
            logger.info("InterruptedException while doing set: " + e.getMessage());
            System.exit(1);
        } catch (ExecutionException e) {
            logger.info("ExecutionException while doing set: " + e.getMessage());
            System.exit(1);
        }

        for (int i = 1; i <= 100; i++)
            client.set("batch:" + i, 0, "value-" + i);

        for (int i = 1; i <= 100; i++)
            Assert.assertEquals((String) client.get("batch:" + i),"value-" + i);

        for (int i = 1; i <= 100; i++)
            client.delete("batch:" + i);

        for (int i = 1; i <= 100; i++)
            Assert.assertNull(client.get("batch" + i));
    }

    @After()
    public void tearDown() throws Exception {
        client.shutdown();
    }
}