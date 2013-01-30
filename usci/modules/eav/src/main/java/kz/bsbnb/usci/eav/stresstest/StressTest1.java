package kz.bsbnb.usci.eav.stresstest;

import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Calendar;

public class StressTest1 {
    private final static Logger logger = LoggerFactory.getLogger(StressTest1.class);

    private final static int dataSize = 10;

    public static void main(String[] args) {
        System.out.println("Test started at: " + Calendar.getInstance().getTime());

        MetaDataGenerator gen = new MetaDataGenerator(25, 2);

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextStressTest1.xml");

        // instantiate our spring dao object from the application context
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
                MetaClass metaClass = gen.generateMetaClass(0);

                dao.save(metaClass);
                data.add(i, metaClass);

                if(i % (dataSize / 10) == 0)
                {
                    System.out.print(".");
                }
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
                MetaClass loadedMetaClassByName = dao.load(mc.getClassName());

                if(!mc.equals(loadedMetaClassById))
                {
                    logger.error("Mismatch with loaded by Id");
                }

                if(!mc.equals(loadedMetaClassByName))
                {
                    logger.error("Mismatch with loaded by Name");
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
            System.out.print(  "Progress  : ");
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
            {
                logger.error("Storage is not clean after test");
            }
        }
        finally {
            storage.clear();
        }
        System.out.println();
        System.out.println("Test ended at: " + Calendar.getInstance().getTime());
        gen.printStats();

        System.out.println("-------------------------------------");
        SQLQueriesStats sqlStats = ctx.getBean(SQLQueriesStats.class);

        if(sqlStats != null)
        {
            for (String query : sqlStats.getStats().keySet())
            {
                QueryEntry qe = sqlStats.getStats().get(query);
                System.out.println("c: " + qe.count + ", a: " + (qe.totalTime / qe.count) + ", q: " + query);
            }
        }
        else
        {
            System.out.println("SQL stats off");
        }
    }

}
