package kz.bsbnb.usci.bconv.xsd;

import junit.framework.Assert;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;

//TODO: Test fails sometimes, can't load DB ddl file from jar
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

    @Autowired
    private Xsd2MetaClass converter;

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
    @Ignore
    public void testMetaClassFromXSDPersistance() throws Exception
    {
        InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream("credit-registry.xsd");

        System.out.println("------- Parsing -------------");
        MetaClass meta = converter.convertXSD(in, "ct_package");

        System.out.println("------- Saving --------------");
        long id = metaClassDao.save(meta);

        System.out.println("------- Loading -------------");
        MetaClass metaClassLoadedById = metaClassDao.load(id);

        //TODO: Assert fails sometimes
        Assert.assertEquals(meta, metaClassLoadedById);

        System.out.println("------- Loading by name -----");
        MetaClass metaClassLoadedByName = metaClassDao.load("ct_package");

        //TODO: Assert fails sometimes
        Assert.assertEquals(meta, metaClassLoadedByName);
    }

    public Xsd2MetaClass getConverter()
    {
        return converter;
    }

    public void setConverter(Xsd2MetaClass converter)
    {
        this.converter = converter;
    }

    public IMetaClassDao getMetaClassDao()
    {
        return metaClassDao;
    }

    public void setMetaClassDao(IMetaClassDao metaClassDao)
    {
        this.metaClassDao = metaClassDao;
    }

    public IStorage getPostgreSQLStorageImpl()
    {
        return postgreSQLStorageImpl;
    }

    public void setPostgreSQLStorageImpl(IStorage postgreSQLStorageImpl)
    {
        this.postgreSQLStorageImpl = postgreSQLStorageImpl;
    }

    public IBatchRepository getBatchRepository()
    {
        return batchRepository;
    }

    public void setBatchRepository(IBatchRepository batchRepository)
    {
        this.batchRepository = batchRepository;
    }
}
