package kz.bsbnb.usci.batch.factory;

import com.couchbase.client.CouchbaseClient;

/**
 * @author k.tulbassiyev
 */
public interface ICouchbaseClientFactory
{
    public CouchbaseClient getCouchbaseClient();
}
