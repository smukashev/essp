package kz.bsbnb.usci.eav_persistance.tool.db;

import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav_persistance.persistance.storage.IStorage;
import kz.bsbnb.usci.eav_persistance.stats.QueryEntry;
import kz.bsbnb.usci.eav_persistance.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.impl.MetaClassGenerator;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Calendar;

public class DDLHelper
{
    private final static Logger logger = LoggerFactory.getLogger(DDLHelper.class);

    public static Database readDatabaseFromXML(String fileName)
    {
        return new DatabaseIO().read(fileName);
    }

    public static void writeDatabaseToXML(Database db, String fileName)
    {
        new DatabaseIO().write(db, fileName);
    }

    public static Database readDatabase(DataSource dataSource)
    {
        Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);

        return platform.readModelFromDatabase("model");
    }

    public static void changeDatabase(DataSource dataSource,
                               Database   targetModel,
                               boolean    alterDb)
    {
        Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);

        if (alterDb)
        {
            platform.alterTables(targetModel, true);
        }
        else
        {
            platform.createTables(targetModel, true, true);
        }
    }

    public static void dropDatabase(DataSource dataSource,
                                      Database   targetModel)
    {
        Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);

        platform.dropTables(targetModel, true);
    }

    public static void main(String[] args)
    {
        logger.info("DBHelper started");

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("applicationContext.xml");

        DataSource source = ctx.getBean(DataSource.class);

        Database db = readDatabase(source);

        String workingDir = System.getProperty("user.dir");
        System.out.println("Current working directory : " + workingDir);

        writeDatabaseToXML(db, "./usci/modules/eav_persistance/target/create_db.ddl");
    }
}
