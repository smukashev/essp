package kz.bsbnb.usci.eav.test;

import kz.bsbnb.usci.eav.persistance.storage.IStorage;

import kz.bsbnb.usci.eav.test.comporator.BasicBaseEntityComporatorTest;
import kz.bsbnb.usci.eav.test.model.BaseEntityTest;
import kz.bsbnb.usci.eav.test.model.BatchTest;
import kz.bsbnb.usci.eav.test.model.batchdata.impl.BaseValueTest;
import kz.bsbnb.usci.eav.test.model.batchdata.impl.BatchRepositoryTest;
import kz.bsbnb.usci.eav.test.model.metadata.impl.BasicMetaClassRepositoryImplTest;
import kz.bsbnb.usci.eav.test.postgresql.dao.PostgreSQLBaseEntityDaoImplTest;
import kz.bsbnb.usci.eav.test.postgresql.dao.PostgreSQLBatchDaoImplTest;
import kz.bsbnb.usci.eav.test.postgresql.dao.PostgreSQLMetaClassDaoImplTest;
import kz.bsbnb.usci.eav.test.relation.RelationTest1;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BaseValueTest.class,
        BatchRepositoryTest.class,
        BasicMetaClassRepositoryImplTest.class,
        BaseEntityTest.class, //
        BatchTest.class,
        PostgreSQLBaseEntityDaoImplTest.class,
        PostgreSQLBatchDaoImplTest.class,
        PostgreSQLMetaClassDaoImplTest.class,
       // BasicBaseEntitySearcherTest.class,
        BasicBaseEntityComporatorTest.class,
        RelationTest1.class
            })
public class EavTestsSuit
{
    static Logger logger = LoggerFactory.getLogger(EavTestsSuit.class);
    static ApplicationContext ctx;

    public static IStorage getStorage()
    {
        if (ctx == null)
        {
            ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        }

        return ctx.getBean(IStorage.class);
    }

    @BeforeClass
    public static void setUpClass() {
        IStorage storage = getStorage();

        try
        {

            if(!storage.testConnection())
            {
                logger.error("Can't connect to storage.");
                System.exit(1);
            }

            storage.initialize();

        }
        catch(Exception e) {
            logger.error("Unknown error in EAV tests suit: " + e.getMessage());
            storage.clear();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        IStorage storage = getStorage();

        storage.clear();
    }
}
