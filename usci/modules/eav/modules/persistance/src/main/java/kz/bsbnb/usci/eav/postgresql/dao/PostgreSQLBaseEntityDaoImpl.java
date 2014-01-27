package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import java.util.Comparator;

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
    IBeStorageDao beStorageDao;

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
    IBeComplexSetValueDao beComplexSetValueDao;


    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    public IBaseEntity load(long id)
    {
        return load(id, false);
    }

    public IBaseEntity load(long id, boolean withClosedValues)
    {
        java.util.Date maxReportDate = getMaxReportDate(id);
        if (maxReportDate != null)
            return load(id, maxReportDate, withClosedValues);
        return load(id, new java.util.Date(), withClosedValues);
    }

    public BaseEntity load(long id, java.util.Date reportDate)
    {
        return load(id, reportDate, false);
    }

    public BaseEntity load(long id, java.util.Date reportDate, boolean withClosedValues)
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
                        EAV_BE_ENTITY_REPORT_DATES.COMPLEX_SETS_COUNT)
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

            MetaClass meta = metaClassRepository.getMetaClass(((BigDecimal)row.get(EAV_BE_ENTITIES.CLASS_ID.getName())).longValue());
            BaseEntity baseEntity = new BaseEntity(id, meta, reportDate, availableReportDates);

            if (((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.INTEGER_VALUES_COUNT.getName())).longValue() != 0)
            {
                loadIntegerValues(baseEntity, withClosedValues);
            }

            if (((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.DATE_VALUES_COUNT.getName())).longValue() != 0)
            {
                loadDateValues(baseEntity, withClosedValues);
            }

            if (((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.STRING_VALUES_COUNT.getName())).longValue() != 0)
            {
                loadStringValues(baseEntity, withClosedValues);
            }

            if (((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.BOOLEAN_VALUES_COUNT.getName())).longValue() != 0)
            {
                loadBooleanValues(baseEntity, withClosedValues);
            }

            if (((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.DOUBLE_VALUES_COUNT.getName())).longValue() != 0)
            {
                loadDoubleValues(baseEntity, withClosedValues);
            }

            if (((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_VALUES_COUNT.getName())).longValue() != 0)
            {
                loadComplexValues(baseEntity, withClosedValues);
            }

            if (((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.SIMPLE_SETS_COUNT.getName())).longValue() != 0)
            {
                loadEntitySimpleSets(baseEntity, withClosedValues);
            }

            if (((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_SETS_COUNT.getName())).longValue() != 0)
            {
                loadEntityComplexSets(baseEntity, withClosedValues);
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
            return applyWithoutComparison(baseEntityForSave);
        }
        else
        {
            java.util.Date reportDate = baseEntityForSave.getReportDate();
            java.util.Date maxReportDate = getMaxReportDate(baseEntityForSave.getId(), reportDate);

            if (maxReportDate == null)
            {
                Set<java.util.Date> availableReportDates =
                        getAvailableReportDates(baseEntityForSave.getId());
                availableReportDates.add(reportDate);

                baseEntityForSave.setAvailableReportDates(availableReportDates);
                return applyWithoutComparison(baseEntityForSave);
            }
            else
            {
                IBaseEntity baseEntityLoaded = ((BaseEntity)beStorageDao
                        .getBaseEntity(baseEntityForSave.getId(), maxReportDate, true)).clone();
                return applyWithComparison(baseEntityForSave, baseEntityLoaded);
            }
        }
    }

    private IBaseEntity applyWithoutComparison(final IBaseEntity baseEntity)
    {
        boolean maxReportDate = baseEntity.isMaxReportDate();
        for (String attribute: baseEntity.getIdentifiers())
        {
            boolean last = maxReportDate ? true : !presentInFuture(baseEntity, attribute);

            IMetaType metaType = baseEntity.getMemberType(attribute);
            IBaseValue baseValueForSave = baseEntity.getBaseValue(attribute);
            if (baseValueForSave.getValue() == null)
            {
                baseEntity.remove(attribute);
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
                                IBaseEntity baseEntityApplied = apply((BaseEntity)baseValue.getValue());
                                baseValue.setValue(baseEntityApplied);
                                baseValue.setLast(last);
                            }
                        }
                    }
                    else
                    {
                        IBaseEntity baseEntityApplied = apply((BaseEntity)baseValueForSave.getValue());
                        baseValueForSave.setValue(baseEntityApplied);
                    }
                }

                baseValueForSave.setLast(last);
            }
        }

        return baseEntity;
    }

    private IBaseEntity applyWithComparison(final IBaseEntity baseEntityForSave, IBaseEntity baseEntityLoaded)
    {
        baseEntityLoaded.setReportDate(baseEntityForSave.getReportDate());

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

        baseEntityLoaded.setListening(true);

        Set<String> insertedAttributes = SetUtils.difference(baseEntityForSave.getIdentifiers(),
                baseEntityLoaded.getIdentifiers());
        for (String attribute : insertedAttributes)
        {
            IBaseValue baseValue = ((BaseValue)baseEntityForSave.getBaseValue(attribute)).clone();
            baseValue.setLast(baseEntityLoaded.isMaxReportDate()? true : !presentInFuture(baseEntityForSave, attribute));

            baseEntityLoaded.put(attribute, baseValue);
        }

        Set<String> otherAttributes = SetUtils.difference(baseEntityForSave.getIdentifiers(),
                insertedAttributes);

        for (String attribute: otherAttributes)
        {
            IMetaType metaType = baseEntityLoaded.getMemberType(attribute);
            IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);
            IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);

            if (baseValueForSave.getValue() == null)
            {
                IBaseValue baseValue = new BaseValue(baseValueForSave.getBatch(), baseValueForSave.getIndex(),
                        baseValueForSave.getRepDate(), baseValueLoaded.getValue(), true,
                        baseEntityLoaded.isMaxReportDate() ? true : baseValueLoaded.isLast());
                baseEntityLoaded.put(attribute, baseValue);
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
                        IBaseSet baseSet = apply((IBaseSet)baseValueForSave.getValue(),
                                (IBaseSet)baseValueLoaded.getValue());
                        baseValueLoaded.setValue(baseSet);

                            /*List<Long> forSaveIds = new ArrayList<Long>();
                            IBaseSet baseSetForSave = (IBaseSet)baseValueForSave.getValue();
                            for (IBaseValue baseValue : baseSetForSave.get())
                            {
                                BaseEntity baseEntity = (BaseEntity)baseValue.getValue();
                                forSaveIds.add(baseEntity.getId());
                            }

                            List<Long> loadedIds = new ArrayList<Long>();
                            IBaseSet baseSetLoaded = (IBaseSet)baseValueLoaded.getValue();
                            for (IBaseValue baseValue : baseSetLoaded.get())
                            {
                                BaseEntity baseEntity = (BaseEntity)baseValue.getValue();
                                loadedIds.add(baseEntity.getId());
                            }
                            Collections.sort(loadedIds);
                            Collections.sort(loadedIds);

                            if (!forSaveIds.equals(loadedIds))
                            {
                                IBaseValue baseValueCloned = ((BaseValue)baseValueForSave).clone();
                                for (IBaseValue baseValue : ((BaseSet)baseValueCloned.getValue()).get())
                                {
                                    IBaseEntity baseEntity = apply((BaseEntity)baseValue.getValue());
                                    baseValue.setValue(baseEntity);
                                }

                                baseEntityLoaded.put(attribute, baseValueCloned);
                            }
                            else
                            {
                                IBaseSet baseSetCloned = ((BaseSet)baseSetForSave).clone();
                                for (IBaseValue baseValue : baseSetCloned.get())
                                {
                                    apply((BaseEntity)baseValue.getValue());
                                }

                                baseValueLoaded.setValue(baseSetCloned);
                            }*/
                    }
                }
                else
                {
                    long forSaveId = ((BaseEntity)baseValueForSave.getValue()).getId();
                    long loadedId = ((BaseEntity)baseValueLoaded.getValue()).getId();

                    if (forSaveId != loadedId)
                    {
                        IBaseValue baseValue = ((BaseValue)baseValueForSave).clone();
                        IBaseEntity baseEntity = apply((BaseEntity)baseValue.getValue());
                        baseValue.setValue(baseEntity);
                        baseValue.setLast(baseEntityLoaded.isMaxReportDate() ? true : baseValueLoaded.isLast());

                        baseEntityLoaded.put(attribute, baseValue);
                    }
                    else
                    {

                        IBaseEntity baseEntity = apply(((BaseEntity)baseValueForSave.getValue()).clone());
                        baseValueLoaded.setValue(baseEntity);
                    }
                }
            }
            else
            {
                if (metaType.isSet())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }
                else
                {
                    if (!baseValueForSave.getValue().equals(baseValueLoaded.getValue()))
                    {
                        IBaseValue baseValue = ((BaseValue)baseValueForSave).clone();
                        baseValue.setLast(baseEntityLoaded.isMaxReportDate() ? true : baseValueLoaded.isLast());

                        baseEntityLoaded.put(attribute, baseValue);
                    }
                }
            }
        }

        baseEntityLoaded.setListening(false);

        return baseEntityLoaded;
    }

    public IBaseSet apply(IBaseSet baseSetForSave, IBaseSet baseSetLoaded)
    {
        IMetaType metaTypeLoaded = baseSetLoaded.getMemberType();
        IMetaType metaTypeForSave = baseSetForSave.getMemberType();
        if (!metaTypeLoaded.equals(metaTypeForSave))
        {
            throw new IllegalArgumentException("Applying can not be executed on set with different metadata.");
        }

        if (metaTypeLoaded.isSet())
        {
            throw new UnsupportedOperationException("Not yet implemented.");
        }
        else
        {
            if (metaTypeLoaded.isComplex())
            {
                Set<String> identifiersForSave = new HashSet<String>();
                Set<String> identifiersLoaded = new HashSet<String>();
                for (String identifierForSave : baseSetForSave.getIdentifiers())
                {
                    boolean find = false;

                    IBaseValue baseValueForSave = baseSetForSave.getBaseValue(identifierForSave);
                    for (String identifierLoaded : baseSetLoaded.getIdentifiers())
                    {
                        IBaseValue baseValueLoaded = baseSetLoaded.getBaseValue(identifierLoaded);
                        if (identifiersLoaded.contains(identifierLoaded))
                        {
                            continue;
                        }
                        else
                        {
                            IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();
                            IBaseEntity baseEntityForSave = (IBaseEntity) baseValueForSave.getValue();

                            long baseEntityLoadedId = baseEntityLoaded.getId();
                            long baseEntityForSaveId = baseEntityForSave.getId();

                            if (baseEntityLoadedId >= 1 && baseEntityForSaveId >= 1 && baseEntityLoadedId == baseEntityForSaveId)
                            {
                                IBaseEntity baseEntity = apply(((BaseEntity)baseValueForSave.getValue()).clone());
                                baseValueLoaded.setValue(baseEntity);

                                identifiersLoaded.add(identifierLoaded);
                                break;
                            }
                        }
                    }

                    if (find)
                    {
                        identifiersForSave.add(identifierForSave);
                    }
                }

                for (String identifier: SetUtils.difference(baseSetLoaded.getIdentifiers(), identifiersLoaded))
                {
                    IBaseValue baseValue = baseSetLoaded.getBaseValue(identifier);
                    baseValue.setClosed(true);
                    baseValue.setLast(false);
                }

                for (String identifier: SetUtils.difference(baseSetForSave.getIdentifiers(), identifiersForSave))
                {
                    IBaseValue baseValue = ((BaseValue)baseSetForSave.getBaseValue(identifier)).clone();
                    baseValue.setLast(true);

                    if (metaTypeLoaded.isComplex())
                    {
                        IBaseEntity baseEntity = apply((BaseEntity)baseValue.getValue());
                        baseValue.setValue(baseEntity);
                    }

                    baseSetLoaded.put(baseValue);
                }
            }
            else
            {
                throw new UnsupportedOperationException("Not yet implemented.");
            }
        }
        return baseSetLoaded;
    }

    private boolean presentInFuture(IBaseEntity baseEntity, String attribute)
    {
        boolean presentInFuture = false;
        IMetaType metaType = baseEntity.getMemberType(attribute);
        if (metaType.isSet())
        {
            // TODO: Write code that implements this situation
        }
        else
        {
            if (metaType.isComplex())
            {
                // TODO: Write code that implements this situation
            }
            else
            {
                IMetaValue metaValue = (IMetaValue)metaType;
                switch (metaValue.getTypeCode())
                {
                    case INTEGER:
                        presentInFuture = beIntegerValueDao.presentInFuture(baseEntity, attribute);
                        break;
                    case DATE:
                        presentInFuture = beDateValueDao.presentInFuture(baseEntity, attribute);
                        break;
                    case STRING:
                        presentInFuture = beStringValueDao.presentInFuture(baseEntity, attribute);
                        break;
                    case BOOLEAN:
                        presentInFuture = beBooleanValueDao.presentInFuture(baseEntity, attribute);
                        break;
                    case DOUBLE:
                        presentInFuture = beDoubleValueDao.presentInFuture(baseEntity, attribute);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown data type.");
                }

            }
        }
        return presentInFuture;
    }

    @Override
    @Transactional
    public IBaseEntity saveOrUpdate(IBaseEntity baseEntity)
    {
        if (baseEntity.getId() == 0)
        {
            return save(baseEntity);
        }
        else
        {
            IBaseEntity baseEntityLoaded = beStorageDao.getBaseEntity(baseEntity.getId(), true);
            return update(baseEntity, baseEntityLoaded);
        }
    }

    @Override
    @Transactional
    public IBaseEntity process(IBaseEntity baseEntity)
    {
        baseEntity = prepare(baseEntity);
        baseEntity = apply(baseEntity);
        baseEntity = saveOrUpdate(baseEntity);

        Long userId = ((BaseValue)((baseEntity.get().toArray())[0])).getBatch().getUserId();
        String str = baseEntity.getMeta().getClassName();

        Insert insert = context
                .insertInto(AUDIT_EVENT)
                .set(AUDIT_EVENT.USER_ID, userId)
                .set(AUDIT_EVENT.KIND_ID, 1L)
                .set(AUDIT_EVENT.TABLE_NAME, str)
                .set(AUDIT_EVENT.ADD_INFO, "test")
                .set(AUDIT_EVENT.EVENT_BEGIN_D, new Date(Calendar.getInstance().getTimeInMillis()))
                .set(AUDIT_EVENT.EVENT_BEGIN_DT, new Timestamp(Calendar.getInstance().getTimeInMillis()))
                .set(AUDIT_EVENT.EVENT_END_DT, new Timestamp(Calendar.getInstance().getTimeInMillis()))
                .set(AUDIT_EVENT.IS_SUCCESS, 1L);


        insertWithId(insert.getSQL(), insert.getBindValues().toArray());



        //((BaseValue)((baseEntity.get().toArray())[0])).getBatch().getUserId() -- userId
        //baseEntity.getMeta().getClassName() -- table
        // TODO: Make an automatic update cache
        beStorageDao.clean();

        return baseEntity;
    }

    @Transactional
    public IBaseEntity update(IBaseEntity baseEntityForSave, IBaseEntity baseEntityLoaded)
    {
        if (baseEntityForSave.getModifiedIdentifiers().size() == 0)
        {
            return baseEntityForSave;
        }

        TreeSet<String> modifiedAttributes = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String thisString, String thatString) {
                return thisString.compareTo(thatString);
            }
        });
        modifiedAttributes.addAll(baseEntityForSave.getModifiedIdentifiers());

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
                    //throw new UnsupportedOperationException("Not yet implemented.");
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
                    //throw new UnsupportedOperationException("Not yet implemented.");
                }
                else
                {
                    IMetaValue metaValue =  (IMetaValue)metaType;
                    switch(metaValue.getTypeCode())
                    {
                        case INTEGER:
                        {
                            IBaseValue baseValue = beIntegerValueDao
                                    .update(parentBaseEntityLoaded, parentBaseEntityForSave, childAttribute);
                            baseEntityForSave.put(childAttribute, baseValue);
                            break;
                        }
                        case DATE:
                        {
                            IBaseValue baseValue = beDateValueDao
                                    .update(parentBaseEntityLoaded, parentBaseEntityForSave, childAttribute);
                            baseEntityForSave.put(childAttribute, baseValue);
                            break;
                        }
                        case STRING:
                        {
                            IBaseValue baseValue = beStringValueDao
                                    .update(parentBaseEntityLoaded, parentBaseEntityForSave, childAttribute);
                            baseEntityForSave.put(childAttribute, baseValue);
                            break;
                        }
                        case BOOLEAN:
                        {
                            IBaseValue baseValue = beBooleanValueDao
                                    .update(parentBaseEntityLoaded, parentBaseEntityForSave, childAttribute);
                            baseEntityForSave.put(childAttribute, baseValue);
                            break;
                        }
                        case DOUBLE:
                        {
                            IBaseValue baseValue = beDoubleValueDao
                                    .update(parentBaseEntityLoaded, parentBaseEntityForSave, childAttribute);
                            baseEntityForSave.put(childAttribute, baseValue);
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("Unknown data type.");
                    }
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

        Set<String> attributes = baseEntity.getIdentifiers();

        Set<String> integerValues = new HashSet<String>();
        Set<String> dateValues = new HashSet<String>();
        Set<String> stringValues = new HashSet<String>();
        Set<String> booleanValues = new HashSet<String>();
        Set<String> doubleValues = new HashSet<String>();
        Set<String> complexValues = new HashSet<String>();
        Set<String> simpleSets = new HashSet<String>();
        Set<String> complexSets = new HashSet<String>();

        Iterator<String> it = attributes.iterator();
        while (it.hasNext())
        {
            String attribute = it.next();

            IMetaType metaType = baseEntity.getMemberType(attribute);
            if (metaType.isSet())
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
            for (String integerValue: integerValues)
            {
                IBaseValue baseValue = beIntegerValueDao.save(baseEntity, integerValue);
                baseEntity.put(integerValue, baseValue);
            }
        }

        if (dateValues.size() != 0)
        {
            for (String dateValue: dateValues)
            {
                IBaseValue baseValue = beDateValueDao.save(baseEntity, dateValue);
                baseEntity.put(dateValue, baseValue);
            }
        }

        if (stringValues.size() != 0)
        {
            for (String stringValue : stringValues)
            {
                IBaseValue baseValue = beStringValueDao.save(baseEntity, stringValue);
                baseEntity.put(stringValue, baseValue);
            }
        }

        if (booleanValues.size() != 0)
        {
            for (String booleanValue : booleanValues)
            {
                IBaseValue baseValue = beBooleanValueDao.save(baseEntity, booleanValue);
                baseEntity.put(booleanValue, baseValue);
            }
        }

        if (doubleValues.size() != 0)
        {
            for (String doubleValue : doubleValues)
            {
                IBaseValue baseValue = beDoubleValueDao.save(baseEntity, doubleValue);
                baseEntity.put(doubleValue, baseValue);
            }
        }

        if (complexValues.size() != 0)
        {
            beComplexValueDao.save(baseEntity, complexValues);
        }

        if (simpleSets.size() != 0)
        {
            //beSetValueDao.save(baseEntity, simpleSets);
        }

        if (complexSets.size() != 0)
        {
            beComplexSetValueDao.save(baseEntity, complexSets);
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
                complexSets.size());

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

            Set<String> attributes = baseEntity.getIdentifiers();
            Iterator<String> attributeIt = attributes.iterator();
            while (attributeIt.hasNext())
            {
                String attribute = attributeIt.next();
                IMetaType metaType = metaClass.getMemberType(attribute);
                if (metaType.isSet())
                {
                    IBaseValue baseValue = baseEntity.getBaseValue(attribute);
                    Set<BaseEntity> baseEntitiesForRemove = collectComplexSetValues((BaseSet)baseValue.getValue());

                    //beSetValueDao.remove(baseEntity, attribute);

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
                        //beSimpleValueDao.remove(baseEntity, attribute);
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
            reportDates.add(DataUtils.convert((Timestamp) row.get(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.getName())));
        }

        return reportDates;
    }

    public java.util.Date getMinReportDate(long baseEntityId)
    {
        Select select = context
                .select(DSL.min(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("min_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("min_report_date"));
    }

    public java.util.Date getMaxReportDate(long baseEntityId, java.util.Date reportDate)
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

    public java.util.Date getMaxReportDate(long baseEntityId)
    {
        Select select = context
                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("max_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("max_report_date"));
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
            long complexSetsCount)
    {
        Insert insert = context
                .insertInto(EAV_BE_ENTITY_REPORT_DATES)
                .set(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_ENTITY_REPORT_DATES.INTEGER_VALUES_COUNT, integerValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.DATE_VALUES_COUNT, dateValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.STRING_VALUES_COUNT, stringValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.BOOLEAN_VALUES_COUNT, booleanValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.DOUBLE_VALUES_COUNT, doubleValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_VALUES_COUNT, complexValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.SIMPLE_SETS_COUNT, simpleSetsCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_SETS_COUNT, complexSetsCount);

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
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.equal(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.equal(DataUtils.convert(reportDate)));

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

        for (String attribute: baseEntity.getIdentifiers())
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
                    complexSetsCount);
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

    private void loadIntegerValues(BaseEntity baseEntity, boolean withClosedValues)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_INTEGER_VALUES.as("v");
        Select select = null;

        if (baseEntity.getReportDate().equals(baseEntity.getMaxReportDate()))
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
                    .and(withClosedValues ?
                            (tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_LAST).equal(true)
                                    .and(tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_CLOSED).equal(false)))
                                    .or(tableOfValues.field(EAV_BE_INTEGER_VALUES.REPORT_DATE).equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_CLOSED).equal(true))) :
                            tableOfValues.field(EAV_BE_INTEGER_VALUES.IS_LAST).equal(true)
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
                    .and(withClosedValues ?
                            tableNumbering.field(EAV_BE_INTEGER_VALUES.IS_CLOSED).equal(false)
                                    .or(tableNumbering.field(EAV_BE_INTEGER_VALUES.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableNumbering.field(EAV_BE_INTEGER_VALUES.IS_CLOSED).equal(true))) :
                            tableNumbering.field(EAV_BE_INTEGER_VALUES.IS_CLOSED).equal(false));
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

    private void loadDateValues(BaseEntity baseEntity, boolean withClosedValues)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_DATE_VALUES.as("v");
        Select select = null;

        if (baseEntity.getReportDate().equals(baseEntity.getMaxReportDate()))
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
                    .and(withClosedValues ?
                            (tableOfValues.field(EAV_BE_DATE_VALUES.IS_LAST).equal(true)
                                    .and(tableOfValues.field(EAV_BE_DATE_VALUES.IS_CLOSED).equal(false)))
                                    .or(tableOfValues.field(EAV_BE_DATE_VALUES.REPORT_DATE).equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableOfValues.field(EAV_BE_DATE_VALUES.IS_CLOSED).equal(true))) :
                            tableOfValues.field(EAV_BE_DATE_VALUES.IS_LAST).equal(true)
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
                    .and(withClosedValues ?
                            tableNumbering.field(EAV_BE_DATE_VALUES.IS_CLOSED).equal(false)
                                    .or(tableNumbering.field(EAV_BE_DATE_VALUES.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableNumbering.field(EAV_BE_DATE_VALUES.IS_CLOSED).equal(true))) :
                            tableNumbering.field(EAV_BE_DATE_VALUES.IS_CLOSED).equal(false));
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

    private void loadBooleanValues(BaseEntity baseEntity, boolean withClosedValues)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_BOOLEAN_VALUES.as("v");
        Select select = null;

        if (baseEntity.getReportDate().equals(baseEntity.getMaxReportDate()))
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
                    .and(withClosedValues ?
                            (tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_LAST).equal(true)
                                    .and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED).equal(false)))
                                    .or(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE).equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED).equal(true))) :
                            tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_LAST).equal(true)
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
                    .and(withClosedValues ?
                            tableNumbering.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED).equal(false)
                                    .or(tableNumbering.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableNumbering.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED).equal(true))) :
                            tableNumbering.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED).equal(false));
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

    private void loadStringValues(BaseEntity baseEntity, boolean withClosedValues)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_STRING_VALUES.as("v");
        Select select = null;

        if (baseEntity.getReportDate().equals(baseEntity.getMaxReportDate()))
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
                    .and(withClosedValues ?
                            (tableOfValues.field(EAV_BE_STRING_VALUES.IS_LAST).equal(true)
                                    .and(tableOfValues.field(EAV_BE_STRING_VALUES.IS_CLOSED).equal(false)))
                                    .or(tableOfValues.field(EAV_BE_STRING_VALUES.REPORT_DATE).equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableOfValues.field(EAV_BE_STRING_VALUES.IS_CLOSED).equal(true))) :
                            tableOfValues.field(EAV_BE_STRING_VALUES.IS_LAST).equal(true)
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
                    .and(withClosedValues ?
                            tableNumbering.field(EAV_BE_STRING_VALUES.IS_CLOSED).equal(false)
                                    .or(tableNumbering.field(EAV_BE_STRING_VALUES.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableNumbering.field(EAV_BE_STRING_VALUES.IS_CLOSED).equal(true))) :
                            tableNumbering.field(EAV_BE_STRING_VALUES.IS_CLOSED).equal(false));
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

    private void loadDoubleValues(BaseEntity baseEntity, boolean withClosedValues)
    {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_DOUBLE_VALUES.as("v");
        Select select = null;

        if (baseEntity.getReportDate().equals(baseEntity.getMaxReportDate()))
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
                    .and(withClosedValues ?
                            (tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_LAST).equal(true)
                                    .and(tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED).equal(false)))
                                    .or(tableOfValues.field(EAV_BE_DOUBLE_VALUES.REPORT_DATE).equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED).equal(true))) :
                            tableOfValues.field(EAV_BE_DOUBLE_VALUES.IS_LAST).equal(true)
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
                    .and(withClosedValues ?
                            tableNumbering.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED).equal(false)
                                    .or(tableNumbering.field(EAV_BE_DOUBLE_VALUES.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableNumbering.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED).equal(true))) :
                            tableNumbering.field(EAV_BE_DOUBLE_VALUES.IS_CLOSED).equal(false));
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

    private void loadComplexValues(BaseEntity baseEntity, boolean withClosedValues)
    {
        Table tableOfAttributes = EAV_M_COMPLEX_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_COMPLEX_VALUES.as("v");
        Select select = null;

        if (baseEntity.getReportDate().equals(baseEntity.getMaxReportDate()))
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
                    .and(withClosedValues ?
                            (tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_LAST).equal(true)
                                    .and(tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED).equal(false)))
                                    .or(tableOfValues.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE).equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED).equal(true))) :
                            tableOfValues.field(EAV_BE_COMPLEX_VALUES.IS_LAST).equal(true)
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
                    .and(withClosedValues ?
                            tableNumbering.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED).equal(false)
                                    .or(tableNumbering.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableNumbering.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED).equal(true))) :
                            tableNumbering.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_COMPLEX_VALUES.BATCH_ID.getName())).longValue());
            long entityValueId = ((BigDecimal)row.get(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.getName())).longValue();
            IBaseEntity childBaseEntity = beStorageDao.getBaseEntity(entityValueId, withClosedValues);

            baseEntity.put(
                    (String) row.get(EAV_M_COMPLEX_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            ((BigDecimal) row.get(EAV_BE_COMPLEX_VALUES.ID.getName())).longValue(),
                            batch,
                            ((BigDecimal) row.get(EAV_BE_COMPLEX_VALUES.INDEX_.getName())).longValue(),
                            DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_COMPLEX_VALUES.REPORT_DATE.getName())),
                            childBaseEntity,
                            ((BigDecimal)row.get(EAV_BE_COMPLEX_VALUES.IS_CLOSED.getName())).longValue() == 1,
                            ((BigDecimal)row.get(EAV_BE_COMPLEX_VALUES.IS_LAST.getName())).longValue() == 1));
        }
    }

    private void loadEntitySimpleSets(BaseEntity baseEntity, boolean withClosedValues)
    {
        Table tableOfSimpleSets = EAV_M_SIMPLE_SET.as("ss");
        Table tableOfEntitySimpleSets = EAV_BE_ENTITY_SIMPLE_SETS.as("ess");
        Select select = null;

        if (baseEntity.getReportDate().equals(baseEntity.getMaxReportDate()))
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
                    .and(withClosedValues ?
                            (tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST).equal(true)
                                    .and(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED).equal(false)))
                                    .or(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED).equal(true))) :
                            tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST).equal(true)
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
                    .and(withClosedValues ?
                            tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED).equal(false)
                                    .or(tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED).equal(true))) :
                            tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED).equal(false));
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
                loadComplexSetValues(baseSet, withClosedValues);
            }
            else
            {
                loadSimpleSetValues(baseSet, withClosedValues);
            }

            Batch batch = batchRepository.getBatch(batchId);
            baseEntity.put(attribute, new BaseValue(baseValueId, batch, index, reportDate, baseSet));
        }
    }

    private void loadEntityComplexSets(BaseEntity baseEntity, boolean withClosedValues)
    {
        Table tableOfComplexSets = EAV_M_COMPLEX_SET.as("cs");
        Table tableOfEntityComplexSets = EAV_BE_ENTITY_COMPLEX_SETS.as("ecs");
        Select select = null;

        if (baseEntity.getReportDate().equals(baseEntity.getMaxReportDate()))
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
                    .and(withClosedValues ?
                            (tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST).equal(true)
                                    .and(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED).equal(false)))
                                    .or(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED).equal(true))) :
                            tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST).equal(true)
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
                    .and(withClosedValues ?
                            tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED).equal(false)
                                    .or(tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE)
                                            .equal(DataUtils.convert(baseEntity.getReportDate()))
                                            .and(tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED).equal(true))) :
                            tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED).equal(false));
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
                loadComplexSetValues(baseSet, withClosedValues);
            }
            else
            {
                loadSimpleSetValues(baseSet, withClosedValues);
            }

            Batch batch = batchRepository.getBatch(batchId);
            baseEntity.put(attribute, new BaseValue(baseValueId, batch, index, reportDate, baseSet));
        }
    }

    private void loadSetOfSimpleSets(IBaseSet baseSet, boolean withClosedValues)
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
                loadComplexSetValues(baseSetChild, withClosedValues);
            }
            else
            {
                loadSimpleSetValues(baseSetChild, withClosedValues);
            }

            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_SET_OF_SIMPLE_SETS.BATCH_ID.getName())).longValue());
            baseSet.put(new BaseValue(batch, ((BigDecimal)row.get(EAV_BE_SET_OF_SIMPLE_SETS.INDEX_.getName())).longValue(),
                    DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_SET_OF_SIMPLE_SETS.REPORT_DATE.getName())), baseSetChild));
        }
    }

    private void loadSetOfComplexSets(IBaseSet baseSet, boolean withClosedValues)
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
                loadComplexSetValues(baseSetChild, withClosedValues);
            }
            else
            {
                loadSimpleSetValues(baseSetChild, withClosedValues);
            }


            Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_SET_OF_COMPLEX_SETS.BATCH_ID.getName())).longValue());
            baseSet.put(new BaseValue(batch, ((BigDecimal)row.get(EAV_BE_SET_OF_COMPLEX_SETS.INDEX_.getName())).longValue(),
                    DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_SET_OF_COMPLEX_SETS.REPORT_DATE.getName())), baseSetChild));
        }
    }

    private void loadSimpleSetValues(IBaseSet baseSet, boolean withClosedValues)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (metaType.isComplex())
            throw new RuntimeException("Load the simple set values is not possible. " +
                    "Simple values ??can not be added to an set of complex values.");

        if (metaType.isSet())
        {
            loadSetOfSimpleSets(baseSet, withClosedValues);
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

    private void loadComplexSetValues(IBaseSet baseSet, boolean withClosedValues)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (!metaType.isComplex())
            throw new RuntimeException("Load the complex set values is not possible. " +
                    "Complex values ??can not be added to an set of simple values.");

        if (metaType.isSet())
        {
            loadSetOfComplexSets(baseSet, withClosedValues);
        }
        else
        {
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

                Batch batch = batchRepository.getBatch(((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.BATCH_ID.getName())).longValue());
                IBaseEntity baseEntity = beStorageDao.getBaseEntity(
                        ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.getName())).longValue(), withClosedValues);
                baseSet.put(
                        new BaseValue(
                                batch,
                                ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.INDEX_.getName())).longValue(),
                                DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE.getName())),
                                baseEntity,
                                ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED.getName())).longValue() == 1,
                                ((BigDecimal)row.get(EAV_BE_COMPLEX_SET_VALUES.IS_LAST.getName())).longValue() == 1));
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
