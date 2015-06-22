package kz.bsbnb.usci.tool.couchbase.singleton;

import com.couchbase.client.CouchbaseClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created by maksat on 6/22/15.
 */
@Component
public class CouchbaseClientManager {

    private CouchbaseClient couchbaseClient;

    @PostConstruct
    public void init() {
        System.setProperty("viewmode", "production");
        //System.setProperty("viewmode", "development");

        ArrayList<URI> nodes = new ArrayList<>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {
            couchbaseClient = new CouchbaseClient(nodes, "test", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CouchbaseClient get() {
        return couchbaseClient;
    }

    @PreDestroy
    public void destroy() {
        if (couchbaseClient != null) {
            couchbaseClient.shutdown();
        }
    }

}
