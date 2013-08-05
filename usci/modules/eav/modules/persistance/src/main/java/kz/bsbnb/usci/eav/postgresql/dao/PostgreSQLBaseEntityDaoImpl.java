package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeComplexValueDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeSetValueDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeSimpleValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.tool.Configuration;
import kz.bsbnb.usci.eav.tool.Constants;
import kz.bsbnb.usci.eav.util.DateUtils;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.jooq.*;
import org.jooq.impl.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static org.jooq.impl.Factory.count;
import static org.jooq.impl.Factory.max;

/**
 * @author a.motov
 */
@Repository
public class PostgreSQLBaseEntityDaoImpl extends JDBCSupport implements IBaseEntityDao
{
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBaseEntityDaoImpl.class);

    @Autowired
    IBatchRepository batchRepository;
    @Autowired
    IMetaClassRepository metaClassRepository;
    @Autowired
    IBeSimpleValueDao beSimpleValueDao;
    @Autowired
    IBeComplexValueDao beComplexValueDao;
    @Autowired
    IBeSetValueDao beSetValueDao;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private Executor sqlGenerator;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    @Override
    public BaseEntity load(long id)
    {
        java.util.Date maxReportDate = getMaxReportDate(id);
        if (maxReportDate != null)
            return load(id, maxReportDate);
        return load(id, new java.util.Date());
    }

    public BaseEntity load(long id, java.util.Date reportDate)
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

        Select select = sqlGenerator
                .select(
                        EAV_BE_ENTITIES.CLASS_ID,
                        sqlGenerator
                                .select(max(EAV_BE_ENTITY_REPORT_DATES.REP_DATE))
                                .from(EAV_BE_ENTITY_REPORT_DATES)
                                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID)).asField("max_report_date"))
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.ID.equal(id));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one BaseEntity found.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalStateException(String.format("BaseEntity with identifier {0} was not found.", id));
        }

        Map<String, Object> row = rows.get(0);
        BaseEntity baseEntity = null;

        if(row != null)
        {
            MetaClass meta = metaClassRepository.getMetaClass((Long)row.get(EAV_BE_ENTITIES.CLASS_ID.getName()));
            Set<java.util.Date> availableReportDates = getAvailableReportDates(id);
            baseEntity = new BaseEntity(id, meta, reportDate, availableReportDates);
        }
        else
        {
            logger.error("Can't load BaseEntity, empty data set.");
        }

        // simple values
        loadIntegerValues(baseEntity);
        loadDateValues(baseEntity);
        loadStringValues(baseEntity);
        loadBooleanValues(baseEntity);
        loadDoubleValues(baseEntity);

        // complex values
        loadComplexValues(baseEntity);

        // entity sets
        loadEntitySimpleSets(baseEntity);
        loadEntityComplexSets(baseEntity);
        loadEntitySetOfSets(baseEntity);

        return baseEntity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseEntity search(BaseEntity baseEntity)
    {
        MetaClass meta = baseEntity.getMeta();
        List<Long> baseEntities = searcherPool.getSearcher(meta.getClassName())
                .findAll(baseEntity);

        if (baseEntities.isEmpty())
        {
            return null;
        }

        return load(baseEntities.get(0));
    }

    public BaseEntity prepare(BaseEntity baseEntity)
    {
        MetaClass metaClass = baseEntity.getMeta();
        if (metaClass.isSearchable())
        {
            /*long baseEntityId = search(baseEntity);
            if (baseEntityId > 1)
            {
                baseEntity.setId(baseEntityId);
            }*/
            // TODO: Change method search()
            BaseEntity baseEntitySearched = search(baseEntity);
            if (baseEntitySearched != null)
            {
                baseEntity.setId(baseEntitySearched.getId());
            }
        }

        for (String attribute: metaClass.getAttributeNames())
        {
            IMetaType metaType = metaClass.getMemberType(attribute);
            if (metaType.isComplex())
            {
                Object value = baseEntity.getBaseValue(attribute).getValue();
                if (value != null)
                {
                    if (metaType.isSet())
                    {
                       BaseSet baseSet = (BaseSet)value;
                       for (IBaseValue baseValue: baseSet.get())
                       {
                           prepare((BaseEntity)baseValue.getValue());
                       }
                    }
                    else
                    {
                        prepare((BaseEntity)value);
                    }
                }
            }
        }

        return baseEntity;
    }

    public BaseEntity apply(final BaseEntity baseEntityForSave)
    {
        if (baseEntityForSave.getId() < 1 || !baseEntityForSave.getMeta().isSearchable())
        {
            for (String attribute: baseEntityForSave.getAttributeNames())
            {
                IMetaType metaType = baseEntityForSave.getMemberType(attribute);
                IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);
                if (metaType.isComplex())
                {
                    if (metaType.isSet())
                    {
                        if (metaType.isSetOfSets())
                        {
                            throw new UnsupportedOperationException("Not implemented yet.");
                        }
                        else
                        {
                            for (IBaseValue baseValue : ((BaseSet)baseValueForSave.getValue()).get())
                            {
                                apply((BaseEntity)baseValue.getValue());
                            }
                        }
                    }
                    else
                    {
                        apply((BaseEntity)baseValueForSave.getValue());
                    }
                }
            }

            return baseEntityForSave;
        }
        else
        {
            BaseEntity baseEntityLoaded = load(baseEntityForSave.getId());

            BasicBaseEntityComparator comparator = new BasicBaseEntityComparator();
            if (!comparator.compare(baseEntityForSave, baseEntityLoaded))
            {
                BaseEntity baseEntitySearched = search(baseEntityForSave);
                if (baseEntitySearched != null && baseEntityLoaded.getId() != baseEntitySearched.getId())
                {
                    throw new RuntimeException("Found a violation of uniqueness. " +
                            "The saving process can not be continued.");
                }
            }

            Set<String> insertedAttributes = SetUtils.difference(baseEntityForSave.getAttributeNames(),
                    baseEntityLoaded.getAttributeNames());
            for (String insertedAttribute: insertedAttributes)
            {
                baseEntityLoaded.put(insertedAttribute, baseEntityForSave.getBaseValue(insertedAttribute));
            }

            for (String attribute: baseEntityLoaded.getAttributeNames())
            {
                IMetaType metaType = baseEntityLoaded.getMemberType(attribute);
                IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);
                IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);

                if (baseValueForSave.getValue() == null)
                {
                    baseEntityLoaded.remove(attribute);
                }
                else
                {
                    if (metaType.isComplex())
                    {
                        if (metaType.isSet())
                        {
                            if (metaType.isSetOfSets())
                            {
                                throw new UnsupportedOperationException("Not implemented yet.");
                            }
                            else
                            {
                                List<Long> forSaveIds = new ArrayList<Long>();
                                BaseSet baseSetForSave = (BaseSet)baseValueForSave.getValue();
                                for (IBaseValue baseValue : baseSetForSave.get())
                                {
                                    BaseEntity baseEntity = (BaseEntity)baseValue.getValue();
                                    forSaveIds.add(baseEntity.getId());
                                }

                                List<Long> loadedIds = new ArrayList<Long>();
                                BaseSet baseSetLoaded = (BaseSet)baseValueLoaded.getValue();
                                for (IBaseValue baseValue : baseSetLoaded.get())
                                {
                                    BaseEntity baseEntity = (BaseEntity)baseValue.getValue();
                                    loadedIds.add(baseEntity.getId());
                                }
                                Collections.sort(loadedIds);
                                Collections.sort(loadedIds);

                                for (IBaseValue baseValue : baseSetForSave.get())
                                {
                                    apply((BaseEntity)baseValue.getValue());
                                }

                                if (!forSaveIds.equals(loadedIds))
                                {
                                    baseEntityLoaded.put(attribute, baseValueForSave);
                                }
                                else
                                {
                                    baseValueLoaded.setValue(baseSetForSave);
                                }
                            }
                        }
                        else
                        {
                            long forSaveId = ((BaseEntity)baseValueForSave.getValue()).getId();
                            long loadedId = ((BaseEntity)baseValueLoaded.getValue()).getId();

                            BaseEntity baseEntityApplied = apply((BaseEntity)baseValueForSave.getValue());

                            if (forSaveId != loadedId)
                            {
                                baseEntityLoaded.put(attribute, baseValueForSave);
                            }
                            else
                            {
                                baseValueLoaded.setValue(baseEntityApplied);
                            }
                        }
                    }
                    else
                    {
                        if (!baseValueForSave.getValue().equals(baseValueLoaded.getValue()))
                        {
                            baseEntityLoaded.put(attribute, baseValueForSave);
                        }
                    }
                }
            }

            return baseEntityLoaded;
        }
    }

    public BaseEntity process(BaseEntity baseEntity)
    {
        //BaseEntity baseEntityPrepared = prepare(baseEntity);
        //BaseEntity baseEntityApplied = apply(baseEntityPrepared);
        //BaseEntity baseEntitySaved = saveOrUpdate(baseEntityApplied);

        return baseEntity;
    }


    @Override
    @Transactional
    public long saveOrUpdate(BaseEntity baseEntity)
    {
        BaseEntity baseEntityLoad = null;
        MetaClass metaClass = baseEntity.getMeta();
        if (metaClass.isSearchable())
        {
            baseEntityLoad = search(baseEntity);
        }

        if (baseEntityLoad == null)
        {
            return save(baseEntity);
        }
        else
        {
            update(baseEntity, baseEntityLoad);
            return baseEntityLoad.getId();
        }
    }

    public void update(BaseEntity baseEntityForSave, BaseEntity baseEntityLoaded)
    {
        if (baseEntityLoaded.getId() < 1)
        {
            throw new IllegalArgumentException("BaseEntity for saveOrUpdate does not contain id.");
        }


        baseEntityForSave.setId(baseEntityLoaded.getId());

        java.util.Date reportDateForSave = baseEntityForSave.getReportDate();
        DateUtils.toBeginningOfTheDay(reportDateForSave);

        java.util.Date reportDateLoaded = baseEntityLoaded.getReportDate();
        DateUtils.toBeginningOfTheDay(reportDateLoaded);

        int reportDateCompare = reportDateLoaded.compareTo(reportDateForSave);

        if (reportDateCompare == 1)
        {
            throw new UnsupportedOperationException("BaseEntity update for previous " +
                    "report date is not implemented.");
        }

        if (reportDateCompare == -1)
        {
            baseEntityLoaded.setReportDate(reportDateForSave);
            insertReportDate(baseEntityLoaded);
        }

        MetaClass meta = baseEntityForSave.getMeta();

        Set<BaseEntity> entitiesForRemove = new HashSet<BaseEntity>();

        Set<String> removeComplexAttributes = new HashSet<String>();
        Set<String> updateComplexAttributes = new HashSet<String>();
        Set<String> insertComplexAttributes = new HashSet<String>();

        Set<String> removeComplexSetAttributes = new HashSet<String>();
        Set<String> updateComplexSetAttributes = new HashSet<String>();
        Set<String> insertComplexSetAttributes = new HashSet<String>();

        Set<String> attributesForSave = baseEntityForSave.getAttributeNames();
        Set<String> attributesLoaded = baseEntityLoaded.getAttributeNames();

        Iterator<String> it = attributesForSave.iterator();
        while (it.hasNext())
        {
            String attribute = it.next();
            IMetaType metaType = meta.getMemberType(attribute);
            IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);

            // SET VALUES
            if (metaType.isSet())
            {
                if (metaType.isComplex())
                {
                    BaseSet comparingBaseSet = (BaseSet)baseValueForSave.getValue();
                    if (comparingBaseSet == null)
                    {
                        removeComplexSetAttributes.add(attribute);
                    }
                    else
                    {
                        if (attributesLoaded.contains(attribute))
                        {
                            MetaSet metaSet = (MetaSet)metaType;
                            MetaClass metaClass = (MetaClass)metaSet.getMemberType();
                            if (metaClass.isSearchable())
                            {
                                IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);
                                BaseSet baseSetLoaded = (BaseSet) baseValueLoaded.getValue();
                                BaseSet baseSetForSave = (BaseSet) baseValueForSave.getValue();
                                if (!baseSetLoaded.equals(baseSetForSave))
                                {
                                    updateComplexSetAttributes.add(attribute);
                                }
                            }
                            else
                            {
                                updateComplexSetAttributes.add(attribute);
                            }
                        }
                        else
                        {
                            baseEntityLoaded.put(attribute, baseValueForSave);
                            insertComplexSetAttributes.add(attribute);
                        }
                    }
                }
                else
                {
                    //TODO: Implement this functionality for simple set values
                }
            }
            else
            {
                // COMPLEX VALUES
                if (metaType.isComplex())
                {
                    BaseEntity comparingBaseEntity = (BaseEntity)baseValueForSave.getValue();
                    if (comparingBaseEntity == null)
                    {
                        removeComplexAttributes.add(attribute);
                    }
                    else
                    {
                        if (attributesLoaded.contains(attribute))
                        {
                            IBaseValue baseValueLoad = baseEntityLoaded.getBaseValue(attribute);
                            BaseEntity anotherBaseEntity = (BaseEntity)baseValueLoad.getValue();

                            BasicBaseEntityComparator comparator = new BasicBaseEntityComparator();
                            if (comparator.compare(comparingBaseEntity, anotherBaseEntity))
                            {
                                baseEntityLoaded.put(attribute, baseValueForSave);
                                updateComplexAttributes.add(attribute);
                            }
                        }
                        else
                        {
                            baseEntityLoaded.put(attribute, baseValueForSave);
                            insertComplexAttributes.add(attribute);
                        }
                    }
                }
                // SIMPLE VALUES
                else
                {
                    beSimpleValueDao.update(baseEntityLoaded, baseEntityForSave, attribute);
                }
            }
        }

        // insert complex values
        if (!insertComplexAttributes.isEmpty())
        {
            beComplexValueDao.save(baseEntityLoaded, insertComplexAttributes);
        }

        // remove complex values
        if (!removeComplexAttributes.isEmpty())
        {
            for (String attribute: removeComplexAttributes)
            {
                beComplexValueDao.remove(baseEntityLoaded, attribute);
                baseEntityLoaded.remove(attribute);
            }
        }

        // insert complex set values
        if (!insertComplexSetAttributes.isEmpty())
        {
            for (String attribute: insertComplexSetAttributes)
            {
                beSetValueDao.save(baseEntityLoaded, attribute);
            }
        }

        // update complex set values
        if (!updateComplexSetAttributes.isEmpty())
        {
            for (String attribute: updateComplexSetAttributes)
            {
                IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);
                IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);

                entitiesForRemove.addAll(
                        collectComplexSetValues((BaseSet)baseValueLoaded.getValue()));

                beSetValueDao.remove(baseEntityLoaded, attribute);
                baseEntityLoaded.put(attribute, baseValueForSave);
                beSetValueDao.save(baseEntityLoaded, attribute);
            }
        }

        // remove complex set values
        if (!removeComplexSetAttributes.isEmpty())
        {
            for(String attribute: removeComplexSetAttributes)
            {
                IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);
                entitiesForRemove.addAll(
                        collectComplexSetValues((BaseSet)baseValueLoaded.getValue()));
                beSetValueDao.remove(baseEntityLoaded, attribute);
            }
        }

        // Remove unused BaseEntities
        for (BaseEntity entityForRemove: entitiesForRemove)
        {
            remove(entityForRemove);
        }
    }

    @Override
    @Transactional
    public long save(BaseEntity baseEntity)
    {
        if (baseEntity.getReportDate() == null)
        {
            throw new IllegalArgumentException("Report date must be set before instance " +
                    "of BaseEntity saving to the DB.");
        }

        if(baseEntity.getMeta() == null)
        {
            throw new IllegalArgumentException("MetaClass must be set before entity insertion to DB.");
        }

        if(baseEntity.getMeta().getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must contain the id before entity insertion to DB.");
        }

        if (baseEntity.getId() < 1)
        {
            long baseEntityId = insertBaseEntity(baseEntity);
            baseEntity.setId(baseEntityId);

            insertReportDate(baseEntity);
        }

        MetaClass meta = baseEntity.getMeta();
        Set<String> attributeNames = baseEntity.getAttributeNames();

        Map<DataTypes, Set<String>> simpleAttributeNames = new HashMap<DataTypes, Set<String>>();
        Set<String> complexAttributeNames = new HashSet<String>();

        Iterator<String> it = attributeNames.iterator();
        while (it.hasNext())
        {
            String attributeName = it.next();

            IMetaType metaType = meta.getMemberType(attributeName);
            if (metaType.isSet())
            {
                beSetValueDao.save(baseEntity, attributeName);
            }
            else
            {
                if (metaType.isComplex())
                {
                    complexAttributeNames.add(attributeName);
                }
                else
                {
                    MetaValue metaValue = (MetaValue)metaType;
                    DataTypes type = metaValue.getTypeCode();
                    SetUtils.putMapValue(simpleAttributeNames, type, attributeName);
                }
            }
        }

        for (DataTypes dataType: DataTypes.values())
        {
            if (simpleAttributeNames.containsKey(dataType))
            {
                beSimpleValueDao.save(baseEntity, simpleAttributeNames.get(dataType), dataType);
            }
        }

        if (!complexAttributeNames.isEmpty())
        {
            beComplexValueDao.save(baseEntity, complexAttributeNames);
        }

        return baseEntity.getId();
    }

    @Override
    public void remove(BaseEntity baseEntity)
    {
        if(baseEntity.getId() < 1)
        {
            throw new IllegalArgumentException("Can't remove BaseEntity without id.");
        }

        boolean used = isUsed(baseEntity.getId());
        if (!used)
        {
            MetaClass metaClass = baseEntity.getMeta();

            Set<String> attributes = baseEntity.getAttributeNames();
            Iterator<String> attributeIt = attributes.iterator();
            while (attributeIt.hasNext())
            {
                String attribute = attributeIt.next();
                IMetaType metaType = metaClass.getMemberType(attribute);
                if (metaType.isSet())
                {
                    IBaseValue baseValue = baseEntity.getBaseValue(attribute);
                    Set<BaseEntity> baseEntitiesForRemove = collectComplexSetValues((BaseSet)baseValue.getValue());

                    beSetValueDao.remove(baseEntity, attribute);

                    Iterator<BaseEntity> baseEntityForRemoveIt = baseEntitiesForRemove.iterator();
                    while (baseEntityForRemoveIt.hasNext())
                    {
                        BaseEntity baseEntityForRemove = baseEntityForRemoveIt.next();
                        remove(baseEntityForRemove);
                    }
                }
                else
                {
                    if (metaType.isComplex())
                    {
                        beComplexValueDao.remove(baseEntity, attribute);
                    }
                    else
                    {
                        beSimpleValueDao.remove(baseEntity, attribute);
                    }
                }
            }

            removeReportDates(baseEntity);

            DeleteConditionStep delete = sqlGenerator
                    .delete(EAV_BE_ENTITIES)
                    .where(EAV_BE_ENTITIES.ID.eq(baseEntity.getId()));

            logger.debug(delete.toString());
            updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
        }
    }

    public boolean isUsed(long baseEntityId)
    {
        Select select;
        List<Map<String, Object>> rows;

        select = sqlGenerator
                .select(count().as("VALUE_COUNT"))
                .from(EAV_BE_COMPLEX_VALUES)
                .where(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.equal(baseEntityId));

        logger.debug(select.toString());
        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        long complexValuesCount = (Long)rows.get(0).get("VALUE_COUNT");

        select = sqlGenerator
                .select(count().as("VALUE_COUNT"))
                .from(EAV_BE_COMPLEX_SET_VALUES)
                .where(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.equal(baseEntityId));

        logger.debug(select.toString());
        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        long complexSetValuesCount = (Long)rows.get(0).get("VALUE_COUNT");

        return complexValuesCount != 0 || complexSetValuesCount != 0;
    }

    public Set<java.util.Date> getAvailableReportDates(long baseEntityId)
    {
        Set<java.util.Date> reportDates = new HashSet<java.util.Date>();

        Select select = sqlGenerator
                .select(EAV_BE_ENTITY_REPORT_DATES.REP_DATE)
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            reportDates.add(DateUtils.convert((Date)row.get(EAV_BE_ENTITY_REPORT_DATES.REP_DATE.getName())));
        }

        return reportDates;
    }

    public java.util.Date getMaxReportDate(long baseEntityId)
    {
        Select select = sqlGenerator
                .select(max(EAV_BE_ENTITY_REPORT_DATES.REP_DATE).as("max_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId))
                .limit(1);

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DateUtils.convert((Date)rows.get(0).get("max_report_date"));
    }

    private void insertReportDate(BaseEntity baseEntity) {
        if (baseEntity.getReportDate() == null)
        {
            throw new IllegalArgumentException("Report date must be set before instance " +
                    "of BaseEntity saving to the DB.");
        }
        InsertOnDuplicateStep insert = sqlGenerator
                .insertInto(
                        EAV_BE_ENTITY_REPORT_DATES,
                        EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID,
                        EAV_BE_ENTITY_REPORT_DATES.REP_DATE)
                .values(baseEntity.getId(), DateUtils.convert(baseEntity.getReportDate()));

        logger.debug(insert.toString());
        updateWithStats(insert.getSQL(), insert.getBindValues().toArray());
    }

    private long insertBaseEntity(BaseEntity baseEntity)
    {
        if(baseEntity.getMeta().getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before entity insertion to DB.");
        }

        InsertOnDuplicateStep insert = sqlGenerator
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

    private void loadIntegerValues(BaseEntity baseEntity)
    {
        Date reportDate = DateUtils.convert(baseEntity.getReportDate());
        Select select = sqlGenerator
                    .select(EAV_BE_INTEGER_VALUES.ID,
                            EAV_BE_INTEGER_VALUES.BATCH_ID,
                            EAV_M_SIMPLE_ATTRIBUTES.NAME,
                            EAV_BE_INTEGER_VALUES.INDEX_,
                            EAV_BE_INTEGER_VALUES.OPEN_DATE,
                            EAV_BE_INTEGER_VALUES.VALUE)
                    .from(EAV_BE_INTEGER_VALUES)
                    .join(EAV_M_SIMPLE_ATTRIBUTES).on(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                    .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.equal(baseEntity.getId()))
                    .and(EAV_BE_INTEGER_VALUES.OPEN_DATE.lessOrEqual(reportDate))
                    .and(Configuration.historyAlgorithm == Constants.HISTORY_ALGORITHM_NOT_FILL ?
                            EAV_BE_INTEGER_VALUES.CLOSE_DATE.greaterThan(reportDate)
                                    .or(EAV_BE_INTEGER_VALUES.CLOSE_DATE.isNull()) :
                            EAV_BE_INTEGER_VALUES.CLOSE_DATE.greaterThan(reportDate));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            (Long) row.get(EAV_BE_INTEGER_VALUES.ID.getName()),
                            batchRepository.getBatch((Long)row.get(EAV_BE_INTEGER_VALUES.BATCH_ID.getName())),
                            (Long) row.get(EAV_BE_INTEGER_VALUES.INDEX_.getName()),
                            (java.sql.Date) row.get(EAV_BE_INTEGER_VALUES.OPEN_DATE.getName()),
                            row.get(EAV_BE_INTEGER_VALUES.VALUE.getName())));
        }
    }

    private void loadDateValues(BaseEntity baseEntity)
    {
        Date reportDate = DateUtils.convert(baseEntity.getReportDate());
        Select select = sqlGenerator
                .select(EAV_BE_DATE_VALUES.ID,
                        EAV_BE_DATE_VALUES.BATCH_ID,
                        EAV_M_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_DATE_VALUES.INDEX_,
                        EAV_BE_DATE_VALUES.OPEN_DATE,
                        EAV_BE_DATE_VALUES.VALUE)
                .from(EAV_BE_DATE_VALUES)
                .join(EAV_M_SIMPLE_ATTRIBUTES).on(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_DATE_VALUES.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_DATE_VALUES.OPEN_DATE.lessOrEqual(reportDate))
                .and(Configuration.historyAlgorithm == Constants.HISTORY_ALGORITHM_NOT_FILL ?
                        EAV_BE_DATE_VALUES.CLOSE_DATE.greaterThan(reportDate)
                                .or(EAV_BE_DATE_VALUES.CLOSE_DATE.isNull()) :
                        EAV_BE_DATE_VALUES.CLOSE_DATE.greaterThan(reportDate));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            (Long) row.get(EAV_BE_DATE_VALUES.ID.getName()),
                            batchRepository.getBatch((Long)row.get(EAV_BE_DATE_VALUES.BATCH_ID.getName())),
                            (Long) row.get(EAV_BE_DATE_VALUES.INDEX_.getName()),
                            (java.sql.Date) row.get(EAV_BE_DATE_VALUES.OPEN_DATE.getName()),
                            DateUtils.convert((java.sql.Date) row.get(EAV_BE_DATE_VALUES.VALUE.getName()))));
        }
    }

    private void loadBooleanValues(BaseEntity baseEntity)
    {
        Date reportDate = DateUtils.convert(baseEntity.getReportDate());
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_BOOLEAN_VALUES.ID,
                        EAV_BE_BOOLEAN_VALUES.BATCH_ID,
                        EAV_M_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_BOOLEAN_VALUES.INDEX_,
                        EAV_BE_BOOLEAN_VALUES.OPEN_DATE,
                        EAV_BE_BOOLEAN_VALUES.VALUE)
                .from(EAV_BE_BOOLEAN_VALUES)
                .join(EAV_M_SIMPLE_ATTRIBUTES).on(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.OPEN_DATE.lessOrEqual(reportDate))
                .and(Configuration.historyAlgorithm == Constants.HISTORY_ALGORITHM_NOT_FILL ?
                        EAV_BE_BOOLEAN_VALUES.CLOSE_DATE.greaterThan(reportDate)
                                .or(EAV_BE_BOOLEAN_VALUES.CLOSE_DATE.isNull()) :
                        EAV_BE_BOOLEAN_VALUES.CLOSE_DATE.greaterThan(reportDate));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            (Long) row.get(EAV_BE_BOOLEAN_VALUES.ID.getName()),
                            batchRepository.getBatch((Long)row.get(EAV_BE_BOOLEAN_VALUES.BATCH_ID.getName())),
                            (Long) row.get(EAV_BE_BOOLEAN_VALUES.INDEX_.getName()),
                            (java.sql.Date) row.get(EAV_BE_BOOLEAN_VALUES.OPEN_DATE.getName()),
                            row.get(EAV_BE_BOOLEAN_VALUES.VALUE.getName())));
        }
    }

    private void loadStringValues(BaseEntity baseEntity)
    {
        Date reportDate = DateUtils.convert(baseEntity.getReportDate());
        Select select = sqlGenerator
                .select(EAV_BE_STRING_VALUES.ID,
                        EAV_BE_STRING_VALUES.BATCH_ID,
                        EAV_M_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_STRING_VALUES.INDEX_,
                        EAV_BE_STRING_VALUES.OPEN_DATE,
                        EAV_BE_STRING_VALUES.VALUE)
                .from(EAV_BE_STRING_VALUES)
                .join(EAV_M_SIMPLE_ATTRIBUTES).on(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_STRING_VALUES.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_STRING_VALUES.OPEN_DATE.lessOrEqual(reportDate))
                .and(Configuration.historyAlgorithm == Constants.HISTORY_ALGORITHM_NOT_FILL ?
                        EAV_BE_STRING_VALUES.CLOSE_DATE.greaterThan(reportDate)
                                .or(EAV_BE_STRING_VALUES.CLOSE_DATE.isNull()) :
                        EAV_BE_STRING_VALUES.CLOSE_DATE.greaterThan(reportDate));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            (Long) row.get(EAV_BE_STRING_VALUES.ID.getName()),
                            batchRepository.getBatch((Long)row.get(EAV_BE_STRING_VALUES.BATCH_ID.getName())),
                            (Long) row.get(EAV_BE_STRING_VALUES.INDEX_.getName()),
                            (java.sql.Date) row.get(EAV_BE_STRING_VALUES.OPEN_DATE.getName()),
                            row.get(EAV_BE_STRING_VALUES.VALUE.getName())));
        }
    }

    private void loadDoubleValues(BaseEntity baseEntity)
    {
        Date reportDate = DateUtils.convert(baseEntity.getReportDate());
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_DOUBLE_VALUES.ID,
                        EAV_BE_DOUBLE_VALUES.BATCH_ID,
                        EAV_M_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_DOUBLE_VALUES.INDEX_,
                        EAV_BE_DOUBLE_VALUES.OPEN_DATE,
                        EAV_BE_DOUBLE_VALUES.VALUE)
                .from(EAV_BE_DOUBLE_VALUES)
                .join(EAV_M_SIMPLE_ATTRIBUTES).on(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_DOUBLE_VALUES.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_DOUBLE_VALUES.OPEN_DATE.lessOrEqual(reportDate))
                .and(Configuration.historyAlgorithm == Constants.HISTORY_ALGORITHM_NOT_FILL ?
                        EAV_BE_DOUBLE_VALUES.CLOSE_DATE.greaterThan(reportDate)
                                .or(EAV_BE_DOUBLE_VALUES.CLOSE_DATE.isNull()) :
                        EAV_BE_DOUBLE_VALUES.CLOSE_DATE.greaterThan(reportDate));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntity.put(
                    (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            (Long) row.get(EAV_BE_DOUBLE_VALUES.ID.getName()),
                            batchRepository.getBatch((Long)row.get(EAV_BE_DOUBLE_VALUES.BATCH_ID.getName())),
                            (Long) row.get(EAV_BE_DOUBLE_VALUES.INDEX_.getName()),
                            (java.sql.Date) row.get(EAV_BE_DOUBLE_VALUES.OPEN_DATE.getName()),
                            row.get(EAV_BE_DOUBLE_VALUES.VALUE.getName())));
        }
    }

    private void loadComplexValues(BaseEntity baseEntity)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_COMPLEX_VALUES.ID,
                        EAV_BE_COMPLEX_VALUES.BATCH_ID,
                        EAV_M_COMPLEX_ATTRIBUTES.NAME,
                        EAV_BE_COMPLEX_VALUES.INDEX_,
                        EAV_BE_COMPLEX_VALUES.REP_DATE,
                        EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID)
                .from(EAV_BE_COMPLEX_VALUES)
                .join(EAV_M_COMPLEX_ATTRIBUTES).on(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID.eq(EAV_M_COMPLEX_ATTRIBUTES.ID))
                .where(EAV_BE_COMPLEX_VALUES.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_COMPLEX_VALUES.IS_LAST.eq(true));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_COMPLEX_VALUES.BATCH_ID.getName()));
            long entityValueId = (Long)row.get(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.getName());
            BaseEntity childBaseEntity = load(entityValueId);

            baseEntity.put(
                    (String) row.get(EAV_M_COMPLEX_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            (Long) row.get(EAV_BE_COMPLEX_VALUES.ID.getName()),
                            batch,
                            (Long) row.get(EAV_BE_COMPLEX_VALUES.INDEX_.getName()),
                            (Date) row.get(EAV_BE_COMPLEX_VALUES.REP_DATE.getName()),
                            childBaseEntity));
        }
    }

    private void loadEntitySet(BaseEntity baseEntity, String attribute) {
        MetaClass metaClass = baseEntity.getMeta();
        IMetaType metaType = metaClass.getMemberType(attribute);
        if (metaType.isSetOfSets())
        {
            // TODO: Add functionality
            return;
        }
        if (metaType.isComplex())
        {
            loadEntityComplexSet(baseEntity, attribute);
        }
        else {
            loadEntitySimpleSet(baseEntity, attribute);
        }
    }

    private void loadEntitySimpleSet(BaseEntity baseEntity, String attribute)
    {
        MetaClass metaClass = baseEntity.getMeta();
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);

        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.ID.as("entity_simple_set_id"),
                        EAV_M_SIMPLE_SET.NAME,
                        EAV_BE_SETS.INDEX_,
                        EAV_BE_SETS.REP_DATE)
                .from(EAV_BE_ENTITY_SIMPLE_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID.eq(EAV_BE_SETS.ID))
                .join(EAV_M_SIMPLE_SET).on(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_SET.ID))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID.equal(metaAttribute.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        if (rows.size() == 1)
        {
            Map<String, Object> row = rows.get(0);
            loadEntitySet(
                    baseEntity,
                    (String) row.get(EAV_M_SIMPLE_SET.NAME.getName()),
                    (Long) row.get("set_id"),
                    (Long) row.get("entity_simple_set_id"),
                    (Long) row.get(EAV_BE_SETS.BATCH_ID.getName()),
                    (Long) row.get(EAV_BE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()));
        }
        else
        {
            throw new RuntimeException("Query returned more than one record. Unable to continue.");
        }
    }

    private void loadEntitySimpleSets(BaseEntity baseEntity)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.ID.as("entity_simple_set_id"),
                        EAV_M_SIMPLE_SET.NAME,
                        EAV_BE_SETS.INDEX_,
                        EAV_BE_SETS.REP_DATE)
                .from(EAV_BE_ENTITY_SIMPLE_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID.eq(EAV_BE_SETS.ID))
                .join(EAV_M_SIMPLE_SET).on(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_SET.ID))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST.eq(true));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            loadEntitySet(
                    baseEntity,
                    (String)row.get(EAV_M_SIMPLE_SET.NAME.getName()),
                    (Long)row.get("set_id"),
                    (Long)row.get("entity_simple_set_id"),
                    (Long)row.get(EAV_BE_SETS.BATCH_ID.getName()),
                    (Long)row.get(EAV_BE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()));
        }
    }

    private void loadEntityComplexSet(BaseEntity baseEntity, String attribute)
    {
        MetaClass metaClass = baseEntity.getMeta();
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);

        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.ID.as("entity_complex_set_id"),
                        EAV_M_COMPLEX_SET.NAME,
                        EAV_BE_SETS.INDEX_,
                        EAV_BE_SETS.REP_DATE)
                .from(EAV_BE_ENTITY_COMPLEX_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID.eq(EAV_BE_SETS.ID))
                .join(EAV_M_COMPLEX_SET).on(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.eq(EAV_M_COMPLEX_SET.ID))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST.eq(true));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        if (rows.size() == 1)
        {
            Map<String, Object> row = rows.get(0);
            loadEntitySet(
                    baseEntity,
                    (String)row.get(EAV_M_COMPLEX_SET.NAME.getName()),
                    (Long)row.get("set_id"),
                    (Long)row.get("entity_complex_set_id"),
                    (Long)row.get(EAV_BE_SETS.BATCH_ID.getName()),
                    (Long)row.get(EAV_BE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()));
        }
        else
        {
            throw new RuntimeException("Query returned more than one record. Unable to continue.");
        }
    }

    private void loadEntityComplexSets(BaseEntity baseEntity)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.ID.as("entity_complex_set_id"),
                        EAV_M_COMPLEX_SET.NAME,
                        EAV_BE_SETS.INDEX_,
                        EAV_BE_SETS.REP_DATE)
                .from(EAV_BE_ENTITY_COMPLEX_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID.eq(EAV_BE_SETS.ID))
                .join(EAV_M_COMPLEX_SET).on(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.eq(EAV_M_COMPLEX_SET.ID))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST.eq(true));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            loadEntitySet(
                    baseEntity,
                    (String)row.get(EAV_M_COMPLEX_SET.NAME.getName()),
                    (Long)row.get("set_id"),
                    (Long)row.get("entity_complex_set_id"),
                    (Long)row.get(EAV_BE_SETS.BATCH_ID.getName()),
                    (Long)row.get(EAV_BE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()));
        }
    }

    private void loadEntitySetOfSets(BaseEntity baseEntity)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_M_SET_OF_SETS.NAME,
                        EAV_BE_ENTITY_SET_OF_SETS.ID.as("entity_set_of_set_id"),
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_SETS.INDEX_,
                        EAV_BE_SETS.REP_DATE)
                .from(EAV_BE_ENTITY_SET_OF_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_ENTITY_SET_OF_SETS.SET_ID.eq(EAV_BE_SETS.ID))
                .join(EAV_M_SET_OF_SETS).on(EAV_BE_ENTITY_SET_OF_SETS.ATTRIBUTE_ID.eq(EAV_M_SET_OF_SETS.ID))
                .where(EAV_BE_ENTITY_SET_OF_SETS.ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_ENTITY_SET_OF_SETS.IS_LAST.eq(true));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            loadEntitySet(
                    baseEntity,
                    (String)row.get(EAV_M_SET_OF_SETS.NAME.getName()),
                    (Long)row.get("set_id"),
                    (Long)row.get("entity_set_of_set_id"),
                    (Long)row.get(EAV_BE_SETS.BATCH_ID.getName()),
                    (Long)row.get(EAV_BE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()));
        }
    }

    private void loadEntitySet(BaseEntity baseEntity, String attribute, Long setId,
                   Long valueId, Long batchId, Long index, Date repDate) {
        MetaClass metaClass = baseEntity.getMeta();
        IMetaType metaType = metaClass.getMemberType(attribute);

        BaseSet baseSet = new BaseSet(setId, ((MetaSet)metaType).getMemberType());

        if (metaType.isComplex())
        {
            loadComplexSetValues(baseSet);
        }
        else
        {
            loadSimpleSetValues(baseSet);
        }

        Batch batch = batchRepository.getBatch(batchId);
        baseEntity.put(attribute, new BaseValue(valueId, batch, index, repDate, baseSet));
    }

    private void loadSetOfSimpleSets(BaseSet baseSet)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_SETS.INDEX_,
                        EAV_BE_SETS.REP_DATE)
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
            BaseSet baseSetChild = new BaseSet((Long)row.get(EAV_BE_SETS.ID.getName()), ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSetChild);
            }
            else
            {
                loadSimpleSetValues(baseSetChild);
            }

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_SETS.BATCH_ID.getName()));
            baseSet.put(new BaseValue(batch, (Long)row.get(EAV_BE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()), baseSetChild));
        }
    }

    private void loadSetOfComplexSets(BaseSet baseSet)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_SETS.INDEX_,
                        EAV_BE_SETS.REP_DATE)
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
            BaseSet baseSetChild = new BaseSet((Long)row.get(EAV_BE_SETS.ID.getName()), ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSetChild);
            }
            else
            {
                loadSimpleSetValues(baseSetChild);
            }


            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_SETS.BATCH_ID.getName()));
            baseSet.put(new BaseValue(batch, (Long)row.get(EAV_BE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()), baseSetChild));
        }
    }

    private void loadSimpleSetValues(BaseSet baseSet)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (metaType.isComplex())
            throw new RuntimeException("Load the simple set values is not possible. " +
                    "Simple values ​​can not be added to an set of complex values.");

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

    private void loadIntegerSetValues(BaseSet baseSet)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_INTEGER_SET_VALUES.BATCH_ID,
                        EAV_BE_INTEGER_SET_VALUES.INDEX_,
                        EAV_BE_INTEGER_SET_VALUES.VALUE,
                        EAV_BE_INTEGER_SET_VALUES.REP_DATE)
                .from(EAV_BE_INTEGER_SET_VALUES)
                .where(EAV_BE_INTEGER_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch((Long)rowValue.get(EAV_BE_INTEGER_SET_VALUES.BATCH_ID.getName()));
            baseSet.put(
                    new BaseValue(
                            batch,
                            (Long)rowValue.get(EAV_BE_INTEGER_SET_VALUES.INDEX_.getName()),
                            (Date) rowValue.get(EAV_BE_INTEGER_SET_VALUES.REP_DATE.getName()),
                            rowValue.get(EAV_BE_INTEGER_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadDateSetValues(BaseSet baseSet)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_DATE_SET_VALUES.BATCH_ID,
                        EAV_BE_DATE_SET_VALUES.INDEX_,
                        EAV_BE_DATE_SET_VALUES.VALUE,
                        EAV_BE_DATE_SET_VALUES.REP_DATE)
                .from(EAV_BE_DATE_SET_VALUES)
                .where(EAV_BE_DATE_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch((Long)rowValue.get(EAV_BE_DATE_SET_VALUES.BATCH_ID.getName()));
            baseSet.put(
                    new BaseValue(
                            batch,
                            (Long)rowValue.get(EAV_BE_DATE_SET_VALUES.INDEX_.getName()),
                            (Date) rowValue.get(EAV_BE_DATE_SET_VALUES.REP_DATE.getName()),
                            rowValue.get(EAV_BE_DATE_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadStringSetValues(BaseSet baseSet)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_STRING_SET_VALUES.BATCH_ID,
                        EAV_BE_STRING_SET_VALUES.INDEX_,
                        EAV_BE_STRING_SET_VALUES.VALUE,
                        EAV_BE_STRING_SET_VALUES.REP_DATE)
                .from(EAV_BE_STRING_SET_VALUES)
                .where(EAV_BE_STRING_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch((Long)rowValue.get(EAV_BE_STRING_SET_VALUES.BATCH_ID.getName()));
            baseSet.put(
                    new BaseValue(
                            batch,
                            (Long)rowValue.get(EAV_BE_STRING_SET_VALUES.INDEX_.getName()),
                            (Date) rowValue.get(EAV_BE_STRING_SET_VALUES.REP_DATE.getName()),
                            rowValue.get(EAV_BE_STRING_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadBooleanSetValues(BaseSet baseSet)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_BOOLEAN_SET_VALUES.BATCH_ID,
                        EAV_BE_BOOLEAN_SET_VALUES.INDEX_,
                        EAV_BE_BOOLEAN_SET_VALUES.VALUE,
                        EAV_BE_BOOLEAN_SET_VALUES.REP_DATE)
                .from(EAV_BE_BOOLEAN_SET_VALUES)
                .where(EAV_BE_BOOLEAN_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch((Long)rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.BATCH_ID.getName()));
            baseSet.put(
                    new BaseValue(
                            batch,
                            (Long)rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.INDEX_.getName()),
                            (Date) rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.REP_DATE.getName()),
                            rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadDoubleSetValues(BaseSet baseSet)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID,
                        EAV_BE_DOUBLE_SET_VALUES.INDEX_,
                        EAV_BE_DOUBLE_SET_VALUES.VALUE,
                        EAV_BE_DOUBLE_SET_VALUES.REP_DATE)
                .from(EAV_BE_DOUBLE_SET_VALUES)
                .where(EAV_BE_DOUBLE_SET_VALUES.SET_ID.equal(baseSet.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> rowValue = it.next();

            Batch batch = batchRepository.getBatch((Long)rowValue.get(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID.getName()));
            baseSet.put(
                    new BaseValue(
                            batch,
                            (Long)rowValue.get(EAV_BE_DOUBLE_SET_VALUES.INDEX_.getName()),
                            (Date) rowValue.get(EAV_BE_DOUBLE_SET_VALUES.REP_DATE.getName()),
                            rowValue.get(EAV_BE_DOUBLE_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadComplexSetValues(BaseSet baseSet)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (!metaType.isComplex())
            throw new RuntimeException("Load the complex set values is not possible. " +
                    "Complex values ​​can not be added to an set of simple values.");

        if (metaType.isSet())
        {
            loadSetOfComplexSets(baseSet);
        }
        else
        {
            SelectForUpdateStep select = sqlGenerator
                    .select(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID,
                            EAV_BE_COMPLEX_SET_VALUES.BATCH_ID,
                            EAV_BE_COMPLEX_SET_VALUES.INDEX_,
                            EAV_BE_COMPLEX_SET_VALUES.REP_DATE)
                    .from(EAV_BE_COMPLEX_SET_VALUES)
                    .where(EAV_BE_COMPLEX_SET_VALUES.SET_ID.equal((long) baseSet.getId()));

            logger.debug(select.toString());
            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

            Iterator<Map<String, Object>> it = rows.iterator();
            while (it.hasNext())
            {
                Map<String, Object> row = it.next();

                Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_COMPLEX_SET_VALUES.BATCH_ID.getName()));
                BaseEntity baseEntity = load((Long)row.get(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.getName()));
                baseSet.put(new BaseValue(batch, (Long)row.get(EAV_BE_COMPLEX_SET_VALUES.INDEX_.getName()),
                        (Date) row.get(EAV_BE_COMPLEX_SET_VALUES.REP_DATE.getName()), baseEntity));
            }
        }
    }

    private void removeReportDates(BaseEntity baseEntity) {
        DeleteConditionStep delete = sqlGenerator
                .delete(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntity.getId()));

        logger.debug(delete.toString());
        batchUpdateWithStats(delete.getSQL(), delete.getBindValues());
    }

    private Set<BaseEntity> collectComplexSetValues(BaseSet baseSet)
    {
        Set<BaseEntity> entities = new HashSet<BaseEntity>();

        IMetaType metaType = baseSet.getMemberType();
        if (metaType.isSetOfSets())
        {
            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext())
            {
                IBaseValue baseValue = it.next();
                entities.addAll(collectComplexSetValues((BaseSet)baseValue.getValue()));
            }
        } else {
            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext())
            {
                IBaseValue baseValue = it.next();
                entities.add((BaseEntity)baseValue.getValue());
            }
        }

        return entities;
    }

}
