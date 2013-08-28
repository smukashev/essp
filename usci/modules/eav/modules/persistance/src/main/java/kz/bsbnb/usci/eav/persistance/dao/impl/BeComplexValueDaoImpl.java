package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.dao.IBeComplexValueDao;
import kz.bsbnb.usci.eav.util.DateUtils;
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
    public long save(IBaseEntity baseEntity, String attribute) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void save(IBaseEntity baseEntity, Set<String> attributes) {
        MetaClass meta = baseEntity.getMeta();

        InsertValuesStep8 insert = context
                .insertInto(
                        EAV_BE_COMPLEX_VALUES,
                        EAV_BE_COMPLEX_VALUES.ENTITY_ID,
                        EAV_BE_COMPLEX_VALUES.BATCH_ID,
                        EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID,
                        EAV_BE_COMPLEX_VALUES.INDEX_,
                        EAV_BE_COMPLEX_VALUES.REPORT_DATE,
                        EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID,
                        EAV_BE_COMPLEX_VALUES.IS_CLOSED,
                        EAV_BE_COMPLEX_VALUES.IS_LAST);

        Iterator<String> it = attributes.iterator();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            IBaseValue batchValue = baseEntity.getBaseValue(attributeNameForInsert);
            IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeNameForInsert);

            if (batchValue.getValue() != null)
            {
                IBaseEntity childBaseEntity = (BaseEntity) batchValue.getValue();
                childBaseEntity = baseEntityDao.saveOrUpdate(childBaseEntity);

                Object[] insertArgs = new Object[] {baseEntity.getId(), batchValue.getBatch().getId(),
                        metaAttribute.getId(), batchValue.getIndex(), batchValue.getRepDate(), childBaseEntity.getId(), false, true};

                insert = insert.values(Arrays.asList(insertArgs));
            }
        }

        logger.debug(insert.toString());
        batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
    }

    @Override
    public void update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute)
    {
        MetaClass metaClass = baseEntityLoaded.getMeta();
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);

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

            Date reportDate = baseValueForSave.getRepDate();
            Set<Date> availableReportDates = baseEntityForSave.getAvailableReportDates();
            if (availableReportDates.size() == 1 ||
                    DateUtils.compareBeginningOfTheDay(reportDate, baseEntityLoaded.getMinReportDate()) == 1)
            {
                // This instance of BaseEntity receive only to one report date
                // and the attributes in the last time has not been sent.
                // Also can be that this attribute has not been previously sent or it was closed.

            }
            else
            {
                if (DateUtils.compareBeginningOfTheDay(reportDate, baseEntityForSave.getMinReportDate()) == 1)
                {
                    // This instance of BaseEntity was saved only by subsequent reporting dates.
                    // Need to save this attribute to the minimum reporting date.

                }
            }

            /*boolean previousClosed = isPreviousClosed(baseEntityForSave.getId(), metaAttribute.getId(),
                    baseValueForSave.getRepDate(), baseValueForSave.getValue(), dataType);
            if (previousClosed)
            {
                rollbackClosedHistory(baseEntityForSave.getId(), metaAttribute.getId(),
                        baseValueForSave.getRepDate(), dataType);
            }
            else
            {
                long baseValueId = save(baseEntityForSave, attribute, dataType);
                baseValueForSave.setId(baseValueId);
            }
            return;*/
        }
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
