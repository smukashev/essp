package kz.bsbnb.usci.eav.postgresql.dao;

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
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.impl.searcher.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.DateUtils;
import org.jooq.*;
import org.jooq.impl.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * @author a.motov
 */
@Repository
public class PostgreSQLBaseEntityDaoImpl extends JDBCSupport implements IBaseEntityDao
{
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBaseEntityDaoImpl.class);

    private String DELETE_ENTITY_BY_ID_SQL;

    @Autowired
    IBatchRepository batchRepository;

    @Autowired
    IMetaClassRepository metaClassRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private Executor sqlGenerator;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    @PostConstruct
    public void init()
    {
        DELETE_ENTITY_BY_ID_SQL = String.format("DELETE FROM %s WHERE id = ?", getConfig().getEntitiesTableName());
    }

    @Override
    public BaseEntity load(long id)
    {
        if(id < 1)
        {
            throw new IllegalArgumentException("Does not have id. Can't load.");
        }

        SelectForUpdateStep select = sqlGenerator
                .select(EAV_ENTITIES.ID, EAV_ENTITIES.CLASS_ID)
                .from(EAV_ENTITIES)
                .where(EAV_ENTITIES.ID.equal(id));

        logger.debug(select.toString());

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one base entity found. Can't load.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalArgumentException("Class not found. Can't load.");
        }

        Map<String, Object> row = rows.get(0);
        BaseEntity baseEntity = null;

        if(row != null)
        {
            MetaClass meta = metaClassRepository.getMetaClass((Long)row.get(EAV_ENTITIES.CLASS_ID.getName()));

            baseEntity = new BaseEntity(meta);
            baseEntity.setId((Long)row.get(EAV_ENTITIES.ID.getName()));
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

    @Override
    @Transactional
    public void update(BaseEntity baseEntity) {
        MetaClass meta = baseEntity.getMeta();
        List<Long> baseEntities = searcherPool.getSearcher(meta.getClassName())
                .findAll(baseEntity);

        if (baseEntities.isEmpty())
        {
            save(baseEntity);
            return;
        }

        BaseEntity baseEntityLoad = load(baseEntities.get(0));

        Map<DataTypes, Set<String>> removeSimpleAttributes = new HashMap<DataTypes, Set<String>>();
        Map<DataTypes, Set<String>> updateSimpleAttributes = new HashMap<DataTypes, Set<String>>();
        Map<DataTypes, Set<String>> insertSimpleAttributes = new HashMap<DataTypes, Set<String>>();

        Set<String> attributes = baseEntity.getAttributeNames();
        Set<String> attributesLoad = baseEntityLoad.getAttributeNames();

        Iterator<String> it = attributes.iterator();
        while (it.hasNext())
        {
            String attribute = it.next();
            IMetaType metaType = meta.getMemberType(attribute);

            //TODO: Whether first to collect the attributes, and then execute query
            if (metaType.isSet())
            {
                //TODO: Implement this functionality
            }
            else
            {
                if (metaType.isComplex())
                {
                    //TODO: Implement this functionality
                }
                else
                {
                    IBaseValue baseValue = baseEntity.getBaseValue(attribute);
                    if (baseValue.getValue() == null)
                    {
                        DataTypes type = ((MetaValue)metaType).getTypeCode();
                        if (removeSimpleAttributes.containsKey(type))
                        {
                            removeSimpleAttributes.get(type).add(attribute);
                        }
                        else
                        {
                            Set<String> removeAttributes = new HashSet<String>();
                            removeAttributes.add(attribute);

                            removeSimpleAttributes.put(type, removeAttributes);
                        }
                    }
                    else
                    {
                        if (attributesLoad.contains(attribute))
                        {
                            IBaseValue baseValueLoad = baseEntityLoad.getBaseValue(attribute);
                            if (!baseValue.getValue().equals(baseValueLoad.getValue()))
                            {
                                DataTypes type = ((MetaValue)metaType).getTypeCode();
                                if (updateSimpleAttributes.containsKey(type))
                                {
                                    updateSimpleAttributes.get(type).add(attribute);
                                }
                                else
                                {
                                    Set<String> updateAttributes = new HashSet<String>();
                                    updateAttributes.add(attribute);

                                    updateSimpleAttributes.put(type, updateAttributes);
                                }

                                // Put value for update in the loaded BaseEntity
                                baseEntityLoad.put(attribute, baseValue);
                            }
                        }
                        else
                        {
                            DataTypes type = ((MetaValue)metaType).getTypeCode();
                            if (insertSimpleAttributes.containsKey(type))
                            {
                                insertSimpleAttributes.get(type).add(attribute);
                            }
                            else
                            {
                                Set<String> insertAttributes = new HashSet<String>();
                                insertAttributes.add(attribute);

                                insertSimpleAttributes.put(type, insertAttributes);
                            }

                            // Put value for insert in the loaded BaseEntity
                            baseEntityLoad.put(attribute, baseValue);
                        }
                    }
                }
            }
        }

        // insert simple values (bulk insert)
        if (!insertSimpleAttributes.isEmpty())
        {
            for (DataTypes dataType: insertSimpleAttributes.keySet())
            {
                insertSimpleValues(baseEntityLoad, insertSimpleAttributes.get(dataType), dataType);
            }
        }

        // remove simple values
        if (!updateSimpleAttributes.isEmpty())
        {
            for (DataTypes dataType: updateSimpleAttributes.keySet())
            {
                for (String attribute: updateSimpleAttributes.get(dataType))
                {
                    updateSimpleAttribute(baseEntityLoad, attribute);
                }
            }
        }

        // remove simple values
        if (!removeSimpleAttributes.isEmpty())
        {
            for (DataTypes dataType: removeSimpleAttributes.keySet())
            {
                for (String attribute: removeSimpleAttributes.get(dataType))
                {
                    removeSimpleAttribute(baseEntityLoad, attribute);
                    //TODO: Add remove attribute from the BaseEntity
                }
            }
        }
    }

    private void removeSimpleAttribute(BaseEntity baseEntity, String attribute) {
        MetaClass meta = baseEntity.getMeta();
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        IMetaType metaType = metaAttribute.getMetaType();

        long metaAttributeId =  metaAttribute.getId();
        long baseEntityId = baseEntity.getId();

        DeleteConditionStep delete;
        switch(((MetaValue)metaType).getTypeCode())
        {
            case INTEGER: {
                delete = sqlGenerator
                        .delete(EAV_BE_INTEGER_VALUES)
                        .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case DATE: {
                delete = sqlGenerator
                        .delete(EAV_BE_DATE_VALUES)
                        .where(EAV_BE_DATE_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case STRING: {
                delete = sqlGenerator
                        .delete(EAV_BE_STRING_VALUES)
                        .where(EAV_BE_STRING_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case BOOLEAN: {
                delete = sqlGenerator
                        .delete(EAV_BE_BOOLEAN_VALUES)
                        .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case DOUBLE: {
                delete = sqlGenerator
                        .delete(EAV_BE_DOUBLE_VALUES)
                        .where(EAV_BE_DOUBLE_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type.");
        }

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }

    private void updateSimpleAttribute(BaseEntity baseEntity, String attribute) {
        MetaClass meta = baseEntity.getMeta();
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        IMetaType metaType = metaAttribute.getMetaType();

        long metaAttributeId =  metaAttribute.getId();
        long baseEntityId = baseEntity.getId();

        IBaseValue baseValue = baseEntity.getBaseValue(attribute);

        UpdateConditionStep update;
        switch(((MetaValue)metaType).getTypeCode())
        {
            case INTEGER: {
                update = sqlGenerator
                        .update(EAV_BE_INTEGER_VALUES)
                        .set(EAV_BE_INTEGER_VALUES.VALUE, (Integer)baseValue.getValue())
                        .set(EAV_BE_INTEGER_VALUES.BATCH_ID, baseValue.getBatch().getId())
                        .set(EAV_BE_INTEGER_VALUES.INDEX, baseValue.getIndex())
                        .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case DATE: {
                update = sqlGenerator
                        .update(EAV_BE_DATE_VALUES)
                        .set(EAV_BE_DATE_VALUES.VALUE, DateUtils.convert((java.util.Date)baseValue.getValue()))
                        .set(EAV_BE_DATE_VALUES.BATCH_ID, baseValue.getBatch().getId())
                        .set(EAV_BE_DATE_VALUES.INDEX, baseValue.getIndex())
                        .where(EAV_BE_DATE_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case STRING: {
                update = sqlGenerator
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_STRING_VALUES.VALUE, (String)baseValue.getValue())
                        .set(EAV_BE_STRING_VALUES.BATCH_ID, baseValue.getBatch().getId())
                        .set(EAV_BE_STRING_VALUES.INDEX, baseValue.getIndex())
                        .where(EAV_BE_STRING_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case BOOLEAN: {
                update = sqlGenerator
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_BOOLEAN_VALUES.VALUE, (Boolean)baseValue.getValue())
                        .set(EAV_BE_BOOLEAN_VALUES.BATCH_ID, baseValue.getBatch().getId())
                        .set(EAV_BE_BOOLEAN_VALUES.INDEX, baseValue.getIndex())
                        .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case DOUBLE: {
                update = sqlGenerator
                        .update(EAV_BE_DOUBLE_VALUES)
                        .set(EAV_BE_DOUBLE_VALUES.VALUE, (Double)baseValue.getValue())
                        .set(EAV_BE_DOUBLE_VALUES.BATCH_ID, baseValue.getBatch().getId())
                        .set(EAV_BE_DOUBLE_VALUES.INDEX, baseValue.getIndex())
                        .where(EAV_BE_DOUBLE_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type.");
        }

        logger.debug(update.toString());
        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }

    @Override
    @Transactional
    public long save(BaseEntity baseEntity)
    {
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
                insertEntitySet(baseEntity, attributeName);
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

                    if (simpleAttributeNames.containsKey(type))
                    {
                        simpleAttributeNames.get(type).add(attributeName);
                    }
                    else
                    {
                        Set<String> attributes = new HashSet<String>();
                        attributes.add(attributeName);

                        simpleAttributeNames.put(type, attributes);
                    }
                }
            }
        }

        for (DataTypes dataType: DataTypes.values())
        {
            if (simpleAttributeNames.containsKey(dataType))
            {
                insertSimpleValues(baseEntity, simpleAttributeNames.get(dataType), dataType);
            }
        }

        if (!complexAttributeNames.isEmpty())
        {
            insertComplexValues(baseEntity, complexAttributeNames);
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

        updateWithStats(DELETE_ENTITY_BY_ID_SQL, baseEntity.getId());
    }

    private long insertBaseEntity(BaseEntity baseEntity)
    {
        if(baseEntity.getMeta().getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before entity insertion to DB.");
        }

        InsertOnDuplicateStep insert = sqlGenerator
                .insertInto(EAV_ENTITIES, EAV_ENTITIES.CLASS_ID)
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

    private void insertSimpleValues(BaseEntity baseEntity, Set<String> attributeNames, DataTypes dataType)
    {
        MetaClass meta = baseEntity.getMeta();

        InsertValuesStep6 insert;
        switch(dataType)
        {
            case INTEGER: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_INTEGER_VALUES,
                        EAV_BE_INTEGER_VALUES.ENTITY_ID,
                        EAV_BE_INTEGER_VALUES.BATCH_ID,
                        EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID,
                        EAV_BE_INTEGER_VALUES.INDEX,
                        EAV_BE_INTEGER_VALUES.REP_DATE,
                        EAV_BE_INTEGER_VALUES.VALUE);
                break;
            }
            case DATE: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_DATE_VALUES,
                        EAV_BE_DATE_VALUES.ENTITY_ID,
                        EAV_BE_DATE_VALUES.BATCH_ID,
                        EAV_BE_DATE_VALUES.ATTRIBUTE_ID,
                        EAV_BE_DATE_VALUES.INDEX,
                        EAV_BE_DATE_VALUES.REP_DATE,
                        EAV_BE_DATE_VALUES.VALUE);
                break;
            }
            case STRING: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_STRING_VALUES,
                        EAV_BE_STRING_VALUES.ENTITY_ID,
                        EAV_BE_STRING_VALUES.BATCH_ID,
                        EAV_BE_STRING_VALUES.ATTRIBUTE_ID,
                        EAV_BE_STRING_VALUES.INDEX,
                        EAV_BE_STRING_VALUES.REP_DATE,
                        EAV_BE_STRING_VALUES.VALUE);
                break;
            }
            case BOOLEAN: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_BOOLEAN_VALUES,
                        EAV_BE_BOOLEAN_VALUES.ENTITY_ID,
                        EAV_BE_BOOLEAN_VALUES.BATCH_ID,
                        EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID,
                        EAV_BE_BOOLEAN_VALUES.INDEX,
                        EAV_BE_BOOLEAN_VALUES.REP_DATE,
                        EAV_BE_BOOLEAN_VALUES.VALUE);
                break;
            }
            case DOUBLE: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_DOUBLE_VALUES,
                        EAV_BE_DOUBLE_VALUES.ENTITY_ID,
                        EAV_BE_DOUBLE_VALUES.BATCH_ID,
                        EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID,
                        EAV_BE_DOUBLE_VALUES.INDEX,
                        EAV_BE_DOUBLE_VALUES.REP_DATE,
                        EAV_BE_DOUBLE_VALUES.VALUE);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type.");
        }

        Iterator<String> it = attributeNames.iterator();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeNameForInsert);

            IBaseValue batchValue = baseEntity.getBaseValue(attributeNameForInsert);

            Object[] insertArgs = new Object[] {
                    baseEntity.getId(),
                    batchValue.getBatch().getId(),
                    metaAttribute.getId(),
                    batchValue.getIndex(),
                    batchValue.getRepDate(),
                    batchValue.getValue()
            };
            insert = insert.values(Arrays.asList(insertArgs));
        }

        logger.debug(insert.toString());
        batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
    }

    private void updateSimpleValues(BaseEntity baseEntity, Set<String> attributeNames, String query) {

    }

    private void insertComplexValues(BaseEntity baseEntity, Set<String> attributeNames)
    {
        MetaClass meta = baseEntity.getMeta();

        InsertValuesStep6 insert = sqlGenerator
                .insertInto(
                        EAV_BE_COMPLEX_VALUES,
                        EAV_BE_COMPLEX_VALUES.ENTITY_ID,
                        EAV_BE_COMPLEX_VALUES.BATCH_ID,
                        EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID,
                        EAV_BE_COMPLEX_VALUES.INDEX,
                        EAV_BE_COMPLEX_VALUES.REP_DATE,
                        EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID);

        Iterator<String> it = attributeNames.iterator();
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            IBaseValue batchValue = baseEntity.getBaseValue(attributeNameForInsert);
            IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeNameForInsert);

            long childBaseEntityId = save((BaseEntity) batchValue.getValue());

            Object[] insertArgs = new Object[] {baseEntity.getId(), batchValue.getBatch().getId(),
                    metaAttribute.getId(), batchValue.getIndex(), batchValue.getRepDate(), childBaseEntityId};

            insert = insert.values(Arrays.asList(insertArgs));
            batchArgs.add(insertArgs);
        }

        logger.debug(insert.toString());
        batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
    }

    private void insertEntitySet(BaseEntity baseEntity, String attributeName)
    {
        MetaClass meta = baseEntity.getMeta();
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeName);
        IMetaType metaType = metaAttribute.getMetaType();
        MetaSet metaSet = (MetaSet)metaType;

        if (metaAttribute.getId() < 1)
        {
            throw new IllegalArgumentException("MetaAttribute does not contain an id. " +
                    "The set can not be saved.");
        }

        IBaseValue baseValue = baseEntity.getBaseValue(attributeName);
        BaseSet baseSet = (BaseSet)baseValue.getValue();
        IMetaType metaTypeValue = baseSet.getMemberType();

        long setId;
        if (metaTypeValue.isComplex())
        {
            setId = insertComplexSet(baseValue, metaSet);
        }
        else
        {
            setId = insertSimpleSet(baseValue, metaSet);
        }

        InsertOnDuplicateStep insert;
        if (metaTypeValue.isSet())
        {
            insert = sqlGenerator
                    .insertInto(
                            EAV_BE_ENTITY_SET_OF_SETS,
                            EAV_BE_ENTITY_SET_OF_SETS.ENTITY_ID,
                            EAV_BE_ENTITY_SET_OF_SETS.ATTRIBUTE_ID,
                            EAV_BE_ENTITY_SET_OF_SETS.SET_ID)
                    .values(baseEntity.getId(), metaAttribute.getId(), setId);
        }
        else
        {
            if (metaTypeValue.isComplex())
            {
                insert = sqlGenerator
                        .insertInto(
                                EAV_BE_ENTITY_COMPLEX_SETS,
                                EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID,
                                EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID,
                                EAV_BE_ENTITY_COMPLEX_SETS.SET_ID)
                        .values(baseEntity.getId(), metaAttribute.getId(), setId);
            }
            else
            {
                insert = sqlGenerator
                        .insertInto(
                                EAV_BE_ENTITY_SIMPLE_SETS,
                                EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID,
                                EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID,
                                EAV_BE_ENTITY_SIMPLE_SETS.SET_ID)
                        .values(baseEntity.getId(), metaAttribute.getId(), setId);
            }
        }

        logger.debug(insert.toString());
        insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    private long insertSet(IBaseValue baseValue) {
        InsertOnDuplicateStep insert = sqlGenerator
                .insertInto(
                        EAV_BE_SETS,
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_SETS.INDEX,
                        EAV_BE_SETS.REP_DATE)
                .values(baseValue.getBatch().getId(), baseValue.getIndex(), baseValue.getRepDate());

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    private long insertSimpleSet(IBaseValue baseValue, MetaSet metaSet)
    {
        BaseSet baseSet = (BaseSet)baseValue.getValue();
        IMetaType metaType = metaSet.getMemberType();

        long setId = insertSet(baseValue);
        if (metaType.isSet())
        {
            InsertValuesStep2 insert = sqlGenerator
                    .insertInto(
                            EAV_BE_SET_OF_SIMPLE_SETS,
                            EAV_BE_SET_OF_SIMPLE_SETS.PARENT_SET_ID,
                            EAV_BE_SET_OF_SIMPLE_SETS.CHILD_SET_ID);

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();

            while (itValue.hasNext())
            {
                IBaseValue baseValueChild = itValue.next();
                MetaSet baseSetChild = (MetaSet)metaType;

                long setIdChild = insertSimpleSet(baseValueChild, baseSetChild);

                Object[] insertArgs = new Object[] {
                        setId,
                        setIdChild,
                };
                insert = insert.values(Arrays.asList(insertArgs));
            }
            logger.debug(insert.toString());
            batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
        }
        else
        {
            InsertValuesStep5 insert;
            DataTypes dataType = metaSet.getTypeCode();
            switch(dataType)
            {
                case INTEGER:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_INTEGER_SET_VALUES,
                                    EAV_BE_INTEGER_SET_VALUES.SET_ID,
                                    EAV_BE_INTEGER_SET_VALUES.BATCH_ID,
                                    EAV_BE_INTEGER_SET_VALUES.INDEX,
                                    EAV_BE_INTEGER_SET_VALUES.REP_DATE,
                                    EAV_BE_INTEGER_SET_VALUES.VALUE);
                    break;
                }
                case DATE:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_DATE_SET_VALUES,
                                    EAV_BE_DATE_SET_VALUES.SET_ID,
                                    EAV_BE_DATE_SET_VALUES.BATCH_ID,
                                    EAV_BE_DATE_SET_VALUES.INDEX,
                                    EAV_BE_DATE_SET_VALUES.REP_DATE,
                                    EAV_BE_DATE_SET_VALUES.VALUE);
                    break;
                }
                case STRING:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_STRING_SET_VALUES,
                                    EAV_BE_STRING_SET_VALUES.SET_ID,
                                    EAV_BE_STRING_SET_VALUES.BATCH_ID,
                                    EAV_BE_STRING_SET_VALUES.INDEX,
                                    EAV_BE_STRING_SET_VALUES.REP_DATE,
                                    EAV_BE_STRING_SET_VALUES.VALUE);
                    break;
                }
                case BOOLEAN:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_BOOLEAN_SET_VALUES,
                                    EAV_BE_BOOLEAN_SET_VALUES.SET_ID,
                                    EAV_BE_BOOLEAN_SET_VALUES.BATCH_ID,
                                    EAV_BE_BOOLEAN_SET_VALUES.INDEX,
                                    EAV_BE_BOOLEAN_SET_VALUES.REP_DATE,
                                    EAV_BE_BOOLEAN_SET_VALUES.VALUE);
                    break;
                }
                case DOUBLE:
                {
                    insert = sqlGenerator
                            .insertInto(
                                    EAV_BE_DOUBLE_SET_VALUES,
                                    EAV_BE_DOUBLE_SET_VALUES.SET_ID,
                                    EAV_BE_DOUBLE_SET_VALUES.BATCH_ID,
                                    EAV_BE_DOUBLE_SET_VALUES.INDEX,
                                    EAV_BE_DOUBLE_SET_VALUES.REP_DATE,
                                    EAV_BE_DOUBLE_SET_VALUES.VALUE);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext())
            {
                IBaseValue batchValueChild = it.next();
                Object[] insertArgs = new Object[] {
                        setId,
                        batchValueChild.getBatch().getId(),
                        batchValueChild.getIndex(),
                        batchValueChild.getRepDate(),
                        batchValueChild.getValue()
                };
                insert = insert.values(Arrays.asList(insertArgs));
            }
            logger.debug(insert.toString());
            batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
        }

        return setId;
    }

    private long insertComplexSet(IBaseValue baseValue, MetaSet metaSet)
    {
        BaseSet baseSet = (BaseSet)baseValue.getValue();
        IMetaType metaType = metaSet.getMemberType();

        long setId = insertSet(baseValue);
        if (metaType.isSet())
        {
            InsertValuesStep2 insert = sqlGenerator
                    .insertInto(
                            EAV_BE_SET_OF_COMPLEX_SETS,
                            EAV_BE_SET_OF_COMPLEX_SETS.PARENT_SET_ID,
                            EAV_BE_SET_OF_COMPLEX_SETS.CHILD_SET_ID);

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();

            while (it.hasNext())
            {
                IBaseValue baseValueChild = it.next();
                MetaSet baseSetChild = (MetaSet)metaType;

                long setIdChild = insertComplexSet(baseValueChild, baseSetChild);

                Object[] insertArgs = new Object[] {
                        setId,
                        setIdChild,
                };
                insert = insert.values(Arrays.asList(insertArgs));
            }
            logger.debug(insert.toString());
            batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
        }
        else
        {
            InsertValuesStep5 insert = sqlGenerator
                    .insertInto(
                            EAV_BE_COMPLEX_SET_VALUES,
                            EAV_BE_COMPLEX_SET_VALUES.SET_ID,
                            EAV_BE_COMPLEX_SET_VALUES.BATCH_ID,
                            EAV_BE_COMPLEX_SET_VALUES.INDEX,
                            EAV_BE_COMPLEX_SET_VALUES.REP_DATE,
                            EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID);

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();
            while (itValue.hasNext()) {
                IBaseValue baseValueChild = itValue.next();

                BaseEntity baseEntityChild = (BaseEntity)baseValueChild.getValue();
                long baseEntityChildId = save(baseEntityChild);

                Object[] insertArgs = new Object[] {
                        setId,
                        baseValueChild.getBatch().getId(),
                        baseValueChild.getIndex(),
                        baseValue.getRepDate(),
                        baseEntityChildId
                };
                insert = insert.values(Arrays.asList(insertArgs));
            }
            logger.debug(insert.toString());
            batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
        }

        return setId;
    }

    private void loadIntegerValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_INTEGER_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_INTEGER_VALUES.INDEX,
                        EAV_BE_INTEGER_VALUES.REP_DATE,
                        EAV_BE_INTEGER_VALUES.VALUE)
                .from(EAV_BE_INTEGER_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.equal(baseEntity.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_INTEGER_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_INTEGER_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_INTEGER_VALUES.REP_DATE.getName()),
                            row.get(EAV_BE_INTEGER_VALUES.VALUE.getName())));
        }
    }

    private void loadDateValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_DATE_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_DATE_VALUES.INDEX,
                        EAV_BE_DATE_VALUES.REP_DATE,
                        EAV_BE_DATE_VALUES.VALUE)
                .from(EAV_BE_DATE_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_DATE_VALUES.ENTITY_ID.equal(baseEntity.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_DATE_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_DATE_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_DATE_VALUES.REP_DATE.getName()),
                            DateUtils.convert((java.sql.Date) row.get(EAV_BE_DATE_VALUES.VALUE.getName()))));
        }
    }

    private void loadBooleanValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_BOOLEAN_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_BOOLEAN_VALUES.INDEX,
                        EAV_BE_BOOLEAN_VALUES.REP_DATE,
                        EAV_BE_BOOLEAN_VALUES.VALUE)
                .from(EAV_BE_BOOLEAN_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.equal(baseEntity.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_BOOLEAN_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_BOOLEAN_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_BOOLEAN_VALUES.REP_DATE.getName()),
                            row.get(EAV_BE_BOOLEAN_VALUES.VALUE.getName())));
        }
    }

    private void loadStringValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_STRING_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_STRING_VALUES.INDEX,
                        EAV_BE_STRING_VALUES.REP_DATE,
                        EAV_BE_STRING_VALUES.VALUE)
                .from(EAV_BE_STRING_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_STRING_VALUES.ENTITY_ID.equal(baseEntity.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_STRING_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_STRING_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_STRING_VALUES.REP_DATE.getName()),
                            row.get(EAV_BE_STRING_VALUES.VALUE.getName())));
        }
    }

    private void loadDoubleValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_DOUBLE_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_DOUBLE_VALUES.INDEX,
                        EAV_BE_DOUBLE_VALUES.REP_DATE,
                        EAV_BE_DOUBLE_VALUES.VALUE)
                .from(EAV_BE_DOUBLE_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_DOUBLE_VALUES.ENTITY_ID.equal(baseEntity.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_DOUBLE_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_DOUBLE_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_DOUBLE_VALUES.REP_DATE.getName()),
                            row.get(EAV_BE_DOUBLE_VALUES.VALUE.getName())));
        }
    }

    private void loadComplexValues(BaseEntity baseEntity)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_COMPLEX_VALUES.BATCH_ID,
                        EAV_COMPLEX_ATTRIBUTES.NAME,
                        EAV_BE_COMPLEX_VALUES.INDEX,
                        EAV_BE_COMPLEX_VALUES.REP_DATE,
                        EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID)
                .from(EAV_BE_COMPLEX_VALUES)
                .join(EAV_COMPLEX_ATTRIBUTES).on(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID.eq(EAV_COMPLEX_ATTRIBUTES.ID))
                .where(EAV_BE_COMPLEX_VALUES.ENTITY_ID.equal(baseEntity.getId()));


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
                    (String) row.get(EAV_COMPLEX_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_COMPLEX_VALUES.INDEX.getName()),
                            (Date) row.get(EAV_BE_COMPLEX_VALUES.REP_DATE.getName()),
                            childBaseEntity));
        }
    }

    private void loadEntitySimpleSets(BaseEntity baseEntity) {
        MetaClass metaClass = baseEntity.getMeta();

        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SETS.BATCH_ID,
                        EAV_SIMPLE_SET.NAME,
                        EAV_BE_SETS.INDEX,
                        EAV_BE_SETS.REP_DATE)
                .from(EAV_BE_ENTITY_SIMPLE_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID.eq(EAV_BE_SETS.ID))
                .join(EAV_SIMPLE_SET).on(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID.eq(EAV_SIMPLE_SET.ID))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID.equal(baseEntity.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            String attributeName = (String)row.get(EAV_SIMPLE_SET.NAME.getName());
            IMetaType metaType = metaClass.getMemberType(attributeName);

            BaseSet baseSet = new BaseSet((Long)row.get(EAV_BE_SETS.ID.getName()), ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSet);
            }
            else
            {
                loadSimpleSetValues(baseSet);
            }

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_SETS.BATCH_ID.getName()));
            baseEntity.put(attributeName, new BaseValue(batch, (Long)row.get(EAV_BE_SETS.INDEX.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()), baseSet));
        }
    }

    private void loadEntityComplexSets(BaseEntity baseEntity) {
        MetaClass metaClass = baseEntity.getMeta();

        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SETS.BATCH_ID,
                        EAV_COMPLEX_SET.NAME,
                        EAV_BE_SETS.INDEX,
                        EAV_BE_SETS.REP_DATE)
                .from(EAV_BE_ENTITY_COMPLEX_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID.eq(EAV_BE_SETS.ID))
                .join(EAV_COMPLEX_SET).on(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.eq(EAV_COMPLEX_SET.ID))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID.equal(baseEntity.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            String attributeName = (String)row.get(EAV_COMPLEX_SET.NAME.getName());
            IMetaType metaType = metaClass.getMemberType(attributeName);

            BaseSet baseSet = new BaseSet((Long)row.get(EAV_BE_SETS.ID.getName()), ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSet);
            }
            else
            {
                loadSimpleSetValues(baseSet);
            }

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_SETS.BATCH_ID.getName()));
            baseEntity.put(attributeName, new BaseValue(batch, (Long)row.get(EAV_BE_SETS.INDEX.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()), baseSet));
        }
    }

    private void loadEntitySetOfSets(BaseEntity baseEntity) {
        MetaClass metaClass = baseEntity.getMeta();

        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SETS.BATCH_ID,
                        EAV_SET_OF_SETS.NAME,
                        EAV_BE_SETS.INDEX,
                        EAV_BE_SETS.REP_DATE)
                .from(EAV_BE_ENTITY_SET_OF_SETS)
                .join(EAV_BE_SETS).on(EAV_BE_ENTITY_SET_OF_SETS.SET_ID.eq(EAV_BE_SETS.ID))
                .join(EAV_SET_OF_SETS).on(EAV_BE_ENTITY_SET_OF_SETS.ATTRIBUTE_ID.eq(EAV_SET_OF_SETS.ID))
                .where(EAV_BE_ENTITY_SET_OF_SETS.ENTITY_ID.equal(baseEntity.getId()));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            String attributeName = (String)row.get(EAV_SET_OF_SETS.NAME.getName());
            IMetaType metaType = metaClass.getMemberType(attributeName);

            BaseSet baseSet = new BaseSet((Long)row.get(EAV_BE_SETS.ID.getName()), ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSet);
            }
            else
            {
                loadSimpleSetValues(baseSet);
            }

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_SETS.BATCH_ID.getName()));
            baseEntity.put(attributeName, new BaseValue(batch, (Long)row.get(EAV_BE_SETS.INDEX.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()), baseSet));
        }
    }

    private void loadSetOfSimpleSets(BaseSet baseSet) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_SETS.INDEX,
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
            baseSet.put(new BaseValue(batch, (Long)row.get(EAV_BE_SETS.INDEX.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()), baseSetChild));
        }
    }

    private void loadSetOfComplexSets(BaseSet baseSet) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_SETS.ID,
                        EAV_BE_SETS.BATCH_ID,
                        EAV_BE_SETS.INDEX,
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
            baseSet.put(new BaseValue(batch, (Long)row.get(EAV_BE_SETS.INDEX.getName()),
                    (Date) row.get(EAV_BE_SETS.REP_DATE.getName()), baseSetChild));
        }
    }

    private void loadSimpleSetValues(BaseSet baseSet) {
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

    private void loadIntegerSetValues(BaseSet baseSet) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_INTEGER_SET_VALUES.BATCH_ID,
                        EAV_BE_INTEGER_SET_VALUES.INDEX,
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
                            (Long)rowValue.get(EAV_BE_INTEGER_SET_VALUES.INDEX.getName()),
                            (Date) rowValue.get(EAV_BE_INTEGER_SET_VALUES.REP_DATE.getName()),
                            rowValue.get(EAV_BE_INTEGER_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadDateSetValues(BaseSet baseSet) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_DATE_SET_VALUES.BATCH_ID,
                        EAV_BE_DATE_SET_VALUES.INDEX,
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
                            (Long)rowValue.get(EAV_BE_DATE_SET_VALUES.INDEX.getName()),
                            (Date) rowValue.get(EAV_BE_DATE_SET_VALUES.REP_DATE.getName()),
                            rowValue.get(EAV_BE_DATE_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadStringSetValues(BaseSet baseSet) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_STRING_SET_VALUES.BATCH_ID,
                        EAV_BE_STRING_SET_VALUES.INDEX,
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
                            (Long)rowValue.get(EAV_BE_STRING_SET_VALUES.INDEX.getName()),
                            (Date) rowValue.get(EAV_BE_STRING_SET_VALUES.REP_DATE.getName()),
                            rowValue.get(EAV_BE_STRING_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadBooleanSetValues(BaseSet baseSet) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_BOOLEAN_SET_VALUES.BATCH_ID,
                        EAV_BE_BOOLEAN_SET_VALUES.INDEX,
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
                            (Long)rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.INDEX.getName()),
                            (Date) rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.REP_DATE.getName()),
                            rowValue.get(EAV_BE_BOOLEAN_SET_VALUES.VALUE.getName())));
        }
    }

    private void loadDoubleSetValues(BaseSet baseSet) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID,
                        EAV_BE_DOUBLE_SET_VALUES.INDEX,
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
                            (Long)rowValue.get(EAV_BE_DOUBLE_SET_VALUES.INDEX.getName()),
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
                            EAV_BE_COMPLEX_SET_VALUES.INDEX,
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
                baseSet.put(new BaseValue(batch, (Long)row.get(EAV_BE_COMPLEX_SET_VALUES.INDEX.getName()),
                        (Date) row.get(EAV_BE_COMPLEX_SET_VALUES.REP_DATE.getName()), baseEntity));
            }
        }
    }

}
