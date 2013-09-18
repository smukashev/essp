package kz.bsbnb.usci.receiver.factory.impl;

import com.couchbase.client.CouchbaseClient;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;

/**
 * @author k.tulbassiyev
 */
@Repository
public class CouchbaseClientFactoryImpl implements ICouchbaseClientFactory {
    //private CouchbaseClient couchbaseClient;
    private Logger logger = Logger.getLogger(CouchbaseClientFactoryImpl.class);

    /*@PostConstruct
    public void init() {
        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {
            couchbaseClient = new CouchbaseClient(nodes, "test", "");
        } catch (Exception e) {
            logger.info("Error connecting to Couchbase: " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public CouchbaseClient getCouchbaseClient() {
        return couchbaseClient;
    }*/

    @Override
    public CouchbaseClient getCouchbaseClient() {
        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {
            return new CouchbaseClient(nodes, "test", "");
        } catch (Exception e) {
            logger.info("Error connecting to Couchbase: " + e.getMessage());
            System.exit(1);
        }

        return null;
    }
}
