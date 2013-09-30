package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeComplexValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_VALUES;

/**
 * @author a.motov
 */
@Repository
public class BeComplexValueDaoImpl extends JDBCSupport implements IBeComplexValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeComplexValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private IBaseEntityDao baseEntityDao;

    @Override
    public IBaseValue save(IBaseEntity baseEntity, String attribute)
    {
        IMetaAttribute metaAttribute = baseEntity.getMetaAttribute(attribute);
        IBaseValue baseValue = baseEntity.getBaseValue(attribute);

        if (baseValue.getValue() == null)
        {
            throw new IllegalArgumentException("Instance of BaseValue should not contain null values.");
        }

        IBaseEntity childBaseEntity = (BaseEntity) baseValue.getValue();
        childBaseEntity = baseEntityDao.saveOrUpdate(childBaseEntity);

        long baseValueId = save( baseEntity.getId(), baseValue.getBatch().getId(),
                metaAttribute.getId(), baseValue.getIndex(), baseValue.getRepDate(),
                childBaseEntity.getId(), baseValue.isClosed(), baseValue.isLast());
        baseValue.setId(baseValueId);

        return baseValue;
    }

    @Override
    public Map<String, IBaseValue> save(IBaseEntity baseEntity, Set<String> attributes) {
        Map<String, IBaseValue> values = new HashMap<String, IBaseValue>();
        for (String attribute: attributes)
        {
            IBaseValue baseValue = save(baseEntity, attribute);
            values.put(attribute, baseValue);
        }
        return values;
    }

    private long save(long baseEntityId, long batchId, long metaAttributeId, long index,
                      Date reportDate, long childBaseEntityId, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_COMPLEX_VALUES)
                .set(EAV_BE_COMPLEX_VALUES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_COMPLEX_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_COMPLEX_VALUES.INDEX_, index)
                .set(EAV_BE_COMPLEX_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID, childBaseEntityId)
                .set(EAV_BE_COMPLEX_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_COMPLEX_VALUES.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public IBaseValue update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute)
    {
        MetaClass metaClass = baseEntityLoaded.getMeta();
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);

        IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);
        IBaseValue baseValueForSave = baseEntityForSave.getBaseValue(attribute);

        IBaseEntity baseEntity = baseEntityDao.saveOrUpdate((IBaseEntity)baseValueForSave.getValue());

        if (baseValueLoaded == null)
        {
            if (baseValueForSave == null)
            {
                logger.warn(String.format("An attempt was made to remove a missing value for the " +
                        "attribute {0} of BaseEntity instance with identifier {1}.", baseEntityLoaded.getId(), attribute));
            }
            else
            {
                //TODO: Check this attribute in the future, if exist then isLast = false
                long baseValueId = save(baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                        metaAttribute.getId(), baseValueForSave.getIndex(),
                        baseValueForSave.getRepDate(), baseEntity.getId(), false, true);
                baseValueForSave.setId(baseValueId);
            }
        }
        else
        {
            int compare = DataUtils.compareBeginningOfTheDay(baseValueForSave.getRepDate(), baseValueLoaded.getRepDate());
            switch(compare)
            {
                case 1:
                {
                    long baseValueId = save(baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                            metaAttribute.getId(), baseValueForSave.getIndex(), baseValueForSave.getRepDate(),
                            baseEntity.getId(), baseValueForSave.isClosed(), baseValueForSave.isLast());
                    baseValueForSave.setId(baseValueId);

                    if (baseValueLoaded.isLast())
                    {
                        Map<String, Object> fields = new HashMap<String, Object>();
                        fields.put("is_last", false);

                        Map<String, Object> conditions = new HashMap<String, Object>();
                        conditions.put("id", baseValueLoaded.getId());

                        updateByCondition(fields, conditions);
                    }
                    break;
                }
                case 0:
                    Map<String, Object> fields = new HashMap<String, Object>();
                    fields.put("batch_id", baseValueForSave.getBatch().getId());
                    fields.put("index_", baseValueForSave.getIndex());
                    fields.put("entityValueId", baseEntity.getId());
                    if (baseValueForSave.isClosed())
                    {
                        fields.put("is_closed", true);
                    }

                    Map<String, Object> conditions = new HashMap<String, Object>();
                    conditions.put("id", baseValueLoaded.getId());

                    updateByCondition(fields, conditions);

                    break;
                case -1:
                {
                    long baseValueId = save(baseEntityForSave.getId(), baseValueForSave.getBatch().getId(),
                            metaAttribute.getId(), baseValueForSave.getIndex(), baseValueForSave.getRepDate(),
                            baseEntity.getId(), baseValueForSave.isClosed(), false);
                    baseValueForSave.setId(baseValueId);
                    break;
                }
                default:
                    throw new RuntimeException("Method Comparable<T>.compareTo(T o) " +
                            "can not return a value other than -1, 0, 1.");
            }
        }

        return baseValueForSave;
    }

    @Override
    public boolean presentInFuture(IBaseEntity baseEntity, String attribute) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private int updateByCondition(Map<String, Object> fields,
                                              Map<String, Object> conditions)
    {
        if (fields.size() == 0)
        {
            throw new IllegalArgumentException("To implement the changes to the record must have at least one field.");
        }

        Table tableOfIntegerValues = EAV_BE_COMPLEX_VALUES.as("v");
        UpdateSetStep updateSetStep = context.update(tableOfIntegerValues);

        UpdateSetMoreStep updateSetMoreStep = null;
        if (fields.containsKey("batch_id"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.BATCH_ID),
                            fields.get("batch_id")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.BATCH_ID),
                            fields.get("batch_id"));
        }
        if (fields.containsKey("index_"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.INDEX_),
                            fields.get("index_")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.INDEX_),
                            fields.get("index_"));
        }
        if (fields.containsKey("entityValueId"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID),
                            fields.get("entityValueId")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID),
                            fields.get("entityValueId"));
        }
        if (fields.containsKey("is_closed"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED),
                            fields.get("is_closed")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.IS_CLOSED),
                            fields.get("is_closed"));
        }
        if (fields.containsKey("is_last"))
        {
            updateSetMoreStep = updateSetMoreStep == null ?
                    updateSetStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.IS_LAST),
                            fields.get("is_last")) :
                    updateSetMoreStep.set(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.IS_LAST),
                            fields.get("is_last"));
        }

        UpdateConditionStep updateConditionStep = null;
        if (conditions.containsKey("id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.ID)
                            .equal(conditions.get("id"))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.ID)
                            .equal(conditions.get("id")));
        }
        if (conditions.containsKey("entity_id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.ENTITY_ID)
                            .equal(conditions.get("entity_id"))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.ENTITY_ID)
                            .equal(conditions.get("entity_id")));
        }
        if (conditions.containsKey("attribute_id"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID)
                            .equal(conditions.get("attribute_id"))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID)
                            .equal(conditions.get("attribute_id")));
        }
        if (conditions.containsKey("report_date"))
        {
            updateConditionStep = updateConditionStep == null ?
                    updateSetMoreStep.where(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE)
                            .equal(DataUtils.convert((Date) conditions.get("report_date")))) :
                    updateConditionStep.and(tableOfIntegerValues.field(EAV_BE_COMPLEX_VALUES.REPORT_DATE)
                            .equal(DataUtils.convert((Date) conditions.get("report_date"))));
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
                    "Removing a complex value is not possible.");
        }

        long metaAttributeId =  metaAttribute.getId();
        long baseEntityId = baseEntity.getId();

        if (baseEntityId < 1)
        {
            throw new IllegalArgumentException("BaseEntity does not contain id. " +
                    "Removing a complex value is not possible.");
        }
        if (metaAttributeId < 1)
        {
            throw new IllegalArgumentException("MetaAttribute does not contain id. " +
                    "Removing a complex value is not possible.");
        }

        DeleteConditionStep delete = context
                .delete(EAV_BE_COMPLEX_VALUES)
                .where(EAV_BE_COMPLEX_VALUES.ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID.eq(metaAttributeId));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }

}
