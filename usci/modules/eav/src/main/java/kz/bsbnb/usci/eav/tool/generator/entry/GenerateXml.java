package kz.bsbnb.usci.eav.tool.generator.entry;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.tool.generator.data.impl.BaseEntityGenerator;
import kz.bsbnb.usci.eav.tool.generator.data.impl.MetaClassGenerator;
import kz.bsbnb.usci.eav.tool.generator.xml.impl.BaseEntityXmlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author k.tulbassiyev
 */
public class GenerateXml
{
    private final static Logger logger
            = LoggerFactory.getLogger(GenerateXml.class);

    private final static int DATA_SIZE = 5;
    private final static String FILE_PATH = "/opt/xmls/test.xml";

    public static void main(String args[])
            throws ParserConfigurationException, TransformerException
    {
        MetaClassGenerator metaClassGenerator = new MetaClassGenerator(25, 2);
        BaseEntityGenerator baseEntityGenerator = new BaseEntityGenerator();

        BaseEntityXmlGenerator baseEntityXmlGenerator = new BaseEntityXmlGenerator();

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("stressApplicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao metaClassDao = ctx.getBean(IMetaClassDao.class);

        ArrayList<MetaClass> data = new ArrayList<MetaClass>();
        ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();

        if(!storage.testConnection())
        {
            logger.error("Can't connect to storage.");
            System.exit(1);
        }

        storage.clear();
        storage.initialize();

        logger.info("Generating data...");

        for(int i = 0; i < DATA_SIZE; i++)
        {
            MetaClass metaClass = metaClassGenerator.generateMetaClass(0);

            long metaClassId = metaClassDao.save(metaClass);

            metaClass = metaClassDao.load(metaClassId);

            data.add(i, metaClass);
        }

        logger.info("Data has been generated.");

        Batch batch = new Batch(new Timestamp(new Date().getTime()));

        long index = 0L;

        logger.info("Generating xml...");

        for (MetaClass metaClass : data)
            entities.add(baseEntityGenerator.generateBaseEntity(batch, metaClass, ++index));

        Document document = baseEntityXmlGenerator.getGeneratedDocument(entities);

        baseEntityXmlGenerator.writeToXml(document, FILE_PATH);

        logger.info("Xml has been generated.");
        logger.info("File " + FILE_PATH + " is ready to use.");
    }
}
