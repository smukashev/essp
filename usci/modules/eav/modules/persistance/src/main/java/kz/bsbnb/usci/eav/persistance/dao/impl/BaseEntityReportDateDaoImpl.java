package kz.bsbnb.usci.eav.persistance.dao.impl;

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

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITY_REPORT_DATES;

/**
 * Created by Alexandr.Motov on 16.03.14.
 */
@Repository
public class BaseEntityReportDateDaoImpl extends JDBCSupport implements IBaseEntityReportDateDao {

    private final Logger logger = LoggerFactory.getLogger(BaseEntityReportDateDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public IBaseEntityReportDate load(long baseEntityId, Date reportDate)
    {
        if(baseEntityId < 1)
        {
            throw new IllegalArgumentException("To load instance of BaseEntityReportDate must always " +
                    "be specified entity ID.");
        }

        if (reportDate == null)
        {
            throw new IllegalArgumentException("To load instance of BaseEntityReportDate must always " +
                    "be specified report date.");
        }

        String tableAlias = "rd";
        Select select = context
                .select(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).INTEGER_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DATE_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).STRING_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).BOOLEAN_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DOUBLE_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_VALUES_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).SIMPLE_SETS_COUNT,
                        EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_SETS_COUNT)
                .from(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.equal(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE.eq(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one instance of BaseEntityReportDate found.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalStateException("Instance of BaseEntityReportDate was not found.");
        }

        Map<String, Object> row = rows.get(0);
        if(row != null)
        {
            long id = ((BigDecimal)row.get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID.getName())).longValue();
            long integerValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).INTEGER_VALUES_COUNT.getName())).longValue();
            long dateValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DATE_VALUES_COUNT.getName())).longValue();
            long stringValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).STRING_VALUES_COUNT.getName())).longValue();
            long booleanValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).BOOLEAN_VALUES_COUNT.getName())).longValue();
            long doubleValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DOUBLE_VALUES_COUNT.getName())).longValue();
            long complexValuesCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_VALUES_COUNT.getName())).longValue();
            long simpleSetsCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).SIMPLE_SETS_COUNT.getName())).longValue();
            long complexSetsCount = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_SETS_COUNT.getName())).longValue();

            IBaseEntityReportDate baseEntityReportDate =
                    new BaseEntityReportDate(
                            id,
                            reportDate,
                            integerValuesCount,
                            dateValuesCount,
                            stringValuesCount,
                            booleanValuesCount,
                            doubleValuesCount,
                            complexValuesCount,
                            simpleSetsCount,
                            complexSetsCount);

            return baseEntityReportDate;
        }
        else
        {
            logger.error("Can't load instance of BaseEntityReportDate, empty data set.");
        }

        return null;
    }

    @Override
    public long insert(IPersistable persistable) {
        IBaseEntityReportDate baseEntityReportDate = (IBaseEntityReportDate)persistable;
        long baseEntityReportDateId = insert(baseEntityReportDate.getBaseEntity().getId(), baseEntityReportDate.getReportDate(),
                baseEntityReportDate.getIntegerValuesCount(), baseEntityReportDate.getDateValuesCount(),
                baseEntityReportDate.getStringValuesCount(), baseEntityReportDate.getBooleanValuesCount(),
                baseEntityReportDate.getDoubleValuesCount(), baseEntityReportDate.getComplexValuesCount(),
                baseEntityReportDate.getSimpleSetsCount(), baseEntityReportDate.getComplexSetsCount());
        baseEntityReportDate.setId(baseEntityReportDateId);

        return baseEntityReportDateId;
    }

    protected long insert(
            long baseEntityId,
            Date reportDate,
            long integerValuesCount,
            long dateValuesCount,
            long stringValuesCount,
            long booleanValuesCount,
            long doubleValuesCount,
            long complexValuesCount,
            long simpleSetsCount,
            long complexSetsCount)
    {
        Insert insert = context
                .insertInto(EAV_BE_ENTITY_REPORT_DATES)
                .set(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_ENTITY_REPORT_DATES.INTEGER_VALUES_COUNT, integerValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.DATE_VALUES_COUNT, dateValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.STRING_VALUES_COUNT, stringValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.BOOLEAN_VALUES_COUNT, booleanValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.DOUBLE_VALUES_COUNT, doubleValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_VALUES_COUNT, complexValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.SIMPLE_SETS_COUNT, simpleSetsCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.COMPLEX_SETS_COUNT, complexSetsCount);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {
        if (persistable.getId() < 1)
        {
            throw new RuntimeException("Can't update instance of BaseEntityReportDate without id.");
        }

        IBaseEntityReportDate baseEntityReportDate = (IBaseEntityReportDate)persistable;
        update(baseEntityReportDate.getId(), baseEntityReportDate.getIntegerValuesCount(),
                baseEntityReportDate.getDateValuesCount(), baseEntityReportDate.getStringValuesCount(),
                baseEntityReportDate.getBooleanValuesCount(), baseEntityReportDate.getDoubleValuesCount(),
                baseEntityReportDate.getComplexValuesCount(), baseEntityReportDate.getSimpleSetsCount(),
                baseEntityReportDate.getComplexSetsCount());
    }

    protected void update(
            long id,
            long integerValuesCount,
            long dateValuesCount,
            long stringValuesCount,
            long booleanValuesCount,
            long doubleValuesCount,
            long complexValuesCount,
            long simpleSetsCount,
            long complexSetsCount)
    {
        String tableAlias = "rd";
        Update update = context
                .update(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).INTEGER_VALUES_COUNT, integerValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DATE_VALUES_COUNT, dateValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).STRING_VALUES_COUNT, stringValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).BOOLEAN_VALUES_COUNT, booleanValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).DOUBLE_VALUES_COUNT, doubleValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_VALUES_COUNT, complexValuesCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).SIMPLE_SETS_COUNT, simpleSetsCount)
                .set(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).COMPLEX_SETS_COUNT, complexSetsCount)
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID.equal(id));

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
        String tableAlias = "rd";
        Delete delete = context
                .delete(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
        if (count != 1)
        {
            throw new RuntimeException("DELETE operation should be delete only one record.");
        }
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
    public Set<Date> getAvailableReportDates(long baseEntityId)
    {
        Set<Date> reportDates = new HashSet<Date>();

        Select select = context
                .select(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
            reportDates.add(DataUtils.convert((Timestamp) row.get(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.getName())));
        }

        return reportDates;
    }

    @Override
    public Date getMinReportDate(long baseEntityId)
    {
        Select select = context
                .select(DSL.min(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE).as("min_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("min_report_date"));
    }

    @Override
    public Date getMaxReportDate(long baseEntityId, Date reportDate)
    {
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
    public Date getMaxReportDate(long baseEntityId)
    {
        String tableAlias = "rd";
        Select select = context
                .select(DSL.max(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).REPORT_DATE).as("max_report_date"))
                .from(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias))
                .where(EAV_BE_ENTITY_REPORT_DATES.as(tableAlias).ENTITY_ID.eq(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return DataUtils.convert((Timestamp) rows.get(0).get("max_report_date"));
    }

}
