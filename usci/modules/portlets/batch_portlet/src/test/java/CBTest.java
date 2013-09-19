import com.couchbase.client.ClusterManager;
import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.google.gson.Gson;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class CBTest
{
    private CouchbaseClient client;
    private ClusterManager manager;

    private static final String MODE_DEVELOPMENT = "development";
    private static final String BUCKET_NAME = "test";

    private Logger logger = Logger.getLogger(CBTest.class);

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
    public void testView() throws Exception {
        View view = client.getView("batch", "batch_statuses");
        Query query = new Query();
        query.setLimit(20);

        ViewResponse viewResponse = client.query(view, query);

        Gson gson = new Gson();
        String line = "";

        if(viewResponse != null) {
            for(ViewRow row : viewResponse) {
                line += "<tr>";
                ViewRowNoDocs viewRowNoDocs = (ViewRowNoDocs) row;

                List list = gson.fromJson(viewRowNoDocs.getValue(), ArrayList.class);

                Double dId = Double.parseDouble(list.get(0).toString());
                Long id = dId.longValue();

                line += "<td>" + id + "</td>";
                line += "<td><a href='/web/guest/who-is-using-liferay?batchId=" + id + "'>" + list.get(1) + "</a></td>";
                line += "<td>" + list.get(2) + "</td>";

                line += "</tr>";
            }
        }

        System.out.println(line);
    }
}
