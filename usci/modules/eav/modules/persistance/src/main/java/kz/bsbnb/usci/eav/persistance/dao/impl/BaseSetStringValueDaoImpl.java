package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseSetStringValueDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
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
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_STRING_SET_VALUES;

@Repository
public class BaseSetStringValueDaoImpl extends JDBCSupport implements IBaseSetStringValueDao {
    private final Logger logger = LoggerFactory.getLogger(BaseSetStringValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public long insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;

        long baseValueId = insert(
                baseValue.getBaseContainer().getId(),
                baseValue.getCreditorId(),
                baseValue.getRepDate(),
                baseValue.getValue(),
                baseValue.isClosed(),
                baseValue.isLast());

        baseValue.setId(baseValueId);

        return baseValueId;
    }

    protected long insert(long baseSetId, long creditorId, Date reportDate, Object value, boolean closed, boolean last) {
        Insert insert = context
                .insertInto(EAV_BE_STRING_SET_VALUES)
                .set(EAV_BE_STRING_SET_VALUES.SET_ID, baseSetId)
                .set(EAV_BE_STRING_SET_VALUES.CREDITOR_ID, creditorId)
                .set(EAV_BE_STRING_SET_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_STRING_SET_VALUES.VALUE, (String) value)
                .set(EAV_BE_STRING_SET_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_STRING_SET_VALUES.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());

        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;

        update(baseValue.getId(),
                baseValue.getBaseContainer().getId(),
                baseValue.getCreditorId(),
                baseValue.getRepDate(),
                baseValue.getValue(),
                baseValue.isClosed(),
                baseValue.isLast());
    }

    protected void update(long id, long baseSetId, long creditorId, Date reportDate, Object value, boolean closed, boolean last) {
        String tableAlias = "sv";
        Update update = context
                .update(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID, baseSetId)
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).CREDITOR_ID, creditorId)
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE, (String) value)
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST, DataUtils.convert(last))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).ID.equal(id));

        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.compose(Errors.E148, count, id));
    }

    @Override
    public void delete(IPersistable persistable) {
        delete(persistable.getId());
    }

    protected void delete(long id) {
        String tableAlias = "sv";
        Delete delete = context
                .delete(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());

        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.compose(Errors.E147, count, id));
    }

    private IBaseValue constructValue (Map<String, Object> row, IMetaType metaType) {
        long id = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.ID.getName())).longValue();

        long creditorId = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.ID.getName())).longValue();

        Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_STRING_SET_VALUES.REPORT_DATE.getName()));

        String value = (String) row.get(EAV_BE_STRING_SET_VALUES.VALUE.getName());

        boolean last = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.IS_LAST.getName())).longValue() == 1;

        boolean closed = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.IS_CLOSED.getName())).longValue() == 1;

        return BaseValueFactory.create(
                MetaContainerTypes.META_SET,
                metaType,
                id,
                creditorId,
                reportDate,
                value,
                closed,
                last);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getExistingBaseValue (IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if(baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue previousBaseValue = null;

        String tableAlias = "bsv";

        Select select = context
                .select(EAV_BE_STRING_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE.equal((String) baseValue.getValue()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.equal(DataUtils.convert(baseValue.getRepDate())));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException(Errors.compose(Errors.E83, baseValue.getMetaAttribute().getName()));

        if (rows.size() == 1)
            previousBaseValue = constructValue(rows.get(0), metaType);

        return previousBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if(baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue previousBaseValue = null;

        String tableAlias = "bsv";
        String subQueryAlias = "bsvn";
        Table subQueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE.equal((String) baseValue.getValue()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_STRING_SET_VALUES.ID),
                        subQueryTable.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                        subQueryTable.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED),
                        subQueryTable.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException(Errors.compose(Errors.E83, baseValue.getMetaAttribute().getName()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            previousBaseValue = constructValue(row, metaType);
        }

        return previousBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getNextBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if(baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue nextBaseValue = null;

        String tableAlias = "bsv";
        String subQueryAlias = "bsvn";
        Table subQueryTable = context
            .select(DSL.rank()
                            .over().orderBy(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                    EAV_BE_STRING_SET_VALUES.as(tableAlias).ID,
                    EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE,
                    EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED,
                    EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST)
            .from(EAV_BE_STRING_SET_VALUES.as(tableAlias))
            .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
            .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
            .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE.equal((String) baseValue.getValue()))
            .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(baseValue.getRepDate())))
            .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_STRING_SET_VALUES.ID),
                        subQueryTable.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                        subQueryTable.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED),
                        subQueryTable.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException(Errors.compose(Errors.E83, baseValue.getMetaAttribute().getName()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            nextBaseValue = constructValue(row, metaType);
        }

        return nextBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if(baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue closedBaseValue = null;

        String tableAlias = "bsv";
        Select select = context
                .select(EAV_BE_STRING_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE.lessOrEqual(DataUtils.convert(baseValue.getRepDate())))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE.equal((String) baseValue.getValue()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException(Errors.compose(Errors.E83, baseValue.getMetaAttribute().getName()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            closedBaseValue = constructValue(row, metaType);
        }

        return closedBaseValue;
    }

    @Override
    public IBaseValue getLastBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if(baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue lastBaseValue = null;

        String tableAlias = "bsv";
        Select select = context
                .select(EAV_BE_STRING_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).VALUE.equal((String) baseValue.getValue()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_STRING_SET_VALUES.as(tableAlias).IS_LAST.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException(Errors.compose(Errors.E83, baseValue.getMetaAttribute().getName()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            lastBaseValue = constructValue(row, metaType);
        }

        return lastBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadBaseValues(IBaseSet baseSet, Date existingReportDate, Date savingReportDate) {
        Table tableOfValues = EAV_BE_STRING_SET_VALUES.as("ssv");
        Select select;

        Table tableNumbering = context
                .select(DSL.rank().over()
                            .partitionBy(tableOfValues.field(EAV_BE_STRING_SET_VALUES.VALUE))
                            .orderBy(tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE).desc()).as("num_pp"),
                        tableOfValues.field(EAV_BE_STRING_SET_VALUES.ID),
                        tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                        tableOfValues.field(EAV_BE_STRING_SET_VALUES.VALUE),
                        tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED),
                        tableOfValues.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                .from(tableOfValues)
                .where(tableOfValues.field(EAV_BE_STRING_SET_VALUES.SET_ID).eq(baseSet.getId()))
                .and(tableOfValues.field(EAV_BE_STRING_SET_VALUES.CREDITOR_ID).eq(baseSet.getCreditorId()))
                .and(tableOfValues.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE).lessOrEqual(DataUtils.convert(existingReportDate)))
                .asTable("ssvn");

        select = context
                .select(tableNumbering.field(EAV_BE_STRING_SET_VALUES.ID),
                        tableNumbering.field(EAV_BE_STRING_SET_VALUES.REPORT_DATE),
                        tableNumbering.field(EAV_BE_STRING_SET_VALUES.VALUE),
                        tableNumbering.field(EAV_BE_STRING_SET_VALUES.IS_LAST))
                .from(tableNumbering)
                .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                .and(tableNumbering.field(EAV_BE_STRING_SET_VALUES.IS_CLOSED).equal(false));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            long id = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.ID.getName())).longValue();

            boolean last = ((BigDecimal) row.get(EAV_BE_STRING_SET_VALUES.IS_LAST.getName())).longValue() == 1;

            String value = (String) row.get(EAV_BE_STRING_SET_VALUES.VALUE.getName());

            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_STRING_SET_VALUES.REPORT_DATE.getName()));

            baseSet.put(BaseValueFactory.create(
                    MetaContainerTypes.META_SET,
                    baseSet.getMemberType(),
                    id,
                    baseSet.getCreditorId(),
                    reportDate,
                    value,
                    false,
                    last));
        }
    }

    @Override
    public void deleteAll(long baseSetId) {
        String tableAlias = "ssv";
        Delete delete = context
                .delete(EAV_BE_STRING_SET_VALUES.as(tableAlias))
                .where(EAV_BE_STRING_SET_VALUES.as(tableAlias).SET_ID.equal(baseSetId));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }
}
