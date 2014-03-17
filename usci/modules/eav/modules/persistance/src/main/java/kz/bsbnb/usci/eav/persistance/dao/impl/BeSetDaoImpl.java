package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBeSetDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_SETS;

/**
 * Created by Alexandr.Motov on 16.03.14.
 */
@Repository
public class BeSetDaoImpl extends JDBCSupport implements IBeSetDao {

    private final Logger logger = LoggerFactory.getLogger(BeSetDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public void insert(IPersistable persistable) {
        IBaseSet baseSet = (IBaseSet)persistable;
        long baseSetId =
                insert(
                        baseSet.getLevel(),
                        baseSet.isLast()
                );
        baseSet.setId(baseSetId);
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

    }

    @Override
    public void delete(IPersistable persistable) {

    }
}
