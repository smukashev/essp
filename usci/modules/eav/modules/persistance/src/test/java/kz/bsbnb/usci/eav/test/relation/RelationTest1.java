package kz.bsbnb.usci.eav.test.relation;

import junit.framework.Assert;
import kz.bsbnb.usci.eav.test.GenericTestCase;
import kz.bsbnb.usci.eav.comparator.IBaseEntityComparator;
import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.factory.IMetaFactory;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.dao.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.Date;

import static junit.framework.Assert.fail;

/**
 * @author k.tulbassiyev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class RelationTest1  extends GenericTestCase
{
    private final Logger logger = LoggerFactory.getLogger(RelationTest1.class);

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    IBatchDao batchDao;

    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    IMetaFactory metaFactory;

    @Autowired
    IBaseEntitySearcherPool baseEntitySearcherPool;

    @Test
    public void MetaClassBaseEntityRelation()
    {
        Batch batch = new Batch(new Timestamp(new Date().getTime()), new java.sql.Date(new Date().getTime()));
        batchDao.save(batch);

        metaClassDao.save(generateMetaClass());

        BaseEntity contractEntity = generateBaseEntity(batch, metaFactory);

        // TODO: Fix this block
        //long id = baseEntityDao.save(contractEntity);

        //BaseEntity contractEntityTest = baseEntityDao.load(id);

        //TODO: fix this
        //Assert.assertTrue(contractEntity.equals(contractEntityTest));
    }

    @Test
    public void equalsTest()
    {
        Batch batch = new Batch(new Timestamp(new Date().getTime()), new java.sql.Date(new Date().getTime()));
        batchDao.save(batch);

        metaClassDao.save(generateMetaClass());

        BaseEntity contractEntity = generateBaseEntity(batch, metaFactory);
        BaseEntity contractEntity1 = generateBaseEntity(batch, metaFactory);

        Assert.assertEquals(contractEntity, contractEntity1);

        BaseEntity subject = (BaseEntity)contractEntity1.getBaseValue("subject").getValue();

        BaseEntity nameEntity = (BaseEntity)subject.getBaseValue("name").getValue();

        nameEntity.put("firstname", new BaseValue(batch, 6, "KANAT_some_fix"));

        Assert.assertFalse(contractEntity.equals(contractEntity1));
    }

    @Test
    public void compareTest()
    {
        if (baseEntitySearcherPool == null)
        {
            fail("No base entity searcher found in spring config!");
        }
        Batch batch = new Batch(new Timestamp(new Date().getTime()), new java.sql.Date(new Date().getTime()));
        batchDao.save(batch);

        metaClassDao.save(generateMetaClass());

        BaseEntity contractEntity = generateBaseEntity(batch, metaFactory);
        BaseEntity contractEntity1 = generateBaseEntity(batch, metaFactory);

        //IBaseEntitySearcher baseEntitySearcher = baseEntitySearcherPool.
          //      getSearcher(contractEntity.getMeta().getClassName());

        IBaseEntityComparator baseEntityComparator = new BasicBaseEntityComparator();

        logger.debug("Trying same objects");
        Assert.assertTrue(baseEntityComparator.compare(contractEntity, contractEntity1));

        BaseEntity subject = (BaseEntity)contractEntity1.getBaseValue("subject").getValue();

        BaseEntity nameEntity = (BaseEntity)subject.getBaseValue("name").getValue();

        nameEntity.put("firstname", new BaseValue(batch, 6, "KANAT_some_fix"));

        logger.debug("Trying changed first name objects");
        Assert.assertFalse(baseEntityComparator.compare(contractEntity, contractEntity1));
    }
}
