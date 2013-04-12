package kz.bsbnb.usci.receiver.test.couchbase;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import junit.framework.Assert;
import net.spy.memcached.CASValue;
import net.spy.memcached.internal.OperationFuture;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;

/**
 * @author k.tulbassiyev
 */
public class CouchbaseUpdateTest {
    private CouchbaseClient couchBaseClient;
    private static final String MODE_PRODUCTION = "production";
    private static final String MODE_DEVELOPMENT = "development";
    private static final String BUCKET_NAME = "test";
    private static final String FILE_PATH = "/opt/xmls/test.xml";

    private Gson gson = new Gson();
    private Logger logger = Logger.getLogger(CouchbaseUpdateTest.class);

    @Before
    public void setUp() throws Exception {
        System.setProperty("viewmode", MODE_DEVELOPMENT);

        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {
            couchBaseClient = new CouchbaseClient(nodes, BUCKET_NAME, "");
        } catch (Exception e) {
            logger.info("Error connecting to Couchbase: " + e.getMessage());
            System.exit(1);
        }
    }

    @Test
    public void update() throws Exception {
        for(int i = 1; i < 10; i++) {
            Student student = new Student(i, "Student" + i);
            OperationFuture<Boolean> result = couchBaseClient.set("student:" + i, 0, gson.toJson(student));

            if(!result.getStatus().isSuccess())
                Assert.fail("Student" + i + " is not installed.");

            while(true) if(result.isDone()) break;
        }

        for(int i = 1; i < 10; i++) {
            Object o = couchBaseClient.get("student:" + i);

            Student student = gson.fromJson(o.toString(), Student.class);

            if(student.id != i || !student.name.equals("Student" + i))
                Assert.fail("Id or Name is not equal!");
        }

        for(int i = 1; i < 10; i++) {
            Student student = gson.fromJson(couchBaseClient.get("student:" + i).toString(), Student.class);
            student.name = "NewStudent" + i;

            OperationFuture<Boolean> result = couchBaseClient.set("student:" + i, 0, gson.toJson(student));

            if(!result.getStatus().isSuccess())
                Assert.fail("Student" + i + " is not installed.");

            while(true) if(result.isDone()) break;
        }

        for(int i = 1; i < 10; i++) {
            Object o = couchBaseClient.get("student:" + i);

            Student student = gson.fromJson(o.toString(), Student.class);

            if(student.id != i || !student.name.equals("NewStudent" + i))
                Assert.fail("Id or Name is not equal!");
        }

        for(int i = 1; i < 10; i++)
            couchBaseClient.delete("student:" + i);

    }

    @After()
    public void tearDown() throws Exception {
        couchBaseClient.shutdown();
    }

    class Student {
        int id;
        String name;

        Student(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}