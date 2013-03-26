package kz.bsbnb.usci.eav.test;

import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericTestCase
{
    @Autowired
    protected IStorage postgreSQLStorageImpl;

    @Autowired
    protected IBatchRepository batchRepository;

    @Before
    public void setUp() throws Exception {
        if(postgreSQLStorageImpl != null)
        {
            postgreSQLStorageImpl.clear();
            postgreSQLStorageImpl.initialize();
        }
        if(batchRepository != null)
        {
            batchRepository.clearCache();
        }
    }

    @After
    public void tearDown() throws Exception {
    }
}
