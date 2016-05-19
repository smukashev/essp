package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityReportDateDao;
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
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITIES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITY_REPORT_DATES;

@Repository
public class BaseEntityReportDateDaoImpl extends JDBCSupport implements IBaseEntityReportDateDao {
    private final Logger logger = LoggerFactory.getLogger(BaseEntityReportDateDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public IBaseEntityReportDate load(long baseEntityId, Date reportDate) {
        if (baseEntityId < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E120));

        if (reportDate == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E121));

        String tableAlias = "rd";
        Select select = context
                .select(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).INTEGER_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DATE_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).STRING_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).BOOLEAN_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DOUBLE_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).SIMPLE_SETS_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_SETS_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).IS_CLOSED)
                .from(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.equal(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE.eq(DataUtils.convert(reportDate)));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E122));

        if (rows.size() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E123, baseEntityId, DataTypes.dateFormatDot.format(reportDate)));

        Map<String, Object> row = rows.get(0);

        long id = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID.getName())).longValue();

        long creditorId = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).CREDITOR_ID.getName())).longValue();

        long integerValuesCount = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).INTEGER_VALUES_COUNT.getName())).longValue();

        long dateValuesCount = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DATE_VALUES_COUNT.getName())).longValue();

        long stringValuesCount = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).STRING_VALUES_COUNT.getName())).longValue();

        long booleanValuesCount = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).BOOLEAN_VALUES_COUNT.getName())).longValue();

        long doubleValuesCount = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DOUBLE_VALUES_COUNT.getName())).longValue();

        long complexValuesCount = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_VALUES_COUNT.getName())).longValue();

        long simpleSetsCount = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).SIMPLE_SETS_COUNT.getName())).longValue();

        long complexSetsCount = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_SETS_COUNT.getName())).longValue();

        boolean isClosed = ((BigDecimal) row
                .get(EAV_BE_ENTITY_REPORT_DATES.IS_CLOSED.getName())).longValue() == 1;

        return new BaseEntityReportDate(id, creditorId, reportDate, integerValuesCount, dateValuesCount,
                stringValuesCount, booleanValuesCount, doubleValuesCount, complexValuesCount, simpleSetsCount,
                complexSetsCount, isClosed);
    }

    @Override
    public long insert(IPersistable persistable) {
        IBaseEntityReportDate baseEntityReportDate = (IBaseEntityReportDate) persistable;

        Insert insert = context
                .insertInto(EAV_BE_ENTITY_REPORT_DATES)
                .set(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID, baseEntityReportDate.getBaseEntity().getId())

                .set(EAV_BE_ENTITY_REPORT_DATES.CREDITOR_ID,
                        baseEntityReportDate.getBaseEntity().getMeta().isReference() ?
                                0 : baseEntityReportDate.getCreditorId())

                .set(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE, DataUtils.convert(baseEntityReportDate.getReportDate()))
                .set(EAV_BE_ENTITY_REPORT_DATES.INTEGER_VALUES_COUNT, baseEntityReportDate.getIntegerValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.DATE_VALUES_COUNT, baseEntityReportDate.getDateValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.STRING_VALUES_COUNT,  baseEntityReportDate.getStringValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.BOOLEAN_VALUES_COUNT, baseEntityReportDate.getBooleanValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.DOUBLE_VALUES_COUNT, baseEntityReportDate.getDoubleValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_VALUES_COUNT, baseEntityReportDate.getComplexValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.SIMPLE_SETS_COUNT, baseEntityReportDate.getSimpleSetsCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_SETS_COUNT, baseEntityReportDate.getComplexSetsCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.IS_CLOSED, DataUtils.convert(baseEntityReportDate.isClosed()));

        logger.debug(insert.toString());

        long baseEntityReportDateId =  insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        baseEntityReportDate.setId(baseEntityReportDateId);

        return baseEntityReportDateId;
    }

    @Override
    public void update(IPersistable persistable) {
        if (persistable.getId() < 1)
            throw new RuntimeException(Errors.compose(Errors.E124));

        IBaseEntityReportDate baseEntityReportDate = (IBaseEntityReportDate) persistable;

        String tableAlias = "rd";
        Update update = context
                .update(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).INTEGER_VALUES_COUNT, baseEntityReportDate.getIntegerValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DATE_VALUES_COUNT, baseEntityReportDate.getDateValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).STRING_VALUES_COUNT, baseEntityReportDate.getStringValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).BOOLEAN_VALUES_COUNT, baseEntityReportDate.getBooleanValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DOUBLE_VALUES_COUNT, baseEntityReportDate.getDoubleValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_VALUES_COUNT, baseEntityReportDate.getComplexValuesCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).SIMPLE_SETS_COUNT, baseEntityReportDate.getSimpleSetsCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_SETS_COUNT, baseEntityReportDate.getComplexSetsCount())
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).IS_CLOSED, DataUtils.convert(baseEntityReportDate.isClosed()))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID.equal(baseEntityReportDate.getId()));

        logger.debug(update.toString());
        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());

        if (count != 1)
            throw new RuntimeException(Errors.compose(Errors.E125));
    }



    @Override
    public void delete(IPersistable persistable) {
        String tableAlias = "rd";
        Delete delete = context
                .delete(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID.equal(persistable.getId()));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count != 1)
            throw new RuntimeException(Errors.compose(Errors.E119));
    }

    @Override
    public void deleteAll(long baseSetId) {
        String tableAlias = "rd";
        Delete delete = context
                .delete(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.equal(baseSetId));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public Set<Date> getAvailableReportDates(long baseEntityId) {
        Set<Date> reportDates = new HashSet<>();

        Select select = context
                .select(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows)
            reportDates.add(DataUtils.convert((Timestamp)
                    row.get(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.getName())));

        return reportDates;
    }

    @Override
    public Date getMinReportDate(long baseEntityId) {
        Select select = context
                .select(DSL.min(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("min_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("min_report_date"));
    }

    @Override
    public Date getMinReportDate(long baseEntityId, Date reportDate) {
        Select select = context
                .select(DSL.min(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("min_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.greaterThan(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("min_report_date"));
    }

    @Override
    public boolean isLastReportDate(long baseEntityId, Date reportDate) {
        String tableAlias = "rd";

        Select select = context
                .select(DSL.decode(DSL.max(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE),
                        DataUtils.convert(reportDate), DSL.val(1, BigDecimal.class),
                        DSL.val(0, BigDecimal.class)).as("is_last"))
                .from(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE.
                        greaterOrEqual(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return rows.get(0).get("is_last").equals(BigDecimal.ONE);
    }

    @Override
    public Date getNextReportDate(long baseEntityId, Date reportDate) {
        String tableAlias = "rd";
        Select select = context
                .select(DSL.min(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE).as("next_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        if (rows.size() != 0) {
            return DataUtils.convert((Timestamp) rows.get(0).get("next_report_date"));
        }
        return null;
    }

    @Override
    public Date getPreviousReportDate(long baseEntityId, Date reportDate) {
        String tableAlias = "rd";
        Select select = context
                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE).as("previous_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        if (rows.size() != 0) {
            return DataUtils.convert((Timestamp) rows.get(0).get("previous_report_date"));
        }
        return null;
    }

    @Override
    public Date getMaxReportDate(long baseEntityId, Date reportDate) {
        Select select = context
                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("min_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.lessOrEqual(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("min_report_date"));
    }

    @Override
    public Date getMaxReportDate(long baseEntityId) {
        String tableAlias = "rd";
        Select select = context
                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE).as("max_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("max_report_date"));
    }

    @Override
    public boolean exists(long baseEntityId, Date reportDate) {
        String reportDateTableAlias = "rd";
        Select select = context.select(EAV_BE_ENTITY_REPORT_DATES.as(reportDateTableAlias).ID)
                        .from(EAV_BE_ENTITY_REPORT_DATES.as(reportDateTableAlias))
                        .where(EAV_BE_ENTITY_REPORT_DATES.as(reportDateTableAlias).ENTITY_ID.eq(baseEntityId)
                        .and(EAV_BE_ENTITY_REPORT_DATES.as(reportDateTableAlias).REPORT_DATE.eq(DataUtils.convert(reportDate))));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return rows.size() > 0;
    }
}
