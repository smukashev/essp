package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityReportDateDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
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

}
