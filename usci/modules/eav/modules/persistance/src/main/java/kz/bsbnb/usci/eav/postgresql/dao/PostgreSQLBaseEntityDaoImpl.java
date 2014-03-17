package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.cr.model.DataTypeUtil;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.*;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBaseEntityRepository;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * @author a.motov
 */
@SuppressWarnings("unchecked")
@Repository
public class PostgreSQLBaseEntityDaoImpl extends JDBCSupport implements IBaseEntityDao
{
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBaseEntityDaoImpl.class);

    @Autowired
    IBatchRepository batchRepository;
    @Autowired
    IMetaClassRepository metaClassRepository;
    @Autowired
    IBaseEntityRepository baseEntityCacheDao;

    @Autowired
    IBeSetDao beSetDao;
    @Autowired
    IBeReportDateDao beReportDateDao;
    @Autowired
    IBeIntegerValueDao beIntegerValueDao;
    @Autowired
    IBeDateValueDao beDateValueDao;
    @Autowired
    IBeStringValueDao beStringValueDao;
    @Autowired
    IBeBooleanValueDao beBooleanValueDao;
    @Autowired
    IBeDoubleValueDao beDoubleValueDao;
    @Autowired
    IBeComplexValueDao beComplexValueDao;
    @Autowired
    IBeSimpleSetDao beSimpleSetValueDao;
    @Autowired
    IBeComplexSetDao beComplexSetValueDao;

    @Autowired
    SQLQueriesStats stats;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    @Override
    public IBaseEntity load(long id)
    {
        return load(id, false);
    }

    @Override
    public IBaseEntity load(long id, boolean caching)
    {
        Date maxReportDate = getMaxReportDate(id);
        if (maxReportDate == null)
        {
            throw new UnsupportedOperationException("Not found appropriate report date.");
        }

        if (caching)
        {
            return baseEntityCacheDao.getBaseEntity(id, maxReportDate);
        }

        return load(id, maxReportDate);
    }

    @Override
    public IBaseEntity load(long id, Date reportDate, boolean caching)
    {
        if (caching)
        {
            return baseEntityCacheDao.getBaseEntity(id, reportDate);
        }

        return load(id, reportDate);
    }

    public IBaseEntity load(long id, Date reportDate)
    {
        if(id < 1)
        {
            throw new IllegalArgumentException("Does not have id. Can't load.");
        }

        if (reportDate == null)
        {
            throw new IllegalArgumentException("To load instance of BaseEntity must always " +
                    "be specified report date.");
        }

        String tableAlias = "e";
        Select select = context
                .select(EAV_BE_ENTITIES.as(tableAlias).CLASS_ID)
                .from(EAV_BE_ENTITIES.as(tableAlias))
                .where(EAV_BE_ENTITIES.as(tableAlias).ID.equal(id));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one instance of BaseEntity found.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalStateException("Instance of BaseEntity was not found.");
        }

        Map<String, Object> row = rows.get(0);
        if(row != null)
        {
            long classId = ((BigDecimal)row.get(EAV_BE_ENTITIES.as(tableAlias).CLASS_ID.getName())).longValue();
            boolean last = DataTypeUtil.compareBeginningOfTheDay(getMaxReportDate(id), reportDate) == 0;

            MetaClass meta = metaClassRepository.getMetaClass(classId);
            IBaseEntityReportDate baseEntityReportDate = loadBaseEntityReportDate(id, reportDate);
            IBaseEntity baseEntity = new BaseEntity(id, meta, baseEntityReportDate);

            if (baseEntityReportDate.getIntegerValuesCount() != 0)
                loadIntegerValues(baseEntity, last);

            if (baseEntityReportDate.getDateValuesCount() != 0)
                loadDateValues(baseEntity, last);

            if (baseEntityReportDate.getStringValuesCount() != 0)
                loadStringValues(baseEntity, last);

            if (baseEntityReportDate.getBooleanValuesCount() != 0)
                loadBooleanValues(baseEntity, last);

            if (baseEntityReportDate.getDoubleValuesCount() != 0)
                loadDoubleValues(baseEntity, last);

            if (baseEntityReportDate.getComplexValuesCount() != 0)
                loadComplexValues(baseEntity, last);

            if (baseEntityReportDate.getSimpleSetsCount() != 0)
                loadEntitySimpleSets(baseEntity, last);

            if (baseEntityReportDate.getComplexSetsCount() != 0)
                loadEntityComplexSets(baseEntity, last);

            return baseEntity;
        }
        else
        {
            logger.error("Can't load instance of BaseEntity, empty data set.");
        }

        return null;
    }

    public IBaseEntityReportDate loadBaseEntityReportDate(long baseEntityId, Date reportDate)
    {
        if(baseEntityId < 1)
        {
            throw new IllegalArgumentException("To load instance of BaseEntityReportDate must always " +
                    "be specified entity ID.");
        }

        if (reportDate == null)
        {
            throw new IllegalArgumentException("To load instance of BaseEntityReportDate must always " +
                    "be specified report date.");
        }

        String tableAlias = "rd";
        Select select = context
                .select(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).INTEGER_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DATE_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).STRING_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).BOOLEAN_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DOUBLE_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).SIMPLE_SETS_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_SETS_COUNT)
                .from(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.equal(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE.eq(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one instance of BaseEntityReportDate found.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalStateException("Instance of BaseEntityReportDate was not found.");
        }

        Map<String, Object> row = rows.get(0);
        if(row != null)
        {
            long id = ((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID.getName())).longValue();
            long integerValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).INTEGER_VALUES_COUNT.getName())).longValue();
            long dateValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DATE_VALUES_COUNT.getName())).longValue();
            long stringValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).STRING_VALUES_COUNT.getName())).longValue();
            long booleanValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).BOOLEAN_VALUES_COUNT.getName())).longValue();
            long doubleValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DOUBLE_VALUES_COUNT.getName())).longValue();
            long complexValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_VALUES_COUNT.getName())).longValue();
            long simpleSetsCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).SIMPLE_SETS_COUNT.getName())).longValue();
            long complexSetsCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_SETS_COUNT.getName())).longValue();

            IBaseEntityReportDate baseEntityReportDate =
                    new BaseEntityReportDate(
                            id,
                            reportDate,
                            integerValuesCount,
                            dateValuesCount,
                            stringValuesCount,
                            booleanValuesCount,
                            doubleValuesCount,
                            complexValuesCount,
                            simpleSetsCount,
                            complexSetsCount);

            return baseEntityReportDate;
        }
        else
        {
            logger.error("Can't load instance of BaseEntityReportDate, empty data set.");
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long search(IBaseEntity baseEntity)
    {
        IMetaClass metaClass = baseEntity.getMeta();
        Long baseEntityId = searcherPool.getSearcher(metaClass.getClassName())
                .findSingle((BaseEntity)baseEntity);

        /*if (metaClass.isReference() && metaClass.isImmutable())
        {
            throw new RuntimeException(
                    String.format("MetaClass with name {0} marked as immutable reference.", metaClass.getClassName()));
        }*/

        return baseEntityId == null ? 0 : baseEntityId;
    }

    public List<Long> search(long metaClassId)
    {
        Select select = context
                .select(EAV_BE_ENTITIES.ID)
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Long> baseEntityIds = new ArrayList<Long>();
        for (Map<String, Object> row: rows)
        {
            baseEntityIds.add(((BigDecimal)row.get(EAV_BE_ENTITIES.ID.getName())).longValue());
        }

        return baseEntityIds;
    }

    public List<Long> search(String className)
    {
        MetaClass metaClass = metaClassRepository.getMetaClass(className);
        if (metaClass != null)
        {
            return search(metaClass.getId());
        }

        return new ArrayList<Long>();
    }


    public IBaseEntity prepare(IBaseEntity baseEntity)
    {
        IMetaClass metaClass = baseEntity.getMeta();
        for (String attribute: baseEntity.getAttributes())
        {
            IMetaType metaType = baseEntity.getMemberType(attribute);
            if (metaType.isComplex())
            {
                IBaseValue baseValue = baseEntity.getBaseValue(attribute);
                if (baseValue.getValue() != null)
                {
                    if (metaType.isSet())
                    {
                        IMetaSet childMetaSet = (IMetaSet)metaType;
                        IMetaType childMetaType = childMetaSet.getMemberType();
                        if (childMetaType.isSet())
                        {
                            throw new UnsupportedOperationException("Not yet implemented.");
                        }
                        else
                        {
                            IBaseSet childBaseSet = (IBaseSet)baseValue.getValue();
                            for (IBaseValue childBaseValue: childBaseSet.get())
                            {
                                IBaseEntity childBaseEntity = (IBaseEntity)childBaseValue.getValue();
                                if (childBaseEntity.getValueCount() != 0)
                                {
                                    prepare((IBaseEntity)childBaseValue.getValue());
                                }
                            }
                        }
                    }
                    else
                    {
                        IBaseEntity childBaseEntity = (IBaseEntity)baseValue.getValue();
                        if (childBaseEntity.getValueCount() != 0)
                        {
                            prepare((IBaseEntity)baseValue.getValue());
                        }
                    }
                }
            }
        }

        if (metaClass.isSearchable())
        {
            long baseEntityId = search(baseEntity);
            if (baseEntityId > 0)
            {
                baseEntity.setId(baseEntityId);
            }
        }

        return baseEntity;
    }

    public void applyToDb(IBaseEntityManager baseEntityManager)
    {
        for (Class objectClass: BaseEntityManager.CLASS_PRIORITY)
        {
            List<IPersistable> insertedObjects = baseEntityManager.getInsertedObjects(objectClass);
            if (insertedObjects != null && insertedObjects.size() != 0)
            {
                if (objectClass == BaseEntity.class)
                {
                    for (IPersistable insertedObject: insertedObjects)
                    {
                        long id = insertBaseEntity((IBaseEntity) insertedObject);
                        insertedObject.setId(id);
                    }
                }
                if (objectClass == BaseEntityReportDate.class)
                {
                    for (IPersistable insertedObject: insertedObjects)
                    {
                        beReportDateDao.insert(insertedObject);
                    }
                }
                if (objectClass == BaseValue.class)
                {
                    for (IPersistable insertedObject: insertedObjects)
                    {
                        IBaseValue baseValue = (IBaseValue)insertedObject;
                        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
                        IMetaType metaType = metaAttribute.getMetaType(); 
                        if (metaType.isComplex())
                        {
                            if (metaType.isSet())
                            {
                                IBaseContainer baseContainer = baseValue.getBaseContainer();
                                if (baseContainer instanceof BaseEntity)
                                {
                                    beComplexSetValueDao.insert(insertedObject);
                                }
                                else
                                {
                                    beSimpleSetValueDao.insert(insertedObject);
                                }
                            }
                            else
                            {
                                beComplexValueDao.insert(insertedObject);
                            }
                        }
                        else
                        {
                            if (metaType.isSet())
                            {

                            }
                            else
                            {
                                IMetaValue metaValue = (IMetaValue)metaType;
                                switch (metaValue.getTypeCode())
                                {
                                    case INTEGER:
                                        beIntegerValueDao.insert(insertedObject);
                                        break;
                                    case DATE:
                                        beDateValueDao.insert(insertedObject);
                                        break;
                                    case STRING:
                                        beStringValueDao.insert(insertedObject);
                                        break;
                                    case BOOLEAN:
                                        beBooleanValueDao.insert(insertedObject);
                                        break;
                                    case DOUBLE:
                                        beDoubleValueDao.insert(insertedObject);
                                        break;
                                    default:
                                        throw new RuntimeException("Unknown data type.");
                                }
                            }
                        }
                    }
                }
                if (objectClass == BaseSet.class)
                {
                    for (IPersistable insertedObject: insertedObjects)
                    {
                        beSetDao.insert(insertedObject);
                    }
                }
            }
        }
    }

    @Override
    public IBaseEntity apply(IBaseEntity baseEntityForSave, IBaseEntityManager baseEntityManager)
    {
        if (baseEntityForSave.getId() < 1 || !baseEntityForSave.getMeta().isSearchable())
        {
            return applyWithoutComparison(baseEntityForSave, baseEntityManager);
        }
        else
        {
            Date reportDate = baseEntityForSave.getReportDate();
            Date maxReportDate = getMaxReportDate(baseEntityForSave.getId(), reportDate);

            if (maxReportDate == null)
            {
                return applyWithoutComparison(baseEntityForSave, baseEntityManager);
            }
            else
            {
                IBaseEntity baseEntityLoaded = load(baseEntityForSave.getId(), maxReportDate);
                return applyWithComparison(baseEntityForSave, baseEntityLoaded);
            }
        }
    }

    private IBaseEntity applyWithoutComparison(IBaseEntity baseEntity, IBaseEntityManager baseEntityManager)
    {
        IBaseEntity baseEntityApplied = new BaseEntity(baseEntity);
        for (String attribute: baseEntity.getAttributes())
        {
            IMetaAttribute metaAttribute = baseEntity.getMetaAttribute(attribute);
            IMetaType metaType = metaAttribute.getMetaType();
            IBaseValue baseValue = baseEntity.getBaseValue(attribute);
            if (baseValue.getValue() != null)
            {
                if (metaType.isComplex())
                {
                    if (metaType.isSet())
                    {
                        if (metaType.isSetOfSets())
                        {
                            throw new UnsupportedOperationException("Not yet implemented.");
                        }
                        else
                        {
                            IMetaSet childMetaSet = (IMetaSet)metaType;
                            IBaseSet childBaseSet = (IBaseSet)baseValue.getValue();

                            // TODO: Add implementation of immutable complex values in sets

                            IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());
                            for (IBaseValue childBaseValue: childBaseSet.get())
                            {
                                IBaseEntity childBaseEntity = (IBaseEntity)childBaseValue.getValue();
                                IBaseEntity childBaseEntityApplied = apply(childBaseEntity, baseEntityManager);

                                IBaseValue childBaseValueApplied =
                                        new BaseValue(
                                                childBaseValue.getBatch(),
                                                childBaseValue.getIndex(),
                                                new Date(baseValue.getRepDate().getTime()),
                                                childBaseEntityApplied,
                                                false,
                                                true);
                                childBaseSetApplied.put(childBaseValueApplied);
                                baseEntityManager.registerAsInserted(childBaseValueApplied);
                            }

                            baseEntityManager.registerAsInserted(childBaseSetApplied);

                            IBaseValue baseValueApplied =
                                    new BaseValue(
                                            baseValue.getBatch(),
                                            baseValue.getIndex(),
                                            new Date(baseValue.getRepDate().getTime()),
                                            childBaseSetApplied,
                                            false,
                                            true
                                    );

                            baseEntityApplied.put(attribute, baseValueApplied);
                            baseEntityManager.registerAsInserted(baseValueApplied);
                        }
                    }
                    else
                    {
                        if (metaAttribute.isImmutable())
                        {
                            IMetaClass childMetaClass = (IMetaClass)metaType;
                            IBaseEntity childBaseEntity = (IBaseEntity)baseValue.getValue();
                            if (childBaseEntity.getValueCount() != 0)
                            {
                                if (childBaseEntity.getId() < 1)
                                {
                                    throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                            childBaseEntity.getMeta().getClassName() + "\n" + childBaseEntity.toString());
                                }

                                IBaseEntity childBaseEntityImmutable = load(childBaseEntity.getId(), childBaseEntity.getReportDate(),
                                        childMetaClass.isReference());
                                if (childBaseEntityImmutable == null)
                                {
                                    throw new RuntimeException(String.format("Instance of BaseEntity with id {0} not found in the DB.",
                                            childBaseEntity.getId()));
                                }

                                IBaseValue baseValueApplied =
                                        new BaseValue(
                                                baseValue.getBatch(),
                                                baseValue.getIndex(),
                                                new Date(baseValue.getRepDate().getTime()),
                                                childBaseEntityImmutable,
                                                false,
                                                true
                                        );

                                baseEntityApplied.put(attribute, baseValueApplied);
                                baseEntityManager.registerAsInserted(baseValueApplied);
                            }
                        }
                        else
                        {
                            IBaseEntity childbaseEntity = (IBaseEntity)baseValue.getValue();
                            IBaseEntity childBaseEntityApplied = apply(childbaseEntity, baseEntityManager);

                            IBaseValue baseValueApplied =
                                    new BaseValue(
                                            baseValue.getBatch(),
                                            baseValue.getIndex(),
                                            new Date(baseValue.getRepDate().getTime()),
                                            childBaseEntityApplied,
                                            false,
                                            true
                                    );

                            baseEntityApplied.put(attribute, baseValueApplied);
                            baseEntityManager.registerAsInserted(baseValueApplied);
                        }
                    }
                }
                else
                {
                    if (metaType.isSet())
                    {

                    }
                    else
                    {
                        IMetaValue metaValue = (IMetaValue)metaType;
                        IBaseValue baseValueApplied =
                                new BaseValue(
                                        baseValue.getBatch(),
                                        baseValue.getIndex(),
                                        new Date(baseValue.getRepDate().getTime()),
                                        metaValue.getTypeCode() == DataTypes.DATE ?
                                                new Date(((Date)baseValue.getValue()).getTime()) :
                                                baseValue.getValue(),
                                        false,
                                        true
                                );

                        baseEntityApplied.put(attribute, baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
            }
        }

        baseEntityApplied.calculateValueCount();
        baseEntityManager.registerAsInserted(baseEntityApplied);

        IBaseEntityReportDate baseEntityReportDate =
                baseEntityApplied.getBaseEntityReportDate();
        baseEntityManager.registerAsInserted(baseEntityReportDate);

        return baseEntityApplied;
    }

    private IBaseEntity applyWithComparison(IBaseEntity baseEntityForSave, IBaseEntity baseEntityLoaded)
    {
        return ((BaseEntity)baseEntityLoaded).clone();
    }

    public IBaseSet apply(IBaseSet baseSetForSave, IBaseSet baseSetLoaded)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional
    public IBaseEntity process(IBaseEntity baseEntity)
    {
        IBaseEntityManager baseEntityManager = new BaseEntityManager();
        IBaseEntity baseEntityPrepared = prepare(((BaseEntity)baseEntity).clone());
        IBaseEntity baseEntityApplied = apply(baseEntityPrepared, baseEntityManager);

        applyToDb(baseEntityManager);

        return baseEntityApplied;
    }

    public boolean isUsed(long baseEntityId)
    {
        Select select;
        List<Map<String, Object>> rows;

        select = context
                .select(DSL.count().as("VALUE_COUNT"))
                .from(EAV_BE_COMPLEX_VALUES)
                .where(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.equal(baseEntityId));

        logger.debug(select.toString());
        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        long complexValuesCount = ((BigDecimal)rows.get(0).get("VALUE_COUNT")).longValue();

        select = context
                .select(DSL.count().as("VALUE_COUNT"))
                .from(EAV_BE_COMPLEX_SET_VALUES)
                .where(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.equal(baseEntityId));

        logger.debug(select.toString());
        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        long complexSetValuesCount = ((BigDecimal)rows.get(0).get("VALUE_COUNT")).longValue();

        return complexValuesCount != 0 || complexSetValuesCount != 0;
    }

    public Set<Date> getAvailableReportDates(long baseEntityId)
    {
        Set<Date> reportDates = new HashSet<Date>();

        Select select = context
                .select(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            reportDates.add(DataUtils.convert((Timestamp) row.get(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.getName())));
        }

        return reportDates;
    }

    public Date getMinReportDate(long baseEntityId)
    {
        Select select = context
                .select(DSL.min(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("min_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("min_report_date"));
    }

    public Date getMaxReportDate(long baseEntityId, Date reportDate)
    {
        Select select = context
                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("min_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.lessOrEqual(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("min_report_date"));
    }

    public Date getMaxReportDate(long baseEntityId)
    {
        String tableAlias = "rd";
        Select select = context
                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE).as("max_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("max_report_date"));
    }

    private long insertBaseEntity(IBaseEntity baseEntity)
    {
        if(baseEntity.getMeta().getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before entity insertion to DB.");
        }

        InsertOnDuplicateStep insert = context
                .insertInto(EAV_BE_ENTITIES, EAV_BE_ENTITIES.CLASS_ID)
                .values(baseEntity.getMeta().getId());

        logger.debug(insert.toString());

        long baseEntityId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        if(baseEntityId < 1)
        {
            logger.error("Can't insert entity");
            return 0;
        }

        return baseEntityId;
    }

    private void loadIntegerValues(IBaseEntity baseEntity, boolean last)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_INTEGER_VALUES.as("v");
        Select select = null;

        if (last)
        {
            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.ID),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .join(tableOfAttributes)
                    .on(tableOfValues.field(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableOfValues.field(EAV_BE_INTEGER_VALUES.ENTITY_ID).equal(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_LAST).equal(true)
                    .and(tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID))
                            .orderBy(tableOfValues.field(EAV_BE_INTEGER_VALUES.REPORT_DATE)).as("num_pp"),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.ID),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.ENTITY_ID),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_INTEGER_VALUES.ENTITY_ID).eq(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_INTEGER_VALUES.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(baseEntity.getReportDate())))
                    .asTable("vn");
    
            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableNumbering.field(EAV_BE_INTEGER_VALUES.ID),
                            tableNumbering.field(EAV_BE_INTEGER_VALUES.BATCH_ID),
                            tableNumbering.field(EAV_BE_INTEGER_VALUES.INDEX_),
                            tableNumbering.field(EAV_BE_INTEGER_VALUES.REPORT_DATE),
                            tableNumbering.field(EAV_BE_INTEGER_VALUES.VALUE),
                            tableNumbering.field(EAV_BE_INTEGER_VALUES.IS_CLOSED),
                            tableNumbering.field(EAV_BE_INTEGER_VALUES.IS_LAST))
                    .from(tableNumbering)
                    .join(tableOfAttributes)
                    .on(tableNumbering.field(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_INTEGER_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            ((BigDecimal) row.get(EAV_BE_INTEGER_VALUES.ID.getName())).longValue(),
                            batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_INTEGER_VALUES.BATCH_ID.getName())).longValue()),
                            ((BigDecimal) row.get(EAV_BE_INTEGER_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_INTEGER_VALUES.REPORT_DATE.getName())),
                            ((BigDecimal)row.get(EAV_BE_INTEGER_VALUES.VALUE.getName())).intValue(),
                            ((BigDecimal)row.get(EAV_BE_INTEGER_VALUES.IS_CLOSED.getName())).longValue() == 1,
                            ((BigDecimal)row.get(EAV_BE_INTEGER_VALUES.IS_LAST.getName())).longValue() == 1));
        }
    }

    private void loadDateValues(IBaseEntity baseEntity, boolean last)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_DATE_VALUES.as("v");
        Select select = null;

        if (last)
        {
            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableOfValues.field(EAV_BE_DATE_VALUES.ID),
                            tableOfValues.field(EAV_BE_DATE_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_DATE_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_DATE_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_DATE_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_DATE_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_DATE_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .join(tableOfAttributes)
                    .on(tableOfValues.field(EAV_BE_DATE_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableOfValues.field(EAV_BE_DATE_VALUES.ENTITY_ID).equal(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_DATE_VALUES.IS_LAST).equal(true)
                    .and(tableOfValues.field(EAV_BE_DATE_VALUES.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_DATE_VALUES.ATTRIBUTE_ID))
                            .orderBy(tableOfValues.field(EAV_BE_DATE_VALUES.REPORT_DATE)).as("num_pp"),
                            tableOfValues.field(EAV_BE_DATE_VALUES.ID),
                            tableOfValues.field(EAV_BE_DATE_VALUES.ENTITY_ID),
                            tableOfValues.field(EAV_BE_DATE_VALUES.ATTRIBUTE_ID),
                            tableOfValues.field(EAV_BE_DATE_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_DATE_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_DATE_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_DATE_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_DATE_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_DATE_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_DATE_VALUES.ENTITY_ID).eq(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_DATE_VALUES.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(baseEntity.getReportDate())))
                    .asTable("vn");

            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableNumbering.field(EAV_BE_DATE_VALUES.ID),
                            tableNumbering.field(EAV_BE_DATE_VALUES.BATCH_ID),
                            tableNumbering.field(EAV_BE_DATE_VALUES.INDEX_),
                            tableNumbering.field(EAV_BE_DATE_VALUES.REPORT_DATE),
                            tableNumbering.field(EAV_BE_DATE_VALUES.VALUE),
                            tableNumbering.field(EAV_BE_DATE_VALUES.IS_CLOSED),
                            tableNumbering.field(EAV_BE_DATE_VALUES.IS_LAST))
                    .from(tableNumbering)
                    .join(tableOfAttributes)
                    .on(tableNumbering.field(EAV_BE_DATE_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_DATE_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            ((BigDecimal) row.get(EAV_BE_DATE_VALUES.ID.getName())).longValue(),
                            batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_DATE_VALUES.BATCH_ID.getName())).longValue()),
                            ((BigDecimal) row.get(EAV_BE_DATE_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_DATE_VALUES.REPORT_DATE.getName())),
                            DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_DATE_VALUES.VALUE.getName())),
                            ((BigDecimal)row.get(EAV_BE_DATE_VALUES.IS_CLOSED.getName())).longValue() == 1,
                            ((BigDecimal)row.get(EAV_BE_DATE_VALUES.IS_LAST.getName())).longValue() == 1));
        }
    }

    private void loadBooleanValues(IBaseEntity baseEntity, boolean last)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_BOOLEAN_VALUES.as("v");
        Select select = null;

        if (last)
        {
            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ID),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .join(tableOfAttributes)
                    .on(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ENTITY_ID).equal(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_LAST).equal(true)
                    .and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID))
                            .orderBy(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE)).as("num_pp"),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ID),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ENTITY_ID),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ENTITY_ID).eq(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(baseEntity.getReportDate())))
                    .asTable("vn");

            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableNumbering.field(EAV_BE_BOOLEAN_VALUES.ID),
                            tableNumbering.field(EAV_BE_BOOLEAN_VALUES.BATCH_ID),
                            tableNumbering.field(EAV_BE_BOOLEAN_VALUES.INDEX_),
                            tableNumbering.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE),
                            tableNumbering.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                            tableNumbering.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                            tableNumbering.field(EAV_BE_BOOLEAN_VALUES.IS_LAST))
                    .from(tableNumbering)
                    .join(tableOfAttributes)
                    .on(tableNumbering.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            ((BigDecimal) row.get(EAV_BE_BOOLEAN_VALUES.ID.getName())).longValue(),
                            batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_BOOLEAN_VALUES.BATCH_ID.getName())).longValue()),
                            ((BigDecimal) row.get(EAV_BE_BOOLEAN_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_BOOLEAN_VALUES.REPORT_DATE.getName())),
                            ((BigDecimal)row.get(EAV_BE_BOOLEAN_VALUES.VALUE.getName())).longValue() == 1,
                            ((BigDecimal)row.get(EAV_BE_BOOLEAN_VALUES.IS_CLOSED.getName())).longValue() == 1,
                            ((BigDecimal)row.get(EAV_BE_BOOLEAN_VALUES.IS_LAST.getName())).longValue() == 1));
        }
    }

    private void loadStringValues(IBaseEntity baseEntity, boolean last)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_STRING_VALUES.as("v");
        Select select = null;

        if (last)
        {
            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableOfValues.field(EAV_BE_STRING_VALUES.ID),
                            tableOfValues.field(EAV_BE_STRING_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_STRING_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_STRING_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_STRING_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_STRING_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_STRING_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .join(tableOfAttributes)
                    .on(tableOfValues.field(EAV_BE_STRING_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableOfValues.field(EAV_BE_STRING_VALUES.ENTITY_ID).equal(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_STRING_VALUES.IS_LAST).equal(true)
                    .and(tableOfValues.field(EAV_BE_STRING_VALUES.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_STRING_VALUES.ATTRIBUTE_ID))
                            .orderBy(tableOfValues.field(EAV_BE_STRING_VALUES.REPORT_DATE)).as("num_pp"),
                            tableOfValues.field(EAV_BE_STRING_VALUES.ID),
                            tableOfValues.field(EAV_BE_STRING_VALUES.ENTITY_ID),
                            tableOfValues.field(EAV_BE_STRING_VALUES.ATTRIBUTE_ID),
                            tableOfValues.field(EAV_BE_STRING_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_STRING_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_STRING_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_STRING_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_STRING_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_STRING_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_STRING_VALUES.ENTITY_ID).eq(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_STRING_VALUES.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(baseEntity.getReportDate())))
                    .asTable("vn");

            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableNumbering.field(EAV_BE_STRING_VALUES.ID),
                            tableNumbering.field(EAV_BE_STRING_VALUES.BATCH_ID),
                            tableNumbering.field(EAV_BE_STRING_VALUES.INDEX_),
                            tableNumbering.field(EAV_BE_STRING_VALUES.REPORT_DATE),
                            tableNumbering.field(EAV_BE_STRING_VALUES.VALUE),
                            tableNumbering.field(EAV_BE_STRING_VALUES.IS_CLOSED),
                            tableNumbering.field(EAV_BE_STRING_VALUES.IS_LAST))
                    .from(tableNumbering)
                    .join(tableOfAttributes)
                    .on(tableNumbering.field(EAV_BE_STRING_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_STRING_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            ((BigDecimal) row.get(EAV_BE_STRING_VALUES.ID.getName())).longValue(),
                            batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_STRING_VALUES.BATCH_ID.getName())).longValue()),
                            ((BigDecimal) row.get(EAV_BE_STRING_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_STRING_VALUES.REPORT_DATE.getName())),
                            row.get(EAV_BE_STRING_VALUES.VALUE.getName()),
                            ((BigDecimal)row.get(EAV_BE_STRING_VALUES.IS_CLOSED.getName())).longValue() == 1,
                            ((BigDecimal)row.get(EAV_BE_STRING_VALUES.IS_LAST.getName())).longValue() == 1));
        }
    }

    private void loadDoubleValues(IBaseEntity baseEntity, boolean last)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_DOUBLE_VALUES.as("v");
        Select select = null;

        if (last)
        {
            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.ID),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .join(tableOfAttributes)
                    .on(tableOfValues.field(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableOfValues.field(EAV_BE_DOUBLE_VALUES.ENTITY_ID).equal(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_LAST).equal(true)
                    .and(tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID))
                            .orderBy(tableOfValues.field(EAV_BE_DOUBLE_VALUES.REPORT_DATE)).as("num_pp"),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.ID),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.ENTITY_ID),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_DOUBLE_VALUES.ENTITY_ID).eq(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_DOUBLE_VALUES.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(baseEntity.getReportDate())))
                    .asTable("vn");

            select = context
                    .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                            tableNumbering.field(EAV_BE_DOUBLE_VALUES.ID),
                            tableNumbering.field(EAV_BE_DOUBLE_VALUES.BATCH_ID),
                            tableNumbering.field(EAV_BE_DOUBLE_VALUES.INDEX_),
                            tableNumbering.field(EAV_BE_DOUBLE_VALUES.REPORT_DATE),
                            tableNumbering.field(EAV_BE_DOUBLE_VALUES.VALUE),
                            tableNumbering.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED),
                            tableNumbering.field(EAV_BE_DOUBLE_VALUES.IS_LAST))
                    .from(tableNumbering)
                    .join(tableOfAttributes)
                    .on(tableNumbering.field(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            ((BigDecimal) row.get(EAV_BE_DOUBLE_VALUES.ID.getName())).longValue(),
                            batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_DOUBLE_VALUES.BATCH_ID.getName())).longValue()),
                            ((BigDecimal) row.get(EAV_BE_DOUBLE_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_DOUBLE_VALUES.REPORT_DATE.getName())),
                            ((BigDecimal)row.get(EAV_BE_DOUBLE_VALUES.VALUE.getName())).doubleValue(),
                            ((BigDecimal)row.get(EAV_BE_DOUBLE_VALUES.IS_CLOSED.getName())).longValue() == 1,
                            ((BigDecimal)row.get(EAV_BE_DOUBLE_VALUES.IS_LAST.getName())).longValue() == 1));
        }
    }

    private void loadComplexValues(IBaseEntity baseEntity, boolean last)
    {
        IMetaClass metaClass = baseEntity.getMeta();

        Table tableOfAttributes = EAV_M_COMPLEX_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_COMPLEX_VALUES.as("v");
        Select select = null;

        if (last)
        {
            select = context
                    .select(tableOfAttributes.field(EAV_M_COMPLEX_ATTRIBUTES.NAME),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.ID),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .join(tableOfAttributes)
                    .on(tableOfValues.field(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_COMPLEX_ATTRIBUTES.ID)))
                    .where(tableOfValues.field(EAV_BE_COMPLEX_VALUES.ENTITY_ID).equal(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_LAST).equal(true)
                    .and(tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID))
                            .orderBy(tableOfValues.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE)).as("num_pp"),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.ID),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.ENTITY_ID),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_COMPLEX_VALUES.ENTITY_ID).eq(baseEntity.getId()))
                    .and(tableOfValues.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(baseEntity.getReportDate())))
                    .asTable("vn");

            select = context
                    .select(tableOfAttributes.field(EAV_M_COMPLEX_ATTRIBUTES.NAME),
                            tableNumbering.field(EAV_BE_COMPLEX_VALUES.ID),
                            tableNumbering.field(EAV_BE_COMPLEX_VALUES.BATCH_ID),
                            tableNumbering.field(EAV_BE_COMPLEX_VALUES.INDEX_),
                            tableNumbering.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE),
                            tableNumbering.field(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID),
                            tableNumbering.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED),
                            tableNumbering.field(EAV_BE_COMPLEX_VALUES.IS_LAST))
                    .from(tableNumbering)
                    .join(tableOfAttributes)
                    .on(tableNumbering.field(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID)
                            .eq(tableOfAttributes.field(EAV_M_COMPLEX_ATTRIBUTES.ID)))
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            long id = ((BigDecimal) row.get(EAV_BE_COMPLEX_VALUES.ID.getName())).longValue();
            long batchId = ((BigDecimal)row.get(EAV_BE_COMPLEX_VALUES.BATCH_ID.getName())).longValue();
            long index = ((BigDecimal) row.get(EAV_BE_COMPLEX_VALUES.INDEX_.getName())).longValue();
            long entityValueId = ((BigDecimal)row.get(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.getName())).longValue();
            boolean isClosed = ((BigDecimal)row.get(EAV_BE_COMPLEX_VALUES.IS_CLOSED.getName())).longValue() == 1;
            boolean isLast = ((BigDecimal)row.get(EAV_BE_COMPLEX_VALUES.IS_LAST.getName())).longValue() == 1;
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_COMPLEX_VALUES.REPORT_DATE.getName()));
            String attribute = (String) row.get(EAV_M_COMPLEX_ATTRIBUTES.NAME.getName());

            Batch batch = batchRepository.getBatch(batchId);
            IMetaClass childMetaClass = (IMetaClass)metaClass.getMemberType(attribute);
            IBaseEntity childBaseEntity = load(entityValueId, childMetaClass.isReference());

            baseEntity.put(attribute, new BaseValue(id, batch, index, reportDate, childBaseEntity, isClosed, isLast));
        }
    }

    private void loadEntitySimpleSets(IBaseEntity baseEntity, boolean last)
    {
        Table tableOfSimpleSets = EAV_M_SIMPLE_SET.as("ss");
        Table tableOfEntitySimpleSets = EAV_BE_ENTITY_SIMPLE_SETS.as("ess");
        Select select = null;

        if (last)
        {
            select = context
                    .select(tableOfSimpleSets.field(EAV_M_SIMPLE_SET.NAME),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ID),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST))
                    .from(tableOfEntitySimpleSets)
                    .join(tableOfSimpleSets)
                    .on(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID)
                            .eq(tableOfSimpleSets.field(EAV_M_SIMPLE_SET.ID)))
                    .where(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID).equal(baseEntity.getId()))
                    .and(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST).equal(true)
                    .and(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID))
                            .orderBy(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE)).as("num_pp"),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ID),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED),
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST))
                    .from(tableOfEntitySimpleSets)
                    .where(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID).eq(baseEntity.getId()))
                    .and(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(baseEntity.getReportDate())))
                    .asTable("essn");

            select = context
                    .select(tableOfSimpleSets.field(EAV_M_SIMPLE_SET.NAME),
                            tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.ID),
                            tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID),
                            tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_),
                            tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE),
                            tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID),
                            tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED),
                            tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST))
                    .from(tableNumbering)
                    .join(tableOfSimpleSets)
                    .on(tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID)
                            .eq(tableOfSimpleSets.field(EAV_M_SIMPLE_SET.ID)))
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            String attribute = (String)row.get(EAV_M_SIMPLE_SET.NAME.getName());
            long setId = ((BigDecimal)row.get(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID.getName())).longValue();

            long baseValueId = ((BigDecimal)row.get(EAV_BE_ENTITY_SIMPLE_SETS.ID.getName())).longValue();
            long batchId = ((BigDecimal)row.get(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID.getName())).longValue();
            long index = ((BigDecimal)row.get(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_.getName())).longValue();
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE.getName()));

            IMetaType metaType = baseEntity.getMemberType(attribute);
            IBaseSet baseSet = new BaseSet(setId, ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSet);
            }
            else
            {
                loadSimpleSetValues(baseSet);
            }

            Batch batch = batchRepository.getBatch(batchId);
            baseEntity.put(attribute, new BaseValue(baseValueId, batch, index, reportDate, baseSet));
        }
    }

    private void loadEntityComplexSets(IBaseEntity baseEntity, boolean last)
    {
        Table tableOfComplexSets = EAV_M_COMPLEX_SET.as("cs");
        Table tableOfEntityComplexSets = EAV_BE_ENTITY_COMPLEX_SETS.as("ecs");
        Select select = null;

        if (last)
        {
            select = context
                    .select(tableOfComplexSets.field(EAV_M_COMPLEX_SET.NAME),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ID),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.INDEX_),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST))
                    .from(tableOfEntityComplexSets)
                    .join(tableOfComplexSets)
                    .on(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID)
                            .eq(tableOfComplexSets.field(EAV_M_COMPLEX_SET.ID)))
                    .where(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID).equal(baseEntity.getId()))
                    .and(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST).equal(true)
                    .and(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID))
                            .orderBy(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE)).as("num_pp"),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ID),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.INDEX_),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED),
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST))
                    .from(tableOfEntityComplexSets)
                    .where(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID).eq(baseEntity.getId()))
                    .and(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(baseEntity.getReportDate())))
                    .asTable("essn");

            select = context
                    .select(tableOfComplexSets.field(EAV_M_COMPLEX_SET.NAME),
                            tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.ID),
                            tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID),
                            tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.INDEX_),
                            tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE),
                            tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID),
                            tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED),
                            tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST))
                    .from(tableNumbering)
                    .join(tableOfComplexSets)
                    .on(tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID)
                            .eq(tableOfComplexSets.field(EAV_M_COMPLEX_SET.ID)))
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            String attribute = (String)row.get(EAV_M_COMPLEX_SET.NAME.getName());
            long setId = ((BigDecimal)row.get(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID.getName())).longValue();

            long baseValueId = ((BigDecimal)row.get(EAV_BE_ENTITY_COMPLEX_SETS.ID.getName())).longValue();
            long batchId = ((BigDecimal)row.get(EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID.getName())).longValue();
            long index = ((BigDecimal)row.get(EAV_BE_ENTITY_COMPLEX_SETS.INDEX_.getName())).longValue();
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE.getName()));

            IMetaType metaType = baseEntity.getMemberType(attribute);
            IBaseSet baseSet = new BaseSet(setId, ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSet);
            }
            else
            {
                loadSimpleSetValues(baseSet);
            }

            Batch batch = batchRepository.getBatch(batchId);
            baseEntity.put(attribute, new BaseValue(baseValueId, batch, index, reportDate, baseSet));
        }
    }

    private void loadSetOfSimpleSets(IBaseSet baseSet)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SET_OF_SIMPLE_SETS.BATCH_ID,
                        EAV_BE_SET_OF_SIMPLE_SETS.INDEX_,
                        EAV_BE_SET_OF_SIMPLE_SETS.REPORT_DATE)
                .from(EAV_BE_SET_OF_SIMPLE_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_SET_OF_SIMPLE_SETS.CHILD_SET_ID.eq(EAV_BE_SETS.ID))
                .where(EAV_BE_SET_OF_SIMPLE_SETS.PARENT_SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            IMetaType metaType = baseSet.getMemberType();
            BaseSet baseSetChild = new BaseSet(((BigDecimal)row.get(EAV_BE_SETS.ID.getName())).longValue(), ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSetChild);
            }
            else
            {
                loadSimpleSetValues(baseSetChild);
            }

            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_SET_OF_SIMPLE_SETS.BATCH_ID.getName())).longValue());
            baseSet.put(new BaseValue(batch, ((BigDecimal)row.get(EAV_BE_SET_OF_SIMPLE_SETS.INDEX_.getName())).longValue(),
                    DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_SET_OF_SIMPLE_SETS.REPORT_DATE.getName())), baseSetChild));
        }
    }

    private void loadSetOfComplexSets(IBaseSet baseSet)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SET_OF_COMPLEX_SETS.BATCH_ID,
                        EAV_BE_SET_OF_COMPLEX_SETS.INDEX_,
                        EAV_BE_SET_OF_COMPLEX_SETS.REPORT_DATE)
                .from(EAV_BE_SET_OF_COMPLEX_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_SET_OF_COMPLEX_SETS.CHILD_SET_ID.eq(EAV_BE_SETS.ID))
                .where(EAV_BE_SET_OF_COMPLEX_SETS.PARENT_SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            IMetaType metaType = baseSet.getMemberType();
            BaseSet baseSetChild = new BaseSet(((BigDecimal)row.get(EAV_BE_SETS.ID.getName())).longValue(), ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSetChild);
            }
            else
            {
                loadSimpleSetValues(baseSetChild);
            }


            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_SET_OF_COMPLEX_SETS.BATCH_ID.getName())).longValue());
            baseSet.put(new BaseValue(batch, ((BigDecimal)row.get(EAV_BE_SET_OF_COMPLEX_SETS.INDEX_.getName())).longValue(),
                    DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_SET_OF_COMPLEX_SETS.REPORT_DATE.getName())), baseSetChild));
        }
    }

    private void loadSimpleSetValues(IBaseSet baseSet)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (metaType.isComplex())
            throw new RuntimeException("Load the simple set values is not possible. " +
                    "Simple values ??can not be added to an set of complex values.");

        if (metaType.isSet())
        {
            loadSetOfSimpleSets(baseSet);
        }
        else
        {
            MetaValue metaValue = (MetaValue)metaType;
            DataTypes dataType = metaValue.getTypeCode();

            switch(dataType)
            {
                case INTEGER:
                {
                    loadIntegerSetValues(baseSet);
                    break;
                }
                case DATE:
                {
                    loadDateSetValues(baseSet);
                    break;
                }
                case STRING:
                {
                    loadStringSetValues(baseSet);
                    break;
                }
                case BOOLEAN:
                {
                    loadBooleanSetValues(baseSet);
                    break;
                }
                case DOUBLE:
                {
                    loadDoubleSetValues(baseSet);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }
        }
    }

    private void loadIntegerSetValues(IBaseSet baseSet)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_INTEGER_SET_VALUES.BATCH_ID,
                        EAV_BE_INTEGER_SET_VALUES.INDEX_,
                        EAV_BE_INTEGER_SET_VALUES.VALUE,
                        EAV_BE_INTEGER_SET_VALUES.REPORT_DATE)
                .from(EAV_BE_INTEGER_SET_VALUES)
                .where(EAV_BE_INTEGER_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch(((BigDecimal)rowValue.get(EAV_BE_INTEGER_SET_VALUES.BATCH_ID.getName())).longValue());
            baseSet.put(
                    new BaseValue(
                            batch,
                            ((BigDecimal)rowValue.get(EAV_BE_INTEGER_SET_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) rowValue.get(EAV_BE_INTEGER_SET_VALUES.REPORT_DATE.getName())),
                            ((BigDecimal)rowValue.get(EAV_BE_INTEGER_SET_VALUES.VALUE.getName())).intValue()));
        }
    }

    private void loadDateSetValues(IBaseSet baseSet)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_DATE_SET_VALUES.BATCH_ID,
                        EAV_BE_DATE_SET_VALUES.INDEX_,
                        EAV_BE_DATE_SET_VALUES.VALUE,
                        EAV_BE_DATE_SET_VALUES.REPORT_DATE)
                .from(EAV_BE_DATE_SET_VALUES)
                .where(EAV_BE_DATE_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch(((BigDecimal)rowValue.get(EAV_BE_DATE_SET_VALUES.BATCH_ID.getName())).longValue());
            baseSet.put(
                    new BaseValue(
                            batch,
                            ((BigDecimal)rowValue.get(EAV_BE_DATE_SET_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) rowValue.get(EAV_BE_DATE_SET_VALUES.REPORT_DATE.getName())),
                            rowValue.get(EAV_BE_DATE_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadStringSetValues(IBaseSet baseSet)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_STRING_SET_VALUES.BATCH_ID,
                        EAV_BE_STRING_SET_VALUES.INDEX_,
                        EAV_BE_STRING_SET_VALUES.VALUE,
                        EAV_BE_STRING_SET_VALUES.REPORT_DATE)
                .from(EAV_BE_STRING_SET_VALUES)
                .where(EAV_BE_STRING_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch(((BigDecimal)rowValue.get(EAV_BE_STRING_SET_VALUES.BATCH_ID.getName())).longValue());
            baseSet.put(
                    new BaseValue(
                            batch,
                            ((BigDecimal)rowValue.get(EAV_BE_STRING_SET_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) rowValue.get(EAV_BE_STRING_SET_VALUES.REPORT_DATE.getName())),
                            rowValue.get(EAV_BE_STRING_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadBooleanSetValues(IBaseSet baseSet)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_BOOLEAN_SET_VALUES.BATCH_ID,
                        EAV_BE_BOOLEAN_SET_VALUES.INDEX_,
                        EAV_BE_BOOLEAN_SET_VALUES.VALUE,
                        EAV_BE_BOOLEAN_SET_VALUES.REPORT_DATE)
                .from(EAV_BE_BOOLEAN_SET_VALUES)
                .where(EAV_BE_BOOLEAN_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch(((BigDecimal)rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.BATCH_ID.getName())).longValue());
            baseSet.put(
                    new BaseValue(
                            batch,
                            ((BigDecimal)rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.REPORT_DATE.getName())),
                            DataUtils.convert((Byte)rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.VALUE.getName()))));
        }
    }

    private void loadDoubleSetValues(IBaseSet baseSet)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID,
                        EAV_BE_DOUBLE_SET_VALUES.INDEX_,
                        EAV_BE_DOUBLE_SET_VALUES.VALUE,
                        EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE)
                .from(EAV_BE_DOUBLE_SET_VALUES)
                .where(EAV_BE_DOUBLE_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch(((BigDecimal)rowValue.get(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID.getName())).longValue());
            baseSet.put(
                    new BaseValue(
                            batch,
                            ((BigDecimal)rowValue.get(EAV_BE_DOUBLE_SET_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) rowValue.get(EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE.getName())),
                            ((BigDecimal)rowValue.get(EAV_BE_DOUBLE_SET_VALUES.VALUE.getName())).doubleValue()));
        }
    }

    private void loadComplexSetValues(IBaseSet baseSet)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (!metaType.isComplex())
        {
            throw new RuntimeException("Load the complex set values is not possible. " +
                    "Complex values ??can not be added to an set of simple values.");
        }

        if (metaType.isSet())
        {
            loadSetOfComplexSets(baseSet);
        }
        else
        {
            IMetaClass metaClass = (IMetaClass)metaType;

            SelectForUpdateStep select = context
                    .select(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID,
                            EAV_BE_COMPLEX_SET_VALUES.BATCH_ID,
                            EAV_BE_COMPLEX_SET_VALUES.INDEX_,
                            EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE,
                            EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED,
                            EAV_BE_COMPLEX_SET_VALUES.IS_LAST)
                    .from(EAV_BE_COMPLEX_SET_VALUES)
                    .where(EAV_BE_COMPLEX_SET_VALUES.SET_ID.equal(baseSet.getId()));

            logger.debug(select.toString());
            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

            Iterator<Map<String, Object>> it = rows.iterator();
            while (it.hasNext())
            {
                Map<String, Object> row = it.next();

                long batchId = ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.BATCH_ID.getName())).longValue();
                long index = ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.INDEX_.getName())).longValue();
                long entityValueId = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.getName())).longValue();
                boolean isClosed = ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED.getName())).longValue() == 1;
                boolean isLast = ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.IS_LAST.getName())).longValue() == 1;
                Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE.getName()));

                Batch batch = batchRepository.getBatch(batchId);
                IBaseEntity baseEntity = load(entityValueId, metaClass.isReference());

                baseSet.put(new BaseValue(batch, index, reportDate, baseEntity, isClosed, isLast));
            }
        }
    }

    private void removeReportDates(IBaseEntity baseEntity) {
        DeleteConditionStep delete = context
                .delete(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntity.getId()));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues());
    }

    private Set<BaseEntity> collectComplexSetValues(BaseSet baseSet)
    {
        Set<BaseEntity> entities = new HashSet<BaseEntity>();

        IMetaType metaType = baseSet.getMemberType();
        if (metaType.isSetOfSets())
        {
            Collection<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext())
            {
                IBaseValue baseValue = it.next();
                entities.addAll(collectComplexSetValues((BaseSet)baseValue.getValue()));
            }
        } else {
            Collection<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext())
            {
                IBaseValue baseValue = it.next();
                entities.add((BaseEntity)baseValue.getValue());
            }
        }

        return entities;
    }

    public List<Long> getEntityIDsByMetaclass(long metaClassId) {
        ArrayList<Long> entityIds = new ArrayList<Long>();

        Select select = context
                .select(EAV_BE_ENTITIES.ID)
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> i = rows.iterator();
        while(i.hasNext())
        {
            Map<String, Object> row = i.next();

            entityIds.add(((BigDecimal)row.get(EAV_BE_ENTITIES.ID.getName())).longValue());
        }

        return entityIds;
    }

//    public List<RefListItem> getRefsByMetaclass(long metaClassId) {
//        ArrayList<RefListItem> entityIds = new ArrayList<RefListItem>();
//
//        Select select = context
//                .select(EAV_BE_ENTITIES.ID,
//                        EAV_BE_STRING_VALUES.as("name_value").VALUE.as("value"),
//                        EAV_BE_STRING_VALUES.as("code_value").VALUE.as("code"))
//                .from(EAV_BE_ENTITIES,
//                        EAV_BE_STRING_VALUES.as("name_value"),
//                        EAV_M_SIMPLE_ATTRIBUTES.as("name_attr"),
//                        EAV_BE_STRING_VALUES.as("code_value"),
//                        EAV_M_SIMPLE_ATTRIBUTES.as("code_attr"))
//                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId))
//
//                .and(EAV_BE_ENTITIES.ID.equal(EAV_BE_STRING_VALUES.as("name_value").ENTITY_ID))
//                .and(EAV_M_SIMPLE_ATTRIBUTES.as("name_attr").ID.equal(EAV_BE_STRING_VALUES.as("name_value").ATTRIBUTE_ID))
//                .and(EAV_M_SIMPLE_ATTRIBUTES.as("name_attr").NAME.equal("name_ru"))
//
//                .and(EAV_BE_ENTITIES.ID.equal(EAV_BE_STRING_VALUES.as("code_value").ENTITY_ID))
//                .and(EAV_M_SIMPLE_ATTRIBUTES.as("code_attr").ID.equal(EAV_BE_STRING_VALUES.as("code_value").ATTRIBUTE_ID))
//                .and(EAV_M_SIMPLE_ATTRIBUTES.as("code_attr").NAME.equal("code"));
//
//        logger.debug(select.toString());
//        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
//
//        Iterator<Map<String, Object>> i = rows.iterator();
//        while(i.hasNext())
//        {
//            Map<String, Object> row = i.next();
//
//            RefListItem rli = new RefListItem();
//
//            rli.setId(((BigDecimal)row.get(EAV_BE_ENTITIES.ID.getName())).longValue());
//            rli.setTitle((String)row.get("value"));
//            rli.setCode((String)row.get("code"));
//
//            entityIds.add(rli);
//        }
//
//        return entityIds;
//    }

    public List<RefListItem> getRefsByMetaclass(long metaClassId) {
        ArrayList<RefListItem> entityIds = new ArrayList<RefListItem>();

        Select select = context.select().from(
                context.select(
                        EAV_BE_ENTITIES.ID,
                        EAV_M_CLASSES.NAME.as("classes_name"),
                        EAV_M_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_STRING_VALUES.VALUE,
                        DSL.val("string", String.class).as("type"))
                .from(EAV_BE_ENTITIES, EAV_M_CLASSES, EAV_M_SIMPLE_ATTRIBUTES, EAV_BE_STRING_VALUES)
                .where(EAV_M_CLASSES.ID.eq(metaClassId))
                .and(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(EAV_M_CLASSES.ID))
                .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(1))
                .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                .and(EAV_BE_STRING_VALUES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                .union(context
                        .select(
                                EAV_BE_ENTITIES.ID,
                                EAV_M_CLASSES.NAME.as("classes_name"),
                                EAV_M_SIMPLE_ATTRIBUTES.NAME,
                                DSL.field(
                                        "TO_CHAR({0})", String.class, EAV_BE_INTEGER_VALUES.VALUE),
                                DSL.val("integer", String.class).as("type"))
                        .from(EAV_BE_ENTITIES, EAV_M_CLASSES, EAV_M_SIMPLE_ATTRIBUTES, EAV_BE_INTEGER_VALUES)
                        .where(EAV_M_CLASSES.ID.eq(metaClassId))
                        .and(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                        .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(EAV_M_CLASSES.ID))
                        .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(1))
                        .and(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                        .and(EAV_BE_INTEGER_VALUES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                    .union(context
                            .select(
                                    EAV_BE_ENTITIES.ID,
                                    EAV_M_CLASSES.NAME.as("classes_name"),
                                    EAV_M_SIMPLE_ATTRIBUTES.NAME,
                                    DSL.field(
                                            "TO_CHAR({0})", String.class, EAV_BE_DATE_VALUES.VALUE),
                                    DSL.val("date", String.class).as("type"))
                            .from(EAV_BE_ENTITIES, EAV_M_CLASSES, EAV_M_SIMPLE_ATTRIBUTES, EAV_BE_DATE_VALUES)
                            .where(EAV_M_CLASSES.ID.eq(metaClassId))
                            .and(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                            .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(EAV_M_CLASSES.ID))
                            .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(1))
                            .and(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                            .and(EAV_BE_DATE_VALUES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                        .union(context
                                .select(
                                        EAV_BE_ENTITIES.ID,
                                        EAV_M_CLASSES.NAME.as("classes_name"),
                                        EAV_M_SIMPLE_ATTRIBUTES.NAME,
                                        DSL.field(
                                                "TO_CHAR({0})", String.class, EAV_BE_BOOLEAN_VALUES.VALUE),
                                        DSL.val("boolean", String.class).as("type"))
                                .from(EAV_BE_ENTITIES, EAV_M_CLASSES, EAV_M_SIMPLE_ATTRIBUTES, EAV_BE_BOOLEAN_VALUES)
                                .where(EAV_M_CLASSES.ID.eq(metaClassId))
                                .and(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                                .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(EAV_M_CLASSES.ID))
                                .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(1))
                                .and(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                                .and(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID)))))).
                orderBy(DSL.field("ID"));

        logger.debug("LIST_BY_CLASS SQL: " + select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> i = rows.iterator();
        while(i.hasNext())
        {
            RefListItem rli = new RefListItem();

            Map<String, Object> row = i.next();
            long id = (Long)row.get("ID");
            long old_id = id;

            logger.debug("#####################");

            rli.setId(id);
            while (old_id == id) {
                if (((String)row.get("NAME")).equals("code")) {
                    rli.setCode((String)row.get("VALUE"));
                } else if (((String)row.get("NAME")).startsWith("name_")) {
                    rli.setTitle((String)row.get("VALUE"));
                }

                for (String key : row.keySet()) {
                    if (key.equals("NAME") || key.startsWith("name_")) {
                        continue;
                    }

                    rli.addValue(key, row.get(key));
                }

                row = i.next();
                old_id = id;
                id = (Long)row.get("ID");
            }

            entityIds.add(rli);
        }

        return entityIds;
    }

    public List<BaseEntity> getEntityByMetaclass(MetaClass meta) {
        List<Long> ids = getEntityIDsByMetaclass(meta.getId());

        ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();

        for (Long id : ids) {
            entities.add((BaseEntity)load(id));
        }

        return entities;
    }

    @Override
    public boolean isApproved(long id) {
        Select select = context
                .select(EAV_A_CREDITOR_STATE.ID)
                .from(EAV_A_CREDITOR_STATE)
                .where(EAV_A_CREDITOR_STATE.CREDITOR_ID.equal(id));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            return true;
        }

        return false;
    }

    @Override
    public int batchCount(long id, String className) {
        /*
        select cu.creditor_id, count(e.id) from eav_be_entities e
left join eav_m_classes c on e.class_id = c.id
left join eav_be_complex_values cval on e.id = cval.entity_value_id
left join eav_batches b on cval.batch_id = b.id
left join creditor_user cu on b.user_id = cu.user_id
where c.name = 'primary_contract' and cu.creditor_id is not null
group by cu.creditor_id;
        */
        Select select = context
                .select(EAV_A_CREDITOR_USER.CREDITOR_ID, EAV_BE_ENTITIES.ID.count().as("cr_count"))
                .from(EAV_BE_ENTITIES)
                .leftOuterJoin(EAV_M_CLASSES).on(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                .leftOuterJoin(EAV_BE_COMPLEX_VALUES).on(EAV_BE_ENTITIES.ID.eq(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID))
                .leftOuterJoin(EAV_BATCHES).on(EAV_BE_COMPLEX_VALUES.BATCH_ID.eq(EAV_BATCHES.ID))
                .leftOuterJoin(EAV_A_CREDITOR_USER).on(EAV_BATCHES.USER_ID.eq(EAV_A_CREDITOR_USER.USER_ID))
                .where(EAV_M_CLASSES.NAME.eq("primary_contract"))
                .and(EAV_A_CREDITOR_USER.CREDITOR_ID.eq(id))
                .groupBy(EAV_A_CREDITOR_USER.CREDITOR_ID);

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0)
        {
            return ((BigDecimal)rows.get(0).get("cr_count")).intValue();
        }

        return 0;
    }
}
