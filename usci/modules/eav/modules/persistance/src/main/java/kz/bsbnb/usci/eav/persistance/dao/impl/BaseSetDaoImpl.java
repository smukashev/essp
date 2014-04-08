package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseSetDao;
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

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_SETS;

/**
 * Created by Alexandr.Motov on 16.03.14.
 */
@Repository
public class BaseSetDaoImpl extends JDBCSupport implements IBaseSetDao {

    private final Logger logger = LoggerFactory.getLogger(BaseSetDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public long insert(IPersistable persistable) {
        IBaseSet baseSet = (IBaseSet)persistable;
        long baseSetId = insert(baseSet.getLevel(), baseSet.isLast());
        baseSet.setId(baseSetId);

        return baseSetId;
    }

    private long insert(long level, boolean last) {
        Insert insert = context
                .insertInto(EAV_BE_SETS)
                .set(EAV_BE_SETS.LEVEL_, level)
                .set(EAV_BE_SETS.IS_LAST, DataUtils.convert(last));

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {
        IBaseSet baseSet = (IBaseSet)persistable;
        update(baseSet.getId(), baseSet.getLevel(), baseSet.isLast());
    }

    private void update(long id, long level, boolean last) {
        String tableAlias = "s";
        Update update = context
                .update(EAV_BE_SETS.as(tableAlias))
                .set(EAV_BE_SETS.as(tableAlias).LEVEL_, level)
                .set(EAV_BE_SETS.as(tableAlias).IS_LAST, DataUtils.convert(last))
                .where(EAV_BE_SETS.as(tableAlias).ID.equal(id));

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
        String tableAlias = "s";
        Delete delete = context
                .delete(EAV_BE_SETS.as(tableAlias))
                .where(EAV_BE_SETS.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
        if (count != 1)
        {
            throw new RuntimeException("DELETE operation should be delete only one record.");
        }
    }

}
