package kz.bsbnb.usci.bconv.xsd;

import junit.framework.Assert;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContextTest.xml"})
@ActiveProfiles({"postgres"})
public class Xsd2MetaClassPersistenceTest
{
    @Autowired
    private IMetaClassDao metaClassDao;

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

    @Test
    public void testMetaClassFromXSDPersistance() throws Exception
    {
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream("credit-registry.xsd");

        System.out.println("------- Parsing -------------");
        MetaClass meta = Xsd2MetaClass.convertXSD(in, "ct_package");

        System.out.println("------- Saving --------------");
        long id = metaClassDao.save(meta);

        System.out.println("------- Loading -------------");
        MetaClass metaClassLoadedById = metaClassDao.load(id);

        Assert.assertEquals(meta, metaClassLoadedById);

        System.out.println("------- Loading by name -----");
        MetaClass metaClassLoadedByName = metaClassDao.load("ct_package");

        Assert.assertEquals(meta, metaClassLoadedByName);
    }
}
