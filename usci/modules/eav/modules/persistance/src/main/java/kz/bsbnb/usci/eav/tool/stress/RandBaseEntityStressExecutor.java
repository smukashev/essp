package kz.bsbnb.usci.eav.tool.stress;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.util.SetUtils;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.tool.generator.random.data.impl.BaseEntityGenerator;
import kz.bsbnb.usci.eav.tool.generator.random.data.impl.MetaClassGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.*;
import java.util.*;
import java.util.Date;


public class RandBaseEntityStressExecutor {
    private final static Logger logger = LoggerFactory.getLogger(RandBaseEntityStressExecutor.class);
    private final static int dataSize = 1000;

    public static void main(String[] args) {
        System.out.println("Test started at: " + Calendar.getInstance().getTime());

        //big entities
        MetaClassGenerator metaClassGenerator = new MetaClassGenerator(25, 20, 2, 4);

        //moderate entities
        //MetaClassGenerator metaClassGenerator = new MetaClassGenerator(15, 10, 2, 2);
        BaseEntityGenerator baseEntityGenerator = new BaseEntityGenerator();

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("applicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao metaClassDao = ctx.getBean(IMetaClassDao.class);
        IBaseEntityDao baseEntityDao = ctx.getBean(IBaseEntityDao.class);
        IBatchDao batchDao = ctx.getBean(IBatchDao.class);
        SQLQueriesStats stats = ctx.getBean(SQLQueriesStats.class);

        ArrayList<MetaClass> data = new ArrayList<MetaClass>();

        try {
            if(!storage.testConnection()) {
                logger.error("Can't connect to storage.");
                System.exit(1);
            }

            storage.clear();
            storage.initialize();

            System.out.println("Generation MetaClasses : ..........");
            System.out.print(  "Progress               : ");

            for(int i = 0; i < dataSize; i++) {
                double t1;
                double t2;

                t1 = System.nanoTime();
                MetaClass metaClass = metaClassGenerator.generateMetaClass();
                t2 = (System.nanoTime() - t1) / 1000;

                stats.put("_META_CLASS_GENERATION", t2);

                t1 = System.nanoTime();
                long metaClassId = metaClassDao.save(metaClass);
                t2 = (System.nanoTime() - t1) / 1000;

                stats.put("_META_CLASS_SAVE", t2);

                t1 = System.nanoTime();
                metaClass = metaClassDao.load(metaClassId);
                t2 = (System.nanoTime() - t1) / 1000;

                stats.put("_META_CLASS_LOAD", t2);

                data.add(i, metaClass);

                //if(i % (dataSize / 10) == 0)
                    System.out.print(".");
            }

            System.out.println();

            // --------

            Batch batch = new Batch(new Timestamp(new Date().getTime()), new java.sql.Date(new Date().getTime()));

            long batchId = batchDao.save(batch);

            batch = batchDao.load(batchId);

            long index = 0L;

            System.out.println("Generation BaseEntities: ..........");
            System.out.print(  "Progress               : ");

            int i = 0;
            for (MetaClass metaClass : data) {
                double t1;
                double t2;

                t1 = System.nanoTime();
                BaseEntity baseEntityCreate = baseEntityGenerator.generateBaseEntity(batch, metaClass, ++index);
                t2 = (System.nanoTime() - t1) / 1000;

                stats.put("_BASE_ENTITY_GENERATION", t2);

                t1 = System.nanoTime();
                // TODO: Fix this block
                //long baseEntityId = baseEntityDao.save(baseEntityCreate);
                t2 = (System.nanoTime() - t1) / 1000;

                stats.put("_BASE_ENTITY_SAVE", t2);

                t1 = System.nanoTime();
                // TODO: Fix this block
                //BaseEntity baseEntityLoad = baseEntityDao.load(baseEntityId);
                t2 = (System.nanoTime() - t1) / 1000;

                stats.put("_BASE_ENTITY_LOAD", t2);

                i++;
                //if(i % (dataSize / 10) == 0)
                    System.out.print(".");
            }
        } finally {
            metaClassGenerator.printStats();

            SQLQueriesStats sqlStats = ctx.getBean(SQLQueriesStats.class);

            HashMap<String, Long> tableCounts = storage.tableCounts();

            System.out.println();
            System.out.println("+---------+");
            System.out.println("|  count  |");
            System.out.println("+---------+");
            List<String> tables = SetUtils.asSortedList(tableCounts.keySet());
            for (String table : tables) {
                long count = tableCounts.get(table);
                System.out.printf("| %7d | %s%n", count, table);
            }
            System.out.println("+---------+");


            //storage.clear();

            if(sqlStats != null) {
                System.out.println();
                System.out.println("+---------+------------------+------------------------+");
                System.out.println("|  count  |     avg (ms)     |       total (ms)       |");
                System.out.println("+---------+------------------+------------------------+");

                List<String> queries = SetUtils.asSortedList(sqlStats.getStats().keySet());
                for (String query : queries) {
                    QueryEntry qe = sqlStats.getStats().get(query);

                    System.out.printf("| %7d | %16.6f | %22.6f | %s%n", qe.count,
                            qe.totalTime / qe.count, qe.totalTime, query);
                }

                System.out.println("+---------+------------------+------------------------+");
            } else {
                System.out.println("SQL stats off.");
            }
        }
    }
}
