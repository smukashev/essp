package kz.bsbnb.usci.eav.tool.stress;

import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.tool.generator.random.data.impl.BaseEntityGenerator;
import kz.bsbnb.usci.eav.tool.generator.random.data.impl.MetaClassGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BaseEntitySearcherStressExecutor {
    private final static Logger logger = LoggerFactory.getLogger(BaseEntitySearcherStressExecutor.class);
    private final static int dataSize = 100;

    public static void main(String[] args) {
        BasicBaseEntitySearcher bes = new BasicBaseEntitySearcher();
        BasicBaseEntityComparator bec = new BasicBaseEntityComparator();

        System.out.println("Test started at: " + Calendar.getInstance().getTime());

        MetaClassGenerator metaClassGenerator = new MetaClassGenerator(25, 20, 2, 4);
        BaseEntityGenerator baseEntityGenerator = new BaseEntityGenerator();

        ClassPathXmlApplicationContext ctx
                = new ClassPathXmlApplicationContext("applicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao metaClassDao = ctx.getBean(IMetaClassDao.class);
        IBaseEntityDao baseEntityDao = ctx.getBean(IBaseEntityDao.class);
        IBatchDao batchDao = ctx.getBean(IBatchDao.class);

        ArrayList<MetaClass> data = new ArrayList<MetaClass>();

        try {
            if(!storage.testConnection()) {
                logger.error("Can't connect to storage.");
                System.exit(1);
            }

            storage.clear();
            storage.initialize();

            System.out.println("Generation: ..........");
            System.out.print(  "Progress  : ");

            for(int i = 0; i < dataSize; i++) {
                MetaClass metaClass = metaClassGenerator.generateMetaClass();
                long metaClassId = metaClassDao.save(metaClass);

                metaClass = metaClassDao.load(metaClassId);

                data.add(i, metaClass);

                if(i % (dataSize / 10) == 0)
                    System.out.print(".");
            }

            System.out.println();

            // --------

            Batch batch = new Batch(new Timestamp(new Date().getTime()), new java.sql.Date(new Date().getTime()));

            long batchId = batchDao.save(batch);
            batch = batchDao.load(batchId);

            long index = 0L;

            for (MetaClass metaClass : data) {
                BaseEntity baseEntity = baseEntityGenerator.generateBaseEntity(batch, metaClass, ++index);

                if (!bec.compare(baseEntity, baseEntity))
                    logger.error("Same objects rejected by searcher");
            }
        } finally {
            storage.clear();
        }
    }
}
