package kz.bsbnb.usci.eav.tool.stress;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.data.AttributeTree;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.data.MetaClassGenerator;
import kz.bsbnb.usci.eav.tool.generator.nonrandom.helper.TreeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author abukabayev
 */
public class NonRandMetaClassStressExecutor {
    private final static Logger logger = LoggerFactory.getLogger(NonRandMetaClassStressExecutor.class);

    private final static int dataSize = 100;

    public static void main(String[] args) throws ParserConfigurationException {

        System.out.println("Test start time: " + Calendar.getInstance().getTime());

        AttributeTree tree = new AttributeTree("packages",null);
        TreeGenerator helper = new TreeGenerator();

        tree = helper.generateTree(tree);
        tree = tree.getChildren().get(0);

        MetaClassGenerator gen = new MetaClassGenerator();

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

            System.out.println("Generating: ..........");

            for(int i = 0; i < dataSize; i++)
            {
                MetaClass metaClass = gen.generateMetaClass(tree,i);

                Long id = dao.save(metaClass);

                data.add(i, dao.load(id));

            }

            for (MetaClass m:gen.getMetaClasses()){
                dao.save(m);
            }

            System.out.println();

            System.out.println("Testing   : ..........");


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

            }

            System.out.println();

            System.out.println("Removing  : ..........");


            for(MetaClass mc : gen.getMetaClasses())
            {
                dao.remove(mc);
            }


            if(!storage.isClean()){
                logger.error("Storage is not clean after test");
            }
        }
        finally
        {
            storage.clear();
        }

        System.out.println();
        System.out.println("Test end time: " + Calendar.getInstance().getTime());

        System.out.println("-------------------------------------");

        SQLQueriesStats sqlStats = ctx.getBean(SQLQueriesStats.class);
        storage.clear();

        if(sqlStats != null)
        {
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
            System.out.println("SQL stats off");
        }
    }
}