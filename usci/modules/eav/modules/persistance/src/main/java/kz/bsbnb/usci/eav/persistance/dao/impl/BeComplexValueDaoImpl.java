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
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertValuesStep7;
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

        InsertValuesStep7 insert = context
                .insertInto(
                        EAV_BE_COMPLEX_VALUES,
                        EAV_BE_COMPLEX_VALUES.ENTITY_ID,
                        EAV_BE_COMPLEX_VALUES.BATCH_ID,
                        EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID,
                        EAV_BE_COMPLEX_VALUES.INDEX_,
                        EAV_BE_COMPLEX_VALUES.REP_DATE,
                        EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID,
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
                        metaAttribute.getId(), batchValue.getIndex(), batchValue.getRepDate(), childBaseEntity.getId(), true};

                insert = insert.values(Arrays.asList(insertArgs));
            }
        }

        logger.debug(insert.toString());
        batchUpdateWithStats(insert.getSQL(), insert.getBindValues());
    }

    @Override
    public void update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute) {
        throw new UnsupportedOperationException("Not implemented yet.");
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
