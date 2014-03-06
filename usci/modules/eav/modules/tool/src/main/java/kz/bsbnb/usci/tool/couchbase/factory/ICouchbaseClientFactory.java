package kz.bsbnb.usci.tool.couchbase.factory;

import com.couchbase.client.CouchbaseClient;

/**
 * @author k.tulbassiyev
 */
public interface ICouchbaseClientFactory {
    public CouchbaseClient getCouchbaseClient();
}
