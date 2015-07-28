package kz.bsbnb.usci.eav.test.persistence.dao;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.*;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static kz.bsbnb.eav.persistance.generated.Tables.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles({"oracle"})
public class BaseEntityApplyDaoImplTest {
    @Autowired
    IBaseEntityApplyDao baseEntityApplyDao;

    @Autowired
    IBaseEntityProcessorDao processorDao;

    @Autowired
    IMetaClassDao metaDao;

    @Autowired
    IBatchRepository batchRepository;

    @Autowired
    IBaseEntityLoadDao loadDao;

    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    DSLContext context;

    @Autowired
    IPersistableDaoPool persistableDaoPool;

    JdbcTemplate jdbcTemplate;

    @Autowired
    public void initJdbc(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    List<IBaseEntity> entitiesToClean;

    private final Date jan1 = new GregorianCalendar(2013,0,1).getTime();
    private final Date feb1 = new GregorianCalendar(2013,1,1).getTime();
    private final Date mar1 = new GregorianCalendar(2013,2,1).getTime();
    private final Date apr1 = new GregorianCalendar(2013,3,1).getTime();
    private final Date may1 = new GregorianCalendar(2013,4,1).getTime();

    @Before
    public void init() throws Exception
    {
        entitiesToClean = new LinkedList<>();
        MetaClass metaClass = new MetaClass("testMetaClass");
        metaClass.setMetaAttribute("uuid",
                new MetaAttribute(true, true, new MetaValue(DataTypes.STRING)));
        metaClass.setMetaAttribute("string_set",
                new MetaAttribute(false, true, new MetaSet(new MetaValue(DataTypes.STRING))));
        metaDao.save(metaClass);
    }

    @After
    public  void cleanUp() throws Exception
    {
        for(IBaseEntity be : entitiesToClean)
            baseEntityDao.deleteRecursive(be.getId());

        metaDao.remove(metaDao.load("testMetaClass"));
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


    private IBaseEntity constructBaseEntity(IMetaClass metaClass, Date reportDate, String uuid, String[] stringValues, long index)
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

    @Test
    public void abSimpleArrayTest() throws Exception {

        String uuid = UUID.randomUUID().toString();

        IBaseEntity be1 = constructBaseEntity(metaDao.load("testMetaClass"), jan1,
                uuid, new String[]{"a"}, 1);

        IBaseEntity be2 = constructBaseEntity(metaDao.load("testMetaClass"), mar1,
                uuid, new String[]{"b"}, 1);

        IBaseEntity beApplied1 = processorDao.process(be1);
        long id = beApplied1.getId();
        IBaseEntity beSaved1 = loadDao.load(id, jan1, jan1);

        IBaseValue baseValueSaving = be2.getBaseValue("string_set");
        IBaseValue baseValueLoaded = beSaved1.getBaseValue("string_set");
        IBaseEntityManager bm = new BaseEntityManager();

        baseEntityApplyDao.applySimpleSet(be1, baseValueSaving, baseValueLoaded, bm);
        baseEntityApplyDao.applyToDb(bm);

        Select select = context.select(DSL.max(EAV_BE_STRING_SET_VALUES.SET_ID))
                .from(EAV_BE_STRING_SET_VALUES);

        long setId = jdbcTemplate.queryForLong(select.getSQL());

        select = context.select(DSL.count())
                .from(EAV_BE_STRING_SET_VALUES)
                .where(EAV_BE_STRING_SET_VALUES.SET_ID.eq(setId))
                .and(EAV_BE_STRING_SET_VALUES.VALUE.eq("a"))
                .and(EAV_BE_STRING_SET_VALUES.REPORT_DATE.eq(DataUtils.convert(jan1)))
                .and(EAV_BE_STRING_SET_VALUES.IS_CLOSED.eq(DataUtils.convert(false)))
                .and(EAV_BE_STRING_SET_VALUES.IS_LAST.eq(DataUtils.convert(false)));


        entitiesToClean.add(beApplied1);

        assertTrue(jdbcTemplate.queryForInt(select.getSQL(), setId, "a", jan1, false, false) == 1);
        assertTrue(jdbcTemplate.queryForInt(select.getSQL(), setId, "b", mar1, false, true) == 1);
        assertTrue(jdbcTemplate.queryForInt(select.getSQL(), setId, "a", mar1, true, true) == 1);
    }

    @Test
    public void mustCleanCancelTest() throws Exception {
        String uuid = UUID.randomUUID().toString();

        IBaseEntity be1 = constructBaseEntity(metaDao.load("testMetaClass"), jan1,
                uuid, new String[]{"a"}, 1);

        IBaseEntity be2 = constructBaseEntity(metaDao.load("testMetaClass"), mar1,
                uuid, new String[]{"b"}, 1);

        IBaseEntity be3 = constructBaseEntity(metaDao.load("testMetaClass"), jan1,
                uuid, new String[]{"c"}, 1);

        IBaseEntity beApplied1 = processorDao.process(be1);
        long id = beApplied1.getId();
        IBaseEntity beSaved1 = loadDao.load(id, jan1, jan1);

        IBaseValue baseValueSaving = be2.getBaseValue("string_set");
        IBaseValue baseValueLoaded = beSaved1.getBaseValue("string_set");
        IBaseEntityManager bm = new BaseEntityManager();
        baseEntityApplyDao.applySimpleSet(be1, baseValueSaving, baseValueLoaded, bm);
        baseEntityApplyDao.applyToDb(bm);

        baseValueSaving = be3.getBaseValue("string_set");
        bm = new BaseEntityManager();
        baseEntityApplyDao.applySimpleSet(be1, baseValueSaving, baseValueLoaded, bm);
        baseEntityApplyDao.applyToDb(bm);

        Select select = context.select(DSL.max(EAV_BE_STRING_SET_VALUES.SET_ID))
                .from(EAV_BE_STRING_SET_VALUES);

        long setId = jdbcTemplate.queryForLong(select.getSQL());

        SelectConditionStep selectData = context.select(DSL.count())
                .from(EAV_BE_STRING_SET_VALUES)
                .where(EAV_BE_STRING_SET_VALUES.SET_ID.eq(setId))
                .and(EAV_BE_STRING_SET_VALUES.VALUE.eq("a"));

        entitiesToClean.add(beApplied1);

        //verify no a there
        assertTrue(jdbcTemplate.queryForInt(selectData.getSQL(), selectData.getBindValues().toArray()) == 0);

        select = selectData.and(EAV_BE_STRING_SET_VALUES.REPORT_DATE.eq(DataUtils.convert(new Date())))
                .and(EAV_BE_STRING_SET_VALUES.IS_LAST.eq(DataUtils.convert(false)))
                .and(EAV_BE_STRING_SET_VALUES.IS_CLOSED.eq(DataUtils.convert(false)));

        //verify b and c values
        assertEquals(jdbcTemplate.queryForInt(select.getSQL(), setId, "c", jan1, false, false), 1);
        assertEquals(jdbcTemplate.queryForInt(select.getSQL(), setId, "c", mar1, true, true), 1);
        assertEquals(jdbcTemplate.queryForInt(select.getSQL(), setId, "b", mar1, true, false), 1);
    }

    @Test
    public void doubleASeqTest() throws Exception {
        String uuid = UUID.randomUUID().toString();

        IBaseEntity be1 = constructBaseEntity(metaDao.load("testMetaClass"), jan1,
                uuid, new String[]{"a","a"}, 1);

        IBaseEntity be2 = constructBaseEntity(metaDao.load("testMetaClass"), mar1,
                uuid, new String[]{"a","a"}, 1);

        IBaseEntity beApplied1 = processorDao.process(be1);
        entitiesToClean.add(beApplied1);
        long id = beApplied1.getId();
        IBaseEntity beSaved1 = loadDao.load(id, jan1, jan1);

        IBaseValue baseValueSaving = be2.getBaseValue("string_set");
        IBaseValue baseValueLoaded = beSaved1.getBaseValue("string_set");
        IBaseEntityManager bm = new BaseEntityManager();
        baseEntityApplyDao.applySimpleSet(be1, baseValueSaving, baseValueLoaded, bm);
        baseEntityApplyDao.applyToDb(bm);

        Select select = context.select(DSL.max(EAV_BE_STRING_SET_VALUES.SET_ID))
                .from(EAV_BE_STRING_SET_VALUES);

        long setId = jdbcTemplate.queryForLong(select.getSQL());

        select = context.select(DSL.count())
                .from(EAV_BE_STRING_SET_VALUES)
                .where(EAV_BE_STRING_SET_VALUES.SET_ID.eq(setId))
                .and(EAV_BE_STRING_SET_VALUES.VALUE.eq("a"))
                .and(EAV_BE_STRING_SET_VALUES.REPORT_DATE.eq(DataUtils.convert(jan1)))
                .and(EAV_BE_STRING_SET_VALUES.IS_CLOSED.eq(DataUtils.convert(false)))
                .and(EAV_BE_STRING_SET_VALUES.IS_LAST.eq(DataUtils.convert(true)));


        assertTrue(jdbcTemplate.queryForInt(select.getSQL(), select.getBindValues().toArray()) == 2);
    }

    @Test
    public void doubleANonSeqTest() throws Exception {
        String uuid = UUID.randomUUID().toString();

        IBaseEntity be1 = constructBaseEntity(metaDao.load("testMetaClass"), mar1,
                uuid, new String[]{"a","a"}, 1);

        IBaseEntity be2 = constructBaseEntity(metaDao.load("testMetaClass"), jan1,
                uuid, new String[]{"a","a"}, 1);

        IBaseEntity beApplied1 = processorDao.process(be1);
        long id = beApplied1.getId();
        entitiesToClean.add(beApplied1);
        IBaseEntity beSaved1 = loadDao.load(id, mar1, mar1);


        be2.setId(id);
        IBaseValue baseValueSaving = be2.getBaseValue("string_set");
        IBaseValue baseValueLoaded = beSaved1.getBaseValue("string_set");
        IBaseEntityManager bm = new BaseEntityManager();
        baseEntityApplyDao.applySimpleSet(be2, baseValueSaving, baseValueLoaded, bm);
        baseEntityApplyDao.applyToDb(bm);

        Select select = context.select(DSL.max(EAV_BE_STRING_SET_VALUES.SET_ID))
                .from(EAV_BE_STRING_SET_VALUES);

        long setId = jdbcTemplate.queryForLong(select.getSQL());

        select = context.select(DSL.count())
                .from(EAV_BE_STRING_SET_VALUES)
                .where(EAV_BE_STRING_SET_VALUES.SET_ID.eq(setId))
                .and(EAV_BE_STRING_SET_VALUES.VALUE.eq("a"))
                .and(EAV_BE_STRING_SET_VALUES.REPORT_DATE.eq(DataUtils.convert(jan1)))
                .and(EAV_BE_STRING_SET_VALUES.IS_CLOSED.eq(DataUtils.convert(false)))
                .and(EAV_BE_STRING_SET_VALUES.IS_LAST.eq(DataUtils.convert(true)));


        assertTrue(jdbcTemplate.queryForInt(select.getSQL(), select.getBindValues().toArray()) == 2);
    }

    @Test
    public void mustNotDeleteAllIdenticalValues() throws Exception {
        String uuid = UUID.randomUUID().toString();

        IBaseEntity be1 = constructBaseEntity(metaDao.load("testMetaClass"), may1,
                uuid, new String[]{"x"}, 1);

        IBaseEntity be2 = constructBaseEntity(metaDao.load("testMetaClass"), jan1,
                uuid, new String[]{"a","a"}, 1);

        IBaseEntity be3 = constructBaseEntity(metaDao.load("testMetaClass"), feb1,
                uuid, new String[]{"b","a"}, 1);

        long id = processorDao.process(be1).getId();
        processorDao.process(be2);
        processorDao.process(be3);

        IBaseEntity beSaved1 = loadByReportDate(id, may1);
        IBaseEntity beSaved2 = loadByReportDate(id, jan1);
        IBaseEntity beSaved3 = loadByReportDate(id, feb1);

        entitiesToClean.add(beSaved1);


        assertEquals(be1.getBaseValue("string_set").getValue(), beSaved1.getBaseValue("string_set").getValue());
        assertEquals(be2.getBaseValue("string_set").getValue(), beSaved2.getBaseValue("string_set").getValue());
        assertEquals(be3.getBaseValue("string_set").getValue(), beSaved3.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom774() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"a","e","e"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"d"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"d","f"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"f"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"c"}, 1);
        processorDao.process(be5);
        entitiesToClean.add(beForClean);
        IBaseEntity beSaved6 = loadByReportDate(id, jan1);
        assertEquals(be3.getBaseValue("string_set").getValue(), beSaved6.getBaseValue("string_set").getValue());
        IBaseEntity beSaved7 = loadByReportDate(id, feb1);
        assertEquals(be4.getBaseValue("string_set").getValue(), beSaved7.getBaseValue("string_set").getValue());
        IBaseEntity beSaved8 = loadByReportDate(id, mar1);
        assertEquals(be4.getBaseValue("string_set").getValue(), beSaved8.getBaseValue("string_set").getValue());
        IBaseEntity beSaved9 = loadByReportDate(id, apr1);
        assertEquals(be4.getBaseValue("string_set").getValue(), beSaved9.getBaseValue("string_set").getValue());
        IBaseEntity beSaved10 = loadByReportDate(id, may1);
        assertEquals(be5.getBaseValue("string_set").getValue(), beSaved10.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom498() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"c"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"f"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"e"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"c","f","d"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"f"}, 1);
        processorDao.process(be5);
        entitiesToClean.add(beForClean);
        IBaseEntity beSaved8 = loadByReportDate(id, mar1);
        assertEquals(be2.getBaseValue("string_set").getValue(), beSaved8.getBaseValue("string_set").getValue());
        IBaseEntity beSaved9 = loadByReportDate(id, apr1);
        assertEquals(be5.getBaseValue("string_set").getValue(), beSaved9.getBaseValue("string_set").getValue());
        IBaseEntity beSaved10 = loadByReportDate(id, may1);
        assertEquals(be3.getBaseValue("string_set").getValue(), beSaved10.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom109() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"e","e","c"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"f"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"b"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"e","e","a"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"e","f","d"}, 1);
        processorDao.process(be5);
        entitiesToClean.add(beForClean);
        IBaseEntity beSaved7 = loadByReportDate(id, feb1);
        assertEquals(be2.getBaseValue("string_set").getValue(), beSaved7.getBaseValue("string_set").getValue());
        IBaseEntity beSaved8 = loadByReportDate(id, mar1);
        assertEquals(be5.getBaseValue("string_set").getValue(), beSaved8.getBaseValue("string_set").getValue());
        IBaseEntity beSaved9 = loadByReportDate(id, apr1);
        assertEquals(be4.getBaseValue("string_set").getValue(), beSaved9.getBaseValue("string_set").getValue());
        IBaseEntity beSaved10 = loadByReportDate(id, may1);
        assertEquals(be3.getBaseValue("string_set").getValue(), beSaved10.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom686() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"f"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"b","e","b"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"f","b","a"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"a","b","d"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"b","b"}, 1);
        processorDao.process(be5);
        entitiesToClean.add(beForClean);
        IBaseEntity beSaved6 = loadByReportDate(id, jan1);
        assertEquals(be4.getBaseValue("string_set").getValue(), beSaved6.getBaseValue("string_set").getValue());
        IBaseEntity beSaved7 = loadByReportDate(id, feb1);
        assertEquals(be2.getBaseValue("string_set").getValue(), beSaved7.getBaseValue("string_set").getValue());
        IBaseEntity beSaved8 = loadByReportDate(id, mar1);
        assertEquals(be2.getBaseValue("string_set").getValue(), beSaved8.getBaseValue("string_set").getValue());
        IBaseEntity beSaved9 = loadByReportDate(id, apr1);
        assertEquals(be5.getBaseValue("string_set").getValue(), beSaved9.getBaseValue("string_set").getValue());
        IBaseEntity beSaved10 = loadByReportDate(id, may1);
        assertEquals(be5.getBaseValue("string_set").getValue(), beSaved10.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom647() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"e","b","b"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        entitiesToClean.add(beForClean);
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"b"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"a"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"c","d"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"f"}, 1);
        processorDao.process(be5);
        IBaseEntity be6= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"d"}, 1);
        processorDao.process(be6);
        IBaseEntity be7= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"b"}, 1);
        processorDao.process(be7);
        IBaseEntity be8= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"e","f"}, 1);
        processorDao.process(be8);
        IBaseEntity be9= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"b","b"}, 1);
        processorDao.process(be9);
        IBaseEntity be10= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"c","f","e"}, 1);
        processorDao.process(be10);
        IBaseEntity be11= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"e","c","a","c"}, 1);
        processorDao.process(be11);
        IBaseEntity be12= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"d","f","e","c"}, 1);
        processorDao.process(be12);
        IBaseEntity be13= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"f"}, 1);
        processorDao.process(be13);
        IBaseEntity beSaved14 = loadByReportDate(id, jan1);
        assertEquals(be11.getBaseValue("string_set").getValue(), beSaved14.getBaseValue("string_set").getValue());
        IBaseEntity beSaved15 = loadByReportDate(id, feb1);
        assertEquals(be9.getBaseValue("string_set").getValue(), beSaved15.getBaseValue("string_set").getValue());
        IBaseEntity beSaved16 = loadByReportDate(id, mar1);
        assertEquals(be13.getBaseValue("string_set").getValue(), beSaved16.getBaseValue("string_set").getValue());
        IBaseEntity beSaved17 = loadByReportDate(id, apr1);
        assertEquals(be8.getBaseValue("string_set").getValue(), beSaved17.getBaseValue("string_set").getValue());
        IBaseEntity beSaved18 = loadByReportDate(id, may1);
        assertEquals(be12.getBaseValue("string_set").getValue(), beSaved18.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom783() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"f","c"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        entitiesToClean.add(beForClean);
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"b","c","e"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"f","e","d"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"d"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"a","f","c","e"}, 1);
        processorDao.process(be5);
        IBaseEntity be6= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"b","b"}, 1);
        processorDao.process(be6);
        IBaseEntity be7= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"a"}, 1);
        processorDao.process(be7);
        IBaseEntity be8= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"b","d"}, 1);
        processorDao.process(be8);
        IBaseEntity be9= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"f","d","f","a"}, 1);
        processorDao.process(be9);
        IBaseEntity be10= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"b","e"}, 1);
        processorDao.process(be10);
        IBaseEntity be11= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"e","d","e","a"}, 1);
        processorDao.process(be11);
        IBaseEntity be12= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"b","f"}, 1);
        processorDao.process(be12);
        IBaseEntity be13= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"a"}, 1);
        processorDao.process(be13);
        IBaseEntity beSaved14 = loadByReportDate(id, jan1);
        assertEquals(be11.getBaseValue("string_set").getValue(), beSaved14.getBaseValue("string_set").getValue());
        IBaseEntity beSaved15 = loadByReportDate(id, feb1);
        assertEquals(be8.getBaseValue("string_set").getValue(), beSaved15.getBaseValue("string_set").getValue());
        IBaseEntity beSaved16 = loadByReportDate(id, mar1);
        assertEquals(be8.getBaseValue("string_set").getValue(), beSaved16.getBaseValue("string_set").getValue());
        IBaseEntity beSaved17 = loadByReportDate(id, apr1);
        assertEquals(be13.getBaseValue("string_set").getValue(), beSaved17.getBaseValue("string_set").getValue());
        IBaseEntity beSaved18 = loadByReportDate(id, may1);
        assertEquals(be10.getBaseValue("string_set").getValue(), beSaved18.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom409() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"f","c","d","a"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        entitiesToClean.add(beForClean);
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"f","a"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"d","e","e"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"c","a","d"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"d","f","f"}, 1);
        processorDao.process(be5);
        IBaseEntity be6= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"c"}, 1);
        processorDao.process(be6);
        IBaseEntity be7= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"b"}, 1);
        processorDao.process(be7);
        IBaseEntity be8= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"a"}, 1);
        processorDao.process(be8);
        IBaseEntity be9= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"c"}, 1);
        processorDao.process(be9);
        IBaseEntity be10= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"b","f"}, 1);
        processorDao.process(be10);
        IBaseEntity be11= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"e","c"}, 1);
        processorDao.process(be11);
        IBaseEntity be12= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"a"}, 1);
        processorDao.process(be12);
        IBaseEntity be13= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"d","f","f","a"}, 1);
        processorDao.process(be13);
        IBaseEntity beSaved14 = loadByReportDate(id, jan1);
        assertEquals(be12.getBaseValue("string_set").getValue(), beSaved14.getBaseValue("string_set").getValue());
        IBaseEntity beSaved15 = loadByReportDate(id, feb1);
        assertEquals(be9.getBaseValue("string_set").getValue(), beSaved15.getBaseValue("string_set").getValue());
        IBaseEntity beSaved16 = loadByReportDate(id, mar1);
        assertEquals(be11.getBaseValue("string_set").getValue(), beSaved16.getBaseValue("string_set").getValue());
        IBaseEntity beSaved17 = loadByReportDate(id, apr1);
        assertEquals(be8.getBaseValue("string_set").getValue(), beSaved17.getBaseValue("string_set").getValue());
        IBaseEntity beSaved18 = loadByReportDate(id, may1);
        assertEquals(be13.getBaseValue("string_set").getValue(), beSaved18.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom703() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"d","a","a"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        entitiesToClean.add(beForClean);
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"d","d"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"c","f","f"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"d"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"b","a"}, 1);
        processorDao.process(be5);
        IBaseEntity be6= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"d"}, 1);
        processorDao.process(be6);
        IBaseEntity be7= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"c","e"}, 1);
        processorDao.process(be7);
        IBaseEntity be8= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"a","e","e","c"}, 1);
        processorDao.process(be8);
        IBaseEntity be9= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"d"}, 1);
        processorDao.process(be9);
        IBaseEntity be10= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"c","a"}, 1);
        processorDao.process(be10);
        IBaseEntity be11= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"f"}, 1);
        processorDao.process(be11);
        IBaseEntity be12= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"c","b","b"}, 1);
        processorDao.process(be12);
        IBaseEntity be13= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"a","b","e"}, 1);
        processorDao.process(be13);
        IBaseEntity beSaved14 = loadByReportDate(id, jan1);
        assertEquals(be4.getBaseValue("string_set").getValue(), beSaved14.getBaseValue("string_set").getValue());
        IBaseEntity beSaved15 = loadByReportDate(id, feb1);
        assertEquals(be13.getBaseValue("string_set").getValue(), beSaved15.getBaseValue("string_set").getValue());
        IBaseEntity beSaved16 = loadByReportDate(id, mar1);
        assertEquals(be11.getBaseValue("string_set").getValue(), beSaved16.getBaseValue("string_set").getValue());
        IBaseEntity beSaved17 = loadByReportDate(id, apr1);
        assertEquals(be9.getBaseValue("string_set").getValue(), beSaved17.getBaseValue("string_set").getValue());
        IBaseEntity beSaved18 = loadByReportDate(id, may1);
        assertEquals(be12.getBaseValue("string_set").getValue(), beSaved18.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom38() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"b","b","c","b"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        entitiesToClean.add(beForClean);
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"f"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"e","b"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"d","d","a"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"e","d","d","c"}, 1);
        processorDao.process(be5);
        IBaseEntity be6= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"e","f"}, 1);
        processorDao.process(be6);
        IBaseEntity be7= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"f","d","e"}, 1);
        processorDao.process(be7);
        IBaseEntity be8= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"d","d","f"}, 1);
        processorDao.process(be8);
        IBaseEntity be9= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"e","b","e","f"}, 1);
        processorDao.process(be9);
        IBaseEntity be10= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"a","e","b","a"}, 1);
        processorDao.process(be10);
        IBaseEntity be11= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"f","f","a","f"}, 1);
        processorDao.process(be11);
        IBaseEntity be12= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"c","b","d","b"}, 1);
        processorDao.process(be12);
        IBaseEntity be13= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"e","d"}, 1);
        processorDao.process(be13);
        IBaseEntity beSaved14 = loadByReportDate(id, jan1);
        assertEquals(be12.getBaseValue("string_set").getValue(), beSaved14.getBaseValue("string_set").getValue());
        IBaseEntity beSaved15 = loadByReportDate(id, feb1);
        assertEquals(be11.getBaseValue("string_set").getValue(), beSaved15.getBaseValue("string_set").getValue());
        IBaseEntity beSaved16 = loadByReportDate(id, mar1);
        assertEquals(be13.getBaseValue("string_set").getValue(), beSaved16.getBaseValue("string_set").getValue());
        IBaseEntity beSaved17 = loadByReportDate(id, apr1);
        assertEquals(be6.getBaseValue("string_set").getValue(), beSaved17.getBaseValue("string_set").getValue());
        IBaseEntity beSaved18 = loadByReportDate(id, may1);
        assertEquals(be8.getBaseValue("string_set").getValue(), beSaved18.getBaseValue("string_set").getValue());
    }

    @Test
    public void testRandom180() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"f","b"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        entitiesToClean.add(beForClean);
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"a","e","d"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"d"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"d"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"c"}, 1);
        processorDao.process(be5);
        IBaseEntity beSaved6 = loadByReportDate(id, jan1);
        assertEquals(be3.getBaseValue("string_set").getValue(), beSaved6.getBaseValue("string_set").getValue());
        IBaseEntity beSaved7 = loadByReportDate(id, feb1);
        assertEquals(be3.getBaseValue("string_set").getValue(), beSaved7.getBaseValue("string_set").getValue());
        IBaseEntity beSaved8 = loadByReportDate(id, mar1);
        assertEquals(be5.getBaseValue("string_set").getValue(), beSaved8.getBaseValue("string_set").getValue());
        IBaseEntity beSaved9 = loadByReportDate(id, apr1);
        assertEquals(be5.getBaseValue("string_set").getValue(), beSaved9.getBaseValue("string_set").getValue());
        IBaseEntity beSaved10 = loadByReportDate(id, may1);
        assertEquals(be2.getBaseValue("string_set").getValue(), beSaved10.getBaseValue("string_set").getValue());
    }


    @Test
    public void testRandom572() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"e","c"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        entitiesToClean.add(beForClean);
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"c","c"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"b","f"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"e","b"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"b","f"}, 1);
        processorDao.process(be5);
        IBaseEntity beSaved6 = loadByReportDate(id, jan1);
        assertEquals(be5.getBaseValue("string_set").getValue(), beSaved6.getBaseValue("string_set").getValue());
        IBaseEntity beSaved7 = loadByReportDate(id, feb1);
        assertEquals(be5.getBaseValue("string_set").getValue(), beSaved7.getBaseValue("string_set").getValue());
        IBaseEntity beSaved8 = loadByReportDate(id, mar1);
        assertEquals(be4.getBaseValue("string_set").getValue(), beSaved8.getBaseValue("string_set").getValue());
        IBaseEntity beSaved9 = loadByReportDate(id, apr1);
        assertEquals(be4.getBaseValue("string_set").getValue(), beSaved9.getBaseValue("string_set").getValue());
        IBaseEntity beSaved10 = loadByReportDate(id, may1);
        assertEquals(be3.getBaseValue("string_set").getValue(), beSaved10.getBaseValue("string_set").getValue());
    }


    @Test
    public void testHeuristic() throws Exception {
        String uuid = UUID.randomUUID().toString();
        long id;
        IBaseEntity beForClean;
        IBaseEntity be1= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"f"}, 1);
        beForClean = processorDao.process(be1);
        id = beForClean.getId();
        entitiesToClean.add(beForClean);
        IBaseEntity be2= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"d"}, 1);
        processorDao.process(be2);
        IBaseEntity be3= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"f"}, 1);
        processorDao.process(be3);
        IBaseEntity be4= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"f"}, 1);
        processorDao.process(be4);
        IBaseEntity be5= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"e"}, 1);
        processorDao.process(be5);
        IBaseEntity be6= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"e"}, 1);
        processorDao.process(be6);
        IBaseEntity be7= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"f"}, 1);
        processorDao.process(be7);
        IBaseEntity be8= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"c"}, 1);
        processorDao.process(be8);
        IBaseEntity be9= constructBaseEntity(metaDao.load("testMetaClass"), feb1, uuid, new String[]{"f"}, 1);
        processorDao.process(be9);
        IBaseEntity be10= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"e"}, 1);
        processorDao.process(be10);
        IBaseEntity be11= constructBaseEntity(metaDao.load("testMetaClass"), mar1, uuid, new String[]{"b"}, 1);
        processorDao.process(be11);
        IBaseEntity be12= constructBaseEntity(metaDao.load("testMetaClass"), apr1, uuid, new String[]{"c"}, 1);
        processorDao.process(be12);
        IBaseEntity be13= constructBaseEntity(metaDao.load("testMetaClass"), jan1, uuid, new String[]{"f"}, 1);
        processorDao.process(be13);
        IBaseEntity be14= constructBaseEntity(metaDao.load("testMetaClass"), may1, uuid, new String[]{"e"}, 1);
        processorDao.process(be14);
        IBaseEntity beSaved15 = loadByReportDate(id, jan1);
        assertEquals(be13.getBaseValue("string_set").getValue(), beSaved15.getBaseValue("string_set").getValue());
        IBaseEntity beSaved16 = loadByReportDate(id, feb1);
        assertEquals(be9.getBaseValue("string_set").getValue(), beSaved16.getBaseValue("string_set").getValue());
        IBaseEntity beSaved17 = loadByReportDate(id, mar1);
        assertEquals(be11.getBaseValue("string_set").getValue(), beSaved17.getBaseValue("string_set").getValue());
        IBaseEntity beSaved18 = loadByReportDate(id, apr1);
        assertEquals(be12.getBaseValue("string_set").getValue(), beSaved18.getBaseValue("string_set").getValue());
        IBaseEntity beSaved19 = loadByReportDate(id, may1);
        assertEquals(be14.getBaseValue("string_set").getValue(), beSaved19.getBaseValue("string_set").getValue());

        Select select = context.select(DSL.max(EAV_BE_STRING_SET_VALUES.SET_ID))
                .from(EAV_BE_STRING_SET_VALUES);

        long setId = jdbcTemplate.queryForInt(select.toString());

        SelectConditionStep dbSelect = context.select(DSL.count())
                .from(EAV_BE_STRING_SET_VALUES)
                .where(EAV_BE_STRING_SET_VALUES.SET_ID.eq(setId));


        //verify overall vals
        assertEquals(jdbcTemplate.queryForInt(dbSelect.toString()), 7);


        //verify f
        Select fSelect = dbSelect.and(EAV_BE_STRING_SET_VALUES.VALUE.eq("f"));
        assertEquals(jdbcTemplate.queryForInt(fSelect.toString()), 2);

        //verify is_closed count
        Select isClosedSelect = dbSelect.and(EAV_BE_STRING_SET_VALUES.IS_CLOSED.eq(DataUtils.convert(true)));
        assertEquals(jdbcTemplate.queryForInt(isClosedSelect.toString()), 3);

        //verify is_last count
        Select isLastSelect = dbSelect.and(EAV_BE_STRING_SET_VALUES.IS_LAST.eq(DataUtils.convert(true)));
        assertEquals(jdbcTemplate.queryForInt(isLastSelect.toString()), 2);
    }
}
