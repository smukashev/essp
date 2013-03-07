package kz.bsbnb.usci.eav_persistance.tool.stress;

import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav_persistance.persistance.storage.IStorage;
import kz.bsbnb.usci.eav_persistance.stats.QueryEntry;
import kz.bsbnb.usci.eav_persistance.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.impl.MetaClassGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Calendar;

public class MetaClassStressExecutor
{
    private final static Logger logger = LoggerFactory.getLogger(MetaClassStressExecutor.class);

    private final static int dataSize = 100;

    public static void main(String[] args)
    {
        System.out.println("Test started at: " + Calendar.getInstance().getTime());

        MetaClassGenerator gen = new MetaClassGenerator(25, 2, 2);

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("stressApplicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao dao = ctx.getBean(IMetaClassDao.class);

        ArrayList<MetaClass> data = new ArrayList<MetaClass>();

        try
        {
            if(!storage.testConnection())
            {
                logger.error("Can't connect to storage.");
                System.exit(1);
            }

            storage.clear();
            storage.initialize();

            System.out.println("Generation: ..........");
            System.out.print(  "Progress  : ");

            for(int i = 0; i < dataSize; i++)
            {
                MetaClass metaClass = gen.generateMetaClass();

                dao.save(metaClass);
                data.add(i, metaClass);

                if(i % (dataSize / 10) == 0)
                    System.out.print(".");
            }

            System.out.println();

            // --------

            System.out.println("Testing   : ..........");
            System.out.print(  "Progress  : ");

            int delta = data.size() / 10;
            int i = 0;

            for(MetaClass mc : data)
            {
                MetaClass loadedMetaClassById = dao.load(mc.getId());

                if(!mc.equals(loadedMetaClassById))
                    logger.error("Mismatch with loaded by Id");

                try
                {
                    MetaClass loadedMetaClassByName = dao.load(mc.getClassName());

                    if(!mc.equals(loadedMetaClassByName))
                        logger.error("Mismatch with loaded by Name");
                }
                catch(IllegalArgumentException e)
                {
                    if(mc.isDisabled())
                        logger.debug("Disabled class skipped");
                    else
                        logger.error("Can't load class: " + e.getMessage());
                }

                i++;

                if(i > delta)
                {
                    i = 0;
                    System.out.print(".");
                }
            }

            System.out.println();

            System.out.println("Removing  : ..........");
            System.out.print("Progress  : ");

            delta = gen.getMetaClasses().size() / 10;

            i = 0;

            for(MetaClass mc : gen.getMetaClasses())
            {
                dao.remove(mc);

                i++;

                if(i > delta)
                {
                    i = 0;
                    System.out.print(".");
                }
            }

            if(!storage.isClean())
                logger.error("Storage is not clean after test");
        }
        finally
        {
            storage.clear();
        }

        System.out.println();
        System.out.println("Test ended at: " + Calendar.getInstance().getTime());

        gen.printStats();

        System.out.println("-------------------------------------");

        SQLQueriesStats sqlStats = ctx.getBean(SQLQueriesStats.class);
            storage.clear();

        if(sqlStats != null)
        {
            System.out.println("+---------+-----+-----------+");
            System.out.println("|  count  | avg |   total   |");
            System.out.println("+---------+-----+-----------+");

            for (String query : sqlStats.getStats().keySet())
            {
                QueryEntry qe = sqlStats.getStats().get(query);

                System.out.printf("| %7d | %3d | %9d | %s%n", qe.count,
                        qe.totalTime / qe.count, qe.totalTime, query);
            }

            System.out.println("+---------+-----+-----------+");
        }
        else
        {
            System.out.println("SQL stats off");
        }
    }
}
