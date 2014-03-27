package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBeDateSetValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.Insert;
import org.jooq.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_DATE_SET_VALUES;

/**
 *
 */
@Repository
public class BeDateSetValueDaoImpl extends JDBCSupport implements IBeDateSetValueDao
{

    private final Logger logger = LoggerFactory.getLogger(BeDateSetValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public long insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue)persistable;
        long baseValueId = insert(
                baseValue.getBaseContainer().getId(),
                baseValue.getBatch().getId(),
                baseValue.getIndex(),
                baseValue.getRepDate(),
                baseValue.getValue(),
                baseValue.isClosed(),
                baseValue.isLast());
        baseValue.setId(baseValueId);

        return baseValueId;
    }

    protected long insert(long baseSetId, long batchId, long index, Date reportDate,
                          Object value, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_DATE_SET_VALUES)
                .set(EAV_BE_DATE_SET_VALUES.SET_ID, baseSetId)
                .set(EAV_BE_DATE_SET_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_DATE_SET_VALUES.INDEX_, index)
                .set(EAV_BE_DATE_SET_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_DATE_SET_VALUES.VALUE, DataUtils.convert((Date) value))
                .set(EAV_BE_DATE_SET_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_DATE_SET_VALUES.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue)persistable;
        update(baseValue.getId(), baseValue.getBaseContainer().getId(), baseValue.getBatch().getId(),
                baseValue.getIndex(), baseValue.getRepDate(), baseValue.getValue(),
                baseValue.isClosed(), baseValue.isLast());
    }

    protected void update(long id, long baseEntityId, long batchId, long index, Date reportDate,
                          Object value, boolean closed, boolean last)
    {
        String tableAlias = "dsv";
        Update update = context
                .update(EAV_BE_DATE_SET_VALUES.as(tableAlias))
                .set(EAV_BE_DATE_SET_VALUES.as(tableAlias).SET_ID, baseEntityId)
                .set(EAV_BE_DATE_SET_VALUES.as(tableAlias).BATCH_ID, batchId)
                .set(EAV_BE_DATE_SET_VALUES.as(tableAlias).INDEX_, index)
                .set(EAV_BE_DATE_SET_VALUES.as(tableAlias).REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_DATE_SET_VALUES.as(tableAlias).VALUE, DataUtils.convert((Date) value))
                .set(EAV_BE_DATE_SET_VALUES.as(tableAlias).IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_DATE_SET_VALUES.as(tableAlias).IS_LAST, DataUtils.convert(last))
                .where(EAV_BE_DATE_SET_VALUES.as(tableAlias).ID.equal(id));

        logger.debug(update.toString());
        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());
        if (count != 1)
        {
            throw new RuntimeException("UPDATE operation should be update only one record.");
        }
    }

    @Override
    public void delete(IPersistable persistable) {
        delete(persistable.getId());
    }

    protected void delete(long id) {
        String tableAlias = "dsv";
        Delete delete = context
                .delete(EAV_BE_DATE_SET_VALUES.as(tableAlias))
                .where(EAV_BE_DATE_SET_VALUES.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
        if (count != 1)
        {
            throw new RuntimeException("DELETE operation should be delete only one record.");
        }
    }

    @Override
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        return null;
    }

    @Override
    public IBaseValue getNextBaseValue(IBaseValue baseValue) {
        return null;
    }

    @Override
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        return null;
    }
}
