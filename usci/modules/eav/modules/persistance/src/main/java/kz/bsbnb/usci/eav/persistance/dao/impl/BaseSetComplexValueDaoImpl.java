package kz.bsbnb.usci.eav.persistance.dao.impl;

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
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseSetComplexValueDao;
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
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_SET_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITIES;

@Repository
public class BaseSetComplexValueDaoImpl extends JDBCSupport implements IBaseSetComplexValueDao {
    private final Logger logger = LoggerFactory.getLogger(BaseSetComplexValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    IBatchRepository batchRepository;

    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Override
    public long insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;
        IBaseEntity baseEntity = (IBaseEntity) baseValue.getValue();

        long baseValueId = insert(
                baseValue.getBaseContainer().getId(),
                baseValue.getRepDate(),
                baseEntity.getId(),
                baseValue.isClosed(),
                baseValue.isLast());

        baseValue.setId(baseValueId);

        return baseValueId;
    }

    protected long insert(long setId, Date reportDate, Object value, boolean closed,
                          boolean last) {
        Insert insert = context
                .insertInto(EAV_BE_COMPLEX_SET_VALUES)
                .set(EAV_BE_COMPLEX_SET_VALUES.SET_ID, setId)
                .set(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID, (Long) value)
                .set(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_COMPLEX_SET_VALUES.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;
        IBaseEntity baseEntity = (IBaseEntity) baseValue.getValue();

        update(baseValue.getId(),
                baseValue.getBaseContainer().getId(),
                baseValue.getRepDate(),
                baseEntity.getId(),
                baseValue.isClosed(),
                baseValue.isLast());
    }

    protected void update(long id, long setId, Date reportDate, Object value, boolean closed, boolean last) {
        String tableAlias = "csv";
        Update update = context
                .update(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID, setId)
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID, (Long) value)
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST, DataUtils.convert(last))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID.equal(id));

        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException("Обновление затронуло " + count + " записей(" + id +
                    ", EAV_BE_COMPLEX_SET_VALUES);");
    }

    @Override
    public void delete(IPersistable persistable) {
        delete(persistable.getId());
    }

    protected void delete(long id) {
        String tableAlias = "csv";
        Delete delete = context
                .delete(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID.equal(id));

        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException("Удаление затронуло " + count + " записей(" + id +
                    ", EAV_BE_COMPLEX_SET_VALUES);");
    }


    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException("Родитель записи(" + baseValue.getMetaAttribute().getName() +
                    ") является NULL;");

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
        IMetaType metaType = baseSet.getMemberType();

        IBaseValue previousBaseValue = null;

        String tableAlias = "csv";
        String subqueryAlias = "csvn";
        Table subqueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID.equal(childBaseEntity.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.
                        lessThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subqueryAlias);

        Select select = context
                .select(subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                .from(subqueryTable)
                .where(subqueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException("Query for get next instance of BaseValue return more than one row.");

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.ID.getName())).longValue();

            boolean last = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.IS_LAST.getName())).longValue() == 1;

            boolean closed = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED.getName())).longValue() == 1;

            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE.getName()));

            IBaseEntity childBaseEntityLoaded = baseEntityLoadDao.loadByMaxReportDate(childBaseEntity.getId(),
                    reportDate);

            previousBaseValue = BaseValueFactory.create(
                    MetaContainerTypes.META_SET,
                    metaType,
                    id,
                    0,
                    reportDate,
                    childBaseEntityLoaded,
                    closed,
                    last);
        }

        return previousBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getNextBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException("Родитель записи(" + baseValue.getMetaAttribute().getName() +
                    ") является NULL;");

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
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID.equal(childBaseEntity.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.
                        greaterThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subqueryAlias);

        Select select = context
                .select(subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED),
                        subqueryTable.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                .from(subqueryTable)
                .where(subqueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException("Query for get next instance of BaseValue return more than one row. Query: " +
                    select.toString());

        if (rows.size() >= 1) {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.ID.getName())).longValue();

            boolean last = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.IS_LAST.getName())).longValue() == 1;

            boolean closed = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED.getName())).longValue() == 1;

            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE.getName()));

            IBaseEntity childBaseEntityLoaded = baseEntityLoadDao.loadByMaxReportDate(childBaseEntity.getId(),
                    reportDate);

            nextBaseValue = BaseValueFactory.create(
                    MetaContainerTypes.META_SET,
                    metaType,
                    id,
                    0,
                    reportDate,
                    childBaseEntityLoaded,
                    closed,
                    last);
        }

        return nextBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException("Родитель записи(" + baseValue.getMetaAttribute().getName() +
                    ") является NULL;");

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseSet baseSet = (IBaseSet) baseContainer;
        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
        IMetaType metaType = baseSet.getMemberType();

        if (baseContainer.getId() == 0)
            throw new RuntimeException("Can not find closed instance of BaseValue without container or container ID.");

        IBaseValue closedBaseValue = null;

        String tableAlias = "csv";
        Select select = context
                .select(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ID,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.
                        lessOrEqual(DataUtils.convert(baseValue.getRepDate())))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID.equal(childBaseEntity.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException("Query for get next instance of BaseValue return more than one row.");

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.ID.getName())).longValue();

            boolean last = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.IS_LAST.getName())).longValue() == 1;

            IBaseEntity childBaseEntityLoaded = baseEntityLoadDao.loadByMaxReportDate(childBaseEntity.getId(),
                    baseValue.getRepDate());

            closedBaseValue = BaseValueFactory.create(
                    MetaContainerTypes.META_SET,
                    metaType,
                    id,
                    0,
                    baseValue.getRepDate(),
                    childBaseEntityLoaded,
                    true,
                    last);
        }

        return closedBaseValue;
    }

    @Override
    public IBaseValue getLastBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException("Родитель записи(" + baseValue.getMetaAttribute().getName() +
                    ") является NULL;");

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
                        EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_CLOSED)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseContainer.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID.equal(childBaseEntity.getId()))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).IS_LAST.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException("Найдено больше одной записи(" + select.toString() + ");");

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.ID.getName())).longValue();

            boolean closed = ((BigDecimal) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED.getName())).longValue() == 1;

            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE.getName()));

            IBaseEntity childBaseEntityLoaded = baseEntityLoadDao.loadByMaxReportDate(childBaseEntity.getId(),
                    reportDate);

            lastBaseValue = BaseValueFactory.create(
                    MetaContainerTypes.META_SET,
                    metaType,
                    id,
                    0,
                    reportDate,
                    childBaseEntityLoaded,
                    closed,
                    true);
        }

        return lastBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadBaseValues(IBaseSet baseSet, Date actualReportDate) {
        Table tableOfValues = EAV_BE_COMPLEX_SET_VALUES.as("csv");
        Select select;

        Table tableNumbering = context
                .select(DSL.rank().over()
                        .partitionBy(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID))
                        .orderBy(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE).desc()).as("num_pp"),
                    tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                    tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID),
                    tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                    tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED),
                    tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                .from(tableOfValues)
                .where(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.SET_ID).eq(baseSet.getId()))
                .and(tableOfValues.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE)
                        .lessOrEqual(DataUtils.convert(actualReportDate)))
                .asTable("csvn");

        select = context
                .select(tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.ID),
                        tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE),
                        tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID),
                        tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.IS_LAST))
                .from(tableNumbering)
                .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                .and(tableNumbering.field(EAV_BE_COMPLEX_SET_VALUES.IS_CLOSED).equal(false));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            long id = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.ID.getName())).longValue();

            long entityValueId = ((BigDecimal)
                    row.get(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.getName())).longValue();

            boolean isLast = ((BigDecimal) row.get(EAV_BE_COMPLEX_SET_VALUES.IS_LAST.getName())).longValue() == 1;

            Date reportDate = DataUtils.convertToSQLDate((Timestamp)
                    row.get(EAV_BE_COMPLEX_SET_VALUES.REPORT_DATE.getName()));

            IBaseEntity baseEntity = baseEntityLoadDao.loadByMaxReportDate(entityValueId, actualReportDate);

            baseSet.put(BaseValueFactory.create(
                    MetaContainerTypes.META_SET,
                    baseSet.getMemberType(),
                    id,
                    0,
                    reportDate,
                    baseEntity,
                    false,
                    isLast));
        }
    }

    @Override
    public void deleteAll(long baseSetId) {
        Set<Long> childBaseEntityIds = getChildBaseEntityIds(baseSetId);

        String tableAlias = "cv";
        Delete delete = context
                .delete(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseSetId));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        for (long childBaseEntityId : childBaseEntityIds)
            baseEntityDao.deleteRecursive(childBaseEntityId);
    }

    @Override
    public Set<Long> getChildBaseEntityIds(long baseSetId) {
        Set<Long> childBaseEntityIds = new HashSet<>();

        String tableAlias = "bv";
        Select select = context
                .select(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.equal(baseSetId))
                .groupBy(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).ENTITY_VALUE_ID);

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0) {
            for (Map<String, Object> row : rows)
                childBaseEntityIds.add(((BigDecimal) row
                        .get(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.getName())).longValue());
        }

        return childBaseEntityIds;
    }

    public boolean isSingleBaseValue(IBaseValue baseValue) {
        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();

        String entitiesTableAlias = "e";
        String complexSetValuesTableAlias = "csv";
        Select select = context
                .select(EAV_BE_ENTITIES.as(entitiesTableAlias).ID)
                .from(EAV_BE_ENTITIES.as(entitiesTableAlias))
                .where(EAV_BE_ENTITIES.as(entitiesTableAlias).ID.equal(childBaseEntity.getId()))
                .and(DSL.exists(context
                        .select(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias).ID)
                        .from(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias))
                        .where(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias).ENTITY_VALUE_ID
                                .equal(EAV_BE_ENTITIES.as(entitiesTableAlias).ID))
                        .and(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias).ID.
                                notEqual(baseValue.getId()))));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return rows.size() == 0;
    }

    @Override
    public Date getNextReportDate(long baseSetId, Date reportDate) {
        String tableAlias = "csv";
        Select select = context
                .select(DSL.min(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE).as("next_report_date"))
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.eq(baseSetId))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(reportDate)));

        logger.debug(select.toString());

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0)
            return DataUtils.convert((Timestamp) rows.get(0).get("next_report_date"));

        return null;
    }

    @Override
    public Date getPreviousReportDate(long baseSetId, Date reportDate) {
        String tableAlias = "csv";
        Select select = context
                .select(DSL.max(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE).as("previous_report_date"))
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).SET_ID.eq(baseSetId))
                .and(EAV_BE_COMPLEX_SET_VALUES.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(reportDate)));

        logger.debug(select.toString());

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0)
            return DataUtils.convert((Timestamp) rows.get(0).get("previous_report_date"));

        return null;
    }
}
