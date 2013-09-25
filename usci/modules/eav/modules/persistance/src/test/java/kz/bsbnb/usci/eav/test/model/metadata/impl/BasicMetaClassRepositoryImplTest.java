package kz.bsbnb.usci.eav.test.model.metadata.impl;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.impl.MetaClassRepositoryImpl;
import kz.bsbnb.usci.eav.test.GenericTestCase;
import kz.bsbnb.usci.eav.test.postgresql.dao.PostgreSQLMetaClassDaoImplTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("oracle")
public class BasicMetaClassRepositoryImplTest  extends GenericTestCase
{

    private final Logger logger = LoggerFactory.getLogger(BasicMetaClassRepositoryImplTest.class);

    @Autowired
    MetaClassRepositoryImpl basicMetaClassRepositoryImpl;

    @Autowired
    IMetaClassDao metaClassDaoImpl;

    @Test
    public void getMetaClass()
    {
        logger.debug("getMetaClass test started");

        logger.debug("Create meta test");

        MetaClass metaCreate = PostgreSQLMetaClassDaoImplTest.generateFullMetaClass();

        long id = metaClassDaoImpl.save(metaCreate);

        MetaClass loadedByNameMetaCreate = basicMetaClassRepositoryImpl.getMetaClass("testClass");

        assertTrue(metaCreate.equals(loadedByNameMetaCreate));
    }
}
