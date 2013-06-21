package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBeSimpleValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.tool.Configuration;
import kz.bsbnb.usci.eav.util.DateUtils;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertValuesStep8;
import org.jooq.Select;
import org.jooq.Update;
import org.jooq.impl.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_BOOLEAN_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_DOUBLE_VALUES;
import static org.jooq.impl.Factory.count;

/**
 * @author a.motov
 */
@Repository
public class BeSimpleValueDaoImpl extends JDBCSupport implements IBeSimpleValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeSimpleValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private Executor sqlGenerator;

    @Override
    public void save(BaseEntity baseEntity, String attribute) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void save(BaseEntity baseEntity, Set<String> attributes) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void save(BaseEntity baseEntity, Set<String> attributeNames, DataTypes dataType)
    {
        MetaClass meta = baseEntity.getMeta();

        InsertValuesStep8 insert;
        switch(dataType)
        {
            case INTEGER: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_INTEGER_VALUES,
                        EAV_BE_INTEGER_VALUES.ENTITY_ID,
                        EAV_BE_INTEGER_VALUES.BATCH_ID,
                        EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID,
                        EAV_BE_INTEGER_VALUES.INDEX_,
                        EAV_BE_INTEGER_VALUES.OPEN_DATE,
                        EAV_BE_INTEGER_VALUES.CLOSE_DATE,
                        EAV_BE_INTEGER_VALUES.VALUE,
                        EAV_BE_INTEGER_VALUES.IS_LAST);
                break;
            }
            case DATE: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_DATE_VALUES,
                        EAV_BE_DATE_VALUES.ENTITY_ID,
                        EAV_BE_DATE_VALUES.BATCH_ID,
                        EAV_BE_DATE_VALUES.ATTRIBUTE_ID,
                        EAV_BE_DATE_VALUES.INDEX_,
                        EAV_BE_DATE_VALUES.OPEN_DATE,
                        EAV_BE_DATE_VALUES.CLOSE_DATE,
                        EAV_BE_DATE_VALUES.VALUE,
                        EAV_BE_DATE_VALUES.IS_LAST);
                break;
            }
            case STRING: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_STRING_VALUES,
                        EAV_BE_STRING_VALUES.ENTITY_ID,
                        EAV_BE_STRING_VALUES.BATCH_ID,
                        EAV_BE_STRING_VALUES.ATTRIBUTE_ID,
                        EAV_BE_STRING_VALUES.INDEX_,
                        EAV_BE_STRING_VALUES.OPEN_DATE,
                        EAV_BE_STRING_VALUES.CLOSE_DATE,
                        EAV_BE_STRING_VALUES.VALUE,
                        EAV_BE_STRING_VALUES.IS_LAST);
                break;
            }
            case BOOLEAN: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_BOOLEAN_VALUES,
                        EAV_BE_BOOLEAN_VALUES.ENTITY_ID,
                        EAV_BE_BOOLEAN_VALUES.BATCH_ID,
                        EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID,
                        EAV_BE_BOOLEAN_VALUES.INDEX_,
                        EAV_BE_BOOLEAN_VALUES.OPEN_DATE,
                        EAV_BE_BOOLEAN_VALUES.CLOSE_DATE,
                        EAV_BE_BOOLEAN_VALUES.VALUE,
                        EAV_BE_BOOLEAN_VALUES.IS_LAST);
                break;
            }
            case DOUBLE: {
                insert = sqlGenerator.insertInto(
                        EAV_BE_DOUBLE_VALUES,
                        EAV_BE_DOUBLE_VALUES.ENTITY_ID,
                        EAV_BE_DOUBLE_VALUES.BATCH_ID,
                        EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID,
                        EAV_BE_DOUBLE_VALUES.INDEX_,
                        EAV_BE_DOUBLE_VALUES.OPEN_DATE,
                        EAV_BE_DOUBLE_VALUES.CLOSE_DATE,
                        EAV_BE_DOUBLE_VALUES.VALUE,
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
                    Configuration.historyMaxDate,
                    batchValue.getValue(),
                    true
            };
            insert = insert.values(Arrays.asList(insertArgs));
        }

        logger.debug(insert.toString());
        batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
    }

    @Override
    public void save(BaseEntity baseEntity, String attribute, DataTypes dataType)
    {
        Set<String> attributes = new HashSet<String>();
        attributes.add(attribute);

        save(baseEntity, attributes, dataType);
    }

    @Override
    public void update(BaseEntity baseEntityLoaded, BaseEntity baseEntityForSave, String attribute)
    {
        MetaClass meta = baseEntityLoaded.getMeta();
        IMetaType metaType = meta.getMemberType(attribute);
        DataTypes dataType = ((MetaValue)metaType).getTypeCode();

        IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);
        IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);

        if (baseValueLoaded == null)
        {
            if (baseValueForSave.getValue() == null)
            {
                logger.warn(String.format("An attempt was made to remove a missing value for the " +
                        "attribute {0} of BaseEntity instance with identifier {1} for the report date {2}.",
                        baseEntityLoaded.getId(), attribute, baseValueForSave.getRepDate()));
            }

            boolean previousClosed = isPreviousClosed(baseEntityForSave, attribute);
            if (previousClosed)
            {
                rollbackClosedHistory(baseEntityForSave, attribute, dataType);
            }
            else
            {
                save(baseEntityForSave, attribute, dataType);
            }
            return;
        }
        else
        {
            if (baseValueForSave.getValue() != null)
            {
                if (dataType.equals(DataTypes.DATE))
                {
                    java.util.Date comparingDate = (java.util.Date)baseValueForSave.getValue();
                    java.util.Date anotherDate = (java.util.Date)baseValueLoaded.getValue();
                    if (DateUtils.compareBeginningOfTheDay(comparingDate, anotherDate) == 0)
                    {
                        return;
                    }
                }
                else
                {
                    if (baseValueForSave.getValue().equals(baseValueLoaded.getValue()))
                    {
                        return;
                    }
                }
            }
        }

        int compare = DateUtils.compareBeginningOfTheDay(baseValueLoaded.getRepDate(), baseValueForSave.getRepDate());
        if (compare == 1)
        {
            throw new UnsupportedOperationException("Updating a simple attribute value for " +
                    "the earlier period is not implemented.");
        }
        else
        {
            if (compare == -1)
            {
                if (baseValueForSave.getValue() != null)
                {
                    save(baseEntityForSave, attribute, dataType);
                }
                closeHistory(baseValueLoaded, baseValueForSave, dataType);
            }
            else
            {
                update(baseValueLoaded, baseValueForSave, dataType);
            }
        }
    }

    @Override
    public void remove(BaseEntity baseEntity, String attribute)
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

    private void closeHistory(IBaseValue baseValueLoaded,
                              IBaseValue baseValueForSave,
                              DataTypes dataType)
    {
        long baseValueLoadedId = baseValueLoaded.getId();

        Update update;
        switch(dataType)
        {
            case INTEGER: {
                update = sqlGenerator
                        .update(EAV_BE_INTEGER_VALUES)
                        .set(EAV_BE_INTEGER_VALUES.IS_LAST, false)
                        .set(EAV_BE_INTEGER_VALUES.CLOSE_DATE, baseValueForSave.getRepDate())
                        .where(EAV_BE_INTEGER_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case DATE: {
                update = sqlGenerator
                        .update(EAV_BE_DATE_VALUES)
                        .set(EAV_BE_DATE_VALUES.IS_LAST, false)
                        .set(EAV_BE_DATE_VALUES.CLOSE_DATE, baseValueForSave.getRepDate())
                        .where(EAV_BE_DATE_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case STRING: {
                update = sqlGenerator
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_STRING_VALUES.IS_LAST, false)
                        .set(EAV_BE_STRING_VALUES.CLOSE_DATE, baseValueForSave.getRepDate())
                        .where(EAV_BE_STRING_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case BOOLEAN: {
                update = sqlGenerator
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_BOOLEAN_VALUES.IS_LAST, false)
                        .set(EAV_BE_BOOLEAN_VALUES.CLOSE_DATE, baseValueForSave.getRepDate())
                        .where(EAV_BE_BOOLEAN_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case DOUBLE: {
                update = sqlGenerator
                        .update(EAV_BE_DOUBLE_VALUES)
                        .set(EAV_BE_DOUBLE_VALUES.IS_LAST, false)
                        .set(EAV_BE_DOUBLE_VALUES.CLOSE_DATE, baseValueForSave.getRepDate())
                        .where(EAV_BE_DOUBLE_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type.");
        }

        logger.debug(update.toString());
        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }

    private void rollbackClosedHistory(BaseEntity baseEntity, String attribute, DataTypes dataType)
    {
        MetaClass metaClass = baseEntity.getMeta();
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
        IBaseValue baseValue = baseEntity.getBaseValue(attribute);

        Update update;

        switch (dataType)
        {
            case INTEGER: {
                update = sqlGenerator
                        .update(EAV_BE_INTEGER_VALUES)
                        .set(EAV_BE_INTEGER_VALUES.IS_LAST, true)
                        .set(EAV_BE_INTEGER_VALUES.CLOSE_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_INTEGER_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()));
                break;
            }
            case DATE: {
                update = sqlGenerator
                        .update(EAV_BE_DATE_VALUES)
                        .set(EAV_BE_DATE_VALUES.IS_LAST, false)
                        .set(EAV_BE_DATE_VALUES.CLOSE_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_DATE_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_DATE_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()));
                break;
            }
            case STRING: {
                update = sqlGenerator
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_STRING_VALUES.IS_LAST, false)
                        .set(EAV_BE_STRING_VALUES.CLOSE_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_STRING_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_STRING_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()));
                break;
            }
            case BOOLEAN: {
                update = sqlGenerator
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_BOOLEAN_VALUES.IS_LAST, false)
                        .set(EAV_BE_BOOLEAN_VALUES.CLOSE_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_BOOLEAN_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()));
                break;
            }
            case DOUBLE: {
                update = sqlGenerator
                        .update(EAV_BE_DOUBLE_VALUES)
                        .set(EAV_BE_DOUBLE_VALUES.IS_LAST, false)
                        .set(EAV_BE_DOUBLE_VALUES.CLOSE_DATE, Configuration.historyMaxDate)
                        .where(EAV_BE_DOUBLE_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_DOUBLE_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown type.");
        }

        logger.debug(update.toString());
        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }

    private void update(IBaseValue baseValueLoaded, IBaseValue baseValueForSave, DataTypes dataType)
    {
        long baseValueLoadedId = baseValueLoaded.getId();

        Update update;
        switch(dataType)
        {
            case INTEGER: {
                update = sqlGenerator
                        .update(EAV_BE_INTEGER_VALUES)
                        .set(EAV_BE_INTEGER_VALUES.VALUE, (Integer)baseValueForSave.getValue())
                        .set(EAV_BE_INTEGER_VALUES.BATCH_ID, baseValueForSave.getBatch().getId())
                        .set(EAV_BE_INTEGER_VALUES.INDEX_, baseValueForSave.getIndex())
                        .where(EAV_BE_INTEGER_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case DATE: {
                update = sqlGenerator
                        .update(EAV_BE_DATE_VALUES)
                        .set(EAV_BE_DATE_VALUES.VALUE, DateUtils.convert((java.util.Date) baseValueForSave.getValue()))
                        .set(EAV_BE_DATE_VALUES.BATCH_ID, baseValueForSave.getBatch().getId())
                        .set(EAV_BE_DATE_VALUES.INDEX_, baseValueForSave.getIndex())
                        .where(EAV_BE_DATE_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case STRING: {
                update = sqlGenerator
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_STRING_VALUES.VALUE, (String)baseValueForSave.getValue())
                        .set(EAV_BE_STRING_VALUES.BATCH_ID, baseValueForSave.getBatch().getId())
                        .set(EAV_BE_STRING_VALUES.INDEX_, baseValueForSave.getIndex())
                        .where(EAV_BE_STRING_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case BOOLEAN: {
                update = sqlGenerator
                        .update(EAV_BE_STRING_VALUES)
                        .set(EAV_BE_BOOLEAN_VALUES.VALUE, (Boolean)baseValueForSave.getValue())
                        .set(EAV_BE_BOOLEAN_VALUES.BATCH_ID, baseValueForSave.getBatch().getId())
                        .set(EAV_BE_BOOLEAN_VALUES.INDEX_, baseValueForSave.getIndex())
                        .where(EAV_BE_BOOLEAN_VALUES.ID.eq(baseValueLoadedId));
                break;
            }
            case DOUBLE: {
                update = sqlGenerator
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
        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }

    private boolean isPreviousClosed(BaseEntity baseEntity, String attribute)
    {
        MetaClass metaClass = baseEntity.getMeta();
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
        IMetaType metaType = metaAttribute.getMetaType();
        IBaseValue baseValue = baseEntity.getBaseValue(attribute);

        Select select;

        switch (((MetaValue)metaType).getTypeCode())
        {
            case INTEGER: {
                select = sqlGenerator
                        .select(count().as("history_count"))
                        .from(EAV_BE_INTEGER_VALUES)
                        .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_INTEGER_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()))
                        .and(EAV_BE_INTEGER_VALUES.VALUE.eq((Integer)baseValue.getValue()))
                        .limit(1);
                break;
            }
            case DATE: {
                select = sqlGenerator
                        .select(count().as("history_count"))
                        .from(EAV_BE_DATE_VALUES)
                        .where(EAV_BE_DATE_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_DATE_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()))
                        .and(EAV_BE_DATE_VALUES.VALUE.eq(DateUtils.convert((java.util.Date) baseValue.getValue())))
                        .limit(1);
                break;
            }
            case STRING: {
                select = sqlGenerator
                        .select(count().as("history_count"))
                        .from(EAV_BE_STRING_VALUES)
                        .where(EAV_BE_STRING_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_STRING_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()))
                        .and(EAV_BE_STRING_VALUES.VALUE.eq((String)baseValue.getValue()))
                        .limit(1);
                break;
            }
            case BOOLEAN: {
                select = sqlGenerator
                        .select(count().as("history_count"))
                        .from(EAV_BE_BOOLEAN_VALUES)
                        .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_BOOLEAN_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()))
                        .and(EAV_BE_BOOLEAN_VALUES.VALUE.eq((Boolean)baseValue.getValue()))
                        .limit(1);
                break;
            }
            case DOUBLE: {
                select = sqlGenerator
                        .select(count().as("history_count"))
                        .from(EAV_BE_DOUBLE_VALUES)
                        .where(EAV_BE_DOUBLE_VALUES.ENTITY_ID.eq(baseEntity.getId()))
                        .and(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_DOUBLE_VALUES.CLOSE_DATE.eq(baseValue.getRepDate()))
                        .and(EAV_BE_DOUBLE_VALUES.VALUE.eq((Double)baseValue.getValue()))
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
    }

}
