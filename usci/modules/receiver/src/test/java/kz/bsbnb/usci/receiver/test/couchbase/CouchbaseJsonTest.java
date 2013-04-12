package kz.bsbnb.usci.receiver.test.couchbase;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import junit.framework.Assert;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import net.spy.memcached.internal.OperationFuture;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * @author k.tulbassiyev
 */
public class CouchbaseJsonTest {
    private CouchbaseClient client;
    private static final String BUCKET_NAME = "test";
    private static final String FILE_PATH = "/opt/xmls/test.xml";

    private Logger logger = Logger.getLogger(CouchbaseTest.class);

    @Before
    public void setUp() throws Exception {
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
    public void jsonBytes() throws Exception {
        Random r = new Random();
        Gson gson = new Gson();

        FileHelper fileHelper = new FileHelper();
        File file = new File(FILE_PATH);
        byte bytes[] = fileHelper.getFileBytes(file);

        long batchId = r.nextLong();
        BatchSample batchSample = new BatchSample(batchId, "batch-file-" + batchId, bytes);

        OperationFuture<Boolean> result = client.set("batch:" + batchId, 0, gson.toJson(batchSample));

        if(!result.getStatus().isSuccess())
            Assert.fail("Value has not been installed.");

        BatchSample batchSample1 = gson.fromJson(client.get("batch:" + batchId).toString(), BatchSample.class);

        if(!batchSample.equals(batchSample1))
            Assert.fail("Batches are not equal.");

        if(batchSample.hashCode() != batchSample1.hashCode())
            Assert.fail("Hashcodes are not equal.");

        client.delete("batch:" + batchId);
    }

    @After()
    public void tearDown() throws Exception {
        client.shutdown();
    }

    class BatchSample {
        private long id;
        private String fileName;
        private byte bytes[];

        public BatchSample(long id, String fileName, byte bytes[]) {
            this.id = id;
            this.fileName = fileName;
            this.bytes = bytes;
        }

        long getId() {
            return id;
        }

        void setId(long id) {
            this.id = id;
        }

        String getFileName() {
            return fileName;
        }

        void setFileName(String fileName) {
            this.fileName = fileName;
        }

        byte[] getBytes() {
            return bytes;
        }

        void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BatchSample)) return false;

            BatchSample that = (BatchSample) o;

            if (id != that.id) return false;
            if (!Arrays.equals(bytes, that.bytes)) return false;
            if (!fileName.equals(that.fileName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + fileName.hashCode();
            result = 31 * result + Arrays.hashCode(bytes);
            return result;
        }

        @Override
        public String toString() {
            return "BatchSample{" +
                    "id=" + id +
                    ", fileName='" + fileName + '\'' +
                    ", bytes=" + Arrays.toString(bytes) +
                    '}';
        }
    }
}
