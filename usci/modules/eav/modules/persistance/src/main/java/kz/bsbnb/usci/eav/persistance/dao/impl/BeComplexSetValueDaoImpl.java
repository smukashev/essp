package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.persistance.dao.IBeComplexSetValueDao;
import kz.bsbnb.usci.eav.util.DateUtils;
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
public class BeComplexSetValueDaoImpl extends AbstractBeSetValueDaoImpl implements IBeComplexSetValueDao {

    private final Logger logger = LoggerFactory.getLogger(BeComplexSetValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    protected long save(long baseEntityId, long metaAttributeId, long baseSetId, long batchId,
                        long index, Date reportDate, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_ENTITY_COMPLEX_SETS)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID, baseEntityId)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID, baseSetId)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.BATCH_ID, batchId)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.INDEX_, index)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE, DateUtils.convert(reportDate))
                .set(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED, closed)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST, last);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    protected long save(long parentSetId, long childSetId, long batchId, long index, Date reportDate, boolean closed, boolean last) {
        Insert insert = context
                .insertInto(EAV_BE_SET_OF_COMPLEX_SETS)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.PARENT_SET_ID, parentSetId)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.CHILD_SET_ID, childSetId)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.BATCH_ID, batchId)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.INDEX_, index)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.REPORT_DATE, DateUtils.convert(reportDate))
                .set(EAV_BE_SET_OF_COMPLEX_SETS.IS_CLOSED, closed)
                .set(EAV_BE_SET_OF_COMPLEX_SETS.IS_LAST, last);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    protected long save(long setId, long batchId, long index, Date reportDate, Object value, boolean closed, boolean last) {
        Insert insert = context
                .insertInto(EAV_BE_COMPLEX_SET_VALUES)
                .set(EAV_BE_COMPLEX_SET_VALUES.SET_ID, setId)
                .set(EAV_BE_COMPLEX_SET_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_COMPLEX_SET_VALUES.INDEX_, index)
                .set(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE, DateUtils.convert(reportDate))
                .set(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID, (Long)value)
                .set(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED, closed)
                .set(EAV_BE_COMPLEX_SET_VALUES.IS_LAST, last);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

}
