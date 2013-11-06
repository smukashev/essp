package kz.bsbnb.usci.core.factory;

import com.couchbase.client.CouchbaseClient;

/**
 * @author k.tulbassiyev
 */
public interface ICouchbaseClientFactory {
    public CouchbaseClient getCouchbaseClient();
}
