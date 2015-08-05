package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.persistance.dao.IEntityStatusDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Override
    public List<EntityStatus> getList(long batchId) {
        Select select = context.selectFrom(EAV_ENTITY_STATUSES).where(EAV_ENTITY_STATUSES.BATCH_ID.eq(batchId));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<EntityStatus> entityStatusList = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            EntityStatus entityStatus = toEntityStatus(row);
            entityStatusList.add(entityStatus);
        }

        return entityStatusList;
    }

    private EntityStatus toEntityStatus(Map<String, Object> row) {
        EntityStatus entityStatus = new EntityStatus();
        entityStatus.setId(((BigDecimal) row.get(EAV_ENTITY_STATUSES.ID.getName())).longValue());
        entityStatus.setBatchId(((BigDecimal) row.get(EAV_ENTITY_STATUSES.BATCH_ID.getName())).longValue());
        entityStatus.setEntityId(((BigDecimal) row.get(EAV_ENTITY_STATUSES.ENTITY_ID.getName())).longValue());
        entityStatus.setStatusId(((BigDecimal) row.get(EAV_ENTITY_STATUSES.STATUS_ID.getName())).longValue());
        entityStatus.setDescription((String) row.get(EAV_ENTITY_STATUSES.DESCRIPTION.getName()));
        entityStatus.setReceiptDate(DataUtils.convert((Timestamp) row.get(EAV_ENTITY_STATUSES.RECEIPT_DATE.getName())));
        entityStatus.setIndex(((BigDecimal) row.get(EAV_ENTITY_STATUSES.INDEX_.getName())).longValue());
        return entityStatus;
    }

}
