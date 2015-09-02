package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.persistance.dao.IBatchStatusDao;
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

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BATCH_STATUSES;

/**
 * Created by maksat on 8/3/15.
 */
@Repository
public class BatchStatusDaoImpl extends JDBCSupport implements IBatchStatusDao {

    @Autowired
    private DSLContext context;

    @Override
    public Long insert(BatchStatus batchStatus) {
        Insert insert = context.insertInto(EAV_BATCH_STATUSES,
                EAV_BATCH_STATUSES.BATCH_ID,
                EAV_BATCH_STATUSES.STATUS_ID,
                EAV_BATCH_STATUSES.RECEIPT_DATE,
                EAV_BATCH_STATUSES.DESCRIPTION
        ).values(
                batchStatus.getBatchId(),
                batchStatus.getStatusId(),
                DataUtils.convertToTimestamp(batchStatus.getReceiptDate()),
                batchStatus.getDescription()
        );
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public List<BatchStatus> getList(long batchId) {
        Select select = context.selectFrom(EAV_BATCH_STATUSES)
                .where(EAV_BATCH_STATUSES.BATCH_ID.eq(batchId))
                .orderBy(EAV_BATCH_STATUSES.RECEIPT_DATE, EAV_BATCH_STATUSES.STATUS_ID);

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<BatchStatus> batchStatusList = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            BatchStatus batchStatus = toBatchStatus(row);
            batchStatusList.add(batchStatus);
        }

        return batchStatusList;
    }

    private BatchStatus toBatchStatus(Map<String, Object> row) {
        BatchStatus batchStatus = new BatchStatus();
        batchStatus.setId(((BigDecimal) row.get(EAV_BATCH_STATUSES.ID.getName())).longValue());
        batchStatus.setBatchId(getNullSafeLong(row, EAV_BATCH_STATUSES.BATCH_ID));
        batchStatus.setStatusId(getNullSafeLong(row, EAV_BATCH_STATUSES.STATUS_ID));
        batchStatus.setDescription((String) row.get(EAV_BATCH_STATUSES.DESCRIPTION.getName()));
        batchStatus.setReceiptDate(DataUtils.convert((Timestamp) row.get(EAV_BATCH_STATUSES.RECEIPT_DATE.getName())));
        return batchStatus;
    }

    private Long getNullSafeLong(Map<String, Object> row, Field field) {
        Object o = row.get(field.getName());
        return o == null ? null : ((BigDecimal) o).longValue();
    }

}
