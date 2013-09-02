package kz.bsbnb.usci.eav.test;

import kz.bsbnb.usci.eav.factory.IMetaFactory;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author a.motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles({"postgres"})
public class BasicBaseEntitySearcherTest extends GenericTestCase
{

    @Autowired
    IBaseEntitySearcher searcher;

    @Autowired
    IBatchDao batchDao;

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    IMetaFactory metaFactory;

	private final Logger logger = LoggerFactory.getLogger(BasicBaseEntitySearcherTest.class);

	public BasicBaseEntitySearcherTest() {
    }

    protected MetaClass generateMetaClass1()
    {
        MetaClass metaStreetHolder = new MetaClass("street");
        metaStreetHolder.setMetaAttribute("lang",
                new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));
        metaStreetHolder.setMetaAttribute("value",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));

        return metaStreetHolder;
    }

    protected BaseEntity generateBaseEntity1(Batch batch, IMetaFactory metaFactory, String str)
    {
        BaseEntity streetEntity = metaFactory.getBaseEntity("street");
        streetEntity.put("lang", new BaseValue(batch, 1, "KAZ"));
        streetEntity.put("value", new BaseValue(batch, 1, str));
        return streetEntity;
    }

    @Test
    public void searcherTest1() throws Exception {
        logger.debug("SearcherTest simple 1");

        Batch batch = new Batch(new Timestamp(new java.util.Date().getTime()), new java.sql.Date(new java.util.Date().getTime()));
        batchDao.save(batch);

        metaClassDao.save(generateMetaClass1());

        BaseEntity streetEntity1 = generateBaseEntity1(batch, metaFactory, "Street1");
        BaseEntity streetEntity2 = generateBaseEntity1(batch, metaFactory, "Street2");
        BaseEntity streetEntity3 = generateBaseEntity1(batch, metaFactory, "Street3");
        BaseEntity streetEntity4 = generateBaseEntity1(batch, metaFactory, "Street4");
        BaseEntity streetEntity5 = generateBaseEntity1(batch, metaFactory, "Street5");

        baseEntityDao.save(streetEntity1);
        baseEntityDao.save(streetEntity2);
        // TODO: Fix this block
        //long id3 = baseEntityDao.save(streetEntity3);
        baseEntityDao.save(streetEntity4);
        baseEntityDao.save(streetEntity5);

        ArrayList<Long> result = searcher.findAll(streetEntity3);

        //assertTrue(result.size() == 1);

        //assertTrue(id3 == result.get(0));
    }

    protected MetaClass generateMetaClass2()
    {
        MetaClass metaStreetHolder = new MetaClass("street");
        metaStreetHolder.setMetaAttribute("lang",
                new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));
        metaStreetHolder.setMetaAttribute("value",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        metaStreetHolder.setMetaAttribute("repDate",
                new MetaAttribute(true, false, new MetaValue(DataTypes.DATE)));

        return metaStreetHolder;
    }

    protected BaseEntity generateBaseEntity2(Batch batch, IMetaFactory metaFactory, String str, Date date)
    {
        BaseEntity streetEntity = metaFactory.getBaseEntity("street");
        streetEntity.put("lang", new BaseValue(batch, 1, "KAZ"));
        streetEntity.put("value", new BaseValue(batch, 1, str));
        streetEntity.put("repDate", new BaseValue(batch, 1, date));

        return streetEntity;
    }

    @Test
    public void searcherTest2() throws Exception {
        logger.debug("SearcherTest simple 2");

        Batch batch = new Batch(new Timestamp(new java.util.Date().getTime()), new java.sql.Date(new java.util.Date().getTime()));
        batchDao.save(batch);

        metaClassDao.save(generateMetaClass2());

        Calendar c = Calendar.getInstance();

        c.set(2010, 01, 01);
        BaseEntity streetEntity1 = generateBaseEntity2(batch, metaFactory, "Street1", new Date(c.getTimeInMillis()));
        c.set(2011, 01, 01);
        BaseEntity streetEntity2 = generateBaseEntity2(batch, metaFactory, "Street2", new Date(c.getTimeInMillis()));
        c.set(2012, 01, 01);
        BaseEntity streetEntity3 = generateBaseEntity2(batch, metaFactory, "Street3", new Date(c.getTimeInMillis()));
        c.set(2013, 01, 01);
        BaseEntity streetEntity4 = generateBaseEntity2(batch, metaFactory, "Street4", new Date(c.getTimeInMillis()));
        c.set(2014, 01, 01);
        BaseEntity streetEntity5 = generateBaseEntity2(batch, metaFactory, "Street5", new Date(c.getTimeInMillis()));

        baseEntityDao.save(streetEntity1);
        baseEntityDao.save(streetEntity2);
        // TODO: Fix this block
        //long id3 = baseEntityDao.save(streetEntity3);
        baseEntityDao.save(streetEntity4);
        baseEntityDao.save(streetEntity5);

        ArrayList<Long> result = searcher.findAll(streetEntity3);

        //assertTrue(result.size() == 1);

        //assertTrue(id3 == result.get(0));
    }
}
