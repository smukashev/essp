package kz.bsbnb.usci.receiver.factory;

import com.couchbase.client.CouchbaseClient;

/**
 * @author k.tulbassiyev
 */
public interface ICouchbaseClientFactory {
    public CouchbaseClient getCouchbaseClient();
}
