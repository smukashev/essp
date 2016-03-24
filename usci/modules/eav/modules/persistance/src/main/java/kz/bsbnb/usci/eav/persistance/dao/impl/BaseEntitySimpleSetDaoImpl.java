package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.meta.impl.MetaContainer;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
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

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITY_SIMPLE_SETS;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_M_SIMPLE_SET;

@Repository
public class BaseEntitySimpleSetDaoImpl extends JDBCSupport implements IBaseEntitySimpleSetDao {
    private final Logger logger = LoggerFactory.getLogger(BaseEntitySimpleSetDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private IBaseSetBooleanValueDao baseSetBooleanValueDao;

    @Autowired
    private IBaseSetIntegerValueDao baseSetIntegerValueDao;

    @Autowired
    private IBaseSetStringValueDao baseSetStringValueDao;

    @Autowired
    private IBaseSetDoubleValueDao baseSetDoubleValueDao;

    @Autowired
    private IBaseSetDateValueDao baseSetDateValueDao;

    @Override
    public long insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;
        IBaseSet baseSet = (IBaseSet) baseValue.getValue();

        Insert insert = context
                .insertInto(EAV_BE_ENTITY_SIMPLE_SETS)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID, baseValue.getBaseContainer().getId())
                .set(EAV_BE_ENTITY_SIMPLE_SETS.CREDITOR_ID, baseValue.getBaseContainer().getId())
                .set(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID, baseValue.getMetaAttribute().getId())
                .set(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE, DataUtils.convert(baseValue.getRepDate()))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED, DataUtils.convert(baseValue.isClosed()))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST, DataUtils.convert(baseValue.isLast()));

        long baseValueId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        baseSet.setId(baseValueId);

        return baseValueId;
    }

    @Override
    public void update(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;

        String tableAlias = "ss";

        Update update = context
                .update(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID, baseValue.getBaseContainer().getId())
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).CREDITOR_ID, baseValue.getCreditorId())
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID, baseValue.getMetaAttribute().getId())
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE, DataUtils.convert(baseValue.getRepDate()))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED, DataUtils.convert(baseValue.isClosed()))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST, DataUtils.convert(baseValue.isLast()))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID.equal(baseValue.getId()));

        logger.debug(update.toString());

        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.getMessage(Errors.E128, count, persistable.getId()));
    }

    @Override
    public void delete(IPersistable persistable) {
        delete(persistable.getId());
    }

    protected void delete(long id) {
        String tableAlias = "ss";
        Delete delete = context
                .delete(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());

        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.getMessage(Errors.E126, count, id));
    }

    private IBaseValue constructValue (long creditorId, Map<String, Object> row, IMetaType metaType, IMetaSet metaSet) {
        long id = ((BigDecimal) row.get(EAV_BE_ENTITY_SIMPLE_SETS.ID.getName())).longValue();

        Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE.getName()));

        boolean closed = ((BigDecimal) row.get(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED.getName())).longValue() == 1;

        boolean last = ((BigDecimal) row.get(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST.getName())).longValue() == 1;

        IBaseSet baseSet = new BaseSet(id, metaSet.getMemberType(), creditorId);

        loadBaseValues(baseSet, reportDate);

        return BaseValueFactory.create(
                MetaContainerTypes.META_CLASS,
                metaType,
                id,
                creditorId,
                reportDate,
                baseSet,
                closed,
                last);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getNextBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.getMessage(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseEntity baseEntity = (IBaseEntity) baseContainer;

        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaSet metaSet = (IMetaSet) metaType;

        IBaseValue nextBaseValue = null;

        String tableAlias = "ess";
        String subQueryAlias = "essn";

        Table subQueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.ID),
                        subQueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE),
                        subQueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED),
                        subQueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.getMessage(Errors.E83, subQueryTable.toString()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            nextBaseValue = constructValue(baseValue.getCreditorId(), row, metaType, metaSet);
        }

        return nextBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.getMessage(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseEntity baseEntity = (IBaseEntity) baseContainer;

        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaSet metaSet = (IMetaSet) metaType;

        IBaseValue previousBaseValue = null;

        String tableAlias = "ess";
        String subQueryAlias = "essn";

        Table subQueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.desc()).as("num_pp"),
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.ID),
                        subQueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE),
                        subQueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED),
                        subQueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.getMessage(Errors.E83, subQueryTable.toString()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            previousBaseValue = constructValue(baseValue.getCreditorId(), row, metaType, metaSet);
        }

        return previousBaseValue;
    }

    @Override
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.getMessage(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaSet metaSet = (IMetaSet) metaType;

        IBaseValue closedBaseValue = null;

        String tableAlias = "ess";
        Select select = context
                .select(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID.equal(baseContainer.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.lessOrEqual(DataUtils.convert(baseValue.getRepDate())))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.getMessage(Errors.E83, select.toString()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            closedBaseValue = constructValue(baseValue.getCreditorId(), row, metaType, metaSet);
        }

        return closedBaseValue;
    }

    @Override
    public IBaseValue getLastBaseValue(IBaseValue baseValue) {
        if (baseValue.getBaseContainer() == null)
            throw new IllegalStateException(Errors.getMessage(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseValue.getBaseContainer().getId() == 0)
            return null;

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaSet metaSet = (IMetaSet) metaType;

        IBaseValue lastBaseValue = null;

        String tableAlias = "ess";
        Select select = context
                .select(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID.equal(baseContainer.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.getMessage(Errors.E83, select.toString()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            lastBaseValue = constructValue(baseValue.getCreditorId(), row, metaType, metaSet);
        }

        return lastBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadBaseValues(IBaseEntity baseEntity, Date actualReportDate) {
        Table tableOfSimpleSets = EAV_M_SIMPLE_SET.as("ss");
        Table tableOfEntitySimpleSets = EAV_BE_ENTITY_SIMPLE_SETS.as("ess");
        Select select;

        Table tableNumbering = context
                .select(DSL.rank().over()
                                .partitionBy(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID))
                                .orderBy(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE)).as("num_pp"),
                        tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ID),
                        tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID),
                        tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE),
                        tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED),
                        tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST))
                .from(tableOfEntitySimpleSets)
                .where(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID).eq(baseEntity.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.CREDITOR_ID.eq(baseEntity.getBaseEntityReportDate().getCreditorId()))
                .and(tableOfEntitySimpleSets.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE).lessOrEqual(DataUtils.convert(actualReportDate)))
                .asTable("essn");

        select = context
                .select(tableOfSimpleSets.field(EAV_M_SIMPLE_SET.NAME),
                        tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.ID),
                        tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE),
                        tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED),
                        tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST))
                .from(tableNumbering)
                .join(tableOfSimpleSets)
                .on(tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID)
                        .eq(tableOfSimpleSets.field(EAV_M_SIMPLE_SET.ID)))
                .where(tableNumbering.field("num_pp").cast(Integer.class).equal(1))
                .and(tableNumbering.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED).equal(false));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            String attribute = (String) row.get(EAV_M_SIMPLE_SET.NAME.getName());

            IMetaType metaType = baseEntity.getMemberType(attribute);

            IMetaSet metaSet = (MetaSet) metaType;

            baseEntity.put(attribute, constructValue(baseEntity.getBaseEntityReportDate().getCreditorId(), row, metaType, metaSet));
        }
    }

    protected void loadBaseValues(IBaseSet baseSet, Date actualReportDate) {
        IMetaType metaType = baseSet.getMemberType();

        if (metaType.isSet())
            throw new UnsupportedOperationException(Errors.getMessage(Errors.E2));

        IMetaValue metaValue = (IMetaValue) metaType;
        DataTypes dataType = metaValue.getTypeCode();

        switch (dataType) {
            case INTEGER: {
                baseSetIntegerValueDao.loadBaseValues(baseSet, actualReportDate);
                break;
            }
            case DATE: {
                baseSetDateValueDao.loadBaseValues(baseSet, actualReportDate);
                break;
            }
            case STRING: {
                baseSetStringValueDao.loadBaseValues(baseSet, actualReportDate);
                break;
            }
            case BOOLEAN: {
                baseSetBooleanValueDao.loadBaseValues(baseSet, actualReportDate);
                break;
            }
            case DOUBLE: {
                baseSetDoubleValueDao.loadBaseValues(baseSet, actualReportDate);
                break;
            }
            default:
                throw new IllegalArgumentException(Errors.getMessage(Errors.E127));
        }
    }

    @Override
    public void deleteAll(long baseEntityId) {
        String tableAlias = "cv";
        Delete delete = context
                .delete(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID.equal(baseEntityId));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }
}

