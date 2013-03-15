package kz.bsbnb.usci.eav.test;

import kz.bsbnb.usci.eav.persistance.storage.IStorage;
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
            postgreSQLStorageImpl.clear();
            postgreSQLStorageImpl.initialize();
        }
    }

    @After
    public void tearDown() throws Exception {
    }
}
