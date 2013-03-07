package kz.bsbnb.usci.eav_persistance.test;

import kz.bsbnb.usci.eav_persistance.persistance.storage.IStorage;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericTestCase
{
    @Autowired
    protected IStorage postgreSQLStorageImpl;

    @Before
    public void setUp() throws Exception {
        if(postgreSQLStorageImpl != null)
        {
            postgreSQLStorageImpl.initialize();
            postgreSQLStorageImpl.empty();
        }
    }

    @After
    public void tearDown() throws Exception {
    }
}
