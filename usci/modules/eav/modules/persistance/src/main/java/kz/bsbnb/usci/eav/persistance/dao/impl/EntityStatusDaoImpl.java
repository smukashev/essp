package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.persistance.dao.IEntityStatusDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.springframework.beans.factory.annotation.Autowired;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_ENTITY_STATUSES;

/**
 * Created by maksat on 8/3/15.
 */
public class EntityStatusDaoImpl extends JDBCSupport implements IEntityStatusDao {

    @Autowired
    private DSLContext context;

    @Override
    public Long insert(EntityStatus entityStatus) {
        Insert insert = context.insertInto(EAV_ENTITY_STATUSES,
                EAV_ENTITY_STATUSES.BATCH_ID,
                EAV_ENTITY_STATUSES.ENTITY_ID,
                EAV_ENTITY_STATUSES.STATUS_ID,
                EAV_ENTITY_STATUSES.RECEIPT_DATE,
                EAV_ENTITY_STATUSES.INDEX_
        ).values(
                entityStatus.getBatchId(),
                entityStatus.getEntityId(),
                entityStatus.getStatusId(),
                DataUtils.convertToTimestamp(entityStatus.getReceiptDate()),
                entityStatus.getIndex()
        );
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

}
