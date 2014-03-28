package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.*;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeEntitySimpleSetDao;
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
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITY_SIMPLE_SETS;

/**
 * Created by Alexandr.Motov on 16.03.14.
 */
@Repository
public class BeEntitySimpleSetDaoImpl extends JDBCSupport implements IBeEntitySimpleSetDao {

    private final Logger logger = LoggerFactory.getLogger(BeEntitySimpleSetDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    IBatchRepository batchRepository;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Override
    public long insert(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue)persistable;
        IBaseSet baseSet = (IBaseSet)baseValue.getValue();
        long baseValueId = insert(
                baseValue.getBaseContainer().getId(),
                baseValue.getMetaAttribute().getId(),
                baseSet.getId(),
                baseValue.getBatch().getId(),
                baseValue.getIndex(),
                baseValue.getRepDate(),
                baseValue.isClosed(),
                baseValue.isLast());
        baseValue.setId(baseValueId);

        return baseValueId;
    }

    protected long insert(long baseEntityId, long metaAttributeId, long baseSetId, long batchId,
                          long index, Date reportDate, boolean closed, boolean last)
    {
        Insert insert = context
                .insertInto(EAV_BE_ENTITY_SIMPLE_SETS)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.ENTITY_ID, baseEntityId)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID, baseSetId)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID, batchId)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_, index)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {
        IBaseValue baseValue = (IBaseValue)persistable;
        IBaseSet baseSet = (IBaseSet)baseValue.getValue();
        update(baseValue.getId(), baseValue.getBaseContainer().getId(), baseValue.getMetaAttribute().getId(),
                baseSet.getId(), baseValue.getBatch().getId(), baseValue.getIndex(), baseValue.getRepDate(),
                baseValue.isClosed(), baseValue.isLast());
    }

    protected void update(long id, long baseEntityId, long metaAttributeId, long baseSetId, long batchId,
                          long index, Date reportDate, boolean closed, boolean last)
    {
        String tableAlias = "ss";
        Update update = context
                .update(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID, baseEntityId)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID, metaAttributeId)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).SET_ID, baseSetId)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).BATCH_ID, batchId)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).INDEX_, index)
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE, DataUtils.convert(reportDate))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED, DataUtils.convert(closed))
                .set(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST, DataUtils.convert(last))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID.equal(id));

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

    protected void delete(long id) {
        String tableAlias = "ss";
        Delete delete = context
                .delete(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
        if (count != 1)
        {
            throw new RuntimeException("DELETE operation should be delete only one record.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getNextBaseValue(IBaseValue baseValue)
    {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseEntity baseEntity = (IBaseEntity)baseContainer;
        IMetaClass metaClass = baseEntity.getMeta();

        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaSet metaSet = (IMetaSet)metaType;

        IBaseValue nextBaseValue = null;

        String tableAlias = "ess";
        String subqueryAlias = "essn";

        Table subqueryTable = context
                .select(DSL.rank().over()
                        .orderBy(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.asc()).as("num_pp"),
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).BATCH_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).INDEX_,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).SET_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.greaterThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subqueryAlias);

        Select select = context
                .select(subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.ID),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST))
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
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.ID.getName())).longValue();
            long index = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_.getName())).longValue();
            boolean closed = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED.getName())).longValue() == 1;
            boolean last = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST.getName())).longValue() == 1;
            long setId = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID.getName())).longValue();
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE.getName()));
            Batch batch = batchRepository.getBatch(((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID.getName())).longValue());

            IBaseSet baseSet = new BaseSet(setId, metaSet.getMemberType());
            baseEntityProcessorDao.loadSimpleSetValues(baseSet, reportDate);

            nextBaseValue = BaseValueFactory.create(metaClass.getType(), metaType,
                    id, batch, index, reportDate, baseSet, closed, last);
        }

        return nextBaseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IBaseValue getPreviousBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IBaseEntity baseEntity = (IBaseEntity)baseContainer;
        IMetaClass metaClass = baseEntity.getMeta();

        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaSet metaSet = (IMetaSet)metaType;

        IBaseValue previousBaseValue = null;

        String tableAlias = "ess";
        String subqueryAlias = "essn";

        Table subqueryTable = context
                .select(DSL.rank().over()
                        .orderBy(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.desc()).as("num_pp"),
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).BATCH_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).INDEX_,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).SET_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID.equal(baseEntity.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.lessThan(DataUtils.convert(baseValue.getRepDate())))
                .asTable(subqueryAlias);

        Select select = context
                .select(subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.ID),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED),
                        subqueryTable.field(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST))
                .from(subqueryTable)
                .where(subqueryTable.field("num_pp").cast(Integer.class).equal(1));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new RuntimeException("Query for get previous instance of BaseValue return more than one row.");
        }

        if (rows.size() == 1)
        {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.ID.getName())).longValue();
            long index = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_.getName())).longValue();
            boolean closed = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED.getName())).longValue() == 1;
            boolean last = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST.getName())).longValue() == 1;
            long setId = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID.getName())).longValue();
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE.getName()));
            Batch batch = batchRepository.getBatch(((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID.getName())).longValue());

            IBaseSet baseSet = new BaseSet(setId, metaSet.getMemberType());
            baseEntityProcessorDao.loadSimpleSetValues(baseSet, reportDate);

            previousBaseValue = BaseValueFactory.create(metaClass.getType(), metaType,
                    id, batch, index, reportDate, baseSet, closed, last);
        }

        return previousBaseValue;
    }

    @Override
    public IBaseValue getClosedBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaSet metaSet = (IMetaSet)metaType;

        IBaseValue closedBaseValue = null;

        String tableAlias = "ess";
        Select select = context
                .select(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).BATCH_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).INDEX_,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).SET_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID.equal(baseContainer.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE.equal(DataUtils.convert(baseValue.getRepDate())))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_CLOSED.equal(DataUtils.convert(true)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new RuntimeException("Query for get closed instance of BaseValue return more than one row.");
        }

        if (rows.size() == 1)
        {
            Map<String, Object> row = rows.iterator().next();

            long id = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.ID.getName())).longValue();
            long index = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_.getName())).longValue();
            boolean last = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.IS_LAST.getName())).longValue() == 1;
            long setId = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID.getName())).longValue();
            Batch batch = batchRepository.getBatch(((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID.getName())).longValue());

            IBaseSet baseSet = new BaseSet(setId, metaSet.getMemberType());
            baseEntityProcessorDao.loadSimpleSetValues(baseSet, baseValue.getRepDate());

            closedBaseValue = BaseValueFactory.create(MetaContainerTypes.META_CLASS, metaType,
                    id, batch, index, baseValue.getRepDate(), baseSet, true, last);
        }

        return closedBaseValue;
    }

    @Override
    public IBaseValue getLastBaseValue(IBaseValue baseValue) {
        IBaseContainer baseContainer = baseValue.getBaseContainer();
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaSet metaSet = (IMetaSet)metaType;

        IBaseValue lastBaseValue = null;

        String tableAlias = "ess";
        Select select = context
                .select(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).BATCH_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).INDEX_,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).REPORT_DATE,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).SET_ID,
                        EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST)
                .from(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias))
                .where(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ENTITY_ID.equal(baseContainer.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                .and(EAV_BE_ENTITY_SIMPLE_SETS.as(tableAlias).IS_LAST.equal(DataUtils.convert(true)));

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
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.ID.getName())).longValue();
            long index = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.INDEX_.getName())).longValue();
            boolean closed = ((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.IS_CLOSED.getName())).longValue() == 1;
            long setId = ((BigDecimal) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.SET_ID.getName())).longValue();
            Date reportDate = DataUtils.convertToSQLDate((Timestamp) row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.REPORT_DATE.getName()));
            Batch batch = batchRepository.getBatch(((BigDecimal)row
                    .get(EAV_BE_ENTITY_SIMPLE_SETS.BATCH_ID.getName())).longValue());

            IBaseSet baseSet = new BaseSet(setId, metaSet.getMemberType());
            baseEntityProcessorDao.loadSimpleSetValues(baseSet, baseValue.getRepDate());

            lastBaseValue = BaseValueFactory.create(MetaContainerTypes.META_CLASS, metaType,
                    id, batch, index, reportDate, baseSet, closed, true);
        }

        return lastBaseValue;
    }
}

