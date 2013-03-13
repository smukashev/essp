package kz.bsbnb.usci.eav_persistance.tool.generator.entry;

import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav_persistance.persistance.storage.IStorage;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.impl.BaseEntityGenerator;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.impl.MetaClassGenerator;
import kz.bsbnb.usci.eav_persistance.tool.generator.xml.impl.BaseEntityXmlGenerator;
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
public class GenerateSimpleInsertXml
{
    private final static Logger logger
            = LoggerFactory.getLogger(GenerateSimpleInsertXml.class);

    private final static int DATA_SIZE = 1;
    private static String OS = System.getProperty("os.name").toLowerCase();
    private final static String FILE_PATH_UNIX = "/opt/xmls/test.xml";
    private final static String FILE_PATH_WINDOWS = "D:/DevTemp/test.xml";

    public static void main(String args[])
            throws ParserConfigurationException, TransformerException
    {
        MetaClassGenerator metaClassGenerator = new MetaClassGenerator(25, 20, 2, 3);
        BaseEntityGenerator baseEntityGenerator = new BaseEntityGenerator();

        BaseEntityXmlGenerator baseEntityXmlGenerator = new BaseEntityXmlGenerator();

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("applicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao metaClassDao = ctx.getBean(IMetaClassDao.class);
        IBatchDao batchDao = ctx.getBean(IBatchDao.class);

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
            MetaClass metaClass = metaClassGenerator.generateMetaClass();

            long metaClassId = metaClassDao.save(metaClass);

            metaClass = metaClassDao.load(metaClassId);

            data.add(i, metaClass);
        }

        logger.info("Data has been generated.");

        Batch batch = new Batch(new Timestamp(new Date().getTime()), new java.sql.Date(new Date().getTime()));

        long batchId = batchDao.save(batch);

        batch = batchDao.load(batchId);

        long index = 0L;

        logger.info("Generating xml...");

        for (MetaClass metaClass : data)
            entities.add(baseEntityGenerator.generateBaseEntity(batch, metaClass, ++index));

        Document document = baseEntityXmlGenerator.getGeneratedDocument(entities);

        String filePath;
        if (isWindows()) {
            filePath = FILE_PATH_WINDOWS;
        } else if (isMac()) {
            throw new RuntimeException("OS is not support.");
        } else if (isUnix()) {
            filePath = FILE_PATH_UNIX;
        } else if (isSolaris()) {
            throw new RuntimeException("OS is not support.");
        } else {
            throw new RuntimeException("OS is not support.");
        }

        baseEntityXmlGenerator.writeToXml(document, filePath);

        logger.info("Xml has been generated.");
        logger.info("File " + filePath + " is ready to use.");
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

}
