package kz.bsbnb.usci.receiver.test.couchbase;

import com.couchbase.client.ClusterManager;
import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import junit.framework.Assert;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import net.spy.memcached.internal.OperationFuture;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
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
    private ClusterManager manager;

    private static final String CLUSTER_ADMIN_USER = "Administrator";
    private static final String CLUSTER_ADMIN_PWD = "123456";

    private static final String MODE_DEVELOPMENT = "development";
    private static final String DEV_PREFIX = "dev_";
    public static String MODE_PREFIX;
    private static final String BUCKET_NAME = "test";
    private static final String FILE_PATH = "/opt/xmls/test.xml";

    private static final int ITEM_NUM = 100;

    private Logger logger = Logger.getLogger(CouchbaseTest.class);

    @Before
    public void setUp() throws Exception {
        System.setProperty("viewmode", MODE_DEVELOPMENT);
        MODE_PREFIX = DEV_PREFIX;

        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {

            client = new CouchbaseClient(nodes, BUCKET_NAME, "");
            manager = new ClusterManager(nodes,CLUSTER_ADMIN_USER,CLUSTER_ADMIN_PWD);
        } catch (Exception e) {
            logger.info("Error connecting to Couchbase: " + e.getMessage());
            System.exit(1);
        }
    }


    @Test
    public void testView() throws Exception {
        client.set("test_view",0,"test_view");

        DesignDocument designDoc = new DesignDocument("tt");
        String viewname = "test_view";
        String mapfunction = "function (doc, meta) {\n" +
                " emit(meta.id,doc);\n" +
                " }\n";

        ViewDesign viewDesign = new ViewDesign(viewname,mapfunction);
        designDoc.getViews().add(viewDesign);

        Boolean result = client.createDesignDoc(designDoc);
        Assert.assertTrue(result);

        View view = client.getView("tt","test_view");
        Assert.assertNotNull(view);

//            Boolean res = client.asyncQuery(view,new Query()).getStatus().isSuccess();
//            Assert.assertTrue(res);

        client.deleteDesignDoc("tt");
        client.delete("test_view");
    }

    @Test
    public void testGetSet() throws Exception {
        Batch batch = new Batch(new Timestamp(new java.util.Date().getTime()),
                new java.sql.Date(new java.util.Date().getTime()));

        OperationFuture<Boolean> setOp = client.set("test_key",0,"test_value");

        try {
            if (!setOp.get().booleanValue()) Assert.fail("Set failed: " + setOp.getStatus().getMessage());
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException while doing set: " + e.getMessage());
        } catch (ExecutionException e) {
            Assert.fail("ExecutionException while doing set: " + e.getMessage());

        }
        client.delete("test_key");


        FileHelper fileHelper = new FileHelper();
        File file = new File(FILE_PATH);
        byte bytes[] = fileHelper.getFileBytes(file);

        client.set("Batch",0,bytes);
        Assert.assertEquals(bytes.length, ((byte[])client.get("Batch")).length);
        Assert.assertTrue(Arrays.equals(bytes, (byte[]) client.get("Batch")));

        client.delete("Batch");
        Assert.assertNull(client.get("Batch"));

        for (int i = 1; i <= ITEM_NUM; i++)
            client.set("Batch_"+i,0,"batch_"+i);

        Assert.assertEquals((String) client.get("Batch_1"),"batch_1");

        Map<String, Object> map = client.getBulk("Batch_1", "Batch_2");
        Assert.assertTrue(map.containsValue("batch_2"));

        Assert.assertEquals(client.getNumVBuckets(),1024);

        String json = "{_id: \"newBatch\"},"
                + "number: 123,"
                + "enabled: true}";
        client.set("newBatch", 0, json);
        Assert.assertEquals(json,client.get("newBatch"));
        client.delete("newBatch");



        try{
            manager.flushBucket(BUCKET_NAME);
        }catch(Exception e){

        }
    }

    @After
    public void tearDown() throws Exception {
        client.shutdown();
        manager.shutdown();
    }


}