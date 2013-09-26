package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.persistance.dao.IBeBooleanValueDao;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_BOOLEAN_VALUES;

/**
 *
 */
@Repository
public class BeBooleanValueDaoImpl extends AbstractBeValueDaoImpl implements IBeBooleanValueDao
{

    private final Logger logger = LoggerFactory.getLogger(BeBooleanValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    protected long save(long baseEntityId, long batchId, long metaAttributeId, long index,
                        Date reportDate, Object value, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_BOOLEAN_VALUES)
                .set(EAV_BE_BOOLEAN_VALUES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_BOOLEAN_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_BOOLEAN_VALUES.INDEX_, index)
                .set(EAV_BE_BOOLEAN_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_BOOLEAN_VALUES.VALUE, DataUtils.convert((Boolean)value))
                .set(EAV_BE_BOOLEAN_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_BOOLEAN_VALUES.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @SuppressWarnings({"constantconditions", "unchecked"})
    @Override
    protected int updateByCondition(Map<String, Object> fields, Map<String, Object> conditions)
    {
        if (fields.size() == 0)
        {
            throw new IllegalArgumentException("To implement the changes to the record must have at least one field.");
        }

        Table tableOfValues = EAV_BE_BOOLEAN_VALUES.as("v");
        UpdateSetStep updateSetStep = context.update(tableOfValues);

        UpdateSetMoreStep updateSetMoreStep = null;
        if (fields.containsKey("batch_id"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.BATCH_ID),
                            fields.get("batch_id")) :
                    updateSetMoreStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.BATCH_ID),
                            fields.get("batch_id"));
        }
        if (fields.containsKey("index_"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.INDEX_),
                            fields.get("index_")) :
                    updateSetMoreStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.INDEX_),
                            fields.get("index_"));
        }
        if (fields.containsKey("value"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                            fields.get("value")) :
                    updateSetMoreStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                            fields.get("value"));
        }
        if (fields.containsKey("is_closed"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                            fields.get("is_closed")) :
                    updateSetMoreStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                            fields.get("is_closed"));
        }
        if (fields.containsKey("is_last"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_LAST),
                            fields.get("is_last")) :
                    updateSetMoreStep.set(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_LAST),
                            fields.get("is_last"));
        }

        UpdateConditionStep updateConditionStep = null;
        if (conditions.containsKey("id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ID)
                            .equal(conditions.get("id"))) :
                    updateConditionStep.and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ID)
                            .equal(conditions.get("id")));
        }
        if (conditions.containsKey("entity_id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ENTITY_ID)
                            .equal(conditions.get("entity_id"))) :
                    updateConditionStep.and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ENTITY_ID)
                            .equal(conditions.get("entity_id")));
        }
        if (conditions.containsKey("attribute_id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID)
                            .equal(conditions.get("attribute_id"))) :
                    updateConditionStep.and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID)
                            .equal(conditions.get("attribute_id")));
        }
        if (conditions.containsKey("report_date"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE)
                            .equal(DataUtils.convert((Date) conditions.get("report_date")))) :
                    updateConditionStep.and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE)
                            .equal(DataUtils.convert((Date) conditions.get("report_date"))));
        }

        logger.debug(updateConditionStep.toString());
        return updateWithStats(updateConditionStep.getSQL(), updateConditionStep.getBindValues().toArray());
    }

    @SuppressWarnings({"constantconditions", "unchecked"})
    @Override
    public boolean presentInFuture(long entityId, long attributeId, Date reportDate) {

        Table table = EAV_BE_BOOLEAN_VALUES.as("v");
        Select select = context
                .select(DSL.count().as("values_count"))
                .from(table)
                .where(table.field(EAV_BE_BOOLEAN_VALUES.ENTITY_ID).equal(entityId))
                .and(table.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID).equal(attributeId))
                .and(table.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE).greaterThan(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        long valuesCount = (Long)rows.get(0).get("values_count");

        return valuesCount != 0;
    }

}
