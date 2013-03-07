package kz.bsbnb.usci.eav_persistance.tool.stress;

import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav_persistance.persistance.storage.IStorage;
import kz.bsbnb.usci.eav_persistance.stats.QueryEntry;
import kz.bsbnb.usci.eav_persistance.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.impl.BaseEntityGenerator;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.impl.MetaClassGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BaseEntityStressExecutor
{
    private final static Logger logger = LoggerFactory.getLogger(BaseEntityStressExecutor.class);

    private final static int dataSize = 10;

    public static void main(String[] args)
    {
        System.out.println("Test started at: " + Calendar.getInstance().getTime());

        MetaClassGenerator metaClassGenerator = new MetaClassGenerator(25, 20, 2, 4);
        BaseEntityGenerator baseEntityGenerator = new BaseEntityGenerator();

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("stressApplicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao metaClassDao = ctx.getBean(IMetaClassDao.class);
        IBaseEntityDao baseEntityDao = ctx.getBean(IBaseEntityDao.class);
        IBatchDao batchDao = ctx.getBean(IBatchDao.class);

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

            System.out.println("Generation MetaClasses : ..........");
            System.out.print(  "Progress               : ");

            for(int i = 0; i < dataSize; i++)
            {
                MetaClass metaClass = metaClassGenerator.generateMetaClass();

                long metaClassId = metaClassDao.save(metaClass);

                metaClass = metaClassDao.load(metaClassId);

                data.add(i, metaClass);

                if(i % (dataSize / 10) == 0)
                    System.out.print(".");
            }

            System.out.println();

            // --------

            Batch batch = new Batch(new Timestamp(new Date().getTime()));

            long batchId = batchDao.save(batch);

            batch = batchDao.load(batchId);

            long index = 0L;

            System.out.println("Generation BaseEntities: ..........");
            System.out.print(  "Progress               : ");

            int i = 0;
            for (MetaClass metaClass : data)
            {
                BaseEntity baseEntity = baseEntityGenerator.generateBaseEntity(batch, metaClass, ++index);
                baseEntityDao.save(baseEntity);

                i++;
                if(i % (dataSize / 10) == 0)
                    System.out.print(".");
            }
        }
        finally
        {
            metaClassGenerator.printStats();

            SQLQueriesStats sqlStats = ctx.getBean(SQLQueriesStats.class);
            storage.clear();

            if(sqlStats != null)
            {
                System.out.println();
                System.out.println("+---------+------------+------------------+");
                System.out.println("|  count  |    avg     |      total       |");
                System.out.println("+---------+------------+------------------+");

                for (String query : sqlStats.getStats().keySet())
                {
                    QueryEntry qe = sqlStats.getStats().get(query);

                    System.out.printf("| %7d | %10.6f | %16.6f | %s%n", qe.count,
                            qe.totalTime / qe.count, qe.totalTime, query);
                }

                System.out.println("+---------+------------+------------------+");
            }
            else
            {
                System.out.println("SQL stats off.");
            }

            storage.clear();
        }
    }
}
