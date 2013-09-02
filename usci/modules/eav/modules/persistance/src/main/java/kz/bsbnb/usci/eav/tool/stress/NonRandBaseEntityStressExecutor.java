package kz.bsbnb.usci.eav.tool.stress;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.data.AttributeTree;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.data.BaseEntityGenerator;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.data.MetaClassGenerator;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.helper.TreeGenerator;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.parsers.ParserConfigurationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * @author abukabayev
 */
public class NonRandBaseEntityStressExecutor {
    private final static Logger logger = LoggerFactory.getLogger(NonRandBaseEntityStressExecutor.class);

    private final static int dataSize = 100;

    public static void main(String[] args) throws ParserConfigurationException {
        System.out.println("Test start time: " + Calendar.getInstance().getTime());

        MetaClassGenerator metaClassGenerator = new MetaClassGenerator();

        BaseEntityGenerator baseEntityGenerator = new BaseEntityGenerator();

        AttributeTree tree = new AttributeTree("packages",null);
        TreeGenerator helper = new TreeGenerator();

        tree = helper.generateTree(tree);
        tree = tree.getChildren().get(0);

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("stressApplicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao metaClassDao = ctx.getBean(IMetaClassDao.class);
        IBaseEntityDao baseEntityDao = ctx.getBean(IBaseEntityDao.class);
        IBatchDao batchDao = ctx.getBean(IBatchDao.class);
        SQLQueriesStats stats = ctx.getBean(SQLQueriesStats.class);

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

            System.out.println("Generating MetaClasses : ..........");

            for(int i = 0; i < dataSize; i++)
            {
                double t1;
                double t2;

                t1 = System.nanoTime();
                MetaClass metaClass = metaClassGenerator.generateMetaClass(tree,i);
                t2 = (System.nanoTime() - t1) / 1000000;

                stats.put("_META_CLASS_GENERATION", t2);

                t1 = System.nanoTime();
                long metaClassId = metaClassDao.save(metaClass);
                t2 = (System.nanoTime() - t1) / 1000000;

                stats.put("_META_CLASS_SAVE", t2);

                t1 = System.nanoTime();
                metaClass = metaClassDao.load(metaClassId);
                t2 = (System.nanoTime() - t1) / 1000000;

                stats.put("_META_CLASS_LOAD", t2);

                data.add(i, metaClass);

            }

//            for (MetaClass m:metaClassGenerator.getMetaClasses()){
//                metaClassDao.save(m);
//            }

            System.out.println();

            // --------

            Batch batch = new Batch(new Timestamp(new java.util.Date().getTime()), new java.sql.Date(new java.util.Date().getTime()));

            long batchId = batchDao.save(batch);

            batch = batchDao.load(batchId);

            long index = 0L;

            System.out.println("Generating BaseEntities: ..........");

            for (MetaClass metaClass : data)
            {
                double t1;
                double t2;

                t1 = System.nanoTime();
                BaseEntity baseEntityCreate = baseEntityGenerator.generateBaseEntity(batch, metaClass, ++index);
                t2 = (System.nanoTime() - t1) / 1000000;

                stats.put("_BASE_ENTITY_GENERATION", t2);

                t1 = System.nanoTime();
                // TODO: Fix this block
                //long baseEntityId = baseEntityDao.save(baseEntityCreate);
                t2 = (System.nanoTime() - t1) / 1000000;

                stats.put("_BASE_ENTITY_SAVE", t2);

                t1 = System.nanoTime();
                // TODO: Fix this block
                //BaseEntity baseEntityLoad = baseEntityDao.load(baseEntityId);
                t2 = (System.nanoTime() - t1) / 1000000;

                stats.put("_BASE_ENTITY_LOAD", t2);

            }
        }
        finally
        {

            SQLQueriesStats sqlStats = ctx.getBean(SQLQueriesStats.class);

            HashMap<String, Long> tableCounts = storage.tableCounts();

            System.out.println();
            System.out.println("+---------+");
            System.out.println("|  count  |");
            System.out.println("+---------+");
            List<String> tables = SetUtils.asSortedList(tableCounts.keySet());
            for (String table : tables)
            {
                long count = tableCounts.get(table);

                System.out.printf("| %7d | %s%n", count, table);
            }
            System.out.println("+---------+");


            storage.clear();

            if(sqlStats != null)
            {
                System.out.println();
                System.out.println("+---------+------------------+------------------------+");
                System.out.println("|  count  |     avg (ms)     |       total (ms)       |");
                System.out.println("+---------+------------------+------------------------+");

                List<String> queries = SetUtils.asSortedList(sqlStats.getStats().keySet());
                for (String query : queries)
                {
                    QueryEntry qe = sqlStats.getStats().get(query);

                    System.out.printf("| %7d | %16.6f | %22.6f | %s%n", qe.count,
                            qe.totalTime / qe.count, qe.totalTime, query);
                }

                System.out.println("+---------+------------------+------------------------+");
            }
            else
            {
                System.out.println("SQL stats off.");
            }
            System.out.println("Test ended at: " + Calendar.getInstance().getTime());
        }
    }
}