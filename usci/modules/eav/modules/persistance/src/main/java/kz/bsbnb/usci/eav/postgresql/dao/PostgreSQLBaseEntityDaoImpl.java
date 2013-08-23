package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
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
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.tool.Configuration;
import kz.bsbnb.usci.eav.tool.Constants;
import kz.bsbnb.usci.eav.util.DateUtils;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;
import java.util.Comparator;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

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
    @Autowired
    IBeStorageDao beStorageDao;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    public IBaseEntity load(long id)
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

        Select select = context
                .select(EAV_BE_ENTITIES.CLASS_ID,
                        EAV_BE_ENTITY_REPORT_DATES.INTEGER_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.DATE_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.STRING_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.BOOLEAN_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.DOUBLE_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.COMPLEX_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.SIMPLE_SETS_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.COMPLEX_SETS_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.SET_OF_SETS_COUNT)
                .from(EAV_BE_ENTITIES, EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITIES.ID.equal(id))
                .and(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.equal(EAV_BE_ENTITIES.ID))
                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.eq(
                        context
                                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE))
                                .from(EAV_BE_ENTITY_REPORT_DATES)
                                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(id))));

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
        if(row != null)
        {
            Set<java.util.Date> availableReportDates = getAvailableReportDates(id);

            if (availableReportDates.size() == 0)
            {
                throw new RuntimeException(String.format("Found a violation of the database structure. " +
                        "For instance BaseEntity with ID {0} is missing the list of available reporting dates.", id));
            }

            if (reportDate.compareTo(Collections.min(availableReportDates)) == -1 ||
                    reportDate.compareTo(Collections.max(availableReportDates)) == 1)
            {
                throw new IllegalArgumentException("Loading instance BaseEntity impossible. " +
                        "Reporting date should be in the range of available reporting dates.");
            }

            MetaClass meta = metaClassRepository.getMetaClass((Long)row.get(EAV_BE_ENTITIES.CLASS_ID.getName()));
            BaseEntity baseEntity = new BaseEntity(id, meta, reportDate, availableReportDates);

            if ((Long)row.get(EAV_BE_ENTITY_REPORT_DATES.INTEGER_VALUES_COUNT.getName()) != 0)
            {
                loadIntegerValues(baseEntity);
            }

            if ((Long)row.get(EAV_BE_ENTITY_REPORT_DATES.DATE_VALUES_COUNT.getName()) != 0)
            {
                loadDateValues(baseEntity);
            }

            if ((Long)row.get(EAV_BE_ENTITY_REPORT_DATES.STRING_VALUES_COUNT.getName()) != 0)
            {
                loadStringValues(baseEntity);
            }

            if ((Long)row.get(EAV_BE_ENTITY_REPORT_DATES.BOOLEAN_VALUES_COUNT.getName()) != 0)
            {
                loadBooleanValues(baseEntity);
            }

            if ((Long)row.get(EAV_BE_ENTITY_REPORT_DATES.DOUBLE_VALUES_COUNT.getName()) != 0)
            {
                loadDoubleValues(baseEntity);
            }

            if ((Long)row.get(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_VALUES_COUNT.getName()) != 0)
            {
                loadComplexValues(baseEntity);
            }

            if ((Long)row.get(EAV_BE_ENTITY_REPORT_DATES.SIMPLE_SETS_COUNT.getName()) != 0)
            {
                loadEntitySimpleSets(baseEntity);
            }

            if ((Long)row.get(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_SETS_COUNT.getName()) != 0)
            {
                loadEntityComplexSets(baseEntity);
            }

            if ((Long)row.get(EAV_BE_ENTITY_REPORT_DATES.SET_OF_SETS_COUNT.getName()) != 0)
            {
                loadEntitySetOfSets(baseEntity);
            }

            return baseEntity;
        }
        else
        {
            logger.error("Can't load instance of BaseEntity, empty data set.");
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long search(IBaseEntity baseEntity)
    {
        MetaClass meta = baseEntity.getMeta();
        Long baseEntityId = searcherPool.getSearcher(meta.getClassName())
                .findSingle((BaseEntity)baseEntity);

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
        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            baseEntityIds.add((Long)row.get(EAV_BE_ENTITIES.ID.getName()));
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
        MetaClass metaClass = baseEntity.getMeta();
        if (metaClass.isSearchable())
        {
            long baseEntityId = search(baseEntity);
            if (baseEntityId > 0)
            {
                baseEntity.setId(baseEntityId);
            }
        }

        for (String attribute: metaClass.getAttributeNames())
        {
            IMetaType metaType = metaClass.getMemberType(attribute);
            if (metaType.isComplex())
            {
                if (baseEntity.getBaseValue(attribute) != null)
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
        }

        return baseEntity;
    }

    public IBaseEntity apply(final IBaseEntity baseEntityForSave)
    {
        if (baseEntityForSave.getId() < 1 || !baseEntityForSave.getMeta().isSearchable())
        {
            for (String attribute: baseEntityForSave.getAttributeNames())
            {
                IMetaType metaType = baseEntityForSave.getMemberType(attribute);
                IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);
                if (baseValueForSave.getValue() == null)
                {
                    baseEntityForSave.remove(attribute);
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
            }

            return baseEntityForSave;
        }
        else
        {
            java.util.Date reportDate = baseEntityForSave.getReportDate();
            java.util.Date maxReportDate = getMaxReportDate(baseEntityForSave.getId(), reportDate);

            IBaseEntity baseEntityLoaded = null;
            if (maxReportDate == null)
            {
                java.util.Date minReportDate = getMinReportDate(baseEntityForSave.getId());
                baseEntityLoaded = ((BaseEntity)beStorageDao.getBaseEntity(baseEntityForSave.getId(), minReportDate)).clone();
            }
            else
            {
                baseEntityLoaded = ((BaseEntity)beStorageDao.getBaseEntity(baseEntityForSave.getId(), maxReportDate)).clone();
            }

            baseEntityLoaded.setReportDate(reportDate);

            BasicBaseEntityComparator comparator = new BasicBaseEntityComparator();
            // TODO: Remove cast IBaseEntity to BaseEntity
            if (!comparator.compare((BaseEntity)baseEntityForSave, (BaseEntity)baseEntityLoaded))
            {
                long baseEntitySearchedId = search(baseEntityForSave);
                if (baseEntitySearchedId > 0 && baseEntityLoaded.getId() != baseEntitySearchedId)
                {
                    throw new RuntimeException("Found a violation of uniqueness. " +
                            "The saving process can not be continued.");
                }
            }

            baseEntityLoaded.setListeners();

            Set<String> insertedAttributes = SetUtils.difference(baseEntityForSave.getAttributeNames(),
                    baseEntityLoaded.getAttributeNames());
            for (String insertedAttribute: insertedAttributes)
            {
                baseEntityLoaded.put(insertedAttribute, baseEntityForSave.getBaseValue(insertedAttribute));
            }

            Set<String> otherAttributes = SetUtils.difference(baseEntityForSave.getAttributeNames(),
                    insertedAttributes);

            for (String attribute: otherAttributes)
            {
                IMetaType metaType = baseEntityLoaded.getMemberType(attribute);
                IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);
                IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);

                if (baseValueForSave.getValue() == null)
                {
                    baseEntityLoaded.remove(attribute);
                    continue;
                }

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

                        IBaseEntity baseEntityApplied = apply((BaseEntity)baseValueForSave.getValue());

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

            baseEntityLoaded.removeListeners();

            return baseEntityLoaded;
        }
    }

    @Override
    public IBaseEntity saveOrUpdate(IBaseEntity baseEntity)
    {
        if (baseEntity.getId() == 0)
        {
            return save(baseEntity);
        }
        else
        {
            IBaseEntity baseEntityLoaded = beStorageDao.getBaseEntity(baseEntity.getId());
            update(baseEntity, baseEntityLoaded);
            return baseEntity;
        }
    }


    @Override
    @Transactional
    public IBaseEntity process(IBaseEntity baseEntity)
    {
        baseEntity = prepare(baseEntity);
        baseEntity = apply(baseEntity);
        baseEntity = saveOrUpdate(baseEntity);

        // TODO: Make an automatic update cache
        beStorageDao.clean();

        return baseEntity;
    }

    public IBaseEntity update(final IBaseEntity baseEntityForSave, IBaseEntity baseEntityLoaded)
    {
        if (baseEntityForSave.getModifiedAttributes().size() == 0)
        {
            throw new RuntimeException("The instance of the BaseEntity does not contain changed values.");
        }

        TreeSet<String> modifiedAttributes = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String thisString, String thatString) {
                return thisString.compareTo(thatString);
            }
        });
        modifiedAttributes.addAll(baseEntityForSave.getModifiedAttributes());

        Iterator<String> it = modifiedAttributes.iterator();
        while (it.hasNext())
        {
            String attribute = it.next();

            IMetaType metaType = baseEntityForSave.getMemberType(attribute);
            if (metaType == null)
            {
                throw new RuntimeException("Meta data for the specified attribute is not found.");
            }

            IBaseEntity parentBaseEntityLoaded = null;
            IBaseEntity parentBaseEntityForSave = null;
            String childAttribute;

            if (attribute.contains("."))
            {
                String parentAttribute = "";

                String[] identifiers = attribute.split(".");
                for (int i = 0; i < identifiers.length - 1; i++)
                {
                    parentAttribute += i == 0 ? identifiers[i] : "." + identifiers[i];
                }
                parentBaseEntityForSave = (BaseEntity)baseEntityForSave.getBaseValue(parentAttribute).getValue();
                parentBaseEntityLoaded = (BaseEntity)baseEntityLoaded.getBaseValue(parentAttribute).getValue();

                childAttribute = identifiers[identifiers.length - 1];
            }
            else
            {
                parentBaseEntityForSave = baseEntityForSave;
                parentBaseEntityLoaded = baseEntityLoaded;

                childAttribute = attribute;
            }

            if (metaType.isComplex())
            {
                if (metaType.isSet())
                {

                }
                else
                {
                    beComplexValueDao.update(parentBaseEntityLoaded, parentBaseEntityForSave, childAttribute);
                }
            }
            else
            {
                if (metaType.isSet())
                {

                }
                else
                {
                    beSimpleValueDao.update(parentBaseEntityLoaded, parentBaseEntityForSave, childAttribute);
                }
            }
        }

        processReportDate(baseEntityForSave, baseEntityLoaded.getAvailableReportDates()
                .contains(baseEntityForSave.getReportDate()));

        return baseEntityForSave;
    }

    @Transactional
    public IBaseEntity save(IBaseEntity baseEntity)
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

        if (baseEntity.getId() > 0)
        {
            throw new IllegalArgumentException("Instance of BaseEntity already contain the ID.");
        }

        long baseEntityId = insertBaseEntity(baseEntity);
        baseEntity.setId(baseEntityId);

        Set<String> attributes = baseEntity.getAttributeNames();

        Set<String> integerValues = new HashSet<String>();
        Set<String> dateValues = new HashSet<String>();
        Set<String> stringValues = new HashSet<String>();
        Set<String> booleanValues = new HashSet<String>();
        Set<String> doubleValues = new HashSet<String>();
        Set<String> complexValues = new HashSet<String>();
        Set<String> simpleSets = new HashSet<String>();
        Set<String> complexSets = new HashSet<String>();
        Set<String> setOfSets = new HashSet<String>();

        Iterator<String> it = attributes.iterator();
        while (it.hasNext())
        {
            String attribute = it.next();

            IMetaType metaType = baseEntity.getMemberType(attribute);
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    setOfSets.add(attribute);
                }
                else
                {
                    if (metaType.isComplex())
                    {
                        complexSets.add(attribute);
                    }
                    else
                    {
                        simpleSets.add(attribute);
                    }
                }
            }
            else
            {
                if (metaType.isComplex())
                {
                    complexValues.add(attribute);
                }
                else
                {
                    MetaValue metaValue = (MetaValue)metaType;
                    switch (metaValue.getTypeCode())
                    {
                        case INTEGER:
                            integerValues.add(attribute);
                            break;
                        case DATE:
                            dateValues.add(attribute);
                            break;
                        case STRING:
                            stringValues.add(attribute);
                            break;
                        case BOOLEAN:
                            booleanValues.add(attribute);
                            break;
                        case DOUBLE:
                            doubleValues.add(attribute);
                            break;
                        default:
                            throw new RuntimeException("Unknown data type.");
                    }

                }
            }
        }

        if (integerValues.size() != 0)
        {
            beSimpleValueDao.save(baseEntity, integerValues, DataTypes.INTEGER);
        }

        if (dateValues.size() != 0)
        {
            beSimpleValueDao.save(baseEntity, dateValues, DataTypes.DATE);
        }

        if (stringValues.size() != 0)
        {
            beSimpleValueDao.save(baseEntity, stringValues, DataTypes.STRING);
        }

        if (booleanValues.size() != 0)
        {
            beSimpleValueDao.save(baseEntity, booleanValues, DataTypes.BOOLEAN);
        }

        if (doubleValues.size() != 0)
        {
            beSimpleValueDao.save(baseEntity, doubleValues, DataTypes.DOUBLE);
        }

        if (complexValues.size() != 0)
        {
            beComplexValueDao.save(baseEntity, complexValues);
        }

        if (simpleSets.size() != 0)
        {
            beSetValueDao.save(baseEntity, simpleSets);
        }

        if (complexSets.size() != 0)
        {
            beSetValueDao.save(baseEntity, complexSets);
        }

        if (setOfSets.size() != 0)
        {
            beSetValueDao.save(baseEntity, setOfSets);
        }

        insertReportDate(
                baseEntity.getId(),
                baseEntity.getReportDate(),
                integerValues.size(),
                dateValues.size(),
                stringValues.size(),
                booleanValues.size(),
                doubleValues.size(),
                complexValues.size(),
                simpleSets.size(),
                complexSets.size(),
                setOfSets.size());

        return baseEntity;
    }

    public void remove(IBaseEntity baseEntity)
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

            DeleteConditionStep delete = context
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

        select = context
                .select(DSL.count().as("VALUE_COUNT"))
                .from(EAV_BE_COMPLEX_VALUES)
                .where(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.equal(baseEntityId));

        logger.debug(select.toString());
        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        long complexValuesCount = (Long)rows.get(0).get("VALUE_COUNT");

        select = context
                .select(DSL.count().as("VALUE_COUNT"))
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
            reportDates.add(DateUtils.convert((Date)row.get(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.getName())));
        }

        return reportDates;
    }

    public java.util.Date getMinReportDate(long baseEntityId)
    {
        Select select = context
                .select(DSL.min(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("min_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId))
                .limit(1);

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DateUtils.convert((Date)rows.get(0).get("min_report_date"));
    }

    public java.util.Date getMaxReportDate(long baseEntityId, java.util.Date reportDate)
    {
        Select select = context
                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("min_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.lessOrEqual(DateUtils.convert(reportDate)))
                .limit(1);

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DateUtils.convert((Date)rows.get(0).get("min_report_date"));
    }

    public java.util.Date getMaxReportDate(long baseEntityId)
    {
        Select select = context
                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("max_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId))
                .limit(1);

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DateUtils.convert((Date)rows.get(0).get("max_report_date"));
    }

    private void insertReportDate(
            long baseEntityId,
            java.util.Date reportDate,
            long integerValuesCount,
            long dateValuesCount,
            long stringValuesCount,
            long booleanValuesCount,
            long doubleValuesCount,
            long complexValuesCount,
            long simpleSetsCount,
            long complexSetsCount,
            long setOfSetsCount)
    {
        Insert insert = context
                .insertInto(EAV_BE_ENTITY_REPORT_DATES)
                .set(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE, DateUtils.convert(reportDate))
                .set(EAV_BE_ENTITY_REPORT_DATES.INTEGER_VALUES_COUNT, integerValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.DATE_VALUES_COUNT, dateValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.STRING_VALUES_COUNT, stringValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.BOOLEAN_VALUES_COUNT, booleanValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.DOUBLE_VALUES_COUNT, doubleValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_VALUES_COUNT, complexValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.SIMPLE_SETS_COUNT, simpleSetsCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_SETS_COUNT, complexSetsCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.SET_OF_SETS_COUNT, setOfSetsCount);

        logger.debug(insert.toString());
        updateWithStats(insert.getSQL(), insert.getBindValues().toArray());
    }

    private void updateReportDate(
            long baseEntityId,
            java.util.Date reportDate,
            long integerValuesCount,
            long dateValuesCount,
            long stringValuesCount,
            long booleanValuesCount,
            long doubleValuesCount,
            long complexValuesCount,
            long simpleSetsCount,
            long complexSetsCount,
            long setOfSetsCount)
    {
        Update update = context
                .update(EAV_BE_ENTITY_REPORT_DATES)
                .set(EAV_BE_ENTITY_REPORT_DATES.INTEGER_VALUES_COUNT, integerValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.DATE_VALUES_COUNT, dateValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.STRING_VALUES_COUNT, stringValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.BOOLEAN_VALUES_COUNT, booleanValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.DOUBLE_VALUES_COUNT, doubleValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_VALUES_COUNT, complexValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.SIMPLE_SETS_COUNT, simpleSetsCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_SETS_COUNT, complexSetsCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.SET_OF_SETS_COUNT, setOfSetsCount)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.equal(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.equal(DateUtils.convert(reportDate)));

        logger.debug(update.toString());
        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }

    private void processReportDate(IBaseEntity baseEntity, boolean existingReportDate)
    {
        long integerValuesCount = 0;
        long dateValuesCount = 0;
        long stringValuesCount = 0;
        long booleanValuesCount = 0;
        long doubleValuesCount = 0;
        long complexValuesCount = 0;
        long simpleSetsCount = 0;
        long complexSetsCount = 0;
        long setOfSetsCount = 0;

        for (String attribute: baseEntity.getAttributeNames())
        {
            IMetaType metaType = baseEntity.getMemberType(attribute);
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    setOfSetsCount++;
                }
                else
                {
                    if (metaType.isComplex())
                    {
                        complexSetsCount++;
                    }
                    else
                    {
                        simpleSetsCount++;
                    }
                }
            }
            else
            {
                if (metaType.isComplex())
                {
                    complexValuesCount++;
                }
                else
                {
                    MetaValue metaValue = (MetaValue)metaType;
                    switch (metaValue.getTypeCode())
                    {
                        case INTEGER:
                            integerValuesCount++;
                            break;
                        case DATE:
                            dateValuesCount++;
                            break;
                        case STRING:
                            stringValuesCount++;
                            break;
                        case BOOLEAN:
                            booleanValuesCount++;
                            break;
                        case DOUBLE:
                            doubleValuesCount++;
                            break;
                        default:
                            throw new RuntimeException("Unknown data type.");
                    }

                }
            }
        }

        if (existingReportDate)
        {
            updateReportDate(baseEntity.getId(), baseEntity.getReportDate(), integerValuesCount, dateValuesCount,
                    stringValuesCount, booleanValuesCount, doubleValuesCount, complexValuesCount, simpleSetsCount,
                    complexSetsCount, setOfSetsCount);
        }
        else
        {
            insertReportDate(baseEntity.getId(), baseEntity.getReportDate(), integerValuesCount, dateValuesCount,
                    stringValuesCount, booleanValuesCount, doubleValuesCount, complexValuesCount, simpleSetsCount,
                    complexSetsCount, setOfSetsCount);
        }
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

    private void loadIntegerValues(BaseEntity baseEntity)
    {
        Date reportDate = DateUtils.convert(baseEntity.getReportDate());
        Select select = context
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
        Select select = context
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
        SelectForUpdateStep select = context
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
        Select select = context
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
        SelectForUpdateStep select = context
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
        SelectForUpdateStep select = context
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
            IBaseEntity childBaseEntity = beStorageDao.getBaseEntity(entityValueId);

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

        SelectForUpdateStep select = context
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.ID.as("entity_simple_set_id"),
                        EAV_M_SIMPLE_SET.NAME,
                        EAV_BE_ENTITY_SIMPLE_SETS.INDEX_,
                        EAV_BE_ENTITY_SIMPLE_SETS.OPEN_DATE)
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
                    (Long) row.get(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID.getName()),
                    (Long) row.get(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_ENTITY_SIMPLE_SETS.OPEN_DATE.getName()));
        }
        else
        {
            throw new RuntimeException("Query returned more than one record. Unable to continue.");
        }
    }

    private void loadEntitySimpleSets(BaseEntity baseEntity)
    {
        Date reportDate = DateUtils.convert(baseEntity.getReportDate());
        SelectForUpdateStep select = context
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.ID.as("entity_simple_set_id"),
                        EAV_M_SIMPLE_SET.NAME,
                        EAV_BE_ENTITY_SIMPLE_SETS.INDEX_,
                        EAV_BE_ENTITY_SIMPLE_SETS.OPEN_DATE)
                .from(EAV_BE_ENTITY_SIMPLE_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID.eq(EAV_BE_SETS.ID))
                .join(EAV_M_SIMPLE_SET).on(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_SET.ID))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID.equal(baseEntity.getId()))
                .and(Configuration.historyAlgorithm == Constants.HISTORY_ALGORITHM_NOT_FILL ?
                        EAV_BE_ENTITY_SIMPLE_SETS.CLOSE_DATE.greaterThan(reportDate)
                                .or(EAV_BE_ENTITY_SIMPLE_SETS.CLOSE_DATE.isNull()) :
                        EAV_BE_ENTITY_SIMPLE_SETS.CLOSE_DATE.greaterThan(reportDate));

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
                    (Long)row.get(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID.getName()),
                    (Long)row.get(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_ENTITY_SIMPLE_SETS.OPEN_DATE.getName()));
        }
    }

    private void loadEntityComplexSet(BaseEntity baseEntity, String attribute)
    {
        MetaClass metaClass = baseEntity.getMeta();
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);

        SelectForUpdateStep select = context
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.ID.as("entity_complex_set_id"),
                        EAV_M_COMPLEX_SET.NAME,
                        EAV_BE_ENTITY_COMPLEX_SETS.INDEX_,
                        EAV_BE_ENTITY_COMPLEX_SETS.OPEN_DATE)
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
                    (Long)row.get(EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID.getName()),
                    (Long)row.get(EAV_BE_ENTITY_COMPLEX_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_ENTITY_COMPLEX_SETS.OPEN_DATE.getName()));
        }
        else
        {
            throw new RuntimeException("Query returned more than one record. Unable to continue.");
        }
    }

    private void loadEntityComplexSets(BaseEntity baseEntity)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.ID.as("entity_complex_set_id"),
                        EAV_M_COMPLEX_SET.NAME,
                        EAV_BE_ENTITY_COMPLEX_SETS.INDEX_,
                        EAV_BE_ENTITY_COMPLEX_SETS.OPEN_DATE)
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
                    (Long)row.get(EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID.getName()),
                    (Long)row.get(EAV_BE_ENTITY_COMPLEX_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_ENTITY_COMPLEX_SETS.OPEN_DATE.getName()));
        }
    }

    private void loadEntitySetOfSets(BaseEntity baseEntity)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_SETS.ID.as("set_id"),
                        EAV_M_SET_OF_SETS.NAME,
                        EAV_BE_ENTITY_SET_OF_SETS.ID.as("entity_set_of_set_id"),
                        EAV_BE_ENTITY_SET_OF_SETS.BATCH_ID,
                        EAV_BE_ENTITY_SET_OF_SETS.INDEX_,
                        EAV_BE_ENTITY_SET_OF_SETS.OPEN_DATE)
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
                    (Long)row.get(EAV_BE_ENTITY_SET_OF_SETS.BATCH_ID.getName()),
                    (Long)row.get(EAV_BE_ENTITY_SET_OF_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_ENTITY_SET_OF_SETS.OPEN_DATE.getName()));
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
        SelectForUpdateStep select = context
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SET_OF_SIMPLE_SETS.BATCH_ID,
                        EAV_BE_SET_OF_SIMPLE_SETS.INDEX_,
                        EAV_BE_SET_OF_SIMPLE_SETS.OPEN_DATE)
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

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_SET_OF_SIMPLE_SETS.BATCH_ID.getName()));
            baseSet.put(new BaseValue(batch, (Long)row.get(EAV_BE_SET_OF_SIMPLE_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_SET_OF_SIMPLE_SETS.OPEN_DATE.getName()), baseSetChild));
        }
    }

    private void loadSetOfComplexSets(BaseSet baseSet)
    {
        SelectForUpdateStep select = context
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SET_OF_COMPLEX_SETS.BATCH_ID,
                        EAV_BE_SET_OF_COMPLEX_SETS.INDEX_,
                        EAV_BE_SET_OF_COMPLEX_SETS.OPEN_DATE)
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


            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_SET_OF_COMPLEX_SETS.BATCH_ID.getName()));
            baseSet.put(new BaseValue(batch, (Long)row.get(EAV_BE_SET_OF_COMPLEX_SETS.INDEX_.getName()),
                    (Date) row.get(EAV_BE_SET_OF_COMPLEX_SETS.OPEN_DATE.getName()), baseSetChild));
        }
    }

    private void loadSimpleSetValues(BaseSet baseSet)
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

    private void loadIntegerSetValues(BaseSet baseSet)
    {
        SelectForUpdateStep select = context
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
        SelectForUpdateStep select = context
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
        SelectForUpdateStep select = context
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
        SelectForUpdateStep select = context
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
        SelectForUpdateStep select = context
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
                    "Complex values ??can not be added to an set of simple values.");

        if (metaType.isSet())
        {
            loadSetOfComplexSets(baseSet);
        }
        else
        {
            SelectForUpdateStep select = context
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
                IBaseEntity baseEntity = beStorageDao.getBaseEntity(
                        (Long)row.get(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.getName()));
                baseSet.put(new BaseValue(batch, (Long)row.get(EAV_BE_COMPLEX_SET_VALUES.INDEX_.getName()),
                        (Date) row.get(EAV_BE_COMPLEX_SET_VALUES.REP_DATE.getName()), baseEntity));
            }
        }
    }

    private void removeReportDates(IBaseEntity baseEntity) {
        DeleteConditionStep delete = context
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

}
