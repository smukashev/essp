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
import kz.bsbnb.usci.eav.util.DateUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * @author a.motov
 */
@SuppressWarnings("unchecked")
@Repository
public class BeSimpleValueDaoImpl extends JDBCSupport implements IBeSimpleValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeSimpleValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public void save(IBaseEntity baseEntity, String attribute) {
        IMetaType metaType = baseEntity.getMemberType(attribute);
        save(baseEntity, attribute, ((MetaValue)metaType).getTypeCode());
    }

    @Override
    public void save(IBaseEntity baseEntity, Set<String> attributes) {
        Iterator<String> it = attributes.iterator();
        while (it.hasNext())
        {
            String attribute = it.next();
            save(baseEntity, attribute);
        }
    }

    @Override
    public void save(IBaseEntity baseEntity, Set<String> attributes, DataTypes dataType)
    {
        Iterator<String> it = attributes.iterator();
        while (it.hasNext())
        {
            String attribute = it.next();
            save(baseEntity, attribute, dataType);
        }
    }

    @Override
    public void save(IBaseEntity baseEntity, String attribute, DataTypes dataType)
    {
        IMetaAttribute metaAttribute = baseEntity.getMetaAttribute(attribute);
        IBaseValue baseValue = baseEntity.getBaseValue(attribute);

        long baseValueId = save(dataType, baseEntity.getId(), baseValue.getBatch().getId(), metaAttribute.getId(), baseValue.getId(),
                baseValue.getRepDate(), baseValue.getValue(), baseValue.isClosed(), baseValue.isLast());
        baseValue.setId(baseValueId);
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
                        "attribute {0} of BaseEntity instance with identifier {1}.", baseEntityLoaded.getId(), attribute));
            }
            else
            {
                //TODO: Check this attribute in the future? if exist then isLast = false
                long baseValueId = save(dataType, baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                        metaAttribute.getId(), baseValueForSave.getIndex(),
                        baseValueForSave.getRepDate(), baseValueForSave.getValue(), false, true);
                baseValueForSave.setId(baseValueId);
            }
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
                    fields.put("value", baseValueForSave.getValue());
                    if (baseValueForSave.isClosed())
                    {
                        fields.put("is_closed", true);
                    }

                    Map<String, Object> conditions = new HashMap<String, Object>();
                    conditions.put("id", baseValueLoaded.getId());

                    updateByCondition(dataType, fields, conditions);

                    break;
                case -1:
                {
                    long baseValueId = save(dataType, baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                            metaAttribute.getId(), baseValueForSave.getIndex(),
                            baseValueForSave.getRepDate(), baseValueForSave.getValue(), baseValueForSave.isClosed(), false);
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
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.INDEX_),
                            fields.get("index_")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_INTEGER_VALUES.INDEX_),
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
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.INDEX_),
                            fields.get("index_")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_DATE_VALUES.INDEX_),
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

}
