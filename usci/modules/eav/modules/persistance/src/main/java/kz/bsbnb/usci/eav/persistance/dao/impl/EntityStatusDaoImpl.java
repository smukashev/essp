package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.persistance.dao.IEntityStatusDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_ENTITY_STATUSES;

@Repository
public class EntityStatusDaoImpl extends JDBCSupport implements IEntityStatusDao {

    @Autowired
    private DSLContext context;

    @Override
    public Long insert(EntityStatus entityStatus) {
        Insert insert = context.insertInto(EAV_ENTITY_STATUSES,
                EAV_ENTITY_STATUSES.BATCH_ID,
                EAV_ENTITY_STATUSES.ENTITY_ID,
                EAV_ENTITY_STATUSES.STATUS_ID,
                EAV_ENTITY_STATUSES.DESCRIPTION,
                EAV_ENTITY_STATUSES.RECEIPT_DATE,
                EAV_ENTITY_STATUSES.INDEX_
        ).values(
                entityStatus.getBatchId(),
                entityStatus.getEntityId(),
                entityStatus.getStatusId(),
                entityStatus.getDescription(),
                DataUtils.convertToTimestamp(entityStatus.getReceiptDate()),
                entityStatus.getIndex()
        );
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public List<EntityStatus> getList(long batchId) {
        Select select = context.selectFrom(EAV_ENTITY_STATUSES)
                .where(EAV_ENTITY_STATUSES.BATCH_ID.eq(batchId))
                .orderBy(EAV_ENTITY_STATUSES.STATUS_ID, EAV_ENTITY_STATUSES.RECEIPT_DATE);

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
        entityStatus.setBatchId(getNullSafeLong(row, EAV_ENTITY_STATUSES.BATCH_ID));
        entityStatus.setEntityId(getNullSafeLong(row, EAV_ENTITY_STATUSES.ENTITY_ID));
        entityStatus.setStatusId(getNullSafeLong(row, EAV_ENTITY_STATUSES.STATUS_ID));
        entityStatus.setDescription((String) row.get(EAV_ENTITY_STATUSES.DESCRIPTION.getName()));
        entityStatus.setReceiptDate(DataUtils.convert((Timestamp) row.get(EAV_ENTITY_STATUSES.RECEIPT_DATE.getName())));
        entityStatus.setIndex(getNullSafeLong(row, EAV_ENTITY_STATUSES.INDEX_));
        return entityStatus;
    }

    private Long getNullSafeLong(Map<String, Object> row, Field field) {
        Object o = row.get(field.getName());
        return o == null ? null : ((BigDecimal) o).longValue();
    }

}
