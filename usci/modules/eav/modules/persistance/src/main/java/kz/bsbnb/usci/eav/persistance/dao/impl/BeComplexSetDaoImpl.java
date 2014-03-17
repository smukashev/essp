package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBeComplexSetDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 *
 */
@Repository
public class BeComplexSetDaoImpl extends JDBCSupport implements IBeComplexSetDao {

    private final Logger logger = LoggerFactory.getLogger(BeComplexSetDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public void insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue)persistable;
        IBaseSet baseSet = (IBaseSet)baseValue.getValue();
        long baseValueId = insert(
                baseValue.getBaseContainer().getId(),
                baseValue.getMetaAttribute().getId(),
                baseSet.getId(),
                baseValue.getBatch().getId(),
                baseValue.getIndex(),
                baseValue.getRepDate(),
                baseValue.isClosed(),
                baseValue.isLast());
        baseValue.setId(baseValueId);
    }

    protected long insert(long baseEntityId, long metaAttributeId, long baseSetId, long batchId,
                          long index, Date reportDate, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_ENTITY_COMPLEX_SETS)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID, baseEntityId)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID, baseSetId)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID, batchId)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.INDEX_, index)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    protected long insert(long parentSetId, long childSetId, long batchId, long index, Date reportDate, boolean closed, boolean last) {
        Insert insert = context
                .insertInto(EAV_BE_SET_OF_COMPLEX_SETS)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.PARENT_SET_ID, parentSetId)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.CHILD_SET_ID, childSetId)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.BATCH_ID, batchId)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.INDEX_, index)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_SET_OF_COMPLEX_SETS.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_SET_OF_COMPLEX_SETS.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    protected long insert(long setId, long batchId, long index, Date reportDate, Object value, boolean closed, boolean last) {
        Insert insert = context
                .insertInto(EAV_BE_COMPLEX_SET_VALUES)
                .set(EAV_BE_COMPLEX_SET_VALUES.SET_ID, setId)
                .set(EAV_BE_COMPLEX_SET_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_COMPLEX_SET_VALUES.INDEX_, index)
                .set(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID, (Long)value)
                .set(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_COMPLEX_SET_VALUES.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {

    }

    @Override
    public void delete(IPersistable persistable) {

    }
}
