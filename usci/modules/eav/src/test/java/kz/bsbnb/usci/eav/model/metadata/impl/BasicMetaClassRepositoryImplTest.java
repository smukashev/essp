package kz.bsbnb.usci.eav.model.metadata.impl;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao.PostgreSQLBaseEntityDaoImplTest;
import kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao.PostgreSQLMetaClassDaoImplTest;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class BasicMetaClassRepositoryImplTest {

    private final Logger logger = LoggerFactory.getLogger(BasicMetaClassRepositoryImplTest.class);

    @Autowired
    BasicMetaClassRepositoryImpl basicMetaClassRepositoryImpl;

    @Autowired
    IStorage storageImpl;
    @Autowired
    IMetaClassDao metaClassDaoImpl;

    @Test
    public void getMetaClass()
    {
        logger.debug("getMetaClass test started");

        try {
            storageImpl.initialize();

            logger.debug("Create metadata test");

            MetaClass metaCreate = PostgreSQLMetaClassDaoImplTest.generateFullMetaClass();

            long id = metaClassDaoImpl.save(metaCreate);

            MetaClass loadedByNameMetaCreate = basicMetaClassRepositoryImpl.getMetaClass("testClass");

            assertTrue(metaCreate.equals(loadedByNameMetaCreate));
        }
        finally
        {
            storageImpl.clear();
        }
    }
}
