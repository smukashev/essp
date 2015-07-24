/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.bsbnb.usci.eav.test.persistence.dao;

import junit.framework.Assert;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseEntityComplexValue;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseSetStringValue;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.*;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
//import kz.bsbnb.usci.eav.test.GenericTestCase;
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
import static org.junit.Assert.*;
import java.util.*;


/**
 *
 * @author alexandr motov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles({"oracle"})
public class BaseEntityProcessorDaoImplTest //extends GenericTestCase
{

    @Autowired
    IBatchRepository batchRepository;
    @Autowired
    IMetaClassDao metaClassDao;
    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    IPersistableDaoPool persistableDaoPool;

    @Autowired
    protected IStorage postgreSQLStorageImpl;

    private final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorDaoImplTest.class);
    private IMetaClass metaClass;

    public BaseEntityProcessorDaoImplTest()
    { }

    public IBaseEntity loadByReportDate(long id, Date actualReportDate) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date reportDate = baseEntityReportDateDao.getMaxReportDate(id, actualReportDate);
        if (reportDate == null) {
            reportDate = baseEntityReportDateDao.getMinReportDate(id, actualReportDate);
            if (reportDate == null)
                throw new RuntimeException("No data found on report date " + actualReportDate + ".");
        }

        return load(id, reportDate, actualReportDate);
    }


    public IBaseEntity load(long id, Date reportDate, Date actualReportDate) {
        IBaseEntityDao baseEntityDao =
                persistableDaoPool.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.load(id, reportDate, actualReportDate);
    }
    
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
    private IBaseEntity constuctBaseEntity(IMetaClass metaClass, Date reportDate, String uuid, Integer integerValue, long index)
    {
        Batch batch = batchRepository.addBatch(new Batch(reportDate));
        BaseEntity baseEntity = new BaseEntity((MetaClass)metaClass, batch.getRepDate());
        baseEntity.put("uuid",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaClass.getMemberType("uuid"),
                        batch, index, uuid));
        baseEntity.put("integer",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaClass.getMemberType("integer"),
                        batch,
                        index,
                        integerValue
                ));

        return baseEntity;
    }

    private IBaseEntity constuctBaseEntity(IMetaClass metaClass, Date reportDate, String uuid, Double doubleValue, long index)
    {
        Batch batch = batchRepository.addBatch(new Batch(reportDate));
        BaseEntity baseEntity = new BaseEntity((MetaClass)metaClass, batch.getRepDate());
        baseEntity.put("uuid",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaClass.getMemberType("uuid"),
                        batch, index, uuid));
        baseEntity.put("double",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaClass.getMemberType("double"),
                        batch,
                        index,
                        doubleValue
                ));

        return baseEntity;
    }

    private IBaseEntity constuctBaseEntity(IMetaClass metaClass, Date reportDate, String uuid, Boolean boolValue, long index)
    {
        Batch batch = batchRepository.addBatch(new Batch(reportDate));
        BaseEntity baseEntity = new BaseEntity((MetaClass)metaClass, batch.getRepDate());
        baseEntity.put("uuid",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaClass.getMemberType("uuid"),
                        batch, index, uuid));
        baseEntity.put("boolean",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaClass.getMemberType("boolean"),
                        batch,
                        index,
                        boolValue
                ));

        return baseEntity;
    }

    private IBaseEntity constuctBaseEntity(IMetaClass metaClass, Date reportDate, String uuid, Date dateValue, long index)
    {
        Batch batch = batchRepository.addBatch(new Batch(reportDate));
        BaseEntity baseEntity = new BaseEntity((MetaClass)metaClass, batch.getRepDate());
        baseEntity.put("uuid",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaClass.getMemberType("uuid"),
                        batch, index, uuid));
        baseEntity.put("date",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaClass.getMemberType("date"),
                        batch,
                        index,
                        dateValue
                ));

        return baseEntity;
    }

    @Before
    public void initialization() throws Exception
    {
        if(postgreSQLStorageImpl != null)
        {
            postgreSQLStorageImpl.clear();
            postgreSQLStorageImpl.initialize();
        }
        if(batchRepository != null)
        {
            batchRepository.clearCache();
        }

        MetaClass metaForCreate = new MetaClass("testMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("string_set",
                new MetaAttribute(false, true, new MetaSet(new MetaValue(DataTypes.STRING))));

        MetaClass childmetaForCreate = new MetaClass("child_meta_class");
        childmetaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        childmetaForCreate.setMetaAttribute("string_set",
                new MetaAttribute(false, true, new MetaSet(new MetaValue(DataTypes.STRING))));

        metaForCreate.setMetaAttribute("child_meta_class", new MetaAttribute(false, true, childmetaForCreate));
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
    public void testSetValue1() throws Exception {


        // 1 january 2013
        IBaseEntity baseEntityAB1 =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "b"}, 1L);

        BaseEntity baseEntityAB1Applied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB1);


        // 1 march 2013
        IBaseEntity baseEntityAB2 =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "b"}, 1L);

        BaseEntity baseEntityAB2Applied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB2);


        // 1 april 2013
        IBaseEntity baseEntityAD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1364752800000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "d"}, 1L);

        BaseEntity baseEntityADApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAD);


        // 1 february 2013
        IBaseEntity baseEntityAC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "c"}, 1L);

        BaseEntity baseEntityACApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAC);

        IBaseEntity baseEntityAB1Saved = loadByReportDate(baseEntityAB1Applied.getId(), new Date(new Long("1356976800000")));
        IBaseEntity baseEntityAB2Saved = loadByReportDate(baseEntityAB2Applied.getId(), new Date(new Long("1362074400000")));
        IBaseEntity baseEntityADSaved = loadByReportDate(baseEntityADApplied.getId(), new Date(new Long("1364752800000")));
        IBaseEntity baseEntityACSaved = loadByReportDate(baseEntityACApplied.getId(), new Date(new Long("1359655200000")));

        assertEquals(baseEntityAB1Applied.getBaseValue("uuid").getValue(), baseEntityAB1Saved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityAB1Applied.getBaseValue("string_set").getValue(), baseEntityAB1Saved.getBaseValue("string_set").getValue());

        assertEquals(baseEntityAB2Applied.getBaseValue("uuid").getValue(), baseEntityAB2Saved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityAB2Applied.getBaseValue("string_set").getValue(), baseEntityAB2Saved.getBaseValue("string_set").getValue());

        assertEquals(baseEntityACApplied.getBaseValue("uuid").getValue(), baseEntityACSaved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityACApplied.getBaseValue("string_set").getValue(), baseEntityACSaved.getBaseValue("string_set").getValue());

        assertEquals(baseEntityADApplied.getBaseValue("uuid").getValue(), baseEntityADSaved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityADApplied.getBaseValue("string_set").getValue(), baseEntityADSaved.getBaseValue("string_set").getValue());
        /*Object[] AB1SavedArray  = ((BaseSet)baseEntityAB1Saved.getBaseValue("string_set").getValue()).get().toArray();
        Object[] AB2SavedArray  = ((BaseSet)baseEntityAB2Saved.getBaseValue("string_set").getValue()).get().toArray();
        Object[] ADSavedArray   = ((BaseSet)baseEntityADSaved.getBaseValue("string_set").getValue()).get().toArray();
        Object[] ACSavedArray   = ((BaseSet)baseEntityACSaved.getBaseValue("string_set").getValue()).get().toArray();

        Assert.assertTrue(((BaseSetStringValue)AB1SavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)AB1SavedArray[1]).getValue().equals("b"));
        Assert.assertTrue(((BaseSetStringValue)AB2SavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)AB2SavedArray[1]).getValue().equals("b"));
        Assert.assertTrue(((BaseSetStringValue)ADSavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)ADSavedArray[1]).getValue().equals("d"));
        Assert.assertTrue(((BaseSetStringValue)ACSavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)ACSavedArray[1]).getValue().equals("c"));
        assertTrue(baseEntityAB1Saved.getMeta().equals(metaClass));*/

    }
    @Test
    public void testSetValue2() throws Exception {
        //UUID uuid = UUID.randomUUID();

        // 1 january 2013
        IBaseEntity baseEntityAB =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "b"}, 1L);

        BaseEntity baseEntityABApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB);


        // 1 march 2013
        IBaseEntity baseEntityAD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "d"}, 2L);

        BaseEntity baseEntityADApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAD);

        // 1 february 2013
        IBaseEntity baseEntityAC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "c"}, 3L);

        BaseEntity baseEntityACApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAC);


        IBaseEntity baseEntityABSaved = loadByReportDate(baseEntityABApplied.getId(), new Date(new Long("1356976800000")));
        IBaseEntity baseEntityADSaved = loadByReportDate(baseEntityADApplied.getId(), new Date(new Long("1362074400000")));
        IBaseEntity baseEntityACSaved = loadByReportDate(baseEntityACApplied.getId(), new Date(new Long("1359655200000")));

        assertEquals(baseEntityABApplied.getBaseValue("uuid").getValue(), baseEntityABSaved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityABApplied.getBaseValue("string_set").getValue(), baseEntityABSaved.getBaseValue("string_set").getValue());

        assertEquals(baseEntityACApplied.getBaseValue("uuid").getValue(), baseEntityACSaved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityACApplied.getBaseValue("string_set").getValue(), baseEntityACSaved.getBaseValue("string_set").getValue());

        assertEquals(baseEntityADApplied.getBaseValue("uuid").getValue(), baseEntityADSaved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityADApplied.getBaseValue("string_set").getValue(), baseEntityADSaved.getBaseValue("string_set").getValue());

       /* Object[] ABSavedArray  = ((BaseSet)baseEntityABSaved.getBaseValue("string_set").getValue()).get().toArray();
        Object[] ADSavedArray  = ((BaseSet)baseEntityADSaved.getBaseValue("string_set").getValue()).get().toArray();
        Object[] ACSavedArray  = ((BaseSet)baseEntityACSaved.getBaseValue("string_set").getValue()).get().toArray();

        Assert.assertTrue(((BaseSetStringValue)ABSavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)ABSavedArray[1]).getValue().equals("b"));
        Assert.assertTrue(((BaseSetStringValue)ADSavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)ADSavedArray[1]).getValue().equals("d"));
        Assert.assertTrue(((BaseSetStringValue)ACSavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)ACSavedArray[1]).getValue().equals("c"));
        assertTrue(baseEntityABSaved.getMeta().equals(metaClass));*/

    }
    @Test
    public void testSetValue3() throws Exception {
       // UUID uuid = UUID.randomUUID();

        // 1 january 2013
        IBaseEntity baseEntityAB =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "b"}, 1L);

        BaseEntity baseEntityABApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB);


        // 1 february 2013
        IBaseEntity baseEntityAC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "c"}, 2L);

        BaseEntity baseEntityACApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAC);


        // 1 march 2013
        IBaseEntity baseEntityAD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "d"}, 3L);

        BaseEntity baseEntityADApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAD);


        IBaseEntity baseEntityABSaved = loadByReportDate(baseEntityABApplied.getId(), new Date(new Long("1356976800000")));
        IBaseEntity baseEntityADSaved = loadByReportDate(baseEntityADApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity baseEntityACSaved = loadByReportDate(baseEntityACApplied.getId(), new Date(new Long("1362074400000")));

        assertEquals(baseEntityABApplied.getBaseValue("uuid").getValue(), baseEntityABSaved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityABApplied.getBaseValue("string_set").getValue(), baseEntityABSaved.getBaseValue("string_set").getValue());

        assertEquals(baseEntityACApplied.getBaseValue("uuid").getValue(), baseEntityACSaved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityACApplied.getBaseValue("string_set").getValue(), baseEntityACSaved.getBaseValue("string_set").getValue());

        assertEquals(baseEntityADApplied.getBaseValue("uuid").getValue(), baseEntityADSaved.getBaseValue("uuid").getValue());
        assertEquals(baseEntityADApplied.getBaseValue("string_set").getValue(), baseEntityADSaved.getBaseValue("string_set").getValue());

        /*Object[] ABSavedArray  = ((BaseSet)baseEntityABSaved.getBaseValue("string_set").getValue()).get().toArray();
        Object[] ADSavedArray  = ((BaseSet)baseEntityADSaved.getBaseValue("string_set").getValue()).get().toArray();
        Object[] ACSavedArray  = ((BaseSet)baseEntityACSaved.getBaseValue("string_set").getValue()).get().toArray();

        Assert.assertTrue(((BaseSetStringValue)ABSavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)ABSavedArray[1]).getValue().equals("b"));
        Assert.assertTrue(((BaseSetStringValue)ADSavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)ADSavedArray[1]).getValue().equals("d"));
        Assert.assertTrue(((BaseSetStringValue)ACSavedArray[0]).getValue().equals("a") && ((BaseSetStringValue)ACSavedArray[1]).getValue().equals("c"));
        assertTrue(baseEntityABSaved.getMeta().equals(metaClass));*/

    }

    @Test
    public void testComplexSetValue1() throws Exception
    {

        // 1 january 2013
        IBaseEntity baseEntityAB =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "b"}, 1L);

        IBaseEntity child_AB =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[]{"a", "c"}, 1L);

        baseEntityAB.put("child_meta_class", new BaseValue(batchRepository.addBatch(new Batch(new Date(new Long("1356976800000")))), 1L, child_AB));
        BaseEntity baseEntityABComplexApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB);



        // 1 february 2013
        IBaseEntity baseEntityAC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "c"}, 2L);

        IBaseEntity child_AC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new String[]{"a", "c"}, 1L);

        baseEntityAC.put("child_meta_class", new BaseValue(batchRepository.addBatch(new Batch(new Date(new Long("1359655200000")))), 1L, child_AC));
        BaseEntity baseEntityACComplexApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAC);


        // 1 march 2013
        IBaseEntity baseEntityAD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "d"}, 3L);

        IBaseEntity child_AD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new String[]{"a", "c"}, 1L);

        baseEntityAD.put("child_meta_class", new BaseValue(batchRepository.addBatch(new Batch(new Date(new Long("1362074400000")))), 1L, child_AD));
        BaseEntity baseEntityADComplexApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAD);



        IBaseEntity baseEntityABComplexSaved = loadByReportDate(baseEntityABComplexApplied.getId(), new Date(new Long("1356976800000")));
        IBaseEntity baseEntityACComplexSaved = loadByReportDate(baseEntityACComplexApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity baseEntityADComplexSaved = loadByReportDate(baseEntityADComplexApplied.getId(), new Date(new Long("1362074400000")));

        assertEquals(((BaseEntity)baseEntityABComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue(),
                ((BaseEntity)baseEntityAB.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue());
        assertEquals(child_AB.getBaseValue("string_set").getValue(),
                (((BaseEntity)baseEntityABComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue()));

        assertEquals(baseEntityABComplexSaved.getBaseValue("string_set").getValue(), baseEntityAB.getBaseValue("string_set").getValue());
        assertEquals(baseEntityABComplexSaved.getReportDate(), baseEntityAB.getReportDate());
        assertEquals(baseEntityABComplexSaved.getBaseValue("uuid").getValue(), baseEntityAB.getBaseValue("uuid").getValue());
        assertEquals(((BaseEntity)baseEntityABComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("uuid").getValue(), child_AB.getBaseValue("uuid").getValue());

        assertEquals(((BaseEntity)baseEntityACComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue(),
                ((BaseEntity)baseEntityAC.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue());
        assertEquals(child_AC.getBaseValue("string_set").getValue(),
                (((BaseEntity)baseEntityACComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue()));

        assertEquals(baseEntityACComplexSaved.getBaseValue("string_set").getValue(), baseEntityAC.getBaseValue("string_set").getValue());
        assertEquals(baseEntityACComplexSaved.getReportDate(), baseEntityAC.getReportDate());
        assertEquals(baseEntityACComplexSaved.getBaseValue("uuid").getValue(), baseEntityAC.getBaseValue("uuid").getValue());
        assertEquals(((BaseEntity)baseEntityACComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("uuid").getValue(), child_AC.getBaseValue("uuid").getValue());

        assertEquals(((BaseEntity)baseEntityADComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue(),
                ((BaseEntity)baseEntityAD.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue());
        assertEquals(child_AD.getBaseValue("string_set").getValue(),
                (((BaseEntity)baseEntityADComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue()));

        assertEquals(baseEntityADComplexSaved.getBaseValue("string_set").getValue(), baseEntityAD.getBaseValue("string_set").getValue());
        assertEquals(baseEntityADComplexSaved.getReportDate(), baseEntityAD.getReportDate());
        assertEquals(baseEntityADComplexSaved.getBaseValue("uuid").getValue(), baseEntityAD.getBaseValue("uuid").getValue());
        assertEquals(((BaseEntity)baseEntityADComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("uuid").getValue(), child_AD.getBaseValue("uuid").getValue());


    }
    @Test
    public void testComplexSetValue2() throws Exception
    {


        // 1 march 2013
        IBaseEntity baseEntityAB =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "b"}, 1L);

        IBaseEntity child_AB =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new String[]{"d", "g"}, 1L);

        baseEntityAB.put("child_meta_class", new BaseValue(batchRepository.addBatch(new Batch(new Date(new Long("1362074400000")))), 1L, child_AB));
        BaseEntity baseEntityABComplexApplied= (BaseEntity) baseEntityProcessorDao.process(baseEntityAB);

        // 1 february 2013
        IBaseEntity baseEntityAC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "c"}, 2L);

        IBaseEntity child_AC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new String[]{"d", "g"}, 1L);
        baseEntityAC.put("child_meta_class", new BaseValue(batchRepository.addBatch(new Batch(new Date(new Long("1359655200000")))), 1L, child_AC));
        BaseEntity baseEntityACComplexApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAC);

        // 1 march 2013
        IBaseEntity baseEntityAD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "d"}, 3L);

        IBaseEntity child_AD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[]{"d", "g"}, 1L);
        baseEntityAD.put("child_meta_class", new BaseValue(batchRepository.addBatch(new Batch(new Date(new Long("1356976800000")))), 1L, child_AD));
        BaseEntity baseEntityADComplexApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAD);

        IBaseEntity baseEntityABComplexSaved = loadByReportDate(baseEntityABComplexApplied.getId(), new Date(new Long("1362074400000")));
        IBaseEntity baseEntityACComplexSaved = loadByReportDate(baseEntityACComplexApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity baseEntityADComplexSaved = loadByReportDate(baseEntityADComplexApplied.getId(), new Date(new Long("1356976800000")));

        assertEquals(((BaseEntity)baseEntityABComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue(),
                ((BaseEntity)baseEntityAB.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue());
        assertEquals(child_AB.getBaseValue("string_set").getValue(),
                (((BaseEntity)baseEntityABComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue()));

        assertEquals(baseEntityABComplexSaved.getBaseValue("string_set").getValue(), baseEntityAB.getBaseValue("string_set").getValue());
        assertEquals(baseEntityABComplexSaved.getReportDate(), baseEntityAB.getReportDate());
        assertEquals(baseEntityABComplexSaved.getBaseValue("uuid").getValue(), baseEntityAB.getBaseValue("uuid").getValue());
        assertEquals(((BaseEntity)baseEntityABComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("uuid").getValue(), child_AB.getBaseValue("uuid").getValue());

        assertEquals(((BaseEntity)baseEntityACComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue(),
                ((BaseEntity)baseEntityAC.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue());
        assertEquals(child_AC.getBaseValue("string_set").getValue(),
                (((BaseEntity)baseEntityACComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue()));

        assertEquals(baseEntityACComplexSaved.getBaseValue("string_set").getValue(), baseEntityAC.getBaseValue("string_set").getValue());
        assertEquals(baseEntityACComplexSaved.getReportDate(), baseEntityAC.getReportDate());
        assertEquals(baseEntityACComplexSaved.getBaseValue("uuid").getValue(), baseEntityAC.getBaseValue("uuid").getValue());
        assertEquals(((BaseEntity)baseEntityACComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("uuid").getValue(), child_AC.getBaseValue("uuid").getValue());

        assertEquals(((BaseEntity)baseEntityADComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue(),
                ((BaseEntity)baseEntityAD.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue());
        assertEquals(child_AD.getBaseValue("string_set").getValue(),
                (((BaseEntity)baseEntityADComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue()));

        assertEquals(baseEntityADComplexSaved.getBaseValue("string_set").getValue(), baseEntityAD.getBaseValue("string_set").getValue());
        assertEquals(baseEntityADComplexSaved.getReportDate(), baseEntityAD.getReportDate());
        assertEquals(baseEntityADComplexSaved.getBaseValue("uuid").getValue(), baseEntityAD.getBaseValue("uuid").getValue());
        assertEquals(((BaseEntity)baseEntityADComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("uuid").getValue(), child_AD.getBaseValue("uuid").getValue());


    }
    @Test
    public void testComplexSetValue3() throws Exception
    {

        // 1 february 2013
        IBaseEntity baseEntityAB =
                constuctBaseEntity(
                        metaClass,
                        //new Date(new Long("1356976800000")),
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "b"}, 1L);

        IBaseEntity child_AB =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new String[]{"d", "g"}, 1L);

        baseEntityAB.put("child_meta_class", new BaseValue(batchRepository.addBatch(new Batch(new Date(new Long("1359655200000")))), 1L, child_AB));

        BaseEntity baseEntityABComplexApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAB);


        // 1 january  2013
        IBaseEntity baseEntityAC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "c"}, 2L);

        IBaseEntity child_AC =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[]{"d", "g"}, 1L);

        baseEntityAC.put("child_meta_class", new BaseValue(batchRepository.addBatch(new Batch(new Date(new Long("1356976800000")))), 1L, child_AC));

        BaseEntity baseEntityACComplexApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAC);


        // 1 march 2013
        IBaseEntity baseEntityAD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new String[] {"a", "d"}, 3L);

        IBaseEntity child_AD =
                constuctBaseEntity(
                        metaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new String[]{"d", "g"}, 1L);
        baseEntityAD.put("child_meta_class", new BaseValue(batchRepository.addBatch(new Batch(new Date(new Long("1362074400000")))), 1L, child_AD));

        BaseEntity baseEntityADComplexApplied = (BaseEntity) baseEntityProcessorDao.process(baseEntityAD);


        IBaseEntity baseEntityABComplexSaved = loadByReportDate(baseEntityABComplexApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity baseEntityACComplexSaved = loadByReportDate(baseEntityACComplexApplied.getId(), new Date(new Long("1356976800000")));
        IBaseEntity baseEntityADComplexSaved = loadByReportDate(baseEntityADComplexApplied.getId(), new Date(new Long("1362074400000")));

        assertEquals(((BaseEntity)baseEntityABComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue(),
                ((BaseEntity)baseEntityAB.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue());
        assertEquals(child_AB.getBaseValue("string_set").getValue(),
                (((BaseEntity)baseEntityABComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue()));

        assertEquals(baseEntityABComplexSaved.getBaseValue("string_set").getValue(), baseEntityAB.getBaseValue("string_set").getValue());
        assertEquals(baseEntityABComplexSaved.getReportDate(), baseEntityAB.getReportDate());
        assertEquals(baseEntityABComplexSaved.getBaseValue("uuid").getValue(), baseEntityAB.getBaseValue("uuid").getValue());
        assertEquals(((BaseEntity)baseEntityABComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("uuid").getValue(), child_AB.getBaseValue("uuid").getValue());

        assertEquals(((BaseEntity)baseEntityACComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue(),
                ((BaseEntity)baseEntityAC.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue());
        assertEquals(child_AC.getBaseValue("string_set").getValue(),
                (((BaseEntity)baseEntityACComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue()));

        assertEquals(baseEntityACComplexSaved.getBaseValue("string_set").getValue(), baseEntityAC.getBaseValue("string_set").getValue());
        assertEquals(baseEntityACComplexSaved.getReportDate(), baseEntityAC.getReportDate());
        assertEquals(baseEntityACComplexSaved.getBaseValue("uuid").getValue(), baseEntityAC.getBaseValue("uuid").getValue());
        assertEquals(((BaseEntity)baseEntityACComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("uuid").getValue(), child_AC.getBaseValue("uuid").getValue());

        assertEquals(((BaseEntity)baseEntityADComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue(),
                ((BaseEntity)baseEntityAD.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue());
        assertEquals(child_AD.getBaseValue("string_set").getValue(),
                (((BaseEntity)baseEntityADComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("string_set").getValue()));

        assertEquals(baseEntityADComplexSaved.getBaseValue("string_set").getValue(), baseEntityAD.getBaseValue("string_set").getValue());
        assertEquals(baseEntityADComplexSaved.getReportDate(), baseEntityAD.getReportDate());
        assertEquals(baseEntityADComplexSaved.getBaseValue("uuid").getValue(), baseEntityAD.getBaseValue("uuid").getValue());
        assertEquals(((BaseEntity)baseEntityADComplexSaved.getBaseValue("child_meta_class").getValue()).getBaseValue("uuid").getValue(), child_AD.getBaseValue("uuid").getValue());


    }

    @Test
    public void testIntegerValue1() throws Exception
    {
        MetaClass metaForCreate = new MetaClass("IntegerMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("integer",
                new MetaAttribute(false, true, new MetaValue(DataTypes.INTEGER)));
        long metaId = metaClassDao.save(metaForCreate);
        IMetaClass IntMetaClass = metaClassDao.load(metaId);
        // 1 march  2013
        IBaseEntity FirstBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        43, 1L);
        BaseEntity FirstBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(FirstBaseEntity);

        // 1 february 2013
        IBaseEntity SecondBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        44, 2L);
        BaseEntity SecondBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(SecondBaseEntity);

        // 1 january 2013
        IBaseEntity ThirdBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        45, 3L);
        BaseEntity ThirdBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(ThirdBaseEntity);

        IBaseEntity FirstBaseEntitySaved = loadByReportDate(FirstBaseEntityApplied.getId(), new Date(new Long("1362074400000")));
        IBaseEntity SecondBaseEntitySaved = loadByReportDate(SecondBaseEntityApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity ThirdBaseEntitySaved = loadByReportDate(ThirdBaseEntityApplied.getId(), new Date(new Long("1356976800000")));

        assertEquals(FirstBaseEntity.getReportDate(), FirstBaseEntitySaved.getReportDate());
        assertEquals(SecondBaseEntity.getReportDate(), SecondBaseEntitySaved.getReportDate());
        assertEquals(ThirdBaseEntity.getReportDate(), ThirdBaseEntitySaved.getReportDate());

        assertEquals(FirstBaseEntity.getBaseValue("uuid").getValue(), FirstBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("uuid").getValue(), SecondBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("uuid").getValue(), ThirdBaseEntitySaved.getBaseValue("uuid").getValue());

        assertEquals(FirstBaseEntity.getBaseValue("integer").getValue(), FirstBaseEntitySaved.getBaseValue("integer").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("integer").getValue(), SecondBaseEntitySaved.getBaseValue("integer").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("integer").getValue(), ThirdBaseEntitySaved.getBaseValue("integer").getValue());


    }
    @Test
    public void testIntegerValue2() throws Exception
    {
        MetaClass metaForCreate = new MetaClass("IntegerMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("integer",
                new MetaAttribute(false, true, new MetaValue(DataTypes.INTEGER)));
        long metaId = metaClassDao.save(metaForCreate);
        IMetaClass IntMetaClass = metaClassDao.load(metaId);
        // 1 february  2013
        IBaseEntity FirstBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        43, 1L);
        BaseEntity FirstBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(FirstBaseEntity);


        // 1 march 2013
        IBaseEntity SecondBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        44, 2L);
        BaseEntity SecondBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(SecondBaseEntity);


        // 1 january 2013
        IBaseEntity ThirdBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        45, 3L);
        BaseEntity ThirdBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(ThirdBaseEntity);


        IBaseEntity FirstBaseEntitySaved = loadByReportDate(FirstBaseEntityApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity SecondBaseEntitySaved = loadByReportDate(SecondBaseEntityApplied.getId(), new Date(new Long("1362074400000")));
        IBaseEntity ThirdBaseEntitySaved = loadByReportDate(ThirdBaseEntityApplied.getId(), new Date(new Long("1356976800000")));

        assertEquals(FirstBaseEntity.getReportDate(), FirstBaseEntitySaved.getReportDate());
        assertEquals(SecondBaseEntity.getReportDate(), SecondBaseEntitySaved.getReportDate());
        assertEquals(ThirdBaseEntity.getReportDate(), ThirdBaseEntitySaved.getReportDate());

        assertEquals(FirstBaseEntity.getBaseValue("uuid").getValue(), FirstBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("uuid").getValue(), SecondBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("uuid").getValue(), ThirdBaseEntitySaved.getBaseValue("uuid").getValue());

        assertEquals(FirstBaseEntity.getBaseValue("integer").getValue(), FirstBaseEntitySaved.getBaseValue("integer").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("integer").getValue(), SecondBaseEntitySaved.getBaseValue("integer").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("integer").getValue(), ThirdBaseEntitySaved.getBaseValue("integer").getValue());
    }

    @Test
    public void testDoubleValue1() throws Exception
    {
        MetaClass metaForCreate = new MetaClass("IntegerMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("double",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DOUBLE)));
        long metaId = metaClassDao.save(metaForCreate);
        IMetaClass IntMetaClass = metaClassDao.load(metaId);
        // 1 march  2013
        IBaseEntity FirstBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new Double(111.111), 1L);
        BaseEntity FirstBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(FirstBaseEntity);

        // 1 february 2013
        IBaseEntity SecondBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new Double(111.222), 2L);
        BaseEntity SecondBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(SecondBaseEntity);

        // 1 january 2013
        IBaseEntity ThirdBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new Double(111.333), 3L);
        BaseEntity ThirdBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(ThirdBaseEntity);


        IBaseEntity FirstBaseEntitySaved = loadByReportDate(FirstBaseEntityApplied.getId(), new Date(new Long("1362074400000")));
        IBaseEntity ThirdBaseEntitySaved = loadByReportDate(ThirdBaseEntityApplied.getId(), new Date(new Long("1356976800000")));
        IBaseEntity SecondBaseEntitySaved = loadByReportDate(SecondBaseEntityApplied.getId(), new Date(new Long("1359655200000")));

        assertEquals(FirstBaseEntity.getReportDate(), FirstBaseEntitySaved.getReportDate());
        assertEquals(SecondBaseEntity.getReportDate(), SecondBaseEntitySaved.getReportDate());
        assertEquals(ThirdBaseEntity.getReportDate(), ThirdBaseEntitySaved.getReportDate());

        assertEquals(FirstBaseEntity.getBaseValue("uuid").getValue(), FirstBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("uuid").getValue(), SecondBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("uuid").getValue(), ThirdBaseEntitySaved.getBaseValue("uuid").getValue());

        assertEquals(FirstBaseEntity.getBaseValue("double").getValue(), FirstBaseEntitySaved.getBaseValue("double").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("double").getValue(), SecondBaseEntitySaved.getBaseValue("double").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("double").getValue(), ThirdBaseEntitySaved.getBaseValue("double").getValue());
    }
    @Test
    public void testDoubleValue2() throws Exception
    {
        MetaClass metaForCreate = new MetaClass("DoubleMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("double",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DOUBLE)));
        long metaId = metaClassDao.save(metaForCreate);
        IMetaClass IntMetaClass = metaClassDao.load(metaId);
        // 1 february  2013
        IBaseEntity FirstBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new Double(111.111), 1L);
        BaseEntity FirstBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(FirstBaseEntity);


        // 1 january 2013
        IBaseEntity SecondBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new Double(111.222), 2L);
        BaseEntity SecondBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(SecondBaseEntity);


        // 1 march 2013
        IBaseEntity ThirdBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new Double(111.333), 3L);
        BaseEntity ThirdBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(ThirdBaseEntity);


        IBaseEntity FirstBaseEntitySaved = loadByReportDate(FirstBaseEntityApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity SecondBaseEntitySaved = loadByReportDate(SecondBaseEntityApplied.getId(), new Date(new Long("1356976800000")));
        IBaseEntity ThirdBaseEntitySaved = loadByReportDate(ThirdBaseEntityApplied.getId(), new Date(new Long("1362074400000")));

        assertEquals(FirstBaseEntity.getReportDate(), FirstBaseEntitySaved.getReportDate());
        assertEquals(SecondBaseEntity.getReportDate(), SecondBaseEntitySaved.getReportDate());
        assertEquals(ThirdBaseEntity.getReportDate(), ThirdBaseEntitySaved.getReportDate());

        assertEquals(FirstBaseEntity.getBaseValue("uuid").getValue(), FirstBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("uuid").getValue(), SecondBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("uuid").getValue(), ThirdBaseEntitySaved.getBaseValue("uuid").getValue());

        assertEquals(FirstBaseEntity.getBaseValue("double").getValue(), FirstBaseEntitySaved.getBaseValue("double").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("double").getValue(), SecondBaseEntitySaved.getBaseValue("double").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("double").getValue(), ThirdBaseEntitySaved.getBaseValue("double").getValue());
    }

    @Test
    public void testBooleanValue1() throws Exception
    {
        MetaClass metaForCreate = new MetaClass("DoubleMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("boolean",
                new MetaAttribute(false, true, new MetaValue(DataTypes.BOOLEAN)));
        long metaId = metaClassDao.save(metaForCreate);
        IMetaClass IntMetaClass = metaClassDao.load(metaId);
        // 1 march  2013
        IBaseEntity FirstBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        true, 1L);
        BaseEntity FirstBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(FirstBaseEntity);


        // 1 february 2013
        IBaseEntity SecondBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        false, 2L);
        BaseEntity SecondBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(SecondBaseEntity);


        // 1 january 2013
        IBaseEntity ThirdBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        false, 3L);
        BaseEntity ThirdBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(ThirdBaseEntity);

        IBaseEntity FirstBaseEntitySaved = loadByReportDate(FirstBaseEntityApplied.getId(), new Date(new Long("1362074400000")));
        IBaseEntity SecondBaseEntitySaved = loadByReportDate(SecondBaseEntityApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity ThirdBaseEntitySaved = loadByReportDate(ThirdBaseEntityApplied.getId(), new Date(new Long("1356976800000")));

        assertEquals(FirstBaseEntity.getReportDate(), FirstBaseEntitySaved.getReportDate());
        assertEquals(SecondBaseEntity.getReportDate(), SecondBaseEntitySaved.getReportDate());
        assertEquals(ThirdBaseEntity.getReportDate(), ThirdBaseEntitySaved.getReportDate());

        assertEquals(FirstBaseEntity.getBaseValue("uuid").getValue(), FirstBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("uuid").getValue(), SecondBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("uuid").getValue(), ThirdBaseEntitySaved.getBaseValue("uuid").getValue());

        assertEquals(FirstBaseEntity.getBaseValue("boolean").getValue(), FirstBaseEntitySaved.getBaseValue("boolean").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("boolean").getValue(), SecondBaseEntitySaved.getBaseValue("boolean").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("boolean").getValue(), ThirdBaseEntitySaved.getBaseValue("boolean").getValue());
    }
    @Test
    public void testBooleanValue2() throws Exception
    {
        MetaClass metaForCreate = new MetaClass("DoubleMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("boolean",
                new MetaAttribute(false, true, new MetaValue(DataTypes.BOOLEAN)));
        long metaId = metaClassDao.save(metaForCreate);
        IMetaClass IntMetaClass = metaClassDao.load(metaId);
        // 1 february  2013
        IBaseEntity FirstBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        true, 1L);
        BaseEntity FirstBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(FirstBaseEntity);

        // 1 january 2013
        IBaseEntity SecondBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        false, 2L);
        BaseEntity SecondBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(SecondBaseEntity);

        // 1 march 2013
        IBaseEntity ThirdBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        false, 3L);
        BaseEntity ThirdBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(ThirdBaseEntity);

        IBaseEntity FirstBaseEntitySaved = loadByReportDate(FirstBaseEntityApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity SecondBaseEntitySaved = loadByReportDate(SecondBaseEntityApplied.getId(), new Date(new Long("1356976800000")));
        IBaseEntity ThirdBaseEntitySaved = loadByReportDate(ThirdBaseEntityApplied.getId(), new Date(new Long("1362074400000")));

        assertEquals(FirstBaseEntity.getReportDate(), FirstBaseEntitySaved.getReportDate());
        assertEquals(SecondBaseEntity.getReportDate(), SecondBaseEntitySaved.getReportDate());
        assertEquals(ThirdBaseEntity.getReportDate(), ThirdBaseEntitySaved.getReportDate());

        assertEquals(FirstBaseEntity.getBaseValue("uuid").getValue(), FirstBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("uuid").getValue(), SecondBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("uuid").getValue(), ThirdBaseEntitySaved.getBaseValue("uuid").getValue());

        assertEquals(FirstBaseEntity.getBaseValue("boolean").getValue(), FirstBaseEntitySaved.getBaseValue("boolean").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("boolean").getValue(), SecondBaseEntitySaved.getBaseValue("boolean").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("boolean").getValue(), ThirdBaseEntitySaved.getBaseValue("boolean").getValue());
    }

    @Test
    public void testDateValue1() throws Exception
    {
        MetaClass metaForCreate = new MetaClass("DateMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("date",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));
        long metaId = metaClassDao.save(metaForCreate);
        IMetaClass IntMetaClass = metaClassDao.load(metaId);
        // 1 march  2013
        IBaseEntity FirstBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new Date(new Long("1362074400000")), 1L);
        BaseEntity FirstBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(FirstBaseEntity);


        // 1 february 2013
        IBaseEntity SecondBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new Date(new Long("1359655200000")), 2L);
        BaseEntity SecondBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(SecondBaseEntity);


        // 1 january 2013
        IBaseEntity ThirdBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new Date(new Long("1356976800000")), 3L);
        BaseEntity ThirdBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(ThirdBaseEntity);


        IBaseEntity FirstBaseEntitySaved = loadByReportDate(FirstBaseEntityApplied.getId(), new Date(new Long("1362074400000")));
        IBaseEntity SecondBaseEntitySaved = loadByReportDate(SecondBaseEntityApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity ThirdBaseEntitySaved = loadByReportDate(ThirdBaseEntityApplied.getId(), new Date(new Long("1356976800000")));

        assertEquals(FirstBaseEntity.getReportDate(), FirstBaseEntitySaved.getReportDate());
        assertEquals(SecondBaseEntity.getReportDate(), SecondBaseEntitySaved.getReportDate());
        assertEquals(ThirdBaseEntity.getReportDate(), ThirdBaseEntitySaved.getReportDate());

        assertEquals(FirstBaseEntity.getBaseValue("uuid").getValue(), FirstBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("uuid").getValue(), SecondBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("uuid").getValue(), ThirdBaseEntitySaved.getBaseValue("uuid").getValue());

        assertEquals(FirstBaseEntity.getBaseValue("date").getValue(), FirstBaseEntitySaved.getBaseValue("date").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("date").getValue(), SecondBaseEntitySaved.getBaseValue("date").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("date").getValue(), ThirdBaseEntitySaved.getBaseValue("date").getValue());
    }
    @Test
    public void testDateValue2() throws Exception
    {
        MetaClass metaForCreate = new MetaClass("DateMetaClass");
        metaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaForCreate.setMetaAttribute("date",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));
        long metaId = metaClassDao.save(metaForCreate);
        IMetaClass IntMetaClass = metaClassDao.load(metaId);
        // 1 february  2013
        IBaseEntity FirstBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1359655200000")),
                        UUID.randomUUID().toString(),
                        new Date(new Long("1359655200000")), 1L);
        BaseEntity FirstBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(FirstBaseEntity);


        // 1 january 2013
        IBaseEntity SecondBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1356976800000")),
                        UUID.randomUUID().toString(),
                        new Date(new Long("1356976800000")), 2L);
        BaseEntity SecondBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(SecondBaseEntity);


        // 1 march 2013
        IBaseEntity ThirdBaseEntity =
                constuctBaseEntity(
                        IntMetaClass,
                        new Date(new Long("1362074400000")),
                        UUID.randomUUID().toString(),
                        new Date(new Long("1362074400000")), 3L);
        BaseEntity ThirdBaseEntityApplied = (BaseEntity) baseEntityProcessorDao.process(ThirdBaseEntity);

        IBaseEntity FirstBaseEntitySaved = loadByReportDate(FirstBaseEntityApplied.getId(), new Date(new Long("1359655200000")));
        IBaseEntity SecondBaseEntitySaved = loadByReportDate(SecondBaseEntityApplied.getId(), new Date(new Long("1356976800000")));
        IBaseEntity ThirdBaseEntitySaved = loadByReportDate(ThirdBaseEntityApplied.getId(), new Date(new Long("1362074400000")));

        assertEquals(FirstBaseEntity.getReportDate(), FirstBaseEntitySaved.getReportDate());
        assertEquals(SecondBaseEntity.getReportDate(), SecondBaseEntitySaved.getReportDate());
        assertEquals(ThirdBaseEntity.getReportDate(), ThirdBaseEntitySaved.getReportDate());

        assertEquals(FirstBaseEntity.getBaseValue("uuid").getValue(), FirstBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("uuid").getValue(), SecondBaseEntitySaved.getBaseValue("uuid").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("uuid").getValue(), ThirdBaseEntitySaved.getBaseValue("uuid").getValue());

        assertEquals(FirstBaseEntity.getBaseValue("date").getValue(), FirstBaseEntitySaved.getBaseValue("date").getValue());
        assertEquals(SecondBaseEntity.getBaseValue("date").getValue(), SecondBaseEntitySaved.getBaseValue("date").getValue());
        assertEquals(ThirdBaseEntity.getBaseValue("date").getValue(), ThirdBaseEntitySaved.getBaseValue("date").getValue());
    }
}
