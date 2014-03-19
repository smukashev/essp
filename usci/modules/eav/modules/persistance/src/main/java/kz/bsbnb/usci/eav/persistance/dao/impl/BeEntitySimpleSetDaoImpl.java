package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBeEntitySimpleSetDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
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

}

