/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.test.persistence.dao;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.*;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.test.GenericTestCase;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.Calendar;
import java.util.UUID;


/**
 *
 * @author alexandr motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles({"oracle"})
public class BaseEntityProcessorDaoImplTest extends GenericTestCase
{

    @Autowired
    IBatchRepository batchRepository;
    @Autowired
    IMetaClassDao metaClassDao;
    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    private final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorDaoImplTest.class);
    private IMetaClass metaClass;

    public BaseEntityProcessorDaoImplTest()
    { }

    private IBaseEntity constuctBaseEntity(IMetaClass metaClass, Date reportDate, String uuid, String[] stringValues, long index)
    {
        Batch batch = batchRepository.addBatch(new Batch(reportDate));
        IMetaSet metaSet = (IMetaSet)metaClass.getMemberType("string_set");
        IBaseSet baseSet = new BaseSet(metaSet.getMemberType());
        for (String stringValue: stringValues)
        {
            baseSet.put(
                    BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            metaSet.getMemberType(),
                            batch,
                            index,
                            stringValue));
        }

        BaseEntity baseEntity = new BaseEntity((MetaClass)metaClass, batch.getRepDate());
        baseEntity.put("uuid",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaClass.getMemberType("uuid"),
                        batch, index, uuid));
        baseEntity.put("string_set",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaSet,
                        batch,
                        index,
                        baseSet));

        return baseEntity;
    }

    @Before
    public void initialization() throws Exception
    {
        MetaClass metaForCreate = new MetaClass("testMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("string_set",
                new MetaAttribute(false, true, new MetaSet(new MetaValue(DataTypes.STRING))));

        long metaId = metaClassDao.save(metaForCreate);
        metaClass = metaClassDao.load(metaId);
    }

    @After
    public void finalization() throws Exception
    {

    }

    @Test
    public void test1() throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.YEAR, 2013);
        long time = calendar.getTimeInMillis();
        System.out.println("TIME:" + time);
    }

    @Test
    public void testT() throws Exception
    {
        UUID uuid = UUID.randomUUID();

        // 1 january 2013
        IBaseEntity baseEntityAB1 =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        uuid.toString(),
                        new String[] {"a", "b"}, 1L);

        BaseEntity baseEntityAB1Saved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB1);

        // 1 march 2013
        IBaseEntity baseEntityAB2 =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        uuid.toString(),
                        new String[] {"a", "b"}, 1L);

        BaseEntity baseEntityAB2Saved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB2);

        // 1 april 2013
        IBaseEntity baseEntityAD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1364752800000")),
                        uuid.toString(),
                        new String[] {"a", "d"}, 2L);

        BaseEntity baseEntityADSaved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAD);

        // 1 february 2013
        IBaseEntity baseEntityAC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        uuid.toString(),
                        new String[] {"a", "c"}, 3L);

        BaseEntity baseEntityACSaved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAC);
    }

    @Test
    public void test2() throws Exception
    {
        UUID uuid = UUID.randomUUID();

        // 1 january 2013
        IBaseEntity baseEntityAB =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        uuid.toString(),
                        new String[] {"a", "b"}, 1L);

        BaseEntity baseEntityABSaved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB);

        // 1 march 2013
        IBaseEntity baseEntityAD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        uuid.toString(),
                        new String[] {"a", "d"}, 2L);

        BaseEntity baseEntityADSaved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAD);

        // 1 february 2013
        IBaseEntity baseEntityAC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        uuid.toString(),
                        new String[] {"a", "c"}, 3L);

        BaseEntity baseEntityACSaved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAC);
    }

    @Test
    public void test3() throws Exception
    {
        UUID uuid = UUID.randomUUID();

        // 1 january 2013
        IBaseEntity baseEntityAB =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        uuid.toString(),
                        new String[] {"a", "b"}, 1L);

        BaseEntity baseEntityABSaved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB);

        // 1 february 2013
        IBaseEntity baseEntityAC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        uuid.toString(),
                        new String[] {"a", "c"}, 2L);

        BaseEntity baseEntityACSaved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAC);

        // 1 march 2013
        IBaseEntity baseEntityAD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        uuid.toString(),
                        new String[] {"a", "d"}, 3L);

        BaseEntity baseEntityADSaved = (BaseEntity) baseEntityProcessorDao.process(baseEntityAD);
    }



}
