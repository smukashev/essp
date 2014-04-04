package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBeStringSetValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
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

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_STRING_SET_VALUES;

/**
 *
 */
@Repository
public class BeStringSetValueDaoImpl extends JDBCSupport implements IBeStringSetValueDao
{

    private final Logger logger = LoggerFactory.getLogger(BeStringSetValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    IBatchRepository batchRepository;

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
                .insertInto(EAV_BE_STRING_SET_VALUES)
                .set(EAV_BE_STRING_SET_VALUES.SET_ID, baseSetId)
                .set(EAV_BE_STRING_SET_VALUES.BATCH_ID, batchId)
                .set(EAV_BE_STRING_SET_VALUES.INDEX_, index)
                .set(EAV_BE_STRING_SET_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_STRING_SET_VALUES.VALUE, (String)value)
                .set(EAV_BE_STRING_SET_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_STRING_SET_VALUES.IS_LAST, DataUtils.convert(last));

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
        String tableAlias = "sv";
        Update update = context
                .update(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID, baseSetId)
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).BATCH_ID, batchId)
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).INDEX_, index)
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE, (String)value)
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST, DataUtils.convert(last))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).ID.equal(id));

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

    protected void delete(long id)
    {
        String tableAlias = "sv";
        Delete delete = context
                .delete(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
        if (count != 1)
        {
            throw new RuntimeException("DELETE operation should be delete only one record.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet)baseContainer;
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue previousBaseValue = null;

        String tableAlias = "bsv";
        String subqueryAlias = "bsvn";
        Table subqueryTable = context
                .select(DSL.rank().over()
                        .orderBy(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).BATCH_ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).INDEX_,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE.equal((String) baseValue.getValue()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subqueryAlias);

        Select select = context
                .select(subqueryTable.field(EAV_BE_STRING_SET_VALUES.ID),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.BATCH_ID),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.INDEX_),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                .from(subqueryTable)
                .where(subqueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new RuntimeException("Query for get next instance of BaseValue return more than one row.");
        }

        if (rows.size() == 1)
        {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_STRING_SET_VALUES.ID.getName())).longValue();
            long index = ((BigDecimal) row
                    .get(EAV_BE_STRING_SET_VALUES.INDEX_.getName())).longValue();
            long batchId = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.BATCH_ID.getName())).longValue();
            boolean last = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.IS_LAST.getName())).longValue() == 1;
            boolean closed = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.IS_CLOSED.getName())).longValue() == 1;
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_STRING_SET_VALUES.REPORT_DATE.getName()));

            Batch batch = batchRepository.getBatch(batchId);

            previousBaseValue = BaseValueFactory.create(MetaContainerTypes.META_SET, metaType,
                    id, batch, index, reportDate, baseValue.getValue(), closed, last);
        }

        return previousBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getNextBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet)baseContainer;
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue nextBaseValue = null;

        String tableAlias = "bsv";
        String subqueryAlias = "bsvn";
        Table subqueryTable = context
                .select(DSL.rank()
                        .over().orderBy(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).BATCH_ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).INDEX_,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE.equal((String) baseValue.getValue()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subqueryAlias);

        Select select = context
                .select(subqueryTable.field(EAV_BE_STRING_SET_VALUES.ID),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.BATCH_ID),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.INDEX_),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED),
                        subqueryTable.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                .from(subqueryTable)
                .where(subqueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new RuntimeException("Query for get next instance of BaseValue return more than one row.");
        }

        if (rows.size() == 1)
        {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_STRING_SET_VALUES.ID.getName())).longValue();
            long index = ((BigDecimal) row
                    .get(EAV_BE_STRING_SET_VALUES.INDEX_.getName())).longValue();
            long batchId = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.BATCH_ID.getName())).longValue();
            boolean last = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.IS_LAST.getName())).longValue() == 1;
            boolean closed = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.IS_CLOSED.getName())).longValue() == 1;
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_STRING_SET_VALUES.REPORT_DATE.getName()));

            Batch batch = batchRepository.getBatch(batchId);

            nextBaseValue = BaseValueFactory.create(MetaContainerTypes.META_SET, metaType,
                    id, batch, index, reportDate, baseValue.getValue(), closed, last);
        }

        return nextBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet)baseContainer;
        IMetaType metaType = baseSet.getMemberType();

        if (baseContainer == null || baseContainer.getId() < 1)
        {
            throw new RuntimeException("Can not find closed instance of BaseValue without container or container ID.");
        }

        IBaseValue closedBaseValue = null;

        String tableAlias = "bsv";
        Select select = context
                .select(EAV_BE_STRING_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).BATCH_ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).INDEX_,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.equal(DataUtils.convert(baseValue.getRepDate())))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE.equal((String) baseValue.getValue()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new RuntimeException("Query for get next instance of BaseValue return more than one row.");
        }

        if (rows.size() == 1)
        {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_STRING_SET_VALUES.ID.getName())).longValue();
            long index = ((BigDecimal) row
                    .get(EAV_BE_STRING_SET_VALUES.INDEX_.getName())).longValue();
            long batchId = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.BATCH_ID.getName())).longValue();
            boolean last = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.IS_LAST.getName())).longValue() == 1;

            Batch batch = batchRepository.getBatch(batchId);

            closedBaseValue = BaseValueFactory.create(MetaContainerTypes.META_SET, metaType,
                    id, batch, index, baseValue.getRepDate(), baseValue.getValue(), true, last);
        }

        return closedBaseValue;
    }

    @Override
    public IBaseValue getLastBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet)baseContainer;
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue lastBaseValue = null;

        String tableAlias = "bsv";
        Select select = context
                .select(EAV_BE_STRING_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).BATCH_ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).INDEX_,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED)
                .from(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE.equal((String) baseValue.getValue()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new RuntimeException("Query for get last instance of BaseValue return more than one row.");
        }

        if (rows.size() == 1)
        {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_STRING_SET_VALUES.ID.getName())).longValue();
            long index = ((BigDecimal) row
                    .get(EAV_BE_STRING_SET_VALUES.INDEX_.getName())).longValue();
            long batchId = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.BATCH_ID.getName())).longValue();
            boolean closed = ((BigDecimal)row
                    .get(EAV_BE_STRING_SET_VALUES.IS_CLOSED.getName())).longValue() == 1;
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_STRING_SET_VALUES.REPORT_DATE.getName()));

            Batch batch = batchRepository.getBatch(batchId);

            lastBaseValue = BaseValueFactory.create(MetaContainerTypes.META_SET, metaType,
                    id, batch, index, reportDate, baseValue.getValue(), closed, true);
        }

        return lastBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadBaseValues(IBaseSet baseSet, Date actualReportDate, boolean lastReportDate)
    {
        Table tableOfValues = EAV_BE_STRING_SET_VALUES.as("ssv");
        Select select;
        if (lastReportDate)
        {
            select = context
                    .select(tableOfValues.field(EAV_BE_STRING_SET_VALUES.ID),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_STRING_SET_VALUES.SET_ID).equal(baseSet.getId()))
                    .and(tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_LAST).equal(true)
                            .and(tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED).equal(false)));
        }
        else
        {
            Table tableNumbering = context
                    .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_STRING_SET_VALUES.VALUE))
                            .orderBy(tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE).desc()).as("num_pp"),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.ID),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.VALUE),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.BATCH_ID),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.INDEX_),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED),
                            tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                    .from(tableOfValues)
                    .where(tableOfValues.field(EAV_BE_STRING_SET_VALUES.SET_ID).eq(baseSet.getId()))
                    .and(tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE)
                            .lessOrEqual(DataUtils.convert(actualReportDate)))
                    .asTable("ssvn");

            select = context
                    .select(tableNumbering.field(EAV_BE_STRING_SET_VALUES.ID),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.BATCH_ID),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.INDEX_),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.VALUE),
                            tableNumbering.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                    .from(tableNumbering)
                    .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                    .and(tableNumbering.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED).equal(false));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            long id = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.ID.getName())).longValue();
            long batchId = ((BigDecimal)row.get(EAV_BE_STRING_SET_VALUES.BATCH_ID.getName())).longValue();
            long index = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.INDEX_.getName())).longValue();
            boolean last = ((BigDecimal)row.get(EAV_BE_STRING_SET_VALUES.IS_LAST.getName())).longValue() == 1;
            String value = (String)row.get(EAV_BE_STRING_SET_VALUES.VALUE.getName());
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_STRING_SET_VALUES.REPORT_DATE.getName()));

            Batch batch = batchRepository.getBatch(batchId);
            baseSet.put(
                    BaseValueFactory.create(
                            MetaContainerTypes.META_SET, baseSet.getMemberType(), id, batch, index, reportDate, value, false, last));
        }
    }

}
