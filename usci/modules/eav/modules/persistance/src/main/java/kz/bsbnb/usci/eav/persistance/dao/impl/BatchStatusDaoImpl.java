package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.persistance.dao.IBatchStatusDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
                EAV_BATCH_STATUSES.RECEIPT_DATE
        ).values(
                batchStatus.getBatchId(),
                batchStatus.getStatusId(),
                DataUtils.convertToTimestamp(batchStatus.getReceiptDate())
        );
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

}
