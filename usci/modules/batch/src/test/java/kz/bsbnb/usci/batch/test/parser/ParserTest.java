package kz.bsbnb.usci.batch.test.parser;

import junit.framework.Assert;
import kz.bsbnb.usci.batch.parser.IParser;
import kz.bsbnb.usci.batch.parser.IParserFactory;
import kz.bsbnb.usci.batch.parser.listener.impl.ListListener;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.IMetaFactory;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.tool.generator.data.impl.BaseEntityGenerator;
import kz.bsbnb.usci.eav.tool.generator.data.impl.MetaClassGenerator;
import kz.bsbnb.usci.eav.tool.generator.xml.impl.BaseEntityXmlGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author k.tulbassiyev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContextTest.xml"})
public class ParserTest
{
    @Autowired
    IStorage storage;

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    IBatchDao batchDao;

    @Autowired
    IParserFactory parserFactory;

    @Autowired
    IMetaFactory metaFactory;

    MetaClassGenerator metaClassGenerator;
    BaseEntityGenerator baseEntityGenerator;

    private final Logger logger = LoggerFactory.getLogger(ParserTest.class);

    private final int DATA_SIZE = 5;
    private final String FILE_PATH = "/opt/xmls/test1.xml";

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
        List<MetaClass> metaClassList = new ArrayList<MetaClass>();
        List<BaseEntity> baseEntityList = new ArrayList<BaseEntity>();

        for(int i = 0; i < DATA_SIZE; i++)
        {
            MetaClass metaClass = metaClassGenerator.generateMetaClass(0);

            long metaClassId = metaClassDao.save(metaClass);

            metaClass = metaClassDao.load(metaClassId);

            metaClassList.add(metaClass);
        }

        Batch batch = new Batch(new Timestamp(new Date().getTime()));

        int index = 1;

        for (MetaClass metaClass : metaClassList)
            baseEntityList.add(baseEntityGenerator.generateBaseEntity(batch, metaClass, index++));

        BaseEntityXmlGenerator baseEntityXmlGenerator = new BaseEntityXmlGenerator();

        Document document = baseEntityXmlGenerator.getGeneratedDocument(baseEntityList);

        baseEntityXmlGenerator.writeToXml(document, FILE_PATH);

        ListListener listener = new ListListener();

        IParser parser = parserFactory.getIParser(FILE_PATH, batch, listener);

        try
        {
            parser.parse();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        List<BaseEntity> pBaseEntityList = listener.getData();

        if(baseEntityList.size() != pBaseEntityList.size())
            fail("Lists are not equal");

        for(int i = 0; i < baseEntityList.size(); i++)
        {
            BaseEntity entity = baseEntityList.get(i);
            BaseEntity pEntity = pBaseEntityList.get(i);

            if(!entity.equals(pEntity))
                fail("Entities are not equal");
        }
    }
}
