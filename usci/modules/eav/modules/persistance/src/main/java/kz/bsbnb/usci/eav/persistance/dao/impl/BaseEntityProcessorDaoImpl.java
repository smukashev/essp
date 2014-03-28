package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.cr.model.DataTypeUtil;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.*;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.*;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBaseEntityRepository;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
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
public class BaseEntityProcessorDaoImpl extends JDBCSupport implements IBaseEntityProcessorDao
{
    private final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorDaoImpl.class);

    @Autowired
    IBatchRepository batchRepository;
    @Autowired
    IMetaClassRepository metaClassRepository;
    @Autowired
    IBaseEntityRepository baseEntityCacheDao;

    @Autowired
    IPersistableDaoPool persistableDaoPool;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    public IBaseEntity loadByMaxReportDate(long id, Date reportDate, boolean caching)
    {
        Date maxReportDate = getMaxReportDate(id, reportDate);
        if (maxReportDate == null)
        {
            throw new RuntimeException("No data found on report date " + reportDate + ".");
        }
        return load(id, maxReportDate, caching);
    }

    public IBaseEntity loadByMaxReportDate(long id, Date reportDate)
    {
        return loadByMaxReportDate(id, reportDate, false);
    }

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

    @Transactional
    public void applyToDb(IBaseEntityManager baseEntityManager)
    {
        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++)
        {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> insertedObjects = baseEntityManager.getInsertedObjects(objectClass);
            if (insertedObjects != null && insertedObjects.size() != 0)
            {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);
                for (IPersistable insertedObject: insertedObjects)
                {
                    persistableDao.insert(insertedObject);
                }
            }
        }

        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++)
        {
                Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> updatedObjects = baseEntityManager.getUpdatedObjects(objectClass);
            if (updatedObjects != null && updatedObjects.size() != 0)
            {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);
                for (IPersistable updatedObject: updatedObjects)
                {
                    persistableDao.update(updatedObject);
                }
            }
        }

        for (int i = BaseEntityManager.CLASS_PRIORITY.size() - 1; i >= 0; i--)
        {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> deletedObjects = baseEntityManager.getDeletedObjects(objectClass);
            if (deletedObjects != null && deletedObjects.size() != 0)
            {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);
                for (IPersistable deletedObject: deletedObjects)
                {
                    persistableDao.delete(deletedObject);
                }
            }
        }
    }

    private IBaseEntity apply(IBaseEntity baseEntityForSave, IBaseEntityManager baseEntityManager)
    {
        if (baseEntityForSave.getId() < 1 || baseEntityForSave.getMeta().isSearchable() == false)
        {
            return applyBaseEntityBasic(baseEntityForSave, baseEntityManager);
        }
        else
        {
            Date reportDate = baseEntityForSave.getReportDate();
            Date maxReportDate = getMaxReportDate(baseEntityForSave.getId(), reportDate);

            if (maxReportDate == null)
            {
                throw new UnsupportedOperationException("Not yet implemented.");
                //return applyBaseEntityAdvanced(baseEntityForSave, baseEntityManager);
            }
            else
            {
                IBaseEntity baseEntityLoaded = load(baseEntityForSave.getId(), maxReportDate);
                return applyBaseEntityAdvanced(baseEntityForSave, baseEntityLoaded, baseEntityManager);
            }
        }
    }

    private IBaseEntity applyBaseEntityBasic(IBaseEntity baseEntitySaving, IBaseEntityManager baseEntityManager)
    {
        IBaseEntity baseEntityApplied =
                new BaseEntity(
                        baseEntitySaving.getMeta(),
                        baseEntitySaving.getReportDate()
                );
        for (String attribute: baseEntitySaving.getAttributes())
        {
            IBaseValue baseValue = baseEntitySaving.getBaseValue(attribute);
            if (baseValue.getValue() != null)
            {
                applyBaseValueBasic(baseEntityApplied, baseValue, baseEntityManager);
            }
        }

        baseEntityApplied.calculateValueCount();
        baseEntityManager.registerAsInserted(baseEntityApplied);

        IBaseEntityReportDate baseEntityReportDate =
                baseEntityApplied.getBaseEntityReportDate();
        baseEntityManager.registerAsInserted(baseEntityReportDate);

        return baseEntityApplied;
    }

    /*private IBaseEntity applyBaseEntityAdvanced(IBaseEntity baseEntitySaving, IBaseEntityManager baseEntityManager)
    {
        IBaseEntity baseEntityApplied =
                new BaseEntity(
                        baseEntitySaving.getId(),
                        baseEntitySaving.getMeta(),
                        baseEntitySaving.getReportDate()
                );
        for (String attribute: baseEntitySaving.getAttributes())
        {
            IBaseValue baseValue = baseEntitySaving.getBaseValue(attribute);
            applyBaseValueAdvanced(baseEntityApplied, baseValue, baseEntityManager);
        }

        baseEntityApplied.calculateValueCount();
        IBaseEntityReportDate baseEntityReportDate =
                baseEntityApplied.getBaseEntityReportDate();
        baseEntityManager.registerAsInserted(baseEntityReportDate);

        return baseEntityApplied;
    }*/



    private IBaseEntity applyBaseEntityAdvanced(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded,
                                                IBaseEntityManager baseEntityManager)
    {
        IMetaClass metaClass = baseEntitySaving.getMeta();
        IBaseEntity baseEntityApplied = new BaseEntity(baseEntityLoaded, baseEntitySaving.getReportDate());

        for (String attribute: metaClass.getAttributeNames())
        {
            IBaseValue baseValueSaving = baseEntitySaving.getBaseValue(attribute);
            IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);

            // Not present in saving instance of BaseEntity
            if (baseValueSaving == null)
            {
                continue;
            }

            IBaseContainer baseContainer = baseValueSaving.getBaseContainer();
            if (baseContainer == null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY)
            {
                throw new RuntimeException("Advanced applying instance of BaseValue changes " +
                        "contained not in the instance of BaseEntity is not possible.");
            }

            IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
            if (metaAttribute == null)
            {
                throw new RuntimeException("Advanced applying instance of BaseValue changes " +
                        "without meta data is not possible.");
            }

            IMetaType metaType = metaAttribute.getMetaType();
            if (metaType.isComplex())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                if (metaType.isSet())
                {
                    applyComplexSet(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                }
                else
                {
                    applyComplexValue(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                }
            }
            else
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                if (metaType.isSet())
                {
                    applySimpleSet(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                }
                else
                {
                    applySimpleValue(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                }
            }
        }

        // Calculate values count
        baseEntityApplied.calculateValueCount();

        // Register instance of BaseEntityReportDate
        IBaseEntityReportDate baseEntityReportDate =
                baseEntityApplied.getBaseEntityReportDate();

        Date reportDateSaving = baseEntitySaving.getReportDate();
        Date reportDateLoaded = baseEntityLoaded.getReportDate();
        int reportDateCompare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);
        if (reportDateCompare == 0)
        {
            //baseEntityManager.registerAsUpdated(baseEntityReportDate);
        }
        else
        {
            baseEntityManager.registerAsInserted(baseEntityReportDate);
        }

        return baseEntityApplied;
    }

    protected void applyBaseValueBasic(IBaseEntity baseEntityApplied, IBaseValue baseValue, IBaseEntityManager baseEntityManager)
    {
        if (baseValue.getValue() == null)
        {
            throw new RuntimeException("Basic applying instance of BaseValue changes with null value is not possible.");
        }

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        if (baseContainer == null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY)
        {
            throw new RuntimeException("Basic applying instance of BaseValue changes contained not in the instance " +
                    "of BaseEntity is not possible.");
        }

        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        if (metaAttribute == null)
        {
            throw new RuntimeException("Basic applying instance of BaseValue changes without meta data is not possible.");
        }

        IMetaType metaType = metaAttribute.getMetaType();
        if (metaType.isComplex())
        {
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                IMetaSet childMetaSet = (IMetaSet)metaType;
                IBaseSet childBaseSet = (IBaseSet)baseValue.getValue();

                // TODO: Add implementation of immutable complex values in sets

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());
                for (IBaseValue childBaseValue: childBaseSet.get())
                {
                    IBaseEntity childBaseEntity = (IBaseEntity)childBaseValue.getValue();
                    IBaseEntity childBaseEntityApplied = apply(childBaseEntity, baseEntityManager);

                    IBaseValue childBaseValueApplied =
                            BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaSet.getMemberType(),
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

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValue.getBatch(),
                        baseValue.getIndex(),
                        new Date(baseValue.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        true
                );

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            }
            else
            {
                /*boolean last = true;
                if (metaAttribute.isFinal())
                {
                    IBeValueDao valueDao = persistableDaoPool
                            .getPersistableDao(baseValue.getClass(), IBeValueDao.class);
                    IBaseValue lastBaseValue = valueDao.getLastBaseValue(baseValue);
                    if (lastBaseValue != null)
                    {

                    }
                }*/

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

                        IBaseEntity childBaseEntityImmutable = loadByMaxReportDate(childBaseEntity.getId(),                                childBaseEntity.getReportDate(), childMetaClass.isReference());
                        if (childBaseEntityImmutable == null)
                        {
                            throw new RuntimeException(String.format("Instance of BaseEntity with id {0} not found in the DB.",
                                    childBaseEntity.getId()));
                        }

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValue.getBatch(),
                                baseValue.getIndex(),
                                new Date(baseValue.getRepDate().getTime()),
                                childBaseEntityImmutable,
                                false,
                                true
                        );

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
                else
                {
                    IBaseEntity childBaseEntity = (IBaseEntity)baseValue.getValue();
                    IBaseEntity childBaseEntityApplied = apply(childBaseEntity, baseEntityManager);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValue.getBatch(),
                            baseValue.getIndex(),
                            new Date(baseValue.getRepDate().getTime()),
                            childBaseEntityApplied,
                            false,
                            true
                    );

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
        else
        {
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                IMetaSet childMetaSet = (IMetaSet)metaType;
                IBaseSet childBaseSet = (IBaseSet)baseValue.getValue();
                IMetaValue childMetaValue = (IMetaValue)childMetaSet.getMemberType();

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());
                for (IBaseValue childBaseValue: childBaseSet.get())
                {
                    IBaseValue childBaseValueApplied =
                            BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaSet.getMemberType(),
                                    childBaseValue.getBatch(),
                                    childBaseValue.getIndex(),
                                    new Date(baseValue.getRepDate().getTime()),
                                    childMetaValue.getTypeCode() == DataTypes.DATE ?
                                            new Date(((Date)childBaseValue.getValue()).getTime()) :
                                            childBaseValue.getValue(),
                                    false,
                                    true);
                    childBaseSetApplied.put(childBaseValueApplied);
                    baseEntityManager.registerAsInserted(childBaseValueApplied);
                }

                baseEntityManager.registerAsInserted(childBaseSetApplied);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValue.getBatch(),
                        baseValue.getIndex(),
                        new Date(baseValue.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        true
                );

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            }
            else
            {
                IMetaValue metaValue = (IMetaValue)metaType;
                IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValue.getBatch(),
                                baseValue.getIndex(),
                                new Date(baseValue.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)baseValue.getValue()).getTime()) :
                                        baseValue.getValue(),
                                false,
                                true
                        );

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            }
        }
    }

    /*protected void applyBaseValueAdvanced(IBaseEntity baseEntityApplied, IBaseValue baseValue,
                                          IBaseEntityManager baseEntityManager)
    {
        if (baseValue.getValue() == null)
        {
            throw new RuntimeException("Advanced applying instance of BaseValue changes with null value is not possible.");
        }

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        if (baseContainer == null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY)
        {
            throw new RuntimeException("Advanced applying instance of BaseValue changes contained not in the instance " +
                    "of BaseEntity is not possible.");
        }

        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        if (metaAttribute == null)
        {
            throw new RuntimeException("Advanced applying instance of BaseValue changes without meta data is not possible.");
        }

        IBeValueDao valueDao = persistableDaoPool
                .getPersistableDao(baseValue.getClass(), IBeValueDao.class);
        IBaseValue nextBaseValue = valueDao.getNextBaseValue(baseValue);

        IMetaType metaType = metaAttribute.getMetaType();
        if (metaType.isComplex())
        {
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }
            }
            else
            {

            }
        }
        else
        {
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                IMetaSet childMetaSet = (IMetaSet)metaType;
                IMetaValue childMetaValue = (IMetaValue)childMetaSet.getMemberType();
                IBaseSet childBaseSet = (IBaseSet)baseValue.getValue();
                if (nextBaseValue != null)
                {
                    IBaseSet childNextBaseSet = (IBaseSet)nextBaseValue.getValue();
                    if (baseValue.equalsByValue(nextBaseValue))
                    {
                        IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());

                        boolean baseValueFound = false;
                        Set<UUID> processedUuids = new HashSet<UUID>();
                        for (IBaseValue childBaseValue: childBaseSet.get())
                        {
                            baseValueFound = false;
                            for (IBaseValue childNextBaseValue: childNextBaseSet.get())
                            {
                                if (processedUuids.contains(childNextBaseValue.getUuid()))
                                {
                                    continue;
                                }
                                else
                                {
                                    if (childBaseValue.equalsByValue(childMetaValue, childNextBaseValue))
                                    {
                                        // Mark as processed and found
                                        processedUuids.add(childNextBaseValue.getUuid());
                                        baseValueFound = true;

                                        // Set new report date
                                        childNextBaseValue.setRepDate(
                                                new Date(childBaseValue.getRepDate().getTime()));

                                        // Put in the result set and register as updated
                                        childBaseSetApplied.put(childNextBaseValue);
                                        baseEntityManager.registerAsUpdated(childNextBaseValue);

                                        break;
                                    }
                                }
                            }

                            if (baseValueFound)
                            {
                                continue;
                            }

                            IBaseValue childBaseValueApplied =
                                    BaseValueFactory.create(
                                            childMetaSet.getType(),
                                            childMetaSet.getMemberType(),
                                            childBaseValue.getBatch(),
                                            childBaseValue.getIndex(),
                                            new Date(baseValue.getRepDate().getTime()),
                                            childMetaValue.getTypeCode() == DataTypes.DATE ?
                                                    new Date(((Date)childBaseValue.getValue()).getTime()) :
                                                    childBaseValue.getValue(),
                                            false,
                                            false);
                            childBaseSetApplied.put(childBaseValueApplied);
                            baseEntityManager.registerAsInserted(childBaseValueApplied);
                        }

                        baseEntityManager.registerAsInserted(childBaseSetApplied);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValue.getBatch(),
                                baseValue.getIndex(),
                                new Date(baseValue.getRepDate().getTime()),
                                childBaseSetApplied,
                                false,
                                true
                        );

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
                else
                {

                }
            }
            else
            {
                if (nextBaseValue != null)
                {
                    if (baseValue.equalsByValue(nextBaseValue))
                    {
                        nextBaseValue.setRepDate(new Date(baseValue.getRepDate().getTime()));
                        baseEntityApplied.put(metaAttribute.getName(), nextBaseValue);
                        baseEntityManager.registerAsUpdated(nextBaseValue);
                    }
                    else
                    {
                        IMetaValue metaValue = (IMetaValue)metaType;
                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValue.getBatch(),
                                baseValue.getIndex(),
                                new Date(baseValue.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)baseValue.getValue()).getTime()) :
                                        baseValue.getValue(),
                                false,
                                false
                        );

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
                else
                {
                    IMetaValue metaValue = (IMetaValue)metaType;
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValue.getBatch(),
                            baseValue.getIndex(),
                            new Date(baseValue.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValue.getValue()).getTime()) :
                                    baseValue.getValue(),
                            false,
                            true
                    );

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
    }*/

    protected void applySimpleSet(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                      IBaseEntityManager baseEntityManager)
    {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();

        IMetaSet childMetaSet = (IMetaSet)metaType;
        IMetaType childMetaType = childMetaSet.getMemberType();
        IMetaValue childMetaValue = (IMetaValue)childMetaType;

        IBaseSet childBaseSetSaving = (IBaseSet)baseValueSaving.getValue();
        IBaseSet childBaseSetLoaded = null;
        IBaseSet childBaseSetApplied = null;
        if (baseValueLoaded != null)
        {
            childBaseSetLoaded = (IBaseSet)baseValueLoaded.getValue();

            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() <= 0)
            {
                IBaseValue baseValueClosed = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        baseValueLoaded.getBatch(),
                        baseValueLoaded.getIndex(),
                        new Date(baseValueLoaded.getRepDate().getTime()),
                        childBaseSetLoaded,
                        true,
                        baseValueLoaded.isLast());
                baseValueClosed.setBaseContainer(baseEntity);
                baseValueClosed.setMetaAttribute(metaAttribute);
                baseEntityManager.registerAsUpdated(baseValueClosed);
            }
            else
            {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        baseValueLoaded.getBatch(),
                        baseValueLoaded.getIndex(),
                        new Date(baseValueLoaded.getRepDate().getTime()),
                        childBaseSetApplied,
                        baseValueLoaded.isClosed(),
                        baseValueLoaded.isLast());
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
            }
        }
        else
        {
            if (childBaseSetSaving == null)
            {
                return;
            }

            IBeValueDao baseValueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBeValueDao.class);

            IBaseValue baseValueClosed = baseValueDao.getClosedBaseValue(baseValueSaving);
            if (baseValueClosed != null)
            {
                childBaseSetLoaded = (IBaseSet)baseValueClosed.getValue();
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueClosed.getId(),
                        baseValueClosed.getBatch(),
                        baseValueClosed.getIndex(),
                        new Date(baseValueClosed.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        baseValueClosed.isLast());
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsUpdated(baseValueApplied);
            }
            else
            {
                IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueSaving);
                if (baseValuePrevious != null)
                {
                    childBaseSetLoaded = (IBaseSet)baseValuePrevious.getValue();
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            baseValuePrevious.isLast());
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValuePrevious.isLast())
                    {
                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);
                        baseValuePrevious.setBatch(baseValueSaving.getBatch());
                        baseValuePrevious.setIndex(baseValueSaving.getIndex());
                        baseValuePrevious.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                }
                else
                {
                    childBaseSetApplied = new BaseSet(childMetaType);
                    baseEntityManager.registerAsInserted(childBaseSetApplied);

                    // TODO: Check next value

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            true);
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }

        Set<UUID> processedUuids = new HashSet<UUID>();
        if (childBaseSetSaving != null && childBaseSetSaving.getValueCount() > 0)
        {
            boolean baseValueFound;
            for (IBaseValue childBaseValueSaving: childBaseSetSaving.get())
            {

                if (childBaseSetLoaded != null)
                {
                    baseValueFound = false;
                    for (IBaseValue childBaseValueLoaded: childBaseSetLoaded.get())
                    {
                        if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                        {
                            continue;
                        }

                        if (childBaseValueSaving.equalsByValue(childMetaValue, childBaseValueLoaded))
                        {
                            // Mark as processed and found
                            processedUuids.add(childBaseValueLoaded.getUuid());
                            baseValueFound = true;

                            IBaseValue baseValueApplied = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaType,
                                    childBaseValueLoaded.getBatch(),
                                    childBaseValueLoaded.getIndex(),
                                    new Date(childBaseValueLoaded.getRepDate().getTime()),
                                    childMetaValue.getTypeCode() == DataTypes.DATE ?
                                            new Date(((Date)childBaseValueLoaded.getValue()).getTime()) :
                                            childBaseValueLoaded.getValue(),
                                    childBaseValueLoaded.isClosed(),
                                    childBaseValueLoaded.isLast());
                            childBaseSetApplied.put(baseValueApplied);
                            break;
                        }
                    }

                    if (baseValueFound)
                    {
                        continue;
                    }
                }

                IBeSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueSaving.getClass(), IBeSetValueDao.class);

                // Check closed value
                IBaseValue baseValueForSearch = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        childBaseValueSaving.getBatch(),
                        childBaseValueSaving.getIndex(),
                        new Date(childBaseValueSaving.getRepDate().getTime()),
                        childMetaValue.getTypeCode() == DataTypes.DATE ?
                                new Date(((Date)childBaseValueSaving.getValue()).getTime()) :
                                childBaseValueSaving.getValue(),
                        childBaseValueSaving.isClosed(),
                        childBaseValueSaving.isLast());
                baseValueForSearch.setBaseContainer(childBaseSetApplied);

                IBaseValue childBaseValueClosed = setValueDao.getClosedBaseValue(baseValueForSearch);
                if (childBaseValueClosed != null)
                {
                    childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                    baseEntityManager.registerAsDeleted(childBaseValueClosed);

                    IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueClosed);
                    if (childBaseValueClosed.isLast())
                    {
                        childBaseValuePrevious.setIndex(childBaseValueSaving.getIndex());
                        childBaseValuePrevious.setBatch(childBaseValueSaving.getBatch());
                        childBaseValuePrevious.setLast(true);

                        childBaseSetApplied.put(childBaseValuePrevious);
                        baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                    }
                    else
                    {
                        childBaseSetApplied.put(childBaseValuePrevious);
                    }

                    continue;
                }

                // Check next value
                IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueSaving);
                if (childBaseValueNext != null)
                {
                    childBaseValueNext.setBatch(childBaseValueSaving.getBatch());
                    childBaseValueNext.setIndex(childBaseValueSaving.getIndex());
                    childBaseValueNext.setRepDate(new Date(childBaseValueSaving.getRepDate().getTime()));

                    childBaseSetApplied.put(childBaseValueNext);
                    baseEntityManager.registerAsUpdated(childBaseValueNext);
                    continue;
                }


                IBaseValue childBaseValueLast = setValueDao.getLastBaseValue(childBaseValueSaving);
                if (childBaseValueLast != null)
                {
                    childBaseValueLast.setBaseContainer(childBaseSetApplied);
                    childBaseValueLast.setBatch(childBaseValueSaving.getBatch());
                    childBaseValueLast.setIndex(childBaseValueSaving.getIndex());
                    childBaseValueLast.setLast(false);

                    baseEntityManager.registerAsUpdated(childBaseValueLast);
                }

                IBaseValue childBaseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        childBaseValueSaving.getBatch(),
                        childBaseValueSaving.getIndex(),
                        childBaseValueSaving.getRepDate(),
                        childMetaValue.getTypeCode() == DataTypes.DATE ?
                                new Date(((Date)childBaseValueSaving.getValue()).getTime()) :
                                childBaseValueSaving.getValue(),
                        false,
                        true
                );
                childBaseSetApplied.put(childBaseValueApplied);
                baseEntityManager.registerAsInserted(childBaseValueApplied);
            }
        }

        if (childBaseSetLoaded != null)
        {
            for (IBaseValue childBaseValueLoaded: childBaseSetLoaded.get())
            {
                if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                {
                    continue;
                }

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = childBaseValueLoaded.getRepDate();
                boolean reportDateEquals = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;

                IBeSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueLoaded.getClass(), IBeSetValueDao.class);

                if (reportDateEquals)
                {
                    baseEntityManager.registerAsDeleted(childBaseValueLoaded);
                    boolean last = childBaseValueLoaded.isLast();

                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                    if (childBaseValueNext != null && childBaseValueNext.isClosed())
                    {
                        baseEntityManager.registerAsDeleted(childBaseValueNext);

                        last = childBaseValueNext.isLast();
                    }

                    if (last) {
                        IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueLoaded);
                        if (childBaseValuePrevious != null)
                        {
                            childBaseValuePrevious.setBaseContainer(childBaseSetApplied);
                            childBaseValuePrevious.setBatch(baseValueSaving.getBatch());
                            childBaseValuePrevious.setIndex(baseValueSaving.getIndex());
                            childBaseValuePrevious.setLast(true);
                            baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                        }
                    }
                }
                else
                {
                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                    if (childBaseValueNext == null || !childBaseValueNext.isClosed())
                    {
                        IBaseValue childBaseValue = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                baseValueSaving.getRepDate(),
                                childMetaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)childBaseValueLoaded.getValue()).getTime()) :
                                        childBaseValueLoaded.getValue(),
                                true,
                                childBaseValueLoaded.isLast()
                        );
                        childBaseValue.setBaseContainer(childBaseSetApplied);
                        baseEntityManager.registerAsInserted(childBaseValue);

                        if (childBaseValueLoaded.isLast())
                        {
                            IBaseValue childBaseValueLast = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaType,
                                    childBaseValueLoaded.getId(),
                                    baseValueSaving.getBatch(),
                                    baseValueSaving.getIndex(),
                                    childBaseValueLoaded.getRepDate(),
                                    childMetaValue.getTypeCode() == DataTypes.DATE ?
                                            new Date(((Date)childBaseValueLoaded.getValue()).getTime()) :
                                            childBaseValueLoaded.getValue(),
                                    childBaseValueLoaded.isClosed(),
                                    false
                            );
                            childBaseValueLast.setBaseContainer(childBaseSetApplied);
                            baseEntityManager.registerAsUpdated(childBaseValueLast);
                        }
                    }
                    else
                    {
                        childBaseValueNext.setBaseContainer(childBaseSetApplied);
                        childBaseValueNext.setBatch(baseValueSaving.getBatch());
                        childBaseValueNext.setIndex(baseValueSaving.getIndex());
                        childBaseValueNext.setRepDate(baseValueSaving.getRepDate());

                        baseEntityManager.registerAsUpdated(childBaseValueNext);
                    }
                }
            }
        }
    }

    protected void applyComplexSet(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                  IBaseEntityManager baseEntityManager)
    {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();

        if (baseValueLoaded != null)
        {
            baseEntity.put(metaAttribute.getName(), baseValueLoaded);
        }
    }

    protected void applySimpleValue(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                     IBaseEntityManager baseEntityManager)
    {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaValue metaValue = (IMetaValue)metaType;
        if (baseValueLoaded != null)
        {
            if (baseValueSaving.getValue() == null)
            {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals =
                        DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                if (reportDateEquals)
                {
                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                    baseValueLoaded.getValue(),
                            true,
                            baseValueLoaded.isLast()
                    );
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
                else
                {
                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                        baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false
                        );
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                    baseValueLoaded.getValue(),
                            true,
                            baseValueLoaded.isLast()
                    );
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsInserted(baseValueClosed);
                }
                return;
            }

            if (baseValueSaving.equalsByValue(baseValueLoaded))
            {
                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        baseValueLoaded.getBatch(),
                        baseValueLoaded.getIndex(),
                        new Date(baseValueLoaded.getRepDate().getTime()),
                        metaValue.getTypeCode() == DataTypes.DATE ?
                                new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                baseValueLoaded.getValue(),
                        baseValueLoaded.isClosed(),
                        baseValueLoaded.isLast()
                );
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
            }
            else
            {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals =
                        DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                if (reportDateEquals)
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast()
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
                else
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            baseValueLoaded.isLast()
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValuePrevious = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                        baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false);
                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                }
            }
        }
        else
        {
            if (baseValueSaving.getValue() == null)
            {
                return;
            }

            IBeValueDao valueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBeValueDao.class);
            IBaseValue baseValueClosed = valueDao.getClosedBaseValue(baseValueSaving);
            baseValueClosed.setMetaAttribute(metaAttribute);

            if (baseValueClosed != null)
            {
                if (baseValueClosed.equalsByValue(baseValueSaving))
                {
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseEntityManager.registerAsDeleted(baseValueClosed);

                    IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueClosed);
                    if (baseValueClosed.isLast())
                    {
                        baseValuePrevious.setIndex(baseValueSaving.getIndex());
                        baseValuePrevious.setBatch(baseValueSaving.getBatch());
                        baseValuePrevious.setLast(true);

                        baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                    else
                    {
                        baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                    }
                }
                else
                {
                    baseValueClosed.setIndex(baseValueSaving.getIndex());
                    baseValueClosed.setBatch(baseValueSaving.getBatch());
                    baseValueClosed.setValue(metaValue.getTypeCode() == DataTypes.DATE ?
                            new Date(((Date) baseValueSaving.getValue()).getTime()) :
                            baseValueSaving.getValue());
                    baseValueClosed.setClosed(false);
                    baseEntity.put(metaAttribute.getName(), baseValueClosed);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
            }
            else
            {
                IBaseValue baseValueLast = valueDao.getLastBaseValue(baseValueSaving);
                if (baseValueLast == null)
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            true
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
                else
                {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();
                    int reportDateCompare =
                            DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLast);
                    boolean last = reportDateCompare == -1 ? false : true;

                    if (last)
                    {
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            last
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
    }

    protected void applyComplexValue(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                      IBaseEntityManager baseEntityManager)
    {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaClass metaClass = (IMetaClass)metaType;
        if (baseValueLoaded != null)
        {
            if (baseValueSaving.getValue() == null)
            {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals =
                        DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                if (reportDateEquals)
                {
                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            baseValueLoaded.getValue(),
                            true,
                            baseValueLoaded.isLast()
                    );
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
                else
                {
                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false
                        );
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseValueLoaded.getValue(),
                            true,
                            baseValueLoaded.isLast()
                    );
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsInserted(baseValueClosed);
                }
                return;
            }

            IBaseEntity baseEntitySaving = (IBaseEntity)baseValueSaving.getValue();
            IBaseEntity baseEntityLoaded = (IBaseEntity)baseValueLoaded.getValue();

            if (baseValueSaving.equalsByValue(baseValueLoaded) || !metaClass.isSearchable())
            {
                IBaseEntity baseEntityApplied;
                if (metaAttribute.isImmutable())
                {
                    if (baseEntitySaving.getId() < 1)
                    {
                        throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                    }
                    baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(), baseEntitySaving.getReportDate());
                }
                else
                {
                    baseEntityApplied = metaClass.isSearchable() ?
                            apply(baseEntitySaving, baseEntityManager) :
                            applyBaseEntityAdvanced(baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        baseValueLoaded.getBatch(),
                        baseValueLoaded.getIndex(),
                        new Date(baseValueLoaded.getRepDate().getTime()),
                        baseEntityApplied,
                        baseValueLoaded.isClosed(),
                        baseValueLoaded.isLast()
                );
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
            }
            else
            {

                IBaseEntity baseEntityApplied;
                if (metaAttribute.isImmutable())
                {
                    if (baseEntitySaving.getId() < 1)
                    {
                        throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                    }
                    baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(), baseEntitySaving.getReportDate());
                }
                else
                {
                    baseEntityApplied = metaClass.isSearchable() ?
                            apply(baseEntitySaving, baseEntityManager) :
                            applyBaseEntityAdvanced(baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals =
                        DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                if (reportDateEquals)
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            baseEntityApplied,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast()
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
                else
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            baseValueLoaded.isLast()
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValuePrevious = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false);
                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                }
            }
        }
        else
        {
            IBaseEntity baseEntitySaving = (IBaseEntity)baseValueSaving.getValue();
            if (baseEntitySaving == null)
            {
                return;
            }

            IBeValueDao valueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBeValueDao.class);
            IBaseValue baseValueClosed = valueDao.getClosedBaseValue(baseValueSaving);
            baseValueClosed.setMetaAttribute(metaAttribute);

            if (baseValueClosed != null)
            {
                IBaseEntity baseEntityClosed = (IBaseEntity)baseValueClosed.getValue();
                if (baseValueClosed.equalsByValue(baseValueSaving))
                {
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseEntityManager.registerAsDeleted(baseValueClosed);

                    IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueClosed);

                    IBaseEntity baseEntityApplied;
                    if (metaAttribute.isImmutable())
                    {
                        if (baseEntitySaving.getId() < 1)
                        {
                            throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                    baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                        }
                        baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(),
                                baseEntitySaving.getReportDate());
                    }
                    else
                    {
                        baseEntityApplied = metaClass.isSearchable() ?
                                apply(baseEntitySaving, baseEntityManager) :
                                applyBaseEntityAdvanced(baseEntitySaving, baseEntityClosed, baseEntityManager);
                    }
                    baseValuePrevious.setValue(baseEntityApplied);

                    if (baseValueClosed.isLast())
                    {
                        baseValuePrevious.setIndex(baseValueSaving.getIndex());
                        baseValuePrevious.setBatch(baseValueSaving.getBatch());
                        baseValuePrevious.setLast(true);

                        baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                    else
                    {
                        baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                    }
                }
                else
                {
                    IBaseEntity baseEntityApplied;
                    if (metaAttribute.isImmutable())
                    {
                        if (baseEntitySaving.getId() < 1)
                        {
                            throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                    baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                        }
                        baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(),
                                baseEntitySaving.getReportDate());
                    }
                    else
                    {
                        baseEntityApplied = metaClass.isSearchable() ?
                                apply(baseEntitySaving, baseEntityManager) :
                                applyBaseEntityAdvanced(baseEntitySaving, baseEntityClosed, baseEntityManager);
                    }

                    baseValueClosed.setIndex(baseValueSaving.getIndex());
                    baseValueClosed.setBatch(baseValueSaving.getBatch());
                    baseValueClosed.setValue(baseEntityApplied);
                    baseValueClosed.setClosed(false);
                    baseEntity.put(metaAttribute.getName(), baseValueClosed);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
            }
            else
            {
                IBaseValue baseValueLast = valueDao.getLastBaseValue(baseValueSaving);
                IBaseEntity baseEntityApplied;
                if (metaAttribute.isImmutable())
                {
                    if (baseEntitySaving.getId() < 1)
                    {
                        throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                    }
                    baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(),
                            baseEntitySaving.getReportDate());
                }
                else
                {
                    baseEntityApplied = apply(baseEntitySaving, baseEntityManager);
                }

                if (baseValueLast == null)
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            true
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
                else
                {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();
                    int reportDateCompare =
                            DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLast);
                    boolean last = reportDateCompare == -1 ? false : true;

                    if (last)
                    {
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            last
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
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

    private void loadIntegerValues(IBaseEntity baseEntity, boolean lastReportDate)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_INTEGER_VALUES.as("v");
        Select select = null;

        if (lastReportDate)
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
                            .orderBy(tableOfValues.field(EAV_BE_INTEGER_VALUES.REPORT_DATE).desc()).as("num_pp"),
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

            long id = ((BigDecimal) row.get(EAV_BE_INTEGER_VALUES.ID.getName())).longValue();
            long index = ((BigDecimal) row.get(EAV_BE_INTEGER_VALUES.INDEX_.getName())).longValue();
            boolean closed = ((BigDecimal)row.get(EAV_BE_INTEGER_VALUES.IS_CLOSED.getName())).longValue() == 1;
            boolean last = ((BigDecimal)row.get(EAV_BE_INTEGER_VALUES.IS_LAST.getName())).longValue() == 1;
            int value = ((BigDecimal)row.get(EAV_BE_INTEGER_VALUES.VALUE.getName())).intValue();
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_INTEGER_VALUES.REPORT_DATE.getName()));
            String attribute = (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_INTEGER_VALUES.BATCH_ID.getName())).longValue());

            IMetaType metaType = baseEntity.getMemberType(attribute);
            baseEntity.put(
                    attribute,
                    BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS, metaType, id, batch, index, reportDate, value, closed, last));
        }
    }

    private void loadDateValues(IBaseEntity baseEntity, boolean lastReportDate)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_DATE_VALUES.as("v");
        Select select = null;

        if (lastReportDate)
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
                            .orderBy(tableOfValues.field(EAV_BE_DATE_VALUES.REPORT_DATE).desc()).as("num_pp"),
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

            long id = ((BigDecimal) row.get(EAV_BE_DATE_VALUES.ID.getName())).longValue();
            long index = ((BigDecimal) row.get(EAV_BE_DATE_VALUES.INDEX_.getName())).longValue();
            boolean closed = ((BigDecimal)row.get(EAV_BE_DATE_VALUES.IS_CLOSED.getName())).longValue() == 1;
            boolean last = ((BigDecimal)row.get(EAV_BE_DATE_VALUES.IS_LAST.getName())).longValue() == 1;
            Date value = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_DATE_VALUES.VALUE.getName()));
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_DATE_VALUES.REPORT_DATE.getName()));
            String attribute = (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_DATE_VALUES.BATCH_ID.getName())).longValue());

            IMetaType metaType = baseEntity.getMemberType(attribute);
            baseEntity.put(
                    attribute,
                    BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS, metaType, id, batch, index, reportDate, value, closed, last));
        }
    }

    private void loadBooleanValues(IBaseEntity baseEntity, boolean lastReportDate)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_BOOLEAN_VALUES.as("v");
        Select select = null;

        if (lastReportDate)
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
                            .orderBy(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE).desc()).as("num_pp"),
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

            long id = ((BigDecimal) row.get(EAV_BE_BOOLEAN_VALUES.ID.getName())).longValue();
            long index = ((BigDecimal) row.get(EAV_BE_BOOLEAN_VALUES.INDEX_.getName())).longValue();
            boolean closed = ((BigDecimal)row.get(EAV_BE_BOOLEAN_VALUES.IS_CLOSED.getName())).longValue() == 1;
            boolean last = ((BigDecimal)row.get(EAV_BE_BOOLEAN_VALUES.IS_LAST.getName())).longValue() == 1;
            boolean value = ((BigDecimal)row.get(EAV_BE_BOOLEAN_VALUES.VALUE.getName())).longValue() == 1;
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_BOOLEAN_VALUES.REPORT_DATE.getName()));
            String attribute = (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_BOOLEAN_VALUES.BATCH_ID.getName())).longValue());

            IMetaType metaType = baseEntity.getMemberType(attribute);
            baseEntity.put(
                    attribute,
                    BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS, metaType, id, batch, index, reportDate, value, closed, last));
        }
    }

    private void loadStringValues(IBaseEntity baseEntity, boolean lastReportDate)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_STRING_VALUES.as("v");
        Select select = null;

        if (lastReportDate)
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
                            .orderBy(tableOfValues.field(EAV_BE_STRING_VALUES.REPORT_DATE).desc()).as("num_pp"),
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

            long id = ((BigDecimal) row.get(EAV_BE_STRING_VALUES.ID.getName())).longValue();
            long index = ((BigDecimal) row.get(EAV_BE_STRING_VALUES.INDEX_.getName())).longValue();
            boolean closed = ((BigDecimal)row.get(EAV_BE_STRING_VALUES.IS_CLOSED.getName())).longValue() == 1;
            boolean last = ((BigDecimal)row.get(EAV_BE_STRING_VALUES.IS_LAST.getName())).longValue() == 1;
            String value = (String)row.get(EAV_BE_STRING_VALUES.VALUE.getName());
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_STRING_VALUES.REPORT_DATE.getName()));
            String attribute = (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_STRING_VALUES.BATCH_ID.getName())).longValue());

            IMetaType metaType = baseEntity.getMemberType(attribute);
            baseEntity.put(
                    attribute,
                    BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS, metaType, id, batch, index, reportDate, value, closed, last));
        }
    }

    private void loadDoubleValues(IBaseEntity baseEntity, boolean lastReportDate)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_DOUBLE_VALUES.as("v");
        Select select = null;

        if (lastReportDate)
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
                            .orderBy(tableOfValues.field(EAV_BE_DOUBLE_VALUES.REPORT_DATE).desc()).as("num_pp"),
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

            long id = ((BigDecimal) row.get(EAV_BE_DOUBLE_VALUES.ID.getName())).longValue();
            long index = ((BigDecimal) row.get(EAV_BE_DOUBLE_VALUES.INDEX_.getName())).longValue();
            boolean closed = ((BigDecimal)row.get(EAV_BE_DOUBLE_VALUES.IS_CLOSED.getName())).longValue() == 1;
            boolean last = ((BigDecimal)row.get(EAV_BE_DOUBLE_VALUES.IS_LAST.getName())).longValue() == 1;
            double value = ((BigDecimal)row.get(EAV_BE_DOUBLE_VALUES.VALUE.getName())).doubleValue();
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_DOUBLE_VALUES.REPORT_DATE.getName()));
            String attribute = (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_DOUBLE_VALUES.BATCH_ID.getName())).longValue());

            IMetaType metaType = baseEntity.getMemberType(attribute);
            baseEntity.put(
                    attribute,
                    BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS, metaType, id, batch, index, reportDate, value, closed, last));
        }
    }

    private void loadComplexValues(IBaseEntity baseEntity, boolean lastReportDate)
    {
        IMetaClass metaClass = baseEntity.getMeta();

        Table tableOfAttributes = EAV_M_COMPLEX_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_COMPLEX_VALUES.as("v");
        Select select = null;

        if (lastReportDate)
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
                            .orderBy(tableOfValues.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE).desc()).as("num_pp"),
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
            IMetaType metaType = metaClass.getMemberType(attribute);
            IBaseEntity childBaseEntity = loadByMaxReportDate(entityValueId, baseEntity.getReportDate(), metaType.isReference());

            baseEntity.put(attribute,
                    BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS, metaType, id, batch, index, reportDate, childBaseEntity, isClosed, isLast));
        }
    }

    private void loadEntitySimpleSets(IBaseEntity baseEntity, boolean lastReportDate)
    {
        Table tableOfSimpleSets = EAV_M_SIMPLE_SET.as("ss");
        Table tableOfEntitySimpleSets = EAV_BE_ENTITY_SIMPLE_SETS.as("ess");
        Select select = null;

        if (lastReportDate)
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
                loadComplexSetValues(baseSet, baseEntity.getReportDate(), lastReportDate);
            }
            else
            {
                loadSimpleSetValues(baseSet, baseEntity.getReportDate(), lastReportDate);
            }

            Batch batch = batchRepository.getBatch(batchId);
            baseEntity.put(attribute, BaseValueFactory.create(MetaContainerTypes.META_CLASS, metaType,
                    baseValueId, batch, index, reportDate, baseSet));
        }
    }

    private void loadEntityComplexSets(IBaseEntity baseEntity, boolean lastReportDate)
    {
        Table tableOfComplexSets = EAV_M_COMPLEX_SET.as("cs");
        Table tableOfEntityComplexSets = EAV_BE_ENTITY_COMPLEX_SETS.as("ecs");

        Select select;
        if (lastReportDate)
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
                loadComplexSetValues(baseSet, baseEntity.getReportDate(), lastReportDate);
            }
            else
            {
                loadSimpleSetValues(baseSet, baseEntity.getReportDate(), lastReportDate);
            }

            Batch batch = batchRepository.getBatch(batchId);
            baseEntity.put(attribute, BaseValueFactory.create(MetaContainerTypes.META_CLASS, metaType,
                    baseValueId, batch, index, reportDate, baseSet));
        }
    }

    private void loadSetOfSimpleSets(IBaseSet baseSet, Date baseEntityReportDate, boolean lastReportDate)
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
                loadComplexSetValues(baseSetChild, baseEntityReportDate, lastReportDate);
            }
            else
            {
                loadSimpleSetValues(baseSetChild, baseEntityReportDate, lastReportDate);
            }

            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_SET_OF_SIMPLE_SETS.BATCH_ID.getName())).longValue());
            baseSet.put(BaseValueFactory.create(MetaContainerTypes.META_SET, metaType,
                    batch, ((BigDecimal)row.get(EAV_BE_SET_OF_SIMPLE_SETS.INDEX_.getName())).longValue(),
                    DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_SET_OF_SIMPLE_SETS.REPORT_DATE.getName())), baseSetChild));
        }
    }

    private void loadSetOfComplexSets(IBaseSet baseSet, Date baseEntityReportDate, boolean lastReportDate)
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
                loadComplexSetValues(baseSetChild, baseEntityReportDate, lastReportDate);
            }
            else
            {
                loadSimpleSetValues(baseSetChild, baseEntityReportDate, lastReportDate);
            }


            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_SET_OF_COMPLEX_SETS.BATCH_ID.getName())).longValue());
            baseSet.put(BaseValueFactory.create(MetaContainerTypes.META_SET, metaType,
                    batch, ((BigDecimal)row.get(EAV_BE_SET_OF_COMPLEX_SETS.INDEX_.getName())).longValue(),
                    DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_SET_OF_COMPLEX_SETS.REPORT_DATE.getName())), baseSetChild));
        }
    }

    public void loadSimpleSetValues(IBaseSet baseSet, Date baseEntityReportDate, boolean lastReportDate)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (metaType.isComplex())
            throw new RuntimeException("Load the simple set values is not possible. " +
                    "Simple values ??can not be added to an set of complex values.");

        if (metaType.isSet())
        {
            loadSetOfSimpleSets(baseSet, baseEntityReportDate, lastReportDate);
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
                    loadStringSetValues(baseSet, baseEntityReportDate, lastReportDate);
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
                    BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            baseSet.getMemberType(),
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
                    BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            baseSet.getMemberType(),
                            batch,
                            ((BigDecimal)rowValue.get(EAV_BE_DATE_SET_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) rowValue.get(EAV_BE_DATE_SET_VALUES.REPORT_DATE.getName())),
                            rowValue.get(EAV_BE_DATE_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadStringSetValues(IBaseSet baseSet, Date baseEntityReportDate, boolean lastReportDate)
    {
        Table tableOfValues = EAV_BE_STRING_SET_VALUES.as("v");
        Select select;
        if (lastReportDate)
        {
            select = context
                    .select(tableOfValues.field(EAV_BE_STRING_SET_VALUES.ID),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_STRING_SET_VALUES.SET_ID).equal(baseSet.getId()))
                    .and(tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_LAST).equal(true)
                            .and(tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_STRING_SET_VALUES.VALUE))
                            .orderBy(tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE).desc()).as("num_pp"),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.ID),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_STRING_SET_VALUES.SET_ID).eq(baseSet.getId()))
                    .and(tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(baseEntityReportDate)))
                    .asTable("vn");

            select = context
                    .select(tableNumbering.field(EAV_BE_STRING_SET_VALUES.ID),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.BATCH_ID),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.INDEX_),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.VALUE),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                    .from(tableNumbering)
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            long id = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.ID.getName())).longValue();
            long batchId = ((BigDecimal)row.get(EAV_BE_STRING_SET_VALUES.BATCH_ID.getName())).longValue();
            long index = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.INDEX_.getName())).longValue();
            boolean last = ((BigDecimal)row.get(EAV_BE_STRING_SET_VALUES.IS_LAST.getName())).longValue() == 1;
            String value = (String)row.get(EAV_BE_STRING_SET_VALUES.VALUE.getName());
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_STRING_SET_VALUES.REPORT_DATE.getName()));

            Batch batch = batchRepository.getBatch(batchId);
            baseSet.put(
                    BaseValueFactory.create(
                            MetaContainerTypes.META_SET, baseSet.getMemberType(), id, batch, index, reportDate, value, false, last));
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
                    BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            baseSet.getMemberType(),
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
                    BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            baseSet.getMemberType(),
                            batch,
                            ((BigDecimal)rowValue.get(EAV_BE_DOUBLE_SET_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) rowValue.get(EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE.getName())),
                            ((BigDecimal)rowValue.get(EAV_BE_DOUBLE_SET_VALUES.VALUE.getName())).doubleValue()));
        }
    }

    public void loadComplexSetValues(IBaseSet baseSet, Date baseEntityReportDate, boolean lastReportDate)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (!metaType.isComplex())
        {
            throw new RuntimeException("Load the complex set values is not possible. " +
                    "Complex values can not be added to an set of simple values.");
        }

        if (metaType.isSet())
        {
            loadSetOfComplexSets(baseSet, baseEntityReportDate, lastReportDate);
        }
        else
        {
            IMetaClass metaClass = (IMetaClass)metaType;

            Table tableOfValues = EAV_BE_COMPLEX_SET_VALUES.as("v");
            Select select;
            if (lastReportDate)
            {
                select = context
                        .select(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.BATCH_ID),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.INDEX_),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                        .from(tableOfValues)
                        .where(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.SET_ID).equal(baseSet.getId()))
                        .and(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST).equal(true)
                                .and(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED).equal(false)));
            }
            else
            {
                Table tableNumbering = context
                        .select(DSL.rank().over()
                                .partitionBy(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID))
                                .orderBy(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE).desc()).as("num_pp"),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.BATCH_ID),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.INDEX_),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED),
                                tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                        .from(tableOfValues)
                        .where(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.SET_ID).eq(baseSet.getId()))
                        .and(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE)
                                .lessOrEqual(DataUtils.convert(baseEntityReportDate)))
                        .asTable("vn");

                select = context
                        .select(tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                                tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.BATCH_ID),
                                tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.INDEX_),
                                tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                                tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID),
                                tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                        .from(tableNumbering)
                        .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                        .and(tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED).equal(false));
            }

            logger.debug(select.toString());
            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

            Iterator<Map<String, Object>> it = rows.iterator();
            while (it.hasNext())
            {
                Map<String, Object> row = it.next();

                long batchId = ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.BATCH_ID.getName())).longValue();
                long index = ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.INDEX_.getName())).longValue();
                long entityValueId = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.getName())).longValue();
                boolean isLast = ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.IS_LAST.getName())).longValue() == 1;
                Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE.getName()));

                Batch batch = batchRepository.getBatch(batchId);
                IBaseEntity baseEntity = loadByMaxReportDate(entityValueId, baseEntityReportDate, metaClass.isReference());

                baseSet.put(BaseValueFactory.create(MetaContainerTypes.META_SET, baseSet.getMemberType(),
                        batch, index, reportDate, baseEntity, false, isLast));
            }
        }
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
