package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBeSimpleValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.tool.Configuration;
import kz.bsbnb.usci.eav.util.DateUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_BOOLEAN_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_DOUBLE_VALUES;

/**
 * @author a.motov
 */
@Repository
public class BeSimpleValueDaoImpl extends JDBCSupport implements IBeSimpleValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeSimpleValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public long save(IBaseEntity baseEntity, String attribute) {
        IMetaType metaType = baseEntity.getMemberType(attribute);
        return save(baseEntity, attribute, ((MetaValue)metaType).getTypeCode());
    }

    @Override
    public void save(IBaseEntity baseEntity, Set<String> attributes) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void save(IBaseEntity baseEntity, Set<String> attributeNames, DataTypes dataType)
    {
        MetaClass meta = baseEntity.getMeta();

        InsertValuesStep8 insert;
        switch(dataType)
        {
            case INTEGER: {
                insert = context.insertInto(
                        EAV_BE_INTEGER_VALUES,
                        EAV_BE_INTEGER_VALUES.ENTITY_ID,
                        EAV_BE_INTEGER_VALUES.BATCH_ID,
                        EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID,
                        EAV_BE_INTEGER_VALUES.INDEX_,
                        EAV_BE_INTEGER_VALUES.REPORT_DATE,
                        EAV_BE_INTEGER_VALUES.VALUE,
                        EAV_BE_INTEGER_VALUES.IS_CLOSED,
                        EAV_BE_INTEGER_VALUES.IS_LAST);
                break;
            }
            case DATE: {
                insert = context.insertInto(
                        EAV_BE_DATE_VALUES,
                        EAV_BE_DATE_VALUES.ENTITY_ID,
                        EAV_BE_DATE_VALUES.BATCH_ID,
                        EAV_BE_DATE_VALUES.ATTRIBUTE_ID,
                        EAV_BE_DATE_VALUES.INDEX_,
                        EAV_BE_DATE_VALUES.REPORT_DATE,
                        EAV_BE_DATE_VALUES.VALUE,
                        EAV_BE_DATE_VALUES.IS_CLOSED,
                        EAV_BE_DATE_VALUES.IS_LAST);
                break;
            }
            case STRING: {
                insert = context.insertInto(
                        EAV_BE_STRING_VALUES,
                        EAV_BE_STRING_VALUES.ENTITY_ID,
                        EAV_BE_STRING_VALUES.BATCH_ID,
                        EAV_BE_STRING_VALUES.ATTRIBUTE_ID,
                        EAV_BE_STRING_VALUES.INDEX_,
                        EAV_BE_STRING_VALUES.REPORT_DATE,
                        EAV_BE_STRING_VALUES.VALUE,
                        EAV_BE_STRING_VALUES.IS_CLOSED,
                        EAV_BE_STRING_VALUES.IS_LAST);
                break;
            }
            case BOOLEAN: {
                insert = context.insertInto(
                        EAV_BE_BOOLEAN_VALUES,
                        EAV_BE_BOOLEAN_VALUES.ENTITY_ID,
                        EAV_BE_BOOLEAN_VALUES.BATCH_ID,
                        EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID,
                        EAV_BE_BOOLEAN_VALUES.INDEX_,
                        EAV_BE_BOOLEAN_VALUES.REPORT_DATE,
                        EAV_BE_BOOLEAN_VALUES.VALUE,
                        EAV_BE_BOOLEAN_VALUES.IS_CLOSED,
                        EAV_BE_BOOLEAN_VALUES.IS_LAST);
                break;
            }
            case DOUBLE: {
                insert = context.insertInto(
                        EAV_BE_DOUBLE_VALUES,
                        EAV_BE_DOUBLE_VALUES.ENTITY_ID,
                        EAV_BE_DOUBLE_VALUES.BATCH_ID,
                        EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID,
                        EAV_BE_DOUBLE_VALUES.INDEX_,
                        EAV_BE_DOUBLE_VALUES.REPORT_DATE,
                        EAV_BE_DOUBLE_VALUES.VALUE,
                        EAV_BE_DOUBLE_VALUES.IS_CLOSED,
                        EAV_BE_DOUBLE_VALUES.IS_LAST);
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
                    batchValue.getValue(),
                    false,
                    true
            };
            insert = insert.values(Arrays.asList(insertArgs));
        }

        logger.debug(insert.toString());
        batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
    }

    @Override
    public long save(IBaseEntity baseEntity, String attribute, DataTypes dataType)
    {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    private long save(DataTypes dataType, long baseEntityId, long batchId, long metaAttributeId, long index,
                      Date reportDate, Object value, boolean closed, boolean last)
    {
        switch (dataType)
        {
            case INTEGER:
                return saveIntegerValue(baseEntityId, batchId, metaAttributeId, index,
                        reportDate, (Integer)value, closed, last);
            case DATE:
                return saveDateValue(baseEntityId, batchId, metaAttributeId, index,
                        reportDate, (Date)value, closed, last);
            case STRING:
                return saveStringValue(baseEntityId, batchId, metaAttributeId, index,
                        reportDate, (String)value, closed, last);
            case BOOLEAN:
                return saveBooleanValue(baseEntityId, batchId, metaAttributeId, index,
                        reportDate, (Boolean)value, closed, last);
            case DOUBLE:
                return saveDoubleValue(baseEntityId, batchId, metaAttributeId, index,
                        reportDate, (Double)value, closed, last);
            default:
                throw new IllegalArgumentException("Unknown data type.");
        }
    }


    private long saveIntegerValue(long baseEntityId, long batchId, long metaAttributeId, long index,
                      Date reportDate, Integer value, boolean closed, boolean last)
    {
        Insert insert = context
                        .insertInto(EAV_BE_INTEGER_VALUES)
                        .set(EAV_BE_INTEGER_VALUES.ENTITY_ID, baseEntityId)
                        .set(EAV_BE_INTEGER_VALUES.BATCH_ID, batchId)
                        .set(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID, metaAttributeId)
                        .set(EAV_BE_INTEGER_VALUES.INDEX_, index)
                        .set(EAV_BE_INTEGER_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                        .set(EAV_BE_INTEGER_VALUES.VALUE, value)
                        .set(EAV_BE_INTEGER_VALUES.IS_CLOSED, closed)
                        .set(EAV_BE_INTEGER_VALUES.IS_LAST, last);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    private long saveDateValue(long baseEntityId, long batchId, long metaAttributeId, long index,
                                  Date reportDate, Date value, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_DATE_VALUES)
                .set(EAV_BE_DATE_VALUES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_DATE_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_DATE_VALUES.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_DATE_VALUES.INDEX_, index)
                .set(EAV_BE_DATE_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                .set(EAV_BE_DATE_VALUES.VALUE, DateUtils.convert(value))
                .set(EAV_BE_DATE_VALUES.IS_CLOSED, closed)
                .set(EAV_BE_DATE_VALUES.IS_LAST, last);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    private long saveStringValue(long baseEntityId, long batchId, long metaAttributeId, long index,
                                 Date reportDate, String value, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_STRING_VALUES)
                .set(EAV_BE_STRING_VALUES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_STRING_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_STRING_VALUES.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_STRING_VALUES.INDEX_, index)
                .set(EAV_BE_STRING_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                .set(EAV_BE_STRING_VALUES.VALUE, value)
                .set(EAV_BE_STRING_VALUES.IS_CLOSED, closed)
                .set(EAV_BE_STRING_VALUES.IS_LAST, last);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    private long saveBooleanValue(long baseEntityId, long batchId, long metaAttributeId, long index,
                               Date reportDate, Boolean value, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_BOOLEAN_VALUES)
                .set(EAV_BE_BOOLEAN_VALUES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_BOOLEAN_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_BOOLEAN_VALUES.INDEX_, index)
                .set(EAV_BE_BOOLEAN_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                .set(EAV_BE_BOOLEAN_VALUES.VALUE, value)
                .set(EAV_BE_BOOLEAN_VALUES.IS_CLOSED, closed)
                .set(EAV_BE_BOOLEAN_VALUES.IS_LAST, last);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    private long saveDoubleValue(long baseEntityId, long batchId, long metaAttributeId, long index,
                                  Date reportDate, Double value, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_DOUBLE_VALUES)
                .set(EAV_BE_DOUBLE_VALUES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_DOUBLE_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_DOUBLE_VALUES.INDEX_, index)
                .set(EAV_BE_DOUBLE_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                .set(EAV_BE_DOUBLE_VALUES.VALUE, value)
                .set(EAV_BE_DOUBLE_VALUES.IS_CLOSED, closed)
                .set(EAV_BE_DOUBLE_VALUES.IS_LAST, last);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute)
    {
        MetaClass metaClass = baseEntityLoaded.getMeta();
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
        IMetaType metaType = metaAttribute.getMetaType();
        DataTypes dataType = ((MetaValue)metaType).getTypeCode();

        IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);
        IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);

        if (baseValueLoaded == null)
        {
            if (baseValueForSave == null)
            {
                logger.warn(String.format("An attempt was made to remove a missing value for the " +
                        "attribute {0} of BaseEntity instance with identifier {1} for the report date {2}.",
                        baseEntityLoaded.getId(), attribute, baseValueForSave.getRepDate()));
            }

            // TODO:
            long baseValueId = save(dataType, baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                    metaAttribute.getId(), baseValueForSave.getIndex(),
                    baseValueForSave.getRepDate(), baseValueForSave.getValue(), false, true);
            baseValueForSave.setId(baseValueId);
        }
        else
        {


            int compare = DateUtils.compareBeginningOfTheDay(baseValueForSave.getRepDate(), baseValueLoaded.getRepDate());
            switch(compare)
            {
                case 1:
                {
                    long baseValueId = save(dataType, baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                            metaAttribute.getId(), baseValueForSave.getIndex(), baseValueForSave.getRepDate(),
                            baseValueForSave.getValue(), baseValueForSave.isClosed(), baseValueForSave.isLast());
                    baseValueForSave.setId(baseValueId);

                    if (baseValueLoaded.isLast())
                    {
                        Map<String, Object> fields = new HashMap<String, Object>();
                        fields.put("is_last", false);

                        Map<String, Object> conditions = new HashMap<String, Object>();
                        conditions.put("id", baseValueLoaded.getId());

                        updateByCondition(dataType, fields, conditions);
                    }
                    break;
                }
                case 0:
                    Map<String, Object> fields = new HashMap<String, Object>();
                    fields.put("batch_id", baseValueForSave.getBatch().getId());
                    fields.put("index_", baseValueForSave.getIndex());
                    if (baseValueForSave.isClosed())
                    {
                        fields.put("is_closed", true);
                    }
                    else
                    {
                        fields.put("value", baseValueForSave.getValue());
                    }

                    Map<String, Object> conditions = new HashMap<String, Object>();
                    conditions.put("id", baseValueLoaded.getId());

                    updateByCondition(dataType, fields, conditions);

                    break;
                case -1:
                {
                    long baseValueId = save(dataType, baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                            metaAttribute.getId(), baseValueForSave.getIndex(),
                            baseValueForSave.getRepDate(), baseValueForSave.getValue(), false, false);
                    baseValueForSave.setId(baseValueId);
                    break;
                }
                default:
                    throw new RuntimeException("Method Comparable<T>.compareTo(T o) " +
                            "can not return a value other than -1, 0, 1.");
            }
        }
    }

    private int updateByCondition(DataTypes dataType, Map<String, Object> fields, Map<String, Object> conditions)
    {
        switch (dataType)
        {
            case INTEGER:
                return updateIntegerValueByCondition(fields, conditions);
            case DATE:
                return updateDateValueByCondition(fields, conditions);
            /*case STRING:
                return saveStringValue(baseEntityId, batchId, metaAttributeId, index,
                        reportDate, (String)value, closed, last);
            case BOOLEAN:
                return saveBooleanValue(baseEntityId, batchId, metaAttributeId, index,
                        reportDate, (Boolean)value, closed, last);
            case DOUBLE:
                return saveDoubleValue(baseEntityId, batchId, metaAttributeId, index,
                        reportDate, (Double)value, closed, last);*/
            default:
                throw new IllegalArgumentException("Unknown data type.");
        }
    }

    private int updateIntegerValueByCondition(Map<String, Object> fields,
                                              Map<String, Object> conditions)
    {
        if (fields.size() == 0)
        {
            throw new IllegalArgumentException("To implement the changes to the record must have at least one field.");
        }

        Table tableOfIntegerValues = EAV_BE_INTEGER_VALUES.as("v");
        UpdateSetStep updateSetStep = context.update(tableOfIntegerValues);

        UpdateSetMoreStep updateSetMoreStep = null;
        if (fields.containsKey("batch_id"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.BATCH_ID),
                            fields.get("batch_id")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.BATCH_ID),
                            fields.get("batch_id"));
        }
        if (fields.containsKey("index_"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.BATCH_ID),
                            fields.get("index_")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.BATCH_ID),
                            fields.get("index_"));
        }
        if (fields.containsKey("value"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.VALUE),
                            fields.get("value")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.VALUE),
                            fields.get("value"));
        }
        if (fields.containsKey("is_closed"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.IS_CLOSED),
                            fields.get("is_closed")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.IS_CLOSED),
                            fields.get("is_closed"));
        }
        if (fields.containsKey("is_last"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.IS_LAST),
                            fields.get("is_last")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.IS_LAST),
                            fields.get("is_last"));
        }

        UpdateConditionStep updateConditionStep = null;
        if (conditions.containsKey("id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.ID)
                            .equal(conditions.get("id"))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.ID)
                            .equal(conditions.get("id")));
        }
        if (conditions.containsKey("entity_id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.ENTITY_ID)
                            .equal(conditions.get("entity_id"))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.ENTITY_ID)
                            .equal(conditions.get("entity_id")));
        }
        if (conditions.containsKey("attribute_id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID)
                            .equal(conditions.get("attribute_id"))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID)
                            .equal(conditions.get("attribute_id")));
        }
        if (conditions.containsKey("report_date"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.REPORT_DATE)
                            .equal(DateUtils.convert((Date)conditions.get("report_date")))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.REPORT_DATE)
                            .equal(DateUtils.convert((Date) conditions.get("report_date"))));
        }

        logger.debug(updateConditionStep.toString());
        return updateWithStats(updateConditionStep.getSQL(), updateConditionStep.getBindValues().toArray());
    }

    private int updateDateValueByCondition(Map<String, Object> fields,
                                           Map<String, Object> conditions)
    {
        if (fields.size() == 0)
        {
            throw new IllegalArgumentException("To implement the changes to the record must have at least one field.");
        }

        Table tableOfIntegerValues = EAV_BE_DATE_VALUES.as("v");
        UpdateSetStep updateSetStep = context.update(tableOfIntegerValues);

        UpdateSetMoreStep updateSetMoreStep = null;
        if (fields.containsKey("batch_id"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.BATCH_ID),
                            fields.get("batch_id")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.BATCH_ID),
                            fields.get("batch_id"));
        }
        if (fields.containsKey("index_"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.BATCH_ID),
                            fields.get("index_")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.BATCH_ID),
                            fields.get("index_"));
        }
        if (fields.containsKey("value"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.VALUE),
                            DateUtils.convert((Date)fields.get("value"))) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.VALUE),
                            DateUtils.convert((Date)fields.get("value")));
        }
        if (fields.containsKey("is_closed"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.IS_CLOSED),
                            fields.get("is_closed")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.IS_CLOSED),
                            fields.get("is_closed"));
        }
        if (fields.containsKey("is_last"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.IS_LAST),
                            fields.get("is_last")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.IS_LAST),
                            fields.get("is_last"));
        }

        UpdateConditionStep updateConditionStep = null;
        if (conditions.containsKey("id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.ID)
                            .equal(conditions.get("id"))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.ID)
                            .equal(conditions.get("id")));
        }
        if (conditions.containsKey("entity_id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.ENTITY_ID)
                            .equal(conditions.get("entity_id"))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.ENTITY_ID)
                            .equal(conditions.get("entity_id")));
        }
        if (conditions.containsKey("attribute_id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.ATTRIBUTE_ID)
                            .equal(conditions.get("attribute_id"))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.ATTRIBUTE_ID)
                            .equal(conditions.get("attribute_id")));
        }
        if (conditions.containsKey("report_date"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.REPORT_DATE)
                            .equal(DateUtils.convert((Date)conditions.get("report_date")))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.REPORT_DATE)
                            .equal(DateUtils.convert((Date) conditions.get("report_date"))));
        }

        logger.debug(updateConditionStep.toString());
        return updateWithStats(updateConditionStep.getSQL(), updateConditionStep.getBindValues().toArray());
    }

    @Override
    public void remove(IBaseEntity baseEntity, String attribute)
    {
        MetaClass meta = baseEntity.getMeta();
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);

        if (metaAttribute == null) {
            throw new IllegalArgumentException("Attribute " + attribute + " not found in the MetaClass. " +
                    "Removing a simple value is not possible.");
        }

        IMetaType metaType = metaAttribute.getMetaType();

        long metaAttributeId =  metaAttribute.getId();
        long baseEntityId = baseEntity.getId();

        if (baseEntityId < 1)
        {
            throw new IllegalArgumentException("BaseEntity does not contain id. " +
                    "Removing a simple value is not possible.");
        }
        if (metaAttributeId < 1)
        {
            throw new IllegalArgumentException("MetaAttribute does not contain id. " +
                    "Removing a simple value is not possible.");
        }

        DeleteConditionStep delete;
        switch(((MetaValue)metaType).getTypeCode())
        {
            case INTEGER: {
                delete = context
                        .delete(EAV_BE_INTEGER_VALUES)
                        .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case DATE: {
                delete = context
                        .delete(EAV_BE_DATE_VALUES)
                        .where(EAV_BE_DATE_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case STRING: {
                delete = context
                        .delete(EAV_BE_STRING_VALUES)
                        .where(EAV_BE_STRING_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case BOOLEAN: {
                delete = context
                        .delete(EAV_BE_BOOLEAN_VALUES)
                        .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));
                break;
            }
            case DOUBLE: {
                delete = context
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

    /*private void closeHistory(long baseValueId, Date reportDate, DataTypes dataType)
    {
        Update update;
        switch(dataType)
        {
            case INTEGER: {
                update = context
                        .update(EAV_BE_INTEGER_VALUES)
                        .set(EAV_BE_INTEGER_VALUES.IS_LAST, false)
                        .set(EAV_BE_INTEGER_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                        .where(EAV_BE_INTEGER_VALUES.ID.eq(baseValueId));
                break;
            }
            case DATE: {
                update = context
                        .update(EAV_BE_DATE_VALUES)
                        .set(EAV_BE_DATE_VALUES.IS_LAST, false)
                        .set(EAV_BE_DATE_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                        .where(EAV_BE_DATE_VALUES.ID.eq(baseValueId));
                break;
            }
            case STRING: {
                update = context
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_STRING_VALUES.IS_LAST, false)
                        .set(EAV_BE_STRING_VALUES.CLOSE_DATE, DateUtils.convert(reportDate))
                        .where(EAV_BE_STRING_VALUES.ID.eq(baseValueId));
                break;
            }
            case BOOLEAN: {
                update = context
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_BOOLEAN_VALUES.IS_LAST, false)
                        .set(EAV_BE_BOOLEAN_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                        .where(EAV_BE_BOOLEAN_VALUES.ID.eq(baseValueId));
                break;
            }
            case DOUBLE: {
                update = context
                        .update(EAV_BE_DOUBLE_VALUES)
                        .set(EAV_BE_DOUBLE_VALUES.IS_LAST, false)
                        .set(EAV_BE_DOUBLE_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                        .where(EAV_BE_DOUBLE_VALUES.ID.eq(baseValueId));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type.");
        }

        logger.debug(update.toString());
        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }*/

    /*private void rollbackClosedHistory(long baseEntityId, long metaAttributeId, Date reportDate, DataTypes dataType)
    {

        Update update = null;
        switch (dataType)
        {
            case INTEGER: {
                update = context
                        .update(EAV_BE_INTEGER_VALUES)
                        .set(EAV_BE_INTEGER_VALUES.IS_LAST, true)
                        .set(EAV_BE_INTEGER_VALUES.REPORT_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_INTEGER_VALUES.REPORT_DATE.eq(DateUtils.convert(reportDate)));
                break;
            }
            case DATE: {
                update = context
                        .update(EAV_BE_DATE_VALUES)
                        .set(EAV_BE_DATE_VALUES.IS_LAST, false)
                        .set(EAV_BE_DATE_VALUES.REPORT_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_DATE_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_DATE_VALUES.REPORT_DATE.eq(DateUtils.convert(reportDate)));
                break;
            }
            case STRING: {
                update = context
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_STRING_VALUES.IS_LAST, false)
                        .set(EAV_BE_STRING_VALUES.REPORT_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_STRING_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_STRING_VALUES.REPORT_DATE.eq(DateUtils.convert(reportDate)));
                break;
            }
            case BOOLEAN: {
                update = context
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_BOOLEAN_VALUES.IS_LAST, false)
                        .set(EAV_BE_BOOLEAN_VALUES.REPORT_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_BOOLEAN_VALUES.REPORT_DATE.eq(DateUtils.convert(reportDate)));
                break;
            }
            case DOUBLE: {
                update = context
                        .update(EAV_BE_DOUBLE_VALUES)
                        .set(EAV_BE_DOUBLE_VALUES.IS_LAST, false)
                        .set(EAV_BE_DOUBLE_VALUES.CLOSE_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_DOUBLE_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_DOUBLE_VALUES.CLOSE_DATE.eq(DateUtils.convert(reportDate)));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type.");
        }

        logger.debug(update.toString());
        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }*/

    private void update(IBaseValue baseValueLoaded, IBaseValue baseValueForSave, DataTypes dataType)
    {
        /*long baseValueLoadedId = baseValueLoaded.getId();

        Update update;
        switch(dataType)
        {
            case INTEGER: {
                update = context
                        .update(EAV_BE_INTEGER_VALUES)
                        .set(EAV_BE_INTEGER_VALUES.VALUE, (Integer)baseValueForSave.getValue())
                        .set(EAV_BE_INTEGER_VALUES.BATCH_ID, baseValueForSave.getBatch().getId())
                        .set(EAV_BE_INTEGER_VALUES.INDEX_, baseValueForSave.getIndex())
                        .where(EAV_BE_INTEGER_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case DATE: {
                update = context
                        .update(EAV_BE_DATE_VALUES)
                        .set(EAV_BE_DATE_VALUES.VALUE, DateUtils.convert((java.util.Date) baseValueForSave.getValue()))
                        .set(EAV_BE_DATE_VALUES.BATCH_ID, baseValueForSave.getBatch().getId())
                        .set(EAV_BE_DATE_VALUES.INDEX_, baseValueForSave.getIndex())
                        .where(EAV_BE_DATE_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case STRING: {
                update = context
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_STRING_VALUES.VALUE, (String)baseValueForSave.getValue())
                        .set(EAV_BE_STRING_VALUES.BATCH_ID, baseValueForSave.getBatch().getId())
                        .set(EAV_BE_STRING_VALUES.INDEX_, baseValueForSave.getIndex())
                        .where(EAV_BE_STRING_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case BOOLEAN: {
                update = context
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_BOOLEAN_VALUES.VALUE, (Boolean)baseValueForSave.getValue())
                        .set(EAV_BE_BOOLEAN_VALUES.BATCH_ID, baseValueForSave.getBatch().getId())
                        .set(EAV_BE_BOOLEAN_VALUES.INDEX_, baseValueForSave.getIndex())
                        .where(EAV_BE_BOOLEAN_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case DOUBLE: {
                update = context
                        .update(EAV_BE_DOUBLE_VALUES)
                        .set(EAV_BE_DOUBLE_VALUES.VALUE, (Double)baseValueForSave.getValue())
                        .set(EAV_BE_DOUBLE_VALUES.BATCH_ID, baseValueForSave.getBatch().getId())
                        .set(EAV_BE_DOUBLE_VALUES.INDEX_, baseValueForSave.getIndex())
                        .where(EAV_BE_DOUBLE_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type.");
        }

        logger.debug(update.toString());
        updateWithStats(update.getSQL(), update.getBindValues().toArray());*/
    }

    /*private boolean isPreviousClosed(long baseEntityId, long metaAttributeId,
                                     Date reportDate, Object value, DataTypes dataType)
    {
        Select select = null;
        switch (dataType)
        {
            case INTEGER: {
                select = context
                        .select(DSL.count().as("history_count"))
                        .from(EAV_BE_INTEGER_VALUES)
                        .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_INTEGER_VALUES.CLOSE_DATE.eq(DateUtils.convert(reportDate)))
                        .and(EAV_BE_INTEGER_VALUES.VALUE.eq((Integer)value));
                break;
            }
            case DATE: {
                select = context
                        .select(DSL.count().as("history_count"))
                        .from(EAV_BE_DATE_VALUES)
                        .where(EAV_BE_DATE_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_DATE_VALUES.CLOSE_DATE.eq(DateUtils.convert(reportDate)))
                        .and(EAV_BE_DATE_VALUES.VALUE.eq(DateUtils.convert((Date)value)));
                break;
            }
            case STRING: {
                select = context
                        .select(DSL.count().as("history_count"))
                        .from(EAV_BE_STRING_VALUES)
                        .where(EAV_BE_STRING_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_STRING_VALUES.CLOSE_DATE.eq(DateUtils.convert(reportDate)))
                        .and(EAV_BE_STRING_VALUES.VALUE.eq((String)value))
                        .limit(1);
                break;
            }
            case BOOLEAN: {
                select = context
                        .select(DSL.count().as("history_count"))
                        .from(EAV_BE_BOOLEAN_VALUES)
                        .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_BOOLEAN_VALUES.CLOSE_DATE.eq(DateUtils.convert(reportDate)))
                        .and(EAV_BE_BOOLEAN_VALUES.VALUE.eq((Boolean)value))
                        .limit(1);
                break;
            }
            case DOUBLE: {
                select = context
                        .select(DSL.count().as("history_count"))
                        .from(EAV_BE_DOUBLE_VALUES)
                        .where(EAV_BE_DOUBLE_VALUES.ENTITY_ID.eq(baseEntityId))
                        .and(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID.eq(metaAttributeId))
                        .and(EAV_BE_DOUBLE_VALUES.CLOSE_DATE.eq(DateUtils.convert(reportDate)))
                        .and(EAV_BE_DOUBLE_VALUES.VALUE.eq((Double)value))
                        .limit(1);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type.");
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        long count = (Long)rows.get(0).get("history_count");
        return count > 0;
    }*/

}
