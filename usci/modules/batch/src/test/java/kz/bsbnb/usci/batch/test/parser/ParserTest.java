package kz.bsbnb.usci.batch.test.parser;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.IMetaFactory;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.tool.generator.data.impl.BaseEntityGenerator;
import kz.bsbnb.usci.eav.tool.generator.data.impl.MetaClassGenerator;
import kz.bsbnb.usci.eav.tool.generator.xml.impl.XmlBaseEntityGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author k.tulbassiyev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class ParserTest
{
    @Autowired
    IStorage storage;

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    IBatchDao batchDao;

    @Autowired
    IMetaFactory metaFactory;

    MetaClassGenerator metaClassGenerator;
    BaseEntityGenerator baseEntityGenerator;

    private final Logger logger = LoggerFactory.getLogger(ParserTest.class);

    private final int DATA_SIZE = 15;

    @Before
    public void init()
    {
        metaClassGenerator = new MetaClassGenerator(25, 2);
        baseEntityGenerator = new BaseEntityGenerator();

        if(!storage.testConnection())
        {
            logger.error("Can't connect to storage.");
            System.exit(1);
        }

        storage.clear();
        storage.initialize();
    }

    @Test
    public void parseTest()
    {
        ArrayList<MetaClass> metaClassList = new ArrayList<MetaClass>();
        ArrayList<BaseEntity> baseEntityList = new ArrayList<BaseEntity>();

        System.out.println("Generation: ..........");
        System.out.print(  "Progress  : ");

        for(int i = 0; i < DATA_SIZE; i++)
        {
            MetaClass metaClass = metaClassGenerator.generateMetaClass(0);

            long metaClassId = metaClassDao.save(metaClass);

            metaClass = metaClassDao.load(metaClassId);

            metaClassList.add(metaClass);

            if(i % (DATA_SIZE * 0.1) == 0)
                System.out.print(".");
        }

        System.out.println();

        // --------

        Batch batch = new Batch(new Timestamp(new Date().getTime()));

        int index = 1;

        for (MetaClass metaClass : metaClassList)
            baseEntityList.add(baseEntityGenerator.generateBaseEntity(batch, metaClass, ++index));

        XmlBaseEntityGenerator xmlBaseEntityGenerator = new XmlBaseEntityGenerator();

        Document document = xmlBaseEntityGenerator.getGeneratedDocument(baseEntityList);

        xmlBaseEntityGenerator.writeToXml(document, "/opt/xmls/test.xml");
    }
}
