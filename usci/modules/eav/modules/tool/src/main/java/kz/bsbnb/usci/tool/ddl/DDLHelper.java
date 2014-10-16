package kz.bsbnb.usci.tool.ddl;

import kz.bsbnb.ddlutils.Platform;
import kz.bsbnb.ddlutils.PlatformFactory;
import kz.bsbnb.ddlutils.io.DatabaseIO;
import kz.bsbnb.ddlutils.model.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.sql.DataSource;

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
            platform.alterTables(targetModel, false);
        }
        else
        {
            platform.createModel(targetModel, false, true);
        }
    }

    public static void dropDatabase(DataSource dataSource,
                                      Database   targetModel)
    {
        Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);

        platform.dropModel(targetModel, true);
    }

    public static void main(String[] args)
    {
        logger.info("DBHelper started");

        if(args.length < 2)
        {
            System.out.println("Usage: <command> <filename> [applicationContext]");
            System.out.println("Commands: gen, apply");
            return;
        }

        AbstractXmlApplicationContext ctx = args.length > 2 ?
                //new FileSystemXmlApplicationContext("file:" + args[2]) :
                new FileSystemXmlApplicationContext("file:" + args[2]) :
                new ClassPathXmlApplicationContext("applicationContextDDL.xml");

//        ClassPathXmlApplicationContext ctx
//                = new ClassPathXmlApplicationContext("applicationContextDDL.xml");

        DataSource source = ctx.getBean(DataSource.class);

        if(args[0].equals("gen"))
        {
            Database db = readDatabase(source);

            String workingDir = System.getProperty("user.dir");
            System.out.println("Current working directory : " + workingDir);

            writeDatabaseToXML(db, args[1]);
        }
        else if(args[0].equals("apply"))
        {
            DDLHelper.dropDatabase(source,
                    DDLHelper.readDatabaseFromXML(args[1]));

            DDLHelper.changeDatabase(source,
                    DDLHelper.readDatabaseFromXML(args[1]),
                    false);
        }
    }
}
