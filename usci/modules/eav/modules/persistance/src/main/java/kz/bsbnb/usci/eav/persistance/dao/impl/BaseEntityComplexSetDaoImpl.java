package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityComplexSetDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseSetComplexValueDao;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Repository
public class BaseEntityComplexSetDaoImpl extends JDBCSupport implements IBaseEntityComplexSetDao {
    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    private final Logger logger = LoggerFactory.getLogger(BaseEntityComplexSetDaoImpl.class);
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;
    @Autowired
    private IBaseSetComplexValueDao baseSetComplexValueDao;

    @Override
    public long insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;
        BaseSet baseSet = (BaseSet) baseValue.getValue();

        Insert insert = context
                .insertInto(EAV_BE_ENTITY_COMPLEX_SETS)
                .set(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID, baseValue.getBaseContainer().getId())
                .set(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID, baseValue.getMetaAttribute().getId())
                .set(EAV_BE_ENTITY_COMPLEX_SETS.CREDITOR_ID, baseValue.getCreditorId())
                .set(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE, DataUtils.convert(baseValue.getRepDate()))
                .set(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED, DataUtils.convert(baseValue.isClosed()))
                .set(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST, DataUtils.convert(baseValue.isLast()));

        logger.debug(insert.toString());

        long baseValueId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        baseSet.setId(baseValueId);

        return baseValueId;
    }

    @Override
    public void update(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue) persistable;

        String tableAlias = "cs";
        Update update = context
                .update(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias))
                .set(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ENTITY_ID, baseValue.getBaseContainer().getId())
                .set(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ATTRIBUTE_ID, baseValue.getMetaAttribute().getId())
                .set(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID, baseValue.getCreditorId())
                .set(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE, DataUtils.convert(baseValue.getRepDate()))
                .set(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_CLOSED, DataUtils.convert(baseValue.isClosed()))
                .set(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_LAST, DataUtils.convert(baseValue.isLast()))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ID.equal(baseValue.getId()));

        logger.debug(update.toString());

        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.compose(Errors.E86, count, baseValue.getId()));
    }

    @Override
    public void delete(IPersistable persistable) {
        String tableAlias = "cs";
        Delete delete = context
                .delete(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ID.equal(persistable.getId()));

        logger.debug(delete.toString());

        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count != 1)
            throw new IllegalStateException(Errors.compose(Errors.E85, count, persistable.getId()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getExistingBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IBaseEntity parentEntity = (IBaseEntity) baseContainer;
        IMetaClass parentEntityMeta = parentEntity.getMeta();

        IMetaType metaType = metaAttribute.getMetaType();

        IBaseValue existingValue = null;

        String tableAlias = "exBV";

        Select select = context
                .select(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ENTITY_ID.equal(parentEntity.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE.equal(DataUtils.convert(baseValue.getRepDate())));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1)
            existingValue = constructValue(rows.get(0), parentEntityMeta, metaType, metaAttribute, null);

        return existingValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getNextBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IBaseEntity parentEntity = (IBaseEntity) baseContainer;
        IMetaClass parentEntityMeta = parentEntity.getMeta();

        IMetaType metaType = metaAttribute.getMetaType();

        IBaseValue nextBaseValue = null;

        String tableAlias = "ess";
        String subQueryAlias = "essn";

        Table subQueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ENTITY_ID.equal(parentEntity.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.ID),
                        subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.CREDITOR_ID),
                        subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE),
                        subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED),
                        subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1)
            nextBaseValue = constructValue(rows.get(0), parentEntityMeta, metaType, metaAttribute, null);

        return nextBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IBaseEntity parentEntity = (IBaseEntity) baseContainer;
        IMetaClass parentEntityMeta = parentEntity.getMeta();

        IMetaType metaType = metaAttribute.getMetaType();

        IBaseValue previousBaseValue = null;

        String tableAlias = "ess";
        String subQueryAlias = "essn";

        Table subQueryTable = context
                .select(DSL.rank().over()
                                .orderBy(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE.desc()).as("num_pp"),
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ENTITY_ID.equal(parentEntity.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subQueryAlias);

        Select select = context
                .select(subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.ID),
                        subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.CREDITOR_ID),
                        subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE),
                        subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED),
                        subQueryTable.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST))
                .from(subQueryTable)
                .where(subQueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1)
            previousBaseValue = constructValue(rows.get(0), parentEntityMeta, metaType, metaAttribute, null);

        return previousBaseValue;
    }

    @Override
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IBaseEntity parentEntity = (IBaseEntity) baseContainer;
        IMetaClass metaClass = parentEntity.getMeta();

        IMetaType metaType = metaAttribute.getMetaType();

        IBaseValue closedBaseValue = null;

        String tableAlias = "ess";
        Select select = context
                .select(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ENTITY_ID.equal(baseContainer.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE.lessOrEqual(DataUtils.convert(baseValue.getRepDate())))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_CLOSED.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1)
            closedBaseValue = constructValue(rows.get(0), metaClass, metaType, metaAttribute, null);

        return closedBaseValue;
    }

    @Override
    public IBaseValue getLastBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();

        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E80));

        if (metaAttribute.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E81));

        if (baseContainer == null)
            throw new IllegalStateException(Errors.compose(Errors.E82, baseValue.getMetaAttribute().getName()));

        if (baseContainer.getId() < 1)
            return null;

        IBaseEntity parentEntity = (IBaseEntity) baseContainer;
        IMetaClass parentEntityMeta = parentEntity.getMeta();

        IMetaType metaType = metaAttribute.getMetaType();

        IBaseValue lastBaseValue = null;

        String tableAlias = "ess";
        Select select = context
                .select(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ENTITY_ID.equal(baseContainer.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).CREDITOR_ID.equal(baseValue.getCreditorId()))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).IS_LAST.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalStateException(Errors.compose(Errors.E83, metaAttribute.getName()));

        if (rows.size() == 1)
            lastBaseValue = constructValue(rows.get(0), parentEntityMeta, metaType, metaAttribute, null);

        return lastBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadBaseValues(IBaseEntity baseEntity, Date existingReportDate, Date savingReportDate) {
        Table tableOfComplexSets = EAV_M_COMPLEX_SET.as("cs");
        Table tableOfEntityComplexSets = EAV_BE_ENTITY_COMPLEX_SETS.as("ecs");

        Date loadingDate = savingReportDate == null ? existingReportDate : savingReportDate.compareTo(existingReportDate) >= 0 ? savingReportDate : existingReportDate;

        Select select;

        long creditorId = baseEntity.getBaseEntityReportDate().getCreditorId();

        Table tableNumbering = context
                .select(DSL.rank().over()
                                .partitionBy(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID))
                                .orderBy(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE).desc()).as("num_pp"),
                        tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ID),
                        tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID),
                        tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.CREDITOR_ID),
                        tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE),
                        tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED),
                        tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST))
                .from(tableOfEntityComplexSets)
                .where(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID).eq(baseEntity.getId()))
                .and(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.CREDITOR_ID).eq(creditorId))
                .and(tableOfEntityComplexSets.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE).lessOrEqual(DataUtils.convert(loadingDate)))
                .asTable("essn");

        select = context
                .select(tableOfComplexSets.field(EAV_M_COMPLEX_SET.NAME),
                        tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.ID),
                        tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.CREDITOR_ID),
                        tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE),
                        tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED),
                        tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST))
                .from(tableNumbering)
                .join(tableOfComplexSets)
                .on(tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID).eq(tableOfComplexSets.field(EAV_M_COMPLEX_SET.ID)))
                .where((tableNumbering.field("num_pp").cast(Integer.class).equal(1)
                        .and(tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED).eq(false))
                        .and(tableOfComplexSets.field(EAV_M_COMPLEX_SET.IS_FINAL).eq(false)))
                        .or(tableNumbering.field(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE).eq(savingReportDate)
                                .and(tableOfComplexSets.field(EAV_M_COMPLEX_SET.IS_FINAL).eq(true))));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            String attribute = (String) row.get(EAV_M_COMPLEX_SET.NAME.getName());
            IMetaAttribute metaAttribute = baseEntity.getMetaAttribute(attribute);
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE.getName()));
            baseEntity.put(attribute, constructValue(row, baseEntity.getMeta(), baseEntity.getMemberType(attribute), metaAttribute, loadingDate));
        }
    }

    @Override
    public void deleteAll(long baseEntityId) {
        String tableAlias = "cv";
        Delete delete = context
                .delete(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(tableAlias).ENTITY_ID.equal(baseEntityId));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }

    private IBaseValue constructValue(final Map<String, Object> row, final IMetaClass metaClass, final IMetaType metaType, final IMetaAttribute metaAttribute, final Date loadingDate) {
        IMetaSet metaSet = (IMetaSet) metaType;

        long id = ((BigDecimal) row.get(EAV_BE_ENTITY_COMPLEX_SETS.ID.getName())).longValue();

        long creditorId = ((BigDecimal) row.get(EAV_BE_ENTITY_COMPLEX_SETS.CREDITOR_ID.getName())).longValue();

        boolean closed = ((BigDecimal) row.get(EAV_BE_ENTITY_COMPLEX_SETS.IS_CLOSED.getName())).longValue() == 1;

        boolean last = ((BigDecimal) row.get(EAV_BE_ENTITY_COMPLEX_SETS.IS_LAST.getName())).longValue() == 1;

        Date reportDate = DataUtils.convertToSQLDate((Timestamp) row.get(EAV_BE_ENTITY_COMPLEX_SETS.REPORT_DATE.getName()));

        IBaseSet baseSet = new BaseSet(id, metaSet.getMemberType(), creditorId);

        baseSetComplexValueDao.loadBaseValues(baseSet, reportDate, loadingDate);

        return BaseValueFactory.create(
                metaClass.getType(),
                metaType,
                id,
                creditorId,
                reportDate,
                baseSet,
                closed,
                last);
    }
}
