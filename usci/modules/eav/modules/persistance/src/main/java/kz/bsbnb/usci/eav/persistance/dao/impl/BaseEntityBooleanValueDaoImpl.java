package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityBooleanValueDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
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

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_BOOLEAN_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_M_SIMPLE_ATTRIBUTES;

@Repository
public class BaseEntityBooleanValueDaoImpl extends JDBCSupport implements IBaseEntityBooleanValueDao {
    private final Logger logger = LoggerFactory.getLogger(BaseEntityBooleanValueDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public long insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;

        long baseValueId = insert(
                baseValue.getBaseContainer().getId(),
                baseValue.getCreditorId(),
                baseValue.getMetaAttribute().getId(),
                baseValue.getRepDate(),
                baseValue.getValue(),
                baseValue.isClosed(),
                baseValue.isLast());

        baseValue.setId(baseValueId);

        return baseValueId;
    }

    protected long insert(long baseEntityId, long creditorId, long metaAttributeId, Date reportDate, Object value,
                          boolean closed, boolean last) {
        Insert insert = context
                .insertInto(EAV_BE_BOOLEAN_VALUES)
                .set(EAV_BE_BOOLEAN_VALUES.ENTITY_ID, baseEntityId)
                .set(EAV_BE_BOOLEAN_VALUES.CREDITOR_ID, creditorId)
                .set(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_BOOLEAN_VALUES.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_BOOLEAN_VALUES.VALUE, DataUtils.convert((Boolean) value))
                .set(EAV_BE_BOOLEAN_VALUES.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_BOOLEAN_VALUES.IS_LAST, DataUtils.convert(last));

        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;

        update(baseValue.getId(),
                baseValue.getBaseContainer().getId(),
                baseValue.getCreditorId(),
                baseValue.getMetaAttribute().getId(),
                baseValue.getRepDate(),
                baseValue.getValue(),
                baseValue.isClosed(),
                baseValue.isLast());
    }

    protected void update(long id, long baseEntityId, long creditorId, long metaAttributeId, Date reportDate,
                          Object value, boolean closed, boolean last) {
        String tableAlias = "sv";
        Update update = context
                .update(EAV_BE_BOOLEAN_VALUES.as(tableAlias))
                .set(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ENTITY_ID, baseEntityId)
                .set(EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID, creditorId)
                .set(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_BOOLEAN_VALUES.as(tableAlias).VALUE, DataUtils.convert((Boolean) value))
                .set(EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_LAST, DataUtils.convert(last))
                .where(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ID.equal(id));

        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.compose(Errors.E84, count, id));
    }

    @Override
    public void delete(IPersistable persistable) {
        delete(persistable.getId());
    }

    protected void delete(long id) {
        String tableAlias = "sv";
        Delete delete = context
                .delete(EAV_BE_BOOLEAN_VALUES.as(tableAlias))
                .where(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());

        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.compose(Errors.E79, count, id));
    }

    private IBaseValue constructValue(Map<String, Object> row, IMetaType metaType) {
        long id = ((BigDecimal) row.get(EAV_BE_BOOLEAN_VALUES.ID.getName())).longValue();

        long creditorId = ((BigDecimal) row.get(EAV_BE_BOOLEAN_VALUES.CREDITOR_ID.getName())).longValue();

        Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_BOOLEAN_VALUES.REPORT_DATE.getName()));

        boolean value = ((BigDecimal) row.get(EAV_BE_BOOLEAN_VALUES.VALUE.getName())).longValue() == 1;

        boolean closed = ((BigDecimal) row.get(EAV_BE_BOOLEAN_VALUES.IS_CLOSED.getName())).longValue() == 1;

        boolean last = ((BigDecimal) row.get(EAV_BE_BOOLEAN_VALUES.IS_LAST.getName())).longValue() == 1;

        return BaseValueFactory.create(
                MetaContainerTypes.META_CLASS,
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
    public IBaseValue getExistingBaseValue(IBaseValue baseValue) {
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        IBaseContainer baseContainer = baseValue.getBaseContainer();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IBaseEntity baseEntity = (IBaseEntity) baseContainer;

        IMetaType metaType = metaAttribute.getMetaType();
        IBaseValue existingBaseValue = null;

        String tableAlias = "bv";
        String subQueryAlias = "bvn";

        Table subQueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).VALUE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_BOOLEAN_VALUES.as(tableAlias))
                .where(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE.equal(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_BOOLEAN_VALUES.ID),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.CREDITOR_ID),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1)
            existingBaseValue = constructValue(rows.get(0), metaType);

        return existingBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getNextBaseValue(IBaseValue baseValue) {
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        IBaseContainer baseContainer = baseValue.getBaseContainer();

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IBaseEntity baseEntity = (IBaseEntity) baseContainer;

        IMetaType metaType = metaAttribute.getMetaType();
        IBaseValue nextBaseValue = null;

        String tableAlias = "bv";
        String subQueryAlias = "bvn";

        Table subQueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).VALUE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_BOOLEAN_VALUES.as(tableAlias))
                .where(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_BOOLEAN_VALUES.ID),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.CREDITOR_ID),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            nextBaseValue = constructValue(row, metaType);
        }

        return nextBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        IBaseContainer baseContainer = baseValue.getBaseContainer();

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IBaseEntity baseEntity = (IBaseEntity) baseContainer;

        IMetaType metaType = metaAttribute.getMetaType();
        IBaseValue previousBaseValue = null;

        String tableAlias = "bv";
        String subQueryAlias = "bvn";

        Table subQueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE.desc()).as("num_pp"),
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).VALUE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_BOOLEAN_VALUES.as(tableAlias))
                .where(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_BOOLEAN_VALUES.ID),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.CREDITOR_ID),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                        subQueryTable.field(EAV_BE_BOOLEAN_VALUES.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            previousBaseValue = constructValue(row, metaType);
        }

        return previousBaseValue;
    }

    @Override
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        IBaseContainer baseContainer = baseValue.getBaseContainer();

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IMetaType metaType = metaAttribute.getMetaType();
        IBaseValue closedBaseValue = null;

        String tableAlias = "bv";
        Select select = context
                .select(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).VALUE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_LAST,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_CLOSED)
                .from(EAV_BE_BOOLEAN_VALUES.as(tableAlias))
                .where(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ENTITY_ID.equal(baseContainer.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE.lessOrEqual(DataUtils.convert(baseValue.getRepDate())))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_CLOSED.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            closedBaseValue = constructValue(row, metaType);
        }

        return closedBaseValue;
    }

    @Override
    public IBaseValue getLastBaseValue(IBaseValue baseValue) {
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        IBaseContainer baseContainer = baseValue.getBaseContainer();

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IMetaType metaType = metaAttribute.getMetaType();
        IBaseValue lastBaseValue = null;

        String tableAlias = "bv";
        Select select = context
                .select(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).REPORT_DATE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).VALUE,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_CLOSED,
                        EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_LAST)
                .from(EAV_BE_BOOLEAN_VALUES.as(tableAlias))
                .where(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ENTITY_ID.equal(baseContainer.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_BOOLEAN_VALUES.as(tableAlias).IS_LAST.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1) {
            Map<String, Object> row = rows.iterator().next();

            lastBaseValue = constructValue(row, metaType);
        }

        return lastBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadBaseValues(IBaseEntity baseEntity, Date existingReportDate, Date savingReportDate) {
        Table tableOfAttributes = EAV_M_SIMPLE_ATTRIBUTES.as("a");
        Table tableOfValues = EAV_BE_BOOLEAN_VALUES.as("v");
        Select select;

        Date loadingDate = savingReportDate == null ? existingReportDate  : savingReportDate.compareTo(existingReportDate) >= 0 ? savingReportDate : existingReportDate;

        Table tableNumbering = context
                .select(DSL.rank().over()
                                .partitionBy(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID))
                                .orderBy(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE).desc()).as("num_pp"),
                        tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ID),
                        tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ENTITY_ID),
                        tableOfValues.field(EAV_BE_BOOLEAN_VALUES.CREDITOR_ID),
                        tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID),
                        tableOfValues.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                        tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE),
                        tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                        tableOfValues.field(EAV_BE_BOOLEAN_VALUES.IS_LAST))
                .from(tableOfValues)
                .where(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.ENTITY_ID).eq(baseEntity.getId()))
                .and(tableOfValues.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE).lessOrEqual(DataUtils.convert(loadingDate)))
                .asTable("vn");

        select = context
                .select(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.NAME),
                        tableNumbering.field(EAV_BE_BOOLEAN_VALUES.ID),
                        tableNumbering.field(EAV_BE_BOOLEAN_VALUES.CREDITOR_ID),
                        tableNumbering.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE),
                        tableNumbering.field(EAV_BE_BOOLEAN_VALUES.VALUE),
                        tableNumbering.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED),
                        tableNumbering.field(EAV_BE_BOOLEAN_VALUES.IS_LAST))
                .from(tableNumbering)
                .join(tableOfAttributes)
                .on(tableNumbering.field(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID)
                        .eq(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.ID)))
                .where((tableNumbering.field("num_pp").cast(Integer.class).equal(1)
                .and((tableNumbering.field(EAV_BE_BOOLEAN_VALUES.IS_CLOSED).equal(false)
                        .and(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.IS_FINAL).equal(false)))))
                        .or(tableNumbering.field(EAV_BE_BOOLEAN_VALUES.REPORT_DATE).equal(savingReportDate)
                                .and(tableOfAttributes.field(EAV_M_SIMPLE_ATTRIBUTES.IS_FINAL).equal(true))));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            String attribute = (String) row.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());

            IMetaType metaType = baseEntity.getMemberType(attribute);

            baseEntity.put(attribute, constructValue(row, metaType));
        }
    }

    @Override
    public void deleteAll(long baseEntityId) {
        String tableAlias = "sv";
        Delete delete = context
                .delete(EAV_BE_BOOLEAN_VALUES.as(tableAlias))
                .where(EAV_BE_BOOLEAN_VALUES.as(tableAlias).ENTITY_ID.equal(baseEntityId));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }
}
