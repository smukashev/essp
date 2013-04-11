package kz.bsbnb.usci.receiver.test.couchbase;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.internal.HttpFuture;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import junit.framework.Assert;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import net.spy.memcached.internal.OperationFuture;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author abukabayev
 */
public class CouchbaseTest {
    private CouchbaseClient client;
    private static final String MODE_PRODUCTION = "production";
    private static final String MODE_DEVELOPMENT = "development";
    private static final String DEV_PREFIX = "dev_";
    private static final String PROD_PREFIX = "";
    public static String MODE_PREFIX;
    private static final String BUCKET_NAME = "test";
    private static final String FILE_PATH = "/opt/xmls/test.xml";

    private Logger logger = Logger.getLogger(CouchbaseTest.class);

    @Before
    public void setUp() throws Exception {
        System.setProperty("viewmode", MODE_DEVELOPMENT);
        MODE_PREFIX = DEV_PREFIX;

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
        Batch batch = new Batch(new Timestamp(new java.util.Date().getTime()),
                new java.sql.Date(new java.util.Date().getTime()));

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

        FileHelper fileHelper = new FileHelper();
        File file = new File(FILE_PATH);
        byte bytes[] = fileHelper.getFileBytes(file);

        client.set("Batch",0,bytes);

        Assert.assertEquals(bytes.length, ((byte[])client.get("Batch")).length);
        Assert.assertTrue(Arrays.equals(bytes, (byte[]) client.get("Batch")));

        for (int i=1;i<=10000;i++)
            client.set("Batch_"+i,0,"batch_"+i);

        Assert.assertEquals((String) client.get("Batch_1"),"batch_1");

        client.set("Batch2",0,"batch2");
        client.delete("Batch2");

        Assert.assertNull(client.get("Batch2"));

        Map<String, Object> map = client.getBulk("Batch_1", "Batch_2");

        Assert.assertTrue(map.containsValue("batch_2"));
        Assert.assertEquals(client.getNumVBuckets(),1024);

        String json = "{_id: \"newBatch\"},"
                + "number: 123,"
                + "enabled: true}";

        client.set("newBatch", 0, json);

        Assert.assertEquals(json,client.get("newBatch"));
    }

    @Test
    public void testViews() throws UnsupportedEncodingException, ExecutionException, InterruptedException {
        DesignDocument designDoc = new DesignDocument("tt");
        String viewname = "test_view";
        String mapfunction = "function (doc, meta) {\n" +
                " emit(meta.id,doc);\n" +
                " }\n";

        ViewDesign viewDesign = new ViewDesign(viewname,mapfunction);
        designDoc.getViews().add(viewDesign);

        client.asyncCreateDesignDoc(designDoc);
        Assert.assertTrue(client.asyncCreateDesignDoc(designDoc).get());

        View view = client.asyncGetView("tt","test_view").get();

        String res2 = String.valueOf(client.query(view,new Query()));
//        Assert.assertEquals(client.query(view,new Query()).size(),10003);
    }

    @After()
    public void tearDown() throws Exception {
        client.shutdown();
    }
}