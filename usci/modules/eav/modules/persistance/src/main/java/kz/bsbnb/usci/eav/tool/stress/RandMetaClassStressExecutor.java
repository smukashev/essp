package kz.bsbnb.usci.eav.tool.stress;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.tool.generator.random.data.impl.MetaClassGenerator;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class RandMetaClassStressExecutor {
    private final static Logger logger = LoggerFactory.getLogger(RandMetaClassStressExecutor.class);

    private final static int dataSize = 1000;

    public static void main(String[] args) {
        System.out.println("Test started at: " + Calendar.getInstance().getTime());

        MetaClassGenerator gen = new MetaClassGenerator(25, 20, 2, 4);

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("applicationContext.xml");

        long t = 0;

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao dao = ctx.getBean(IMetaClassDao.class);
        SQLQueriesStats sqlStats = ctx.getBean(SQLQueriesStats.class);

        ArrayList<MetaClass> data = new ArrayList<MetaClass>();

        if(!storage.testConnection()){
            logger.error("Can't connect to storage.");
            System.exit(1);
        }

        storage.clear();
        storage.initialize();

        System.out.println("Generation: ..........");
        System.out.print(  "Progress  : ");

        for(int i = 0; i < dataSize; i++) {
            t = System.nanoTime();
            MetaClass metaClass = gen.generateMetaClass();
            sqlStats.put("GENERATE_METACLASS", (double)(System.nanoTime() - t) / 1000000);

            t = System.nanoTime();
            dao.save(metaClass);
            sqlStats.put("SAVE_METACLASS", (double)(System.nanoTime() - t) / 1000000);
            data.add(i, metaClass);

            sqlStats.put("HEAP",
                    (double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);

            if(i % (dataSize / 10) == 0)
                System.out.print(".");
        }

        System.out.println();

        // --------

        System.out.println("Testing   : ..........");
        System.out.print(  "Progress  : ");

        int delta = data.size() / 10;
        int i = 0;

        for(MetaClass mc : data) {
            MetaClass loadedMetaClassById = dao.load(mc.getId());

            if(!mc.equals(loadedMetaClassById))
                logger.error("Mismatch with loaded by Id");

            try {
                t = System.nanoTime();
                MetaClass loadedMetaClassByName = dao.load(mc.getClassName());
                sqlStats.put("LOAD_METACLASS", (double)(System.nanoTime() - t) / 1000000);

                if(!mc.equals(loadedMetaClassByName))
                    logger.error("Mismatch with loaded by Name");
            } catch(IllegalArgumentException e) {
                if(mc.isDisabled())
                    logger.debug("Disabled class skipped");
                else
                    logger.error("Can't load class: " + e.getMessage());
            }

            sqlStats.put("HEAP",
                    (double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);

            i++;

            if(i > delta) {
                i = 0;
                System.out.print(".");
            }
        }

        System.out.println();

        System.out.println("Removing  : ..........");
        System.out.print("Progress  : ");

        delta = gen.getMetaClasses().size() / 10;

        i = 0;

        for(MetaClass mc : gen.getMetaClasses()) {
            dao.remove(mc);
            i++;

            if(i > delta) {
                i = 0;
                System.out.print(".");
            }
        }

        if(!storage.isClean())
            logger.error("Storage is not clean after test");


        System.out.println();
        System.out.println("Test ended at: " + Calendar.getInstance().getTime());

        gen.printStats();

        System.out.println("-------------------------------------");

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

        storage.clear();

        if(sqlStats != null) {
            System.out.println("+---------+------------+------------------+");
            System.out.println("|  count  |    avg     |      total       |");
            System.out.println("+---------+------------+------------------+");

            for (String query : sqlStats.getStats().keySet()) {
                QueryEntry qe = sqlStats.getStats().get(query);

                System.out.printf("| %7d | %10.6f | %16.6f | %s%n", qe.count,
                        qe.totalTime / qe.count, qe.totalTime, query);
            }

            System.out.println("+---------+------------+------------------+");
        } else {
            System.out.println("SQL stats off");
        }
    }
}