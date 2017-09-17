package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.meta.HistoryType;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseSetComplexValueDao;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_SET_VALUES;

@Repository
public class BaseSetComplexValueDaoImpl extends JDBCSupport implements IBaseSetComplexValueDao {
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    private final Logger logger = LoggerFactory.getLogger(BaseSetComplexValueDaoImpl.class);
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;
    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;

    @Override
    public long insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;
        IBaseEntity baseEntity = (IBaseEntity) baseValue.getValue();

        Insert insert = context
                .insertInto(EAV_BE_COMPLEX_SET_VALUES)
                .set(EAV_BE_COMPLEX_SET_VALUES.SET_ID, baseValue.getBaseContainer().getId())
                .set(EAV_BE_COMPLEX_SET_VALUES.CREDITOR_ID, baseValue.getCreditorId())
                .set(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE, DataUtils.convert(baseValue.getRepDate()))
                .set(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID, baseEntity.getId())
                .set(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED, DataUtils.convert(baseValue.isClosed()))
                .set(EAV_BE_COMPLEX_SET_VALUES.IS_LAST, DataUtils.convert(baseValue.isLast()));

        logger.debug(insert.toString());
        long baseValueId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        baseValue.setId(baseValueId);

        return baseValueId;
    }

    @Override
    public void update(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;
        IBaseEntity baseEntity = (IBaseEntity) baseValue.getValue();

        String tableAlias = "csv";
        Update update = context
                .update(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID, baseValue.getBaseContainer().getId())
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID, baseValue.getCreditorId())
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE, DataUtils.convert(baseValue.getRepDate()))
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID, baseEntity.getId())
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED, DataUtils.convert(baseValue.isClosed()))
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST, DataUtils.convert(baseValue.isLast()))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID.equal(baseValue.getId()));

        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.compose(Errors.E138, count, baseValue.getId()));
    }

    @Override
    public void delete(IPersistable persistable) {
        String tableAlias = "csv";
        Delete delete = context
                .delete(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID.equal(persistable.getId()));

        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.compose(Errors.E133, count, persistable.getId()));
    }

    private IBaseValue constructValue(Map<String, Object> row, IMetaType metaType, IBaseEntity childBaseEntity) {
        long id = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.ID.getName())).longValue();

        long creditorId = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.CREDITOR_ID.getName())).longValue();

        boolean last = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.IS_LAST.getName())).longValue() == 1;

        boolean closed = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED.getName())).longValue() == 1;

        Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE.getName()));

        IBaseEntity childBaseEntityLoaded = baseEntityLoadDao.loadByMaxReportDate(childBaseEntity.getId(), reportDate);

        return BaseValueFactory.create(
                MetaContainerTypes.META_SET,
                metaType,
                id,
                creditorId,
                reportDate,
                childBaseEntityLoaded,
                closed,
                last);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getExistingBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue previousBaseValue = null;

        String tableAlias = "csv";

        Select select = context
                .select(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID.equal(childBaseEntity.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.equal(DataUtils.convert(baseValue.getRepDate())));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E137, childBaseEntity.getId(), childBaseEntity.getMeta().getClassName()));

        if (rows.size() == 1)
            previousBaseValue = constructValue(rows.get(0), metaType, childBaseEntity);

        return previousBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue previousBaseValue = null;

        String tableAlias = "csv";
        String subQueryAlias = "csvn";
        Table subQueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID.equal(childBaseEntity.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                        subQueryTable.field(EAV_BE_COMPLEX_SET_VALUES.CREDITOR_ID),
                        subQueryTable.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                        subQueryTable.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED),
                        subQueryTable.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E137, childBaseEntity.getId(), childBaseEntity.getMeta().getClassName()));

        if (rows.size() == 1)
            previousBaseValue = constructValue(rows.get(0), metaType, childBaseEntity);

        return previousBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getNextBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IMetaType metaType = baseSet.getMemberType();
        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();

        IBaseValue nextBaseValue = null;

        String tableAlias = "csv";
        String subqueryAlias = "csvn";
        Table subqueryTable = context
                .select(DSL.rank()
                                .over().orderBy(EAV_BE_COMPLEX_SET_VALUES.
                                        as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID.equal(childBaseEntity.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subqueryAlias);

        Select select = context
                .select(subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.CREDITOR_ID),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                .from(subqueryTable)
                .where(subqueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E136, childBaseEntity.getId(), childBaseEntity.getMeta().getClassName()));

        if (rows.size() >= 1)
            nextBaseValue = constructValue(rows.get(0), metaType, childBaseEntity);

        return nextBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
        IMetaType metaType = baseSet.getMemberType();

        if (baseContainer.getId() == 0)
            throw new RuntimeException(Errors.compose(Errors.E134));

        IBaseValue closedBaseValue = null;

        String tableAlias = "csv";
        Select select = context
                .select(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.lessOrEqual(DataUtils.convert(baseValue.getRepDate())))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID.equal(childBaseEntity.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E135,
                    childBaseEntity.getId(), childBaseEntity.getMeta().getClassName()));

        if (rows.size() == 1)
            closedBaseValue = constructValue(rows.get(0), metaType, childBaseEntity);

        return closedBaseValue;
    }

    @Override
    public IBaseValue getLastBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue lastBaseValue = null;

        String tableAlias = "csv";
        Select select = context
                .select(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID.equal(childBaseEntity.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, select.toString()));

        if (rows.size() == 1)
            lastBaseValue = constructValue(rows.get(0), metaType, childBaseEntity);

        return lastBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadBaseValues(IBaseSet baseSet, Date existingReportDate, Date savingReportDate) {
        Table tableOfValues = EAV_BE_COMPLEX_SET_VALUES.as("csv");
        Select select;

        Date loadingDate = savingReportDate == null ? existingReportDate : savingReportDate.compareTo(existingReportDate) >= 0 ? savingReportDate : existingReportDate;

        Table tableNumbering = context
                .select(DSL.rank().over()
                                .partitionBy(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID))
                                .orderBy(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE).desc()).as("num_pp"),
                        tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                        tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.CREDITOR_ID),
                        tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID),
                        tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                        tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED),
                        tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                .from(tableOfValues)
                .where(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.SET_ID).eq(baseSet.getId()))
                .and(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE).lessOrEqual(DataUtils.convert(loadingDate)))
                .asTable("csvn");

        select = context
                .select(tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                        tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.CREDITOR_ID),
                        tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                        tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID),
                        tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED),
                        tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                .from(tableNumbering)
                .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                .and(tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED).equal(false));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            long id = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.ID.getName())).longValue();

            long creditorId = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.CREDITOR_ID.getName())).longValue();

            long entityValueId = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.getName())).longValue();

            boolean isLast = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.IS_LAST.getName())).longValue() == 1;

            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE.getName()));

            IBaseEntity baseEntity = baseEntityLoadDao.loadByMaxReportDate(entityValueId, loadingDate);

            Date repDate = baseEntity.getBaseEntityReportDate().getReportDate();

            if (true && baseEntity.getMeta().getHistoryType().equals(HistoryType.ADVANCED) && !repDate.equals(loadingDate))
                continue;

            baseSet.put(BaseValueFactory.create(
                    MetaContainerTypes.META_SET,
                    baseSet.getMemberType(),
                    id,
                    creditorId,
                    reportDate,
                    baseEntity,
                    false,
                    isLast));
        }
    }

    @Override
    public void deleteAll(long baseSetId) {
        String tableAlias = "cv";
        Delete delete = context
                .delete(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseSetId));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }
}
