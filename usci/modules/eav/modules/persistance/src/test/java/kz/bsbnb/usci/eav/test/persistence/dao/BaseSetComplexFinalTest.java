package kz.bsbnb.usci.eav.test.persistence.dao;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.base.impl.value.BaseSetComplexValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.*;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles({"oracle"})

public class BaseSetComplexFinalTest {

    @Autowired
    IMetaClassDao metaDao;

    @Autowired
    IBatchRepository batchRepository;

    MetaClass metaClass;

    @Autowired
    IBaseEntityProcessorDao processorDao;

    @Autowired
    IPersistableDaoPool persistableDaoPool;

    @Autowired
    IBaseEntityLoadDao loadDao;

    @Autowired
    IBaseEntityDao baseEntityDao;

    private Date jan1 = new GregorianCalendar(2015,0,1).getTime();
    private Date feb1 = new GregorianCalendar(2015,1,1).getTime();
    private Date mar1 = new GregorianCalendar(2015,2,1).getTime();
    private Date apr1 = new GregorianCalendar(2015,3,1).getTime();
    private Date may1 = new GregorianCalendar(2015,4,1).getTime();

    private List<IBaseEntity> entitiesToClean;

    @Before
    public void init(){
        entitiesToClean = new LinkedList<>();
        MetaClass rootMeta = new MetaClass("testMetaClass");
        rootMeta.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        rootMeta.setMetaAttribute("string_set",
                new MetaAttribute(false, true, new MetaSet(new MetaValue(DataTypes.STRING))));

        MetaClass childmetaForCreate = new MetaClass("child_meta_class");
        childmetaForCreate.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        childmetaForCreate.setMetaAttribute("string_info",
                new MetaAttribute(false, true, new MetaValue(DataTypes.STRING)));

        childmetaForCreate.setMetaAttribute("double_info",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DOUBLE)));

        childmetaForCreate.setMetaAttribute("date_info",
                new MetaAttribute(false, true, new MetaValue(DataTypes.DATE)));

        //IMetaAttribute metaAttribute = new MetaAttribute(false, true, childmetaForCreate);
        //metaAttribute.setImmutable(false);
        //metaAttribute.setFinal(true);


        IMetaAttribute metaAttribute = new MetaAttribute(false,true,new MetaSet(childmetaForCreate));
        metaAttribute.setFinal(true);
        metaAttribute.setImmutable(false);

        rootMeta.setMetaAttribute("complex_set", metaAttribute);


        metaDao.save(rootMeta);
        metaClass = metaDao.load("testMetaClass");
    }

    private IBaseEntity constructBaseEntity(IMetaClass metaClass, Date reportDate, String uuid,
                                            List<Map<String, Object>> childMapList,
                                            long index)
    {
        Batch batch = batchRepository.addBatch(new Batch(reportDate));
        IMetaSet metaSet = (IMetaSet)metaClass.getMemberType("complex_set");
        IMetaClass arrayItemMeta = (IMetaClass) metaSet.getMemberType();


        BaseSet baseSet = new BaseSet(arrayItemMeta);

        for(Map<String,Object> childMap : childMapList) {
            BaseEntity baseEntity = new BaseEntity((MetaClass) arrayItemMeta, reportDate);
            assert (childMap.containsKey("child_uuid"));

            baseEntity.put("uuid",
                    BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaClass.getMemberType("uuid"),
                            batch, index, childMap.get("child_uuid")));

            if (childMap.containsKey("string_info"))
                baseEntity.put("string_info",
                        BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                new MetaValue(DataTypes.STRING),
                                batch,
                                index,
                                childMap.get("string_info")));

            if (childMap.containsKey("double_info"))
                baseEntity.put("double_info",
                        BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                new MetaValue(DataTypes.DOUBLE),
                                batch,
                                index,
                                childMap.get("double_info")));


            if (childMap.containsKey("date_info"))
                baseEntity.put("date_info",
                        BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                new MetaValue(DataTypes.DATE),
                                batch,
                                index,
                                childMap.get("date_info")));

            baseSet.put(new BaseSetComplexValue(batch, index, baseEntity));
        }

        IBaseEntity rootBaseEntity = new BaseEntity((MetaClass)metaClass, reportDate);

        rootBaseEntity.put("uuid",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        new MetaValue(DataTypes.STRING),
                        batch, index, uuid));

        rootBaseEntity.put("complex_set",
                BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaSet,
                        batch, index, baseSet));

        return rootBaseEntity;
    }

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

    @After
    public void cleanUp(){
        for(IBaseEntity be : entitiesToClean)
            baseEntityDao.deleteRecursive(be.getId());

        metaDao.remove(metaDao.load("testMetaClass"));
    }

    @Test
    public void testFirst() throws Exception {
        String uuid = UUID.randomUUID().toString();

        List<Map<String,Object>> childMapList;
        Map<String,Object> childMap;
        long id;
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "child_uuid1");
        childMap.put("string_info", "b");
        childMap.put("double_info", 13.00);
        childMap.put("date_info", new GregorianCalendar(2012, 0, 12).getTime());
        childMapList.add(childMap);

        childMap = new HashMap<>();
        childMap.put("child_uuid", "child_uuid2");
        childMap.put("string_info", "a");
        childMap.put("double_info", 15.10);
        childMap.put("date_info", new GregorianCalendar(2012, 0, 15).getTime());
        childMapList.add(childMap);

        IBaseEntity be1 = constructBaseEntity(metaClass,jan1,uuid,childMapList,1);
        IBaseEntity beApplied1 = processorDao.process(be1);
        id = beApplied1.getId();
        entitiesToClean.add(beApplied1);

        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "child_uuid2");
        childMap.put("string_info", "bb");
        childMap.put("double_info", 13.70);
        childMap.put("date_info", new GregorianCalendar(2012, 0, 12).getTime());
        childMapList.add(childMap);

        IBaseEntity be2 = constructBaseEntity(metaClass,mar1,uuid,childMapList,1);
        IBaseEntity beApplied2 = processorDao.process(be2);
        System.out.println(beApplied2);
        assertEquals(((BaseSet) beApplied2.getBaseValue("complex_set").getValue()).get().size(), 1);


        IBaseEntity beSaved1 = loadByReportDate(id, jan1);
        IBaseEntity beSaved2 = loadByReportDate(id, mar1);
        IBaseEntity beSaved3 = loadByReportDate(id, feb1);

        assertEquals(beApplied1.getBaseValue("complex_set").getValue(), beSaved1.getBaseValue("complex_set").getValue());
        assertEquals(beApplied2.getBaseValue("complex_set").getValue(), beSaved2.getBaseValue("complex_set").getValue());
        assertTrue(beSaved3.getBaseValue("complex_set") == null);

        //System.out.println(beSaved3.getBaseValue("complex_set"));
    }

    @Test
    public void testRandom742() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        List<Map<String,Object>> childMapList;
        Map<String,Object> childMap;
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ccc-789");
        childMap.put("string_info", "d");
        childMap.put("double_info", 1.234e8);
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("string_info", "f");
        childMap.put("double_info", 1834.2341);
        childMap.put("date_info", new GregorianCalendar(2014,2,6).getTime());
        childMapList.add(childMap);

        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, childMapList, 1);
        IBaseEntity beApplied1= processorDao.process(be1);

        id = beApplied1.getId();
        entitiesToClean.add(beApplied1);
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ddd-000");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("double_info", 1834.2341);
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("string_info", "e");
        childMap.put("date_info", new GregorianCalendar(2013,0,14).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ccc-789");
        childMap.put("string_info", "d");
        childMap.put("double_info", 1.234e8);
        childMapList.add(childMap);

        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, childMapList, 1);
        IBaseEntity beApplied2= processorDao.process(be2);

        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("string_info", "d");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ccc-789");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMapList.add(childMap);

        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, childMapList, 1);
        IBaseEntity beApplied3= processorDao.process(be3);

        IBaseEntity beSaved4 = loadByReportDate(id, jan1);
        assertEquals(beApplied3.getBaseValue("string_set").getValue(), beSaved4.getBaseValue("string_set").getValue());
        IBaseEntity beSaved5 = loadByReportDate(id, feb1);
        assertTrue(beSaved5.getBaseValue("complex_set") == null);
        IBaseEntity beSaved6 = loadByReportDate(id, mar1);
        assertTrue(beSaved6.getBaseValue("complex_set") == null);
        IBaseEntity beSaved7 = loadByReportDate(id, apr1);
        assertEquals(beApplied1.getBaseValue("string_set").getValue(), beSaved7.getBaseValue("string_set").getValue());
        IBaseEntity beSaved8 = loadByReportDate(id, may1);
        assertTrue(beSaved8.getBaseValue("complex_set") == null);
    }

    @Test
    public void testRandom971() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        List<Map<String,Object>> childMapList;
        Map<String,Object> childMap;
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("double_info", 1.234e8);
        childMap.put("date_info", new GregorianCalendar(2013,0,14).getTime());
        childMapList.add(childMap);

        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, childMapList, 1);
        IBaseEntity beApplied1= processorDao.process(be1);

        id = beApplied1.getId();
        entitiesToClean.add(beApplied1);
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("string_info", "a");
        childMapList.add(childMap);

        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, childMapList, 1);
        IBaseEntity beApplied2= processorDao.process(be2);

        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("string_info", "d");
        childMap.put("double_info", 324.2341);
        childMap.put("date_info", new GregorianCalendar(2013,6,31).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ccc-789");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ddd-000");
        childMap.put("string_info", "d");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("string_info", "b");
        childMapList.add(childMap);

        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, childMapList, 1);
        IBaseEntity beApplied3= processorDao.process(be3);

        IBaseEntity beSaved4 = loadByReportDate(id, jan1);
        assertEquals(beApplied1.getBaseValue("string_set").getValue(), beSaved4.getBaseValue("string_set").getValue());
        IBaseEntity beSaved5 = loadByReportDate(id, feb1);
        assertEquals(beApplied3.getBaseValue("string_set").getValue(), beSaved5.getBaseValue("string_set").getValue());
        IBaseEntity beSaved6 = loadByReportDate(id, mar1);
        assertTrue(beSaved6.getBaseValue("complex_set") == null);
        IBaseEntity beSaved7 = loadByReportDate(id, apr1);
        assertTrue(beSaved7.getBaseValue("complex_set") == null);
        IBaseEntity beSaved8 = loadByReportDate(id, may1);
        assertEquals(beApplied2.getBaseValue("string_set").getValue(), beSaved8.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom456() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        List<Map<String,Object>> childMapList;
        Map<String,Object> childMap;
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("string_info", "f");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("double_info", 1.234e8);
        childMapList.add(childMap);

        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, childMapList, 1);
        IBaseEntity beApplied1= processorDao.process(be1);

        id = beApplied1.getId();
        entitiesToClean.add(beApplied1);
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("date_info", new GregorianCalendar(2013,3,23).getTime());
        childMapList.add(childMap);

        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, childMapList, 1);
        IBaseEntity beApplied2= processorDao.process(be2);

        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("double_info", 1834.2341);
        childMap.put("date_info", new GregorianCalendar(2014,2,6).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ccc-789");
        childMap.put("string_info", "b");
        childMap.put("date_info", new GregorianCalendar(2013,2,8).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMapList.add(childMap);

        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, childMapList, 1);
        IBaseEntity beApplied3= processorDao.process(be3);

        IBaseEntity beSaved4 = loadByReportDate(id, jan1);
        assertTrue(beSaved4.getBaseValue("complex_set") == null);
        IBaseEntity beSaved5 = loadByReportDate(id, feb1);
        assertTrue(beSaved5.getBaseValue("complex_set") == null);
        IBaseEntity beSaved6 = loadByReportDate(id, mar1);
        assertEquals(beApplied3.getBaseValue("string_set").getValue(), beSaved6.getBaseValue("string_set").getValue());
        IBaseEntity beSaved7 = loadByReportDate(id, apr1);
        assertTrue(beSaved7.getBaseValue("complex_set") == null);
        IBaseEntity beSaved8 = loadByReportDate(id, may1);
        assertEquals(beApplied2.getBaseValue("string_set").getValue(), beSaved8.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom867() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        List<Map<String,Object>> childMapList;
        Map<String,Object> childMap;
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("date_info", new GregorianCalendar(2013,3,23).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ccc-789");
        childMap.put("string_info", "d");
        childMap.put("double_info", 1.234e8);
        childMap.put("date_info", new GregorianCalendar(2014,2,6).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("string_info", "c");
        childMap.put("double_info", 12.123);
        childMapList.add(childMap);

        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, childMapList, 1);
        IBaseEntity beApplied1= processorDao.process(be1);

        id = beApplied1.getId();
        entitiesToClean.add(beApplied1);
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("date_info", new GregorianCalendar(2013,3,23).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ccc-789");
        childMap.put("string_info", "e");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("string_info", "f");
        childMap.put("date_info", new GregorianCalendar(2013,2,8).getTime());
        childMapList.add(childMap);

        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, childMapList, 1);
        IBaseEntity beApplied2= processorDao.process(be2);

        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("string_info", "f");
        childMap.put("double_info", 12.123);
        childMap.put("date_info", new GregorianCalendar(2013,6,31).getTime());
        childMapList.add(childMap);

        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, childMapList, 1);
        IBaseEntity beApplied3= processorDao.process(be3);

        IBaseEntity beSaved4 = loadByReportDate(id, jan1);
        assertTrue(beSaved4.getBaseValue("complex_set") == null);
        IBaseEntity beSaved5 = loadByReportDate(id, feb1);
        assertEquals(beApplied2.getBaseValue("string_set").getValue(), beSaved5.getBaseValue("string_set").getValue());
        IBaseEntity beSaved6 = loadByReportDate(id, mar1);
        assertTrue(beSaved6.getBaseValue("complex_set") == null);
        IBaseEntity beSaved7 = loadByReportDate(id, apr1);
        assertEquals(beApplied3.getBaseValue("string_set").getValue(), beSaved7.getBaseValue("string_set").getValue());
        IBaseEntity beSaved8 = loadByReportDate(id, may1);
        assertTrue(beSaved8.getBaseValue("complex_set") == null);
    }

    @Test
    public void testRandom610() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        List<Map<String,Object>> childMapList;
        Map<String,Object> childMap;
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("double_info", 1.234e8);
        childMap.put("date_info", new GregorianCalendar(2013,2,8).getTime());
        childMapList.add(childMap);

        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, childMapList, 1);
        IBaseEntity beApplied1= processorDao.process(be1);

        id = beApplied1.getId();
        entitiesToClean.add(beApplied1);
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ccc-789");
        childMap.put("date_info", new GregorianCalendar(2013,6,31).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("string_info", "f");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("string_info", "d");
        childMap.put("date_info", new GregorianCalendar(2013,2,8).getTime());
        childMapList.add(childMap);

        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, childMapList, 1);
        IBaseEntity beApplied2= processorDao.process(be2);

        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "ccc-789");
        childMap.put("string_info", "a");
        childMap.put("date_info", new GregorianCalendar(2014,2,6).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("date_info", new GregorianCalendar(2013,2,8).getTime());
        childMapList.add(childMap);

        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, childMapList, 1);
        IBaseEntity beApplied3= processorDao.process(be3);

        IBaseEntity beSaved4 = loadByReportDate(id, jan1);
        assertEquals(beApplied2.getBaseValue("string_set").getValue(), beSaved4.getBaseValue("string_set").getValue());
        IBaseEntity beSaved5 = loadByReportDate(id, feb1);
        assertEquals(beApplied3.getBaseValue("string_set").getValue(), beSaved5.getBaseValue("string_set").getValue());
        IBaseEntity beSaved6 = loadByReportDate(id, mar1);
        assertTrue(beSaved6.getBaseValue("complex_set") == null);
        IBaseEntity beSaved7 = loadByReportDate(id, apr1);
        assertTrue(beSaved7.getBaseValue("complex_set") == null);
        IBaseEntity beSaved8 = loadByReportDate(id, may1);
        assertEquals(beApplied1.getBaseValue("string_set").getValue(), beSaved8.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom738() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        List<Map<String,Object>> childMapList;
        Map<String,Object> childMap;
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("double_info", 324.2341);
        childMapList.add(childMap);

        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, childMapList, 1);
        IBaseEntity beApplied1= processorDao.process(be1);

        id = beApplied1.getId();
        entitiesToClean.add(beApplied1);
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("double_info", 12.123);
        childMap.put("date_info", new GregorianCalendar(2014,2,6).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("string_info", "e");
        childMap.put("double_info", 12.123);
        childMap.put("date_info", new GregorianCalendar(2013,0,14).getTime());
        childMapList.add(childMap);

        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, childMapList, 1);
        IBaseEntity beApplied2= processorDao.process(be2);

        IBaseEntity beSaved3 = loadByReportDate(id, jan1);
        assertTrue(beSaved3.getBaseValue("complex_set") == null);
        IBaseEntity beSaved4 = loadByReportDate(id, feb1);
        assertTrue(beSaved4.getBaseValue("complex_set") == null);
        IBaseEntity beSaved5 = loadByReportDate(id, mar1);
        assertEquals(beApplied1.getBaseValue("string_set").getValue(), beSaved5.getBaseValue("string_set").getValue());
        IBaseEntity beSaved6 = loadByReportDate(id, apr1);
        assertTrue(beSaved6.getBaseValue("complex_set") == null);
        IBaseEntity beSaved7 = loadByReportDate(id, may1);
        assertEquals(beApplied2.getBaseValue("string_set").getValue(), beSaved7.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom296() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        List<Map<String,Object>> childMapList;
        Map<String,Object> childMap;
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("string_info", "c");
        childMapList.add(childMap);

        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, childMapList, 1);
        IBaseEntity beApplied1= processorDao.process(be1);

        id = beApplied1.getId();
        entitiesToClean.add(beApplied1);
        childMapList = new LinkedList<>();
        childMap = new HashMap<>();
        childMap.put("child_uuid", "bbb-456");
        childMap.put("string_info", "a");
        childMap.put("double_info", 324.2341);
        childMap.put("date_info", new GregorianCalendar(2013,2,8).getTime());
        childMapList.add(childMap);
        childMap = new HashMap<>();
        childMap.put("child_uuid", "aa-123");
        childMap.put("double_info", 1834.2341);
        childMap.put("date_info", new GregorianCalendar(2013,2,8).getTime());
        childMapList.add(childMap);

        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, childMapList, 1);
        IBaseEntity beApplied2= processorDao.process(be2);

        IBaseEntity beSaved3 = loadByReportDate(id, jan1);
        assertEquals(beApplied2.getBaseValue("string_set").getValue(), beSaved3.getBaseValue("string_set").getValue());
        IBaseEntity beSaved4 = loadByReportDate(id, feb1);
        assertTrue(beSaved4.getBaseValue("complex_set") == null);
        IBaseEntity beSaved5 = loadByReportDate(id, mar1);
        assertTrue(beSaved5.getBaseValue("complex_set") == null);
        IBaseEntity beSaved6 = loadByReportDate(id, apr1);
        assertTrue(beSaved6.getBaseValue("complex_set") == null);
        IBaseEntity beSaved7 = loadByReportDate(id, may1);
        assertEquals(beApplied1.getBaseValue("string_set").getValue(), beSaved7.getBaseValue("string_set").getValue());
    }

}
