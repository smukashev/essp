package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseSetDoubleValueDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_DOUBLE_SET_VALUES;

/**
 *
 */
@Repository
public class BaseSetDoubleValueDaoImpl extends JDBCSupport implements IBaseSetDoubleValueDao
{

    private final Logger logger = LoggerFactory.getLogger(BaseSetDoubleValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private IBatchRepository batchRepository;

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
                .insertInto(EAV_BE_DOUBLE_SET_VALUES)
                .set(EAV_BE_DOUBLE_SET_VALUES.SET_ID, baseSetId)
                .set(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_DOUBLE_SET_VALUES.INDEX_, index)
                .set(EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_DOUBLE_SET_VALUES.VALUE, (Double)value)
                .set(EAV_BE_DOUBLE_SET_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_DOUBLE_SET_VALUES.IS_LAST, DataUtils.convert(last));

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

    protected void update(long id, long baseSetId, long batchId, long index,
                          Date reportDate, Object value, boolean closed, boolean last)
    {
        String tableAlias = "dsv";
        Update update = context
                .update(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias))
                .set(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).SET_ID, baseSetId)
                .set(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).BATCH_ID, batchId)
                .set(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).INDEX_, index)
                .set(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).VALUE, (Double)value)
                .set(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).IS_LAST, DataUtils.convert(last))
                .where(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).ID.equal(id));

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
                .delete(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias))
                .where(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
        if (count != 1)
        {
            throw new RuntimeException("DELETE operation should be delete only one record.");
        }
    }

    @Override
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        throw new UnsupportedOperationException("Не реализовано;");
    }

    @Override
    public IBaseValue getNextBaseValue(IBaseValue baseValue) {
        throw new UnsupportedOperationException("Не реализовано;");
    }

    @Override
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        throw new UnsupportedOperationException("Не реализовано;");
    }

    @Override
    public IBaseValue getLastBaseValue(IBaseValue baseValue) {
        throw new UnsupportedOperationException("Не реализовано;");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadBaseValues(IBaseSet baseSet, Date actualReportDate, boolean lastReportDate)
    {
        Table tableOfValues = EAV_BE_DOUBLE_SET_VALUES.as("dsv");
        Select select;
        if (lastReportDate)
        {
            select = context
                    .select(tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.ID),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.SET_ID).equal(baseSet.getId()))
                    .and(tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.IS_LAST).equal(true)
                            .and(tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.VALUE))
                            .orderBy(tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE).desc()).as("num_pp"),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.ID),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.SET_ID).eq(baseSet.getId()))
                    .and(tableOfValues.field(EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(actualReportDate)))
                    .asTable("dsvn");

            select = context
                    .select(tableNumbering.field(EAV_BE_DOUBLE_SET_VALUES.ID),
                            tableNumbering.field(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID),
                            tableNumbering.field(EAV_BE_DOUBLE_SET_VALUES.INDEX_),
                            tableNumbering.field(EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE),
                            tableNumbering.field(EAV_BE_DOUBLE_SET_VALUES.VALUE),
                            tableNumbering.field(EAV_BE_DOUBLE_SET_VALUES.IS_LAST))
                    .from(tableNumbering)
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_DOUBLE_SET_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            long id = ((BigDecimal) row.get(EAV_BE_DOUBLE_SET_VALUES.ID.getName())).longValue();
            long batchId = ((BigDecimal)row.get(EAV_BE_DOUBLE_SET_VALUES.BATCH_ID.getName())).longValue();
            long index = ((BigDecimal) row.get(EAV_BE_DOUBLE_SET_VALUES.INDEX_.getName())).longValue();
            boolean last = ((BigDecimal)row.get(EAV_BE_DOUBLE_SET_VALUES.IS_LAST.getName())).longValue() == 1;
            double value = ((BigDecimal)row.get(EAV_BE_DOUBLE_SET_VALUES.VALUE.getName())).doubleValue();
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_DOUBLE_SET_VALUES.REPORT_DATE.getName()));

            Batch batch = batchRepository.getBatch(batchId);
            baseSet.put(
                    BaseValueFactory.create(
                            MetaContainerTypes.META_SET, baseSet.getMemberType(), id, batch, index, reportDate, value, false, last));
        }
    }

    @Override
    public void deleteAll(long baseSetId) {
        String tableAlias = "dsv";
        Delete delete = context
                .delete(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias))
                .where(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).SET_ID.equal(baseSetId));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public Date getNextReportDate(long baseSetId, Date reportDate)
    {
        String tableAlias = "dsv";
        Select select = context
                .select(DSL.min(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).REPORT_DATE).as("next_report_date"))
                .from(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias))
                .where(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).SET_ID.eq(baseSetId))
                .and(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        if (rows.size() > 0)
        {
            return DataUtils.convert((Timestamp) rows.get(0).get("next_report_date"));
        }
        return null;
    }

    @Override
    public Date getPreviousReportDate(long baseSetId, Date reportDate)
    {
        String tableAlias = "dsv";
        Select select = context
                .select(DSL.max(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).REPORT_DATE).as("previous_report_date"))
                .from(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias))
                .where(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).SET_ID.eq(baseSetId))
                .and(EAV_BE_DOUBLE_SET_VALUES.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        if (rows.size() > 0)
        {
            return DataUtils.convert((Timestamp) rows.get(0).get("previous_report_date"));
        }
        return null;
    }

}
