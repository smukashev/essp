package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBeEntityDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.Insert;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_SET_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITIES;

/**
 * Created by Alexandr.Motov on 16.03.14.
 */
@Repository
public class BeEntityDaoImpl extends JDBCSupport implements IBeEntityDao {

    private final Logger logger = LoggerFactory.getLogger(BeEntityDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public long insert(IPersistable persistable) {
        IBaseEntity baseEntity = (IBaseEntity)persistable;
        long baseEntityId =
                insert(
                        baseEntity.getMeta().getId()
                );
        baseEntity.setId(baseEntityId);

        return baseEntityId;
    }

    protected long insert(long metaClassId) {
        Insert insert = context
                .insertInto(EAV_BE_ENTITIES)
                .set(EAV_BE_ENTITIES.CLASS_ID, metaClassId);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void delete(IPersistable persistable) {
        delete(persistable.getId());
    }

    protected void delete(long id)
    {
        String tableAlias = "e";
        Delete delete = context
                .delete(EAV_BE_ENTITIES.as(tableAlias))
                .where(EAV_BE_ENTITIES.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
        if (count != 1)
        {
            throw new RuntimeException("DELETE operation should be delete only one record.");
        }
    }

    public boolean isUsed(long baseEntityId)
    {
        Select select;
        List<Map<String, Object>> rows;

        String complexValuesTableAlias = "cv";
        select = context
                .select(DSL.count().as("VALUE_COUNT"))
                .from(EAV_BE_COMPLEX_VALUES.as(complexValuesTableAlias))
                .where(EAV_BE_COMPLEX_VALUES.as(complexValuesTableAlias).ENTITY_VALUE_ID.equal(baseEntityId));

        logger.debug(select.toString());
        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        long complexValuesCount = ((BigDecimal)rows.get(0).get("VALUE_COUNT")).longValue();

        String complexSetValuesTableAlias = "csv";
        select = context
                .select(DSL.count().as("VALUE_COUNT"))
                .from(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias).ENTITY_VALUE_ID.equal(baseEntityId));

        logger.debug(select.toString());
        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        long complexSetValuesCount = ((BigDecimal)rows.get(0).get("VALUE_COUNT")).longValue();

        return complexValuesCount != 0 || complexSetValuesCount != 0;
    }

}
