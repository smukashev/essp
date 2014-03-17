package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBeComplexSetDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeSimpleSetDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeValueDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Insert;
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
public class BeSimpleSetDaoImpl extends JDBCSupport implements IBeSimpleSetDao {

    private final Logger logger = LoggerFactory.getLogger(BeSimpleSetDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public void insert(IPersistable persistable) {
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

    }

    @Override
    public void delete(IPersistable persistable) {

    }
}

