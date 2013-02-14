package kz.bsbnb.usci.tool.entry;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.IMetaFactory;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class InsertData
{
    private final static Logger logger = LoggerFactory.getLogger(InsertData.class);

    public static void main(String[] args)
    {
        System.out.println("Generator started at: " + Calendar.getInstance().getTime());

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("generatorContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);
        IMetaClassDao metaClassDao = ctx.getBean(IMetaClassDao.class);
        IBaseEntityDao baseEntityDao = ctx.getBean(IBaseEntityDao.class);

        if(!storage.testConnection())
        {
            logger.error("Can't connect to storage.");
            System.exit(1);
        }

        storage.clear();
        storage.initialize();

        MetaClass documentMeta = new MetaClass("document", new Timestamp(new Date().getTime()));
        documentMeta.setMemberType("type", new MetaValue(DataTypes.STRING, true, false));
        documentMeta.setMemberType("no", new MetaValue(DataTypes.STRING, true, false));

        metaClassDao.save(documentMeta);

        IMetaFactory metaFactory = ctx.getBean(IMetaFactory.class);

        BaseEntity documentEntity = metaFactory.getBaseEntity("document", null);
        documentEntity.set("type", 0L, "RNN");
        documentEntity.set("no", 0L, "1234567890");

        baseEntityDao.save(documentEntity);

    }

}
