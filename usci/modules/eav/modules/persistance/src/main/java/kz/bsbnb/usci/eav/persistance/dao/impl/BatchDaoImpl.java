package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IEavGlobalDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Repository
public class BatchDaoImpl extends JDBCSupport implements IBatchDao {
    private final Logger logger = LoggerFactory.getLogger(BatchDaoImpl.class);

    @Autowired
    private DSLContext context;

    @Autowired
    private IEavGlobalDao eavGlobalDao;

    @Override
    public Batch load(long id) {
        if (id < 0)
            return null;

        Batch batch = new Batch();
        batch.setId(id);

        loadBatch(batch);

        return batch;
    }

    @Override
    @Transactional
    public long save(Batch batch) {
        long batchId;

        if (batch.getId() < 1) {
            batchId = insertBatch(batch);
            batch.setId(batchId);
        } else {
            updateBatch(batch);
            batchId = batch.getId();
        }

        return batchId;
    }

    @Override
    public List<Batch> getPendingBatchList() {
        EavGlobal statusCompleted = eavGlobalDao.get(BatchStatuses.COMPLETED.type(), BatchStatuses.COMPLETED.code());
        EavGlobal statusError = eavGlobalDao.get(BatchStatuses.ERROR.type(), BatchStatuses.ERROR.code());

        Select select = context.select(
                EAV_BATCHES.ID,
                EAV_BATCHES.USER_ID,
                EAV_BATCHES.CREDITOR_ID,
                EAV_BATCHES.FILE_NAME,
                EAV_BATCHES.HASH,
                EAV_BATCHES.SIGN,
                EAV_BATCHES.REP_DATE,
                EAV_BATCHES.RECEIPT_DATE,
                EAV_BATCHES.BATCH_TYPE,
                EAV_BATCHES.TOTAL_COUNT,
                EAV_BATCHES.ACTUAL_COUNT,
                EAV_BATCHES.REPORT_ID,
                DSL.field("\"bs\".STATUS_ID")
        ).from(EAV_BATCHES).join(
                context.select(
                        EAV_BATCH_STATUSES.BATCH_ID,
                        EAV_BATCH_STATUSES.STATUS_ID,
                        DSL.rowNumber().over()
                                .partitionBy(EAV_BATCH_STATUSES.BATCH_ID)
                                .orderBy(EAV_BATCH_STATUSES.RECEIPT_DATE.desc(),
                                        EAV_BATCH_STATUSES.STATUS_ID.desc()).as("num"))
                        .from(EAV_BATCH_STATUSES).asTable("bs")).
                on(EAV_BATCHES.ID.eq(DSL.field("\"bs\".\"BATCH_ID\"", Long.class)))
                .where(DSL.field("\"bs\".\"num\"").eq(1))
                .and(DSL.field("\"bs\".STATUS_ID").ne(statusCompleted.getId())
                        .and(DSL.field("\"bs\".STATUS_ID").ne(statusError.getId())))
                .and(EAV_BATCHES.IS_DISABLED.eq(DataUtils.convert(false))).orderBy(EAV_BATCHES.ID);

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Batch> pendingBatchList = new ArrayList<>();

        for (Map<String, Object> row : rows)
            pendingBatchList.add(fillBatch(new Batch(), row));

        return pendingBatchList;
    }

    @Override
    public List<Batch> getBatchListToSign(long userId) {
        Select select = context.selectFrom(EAV_BATCHES)
                .where(EAV_BATCHES.USER_ID.eq(userId)).and(EAV_BATCHES.SIGN.isNull());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Batch> batchListToSign = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            batchListToSign.add(fillBatch(new Batch(), row));
        }

        return batchListToSign;
    }

    @Override
    public List<Batch> getAll(java.util.Date repDate) {
        SelectWhereStep select = context.selectFrom(EAV_BATCHES);

//        select.limit(100);

        if (repDate != null) {
            select.where(EAV_BATCHES.REP_DATE.eq(DataUtils.convert(repDate)));
        }

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Batch> batchList = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            batchList.add(fillBatch(new Batch(), row));
        }

        return batchList;
    }

    private long insertBatch(Batch batch) {
        Insert insert = context.insertInto(EAV_BATCHES,
                EAV_BATCHES.USER_ID,
                EAV_BATCHES.CREDITOR_ID,
                EAV_BATCHES.FILE_NAME,
                EAV_BATCHES.HASH,
                EAV_BATCHES.SIGN,
                EAV_BATCHES.REP_DATE,
                EAV_BATCHES.RECEIPT_DATE,
                EAV_BATCHES.BATCH_TYPE,
                EAV_BATCHES.TOTAL_COUNT,
                EAV_BATCHES.ACTUAL_COUNT,
                EAV_BATCHES.REPORT_ID
        ).values(
                batch.getUserId(),
                batch.getCreditorId(),
                batch.getFileName(),
                batch.getHash(),
                batch.getSign(),
                DataUtils.convert(batch.getRepDate()),
                DataUtils.convertToTimestamp(batch.getReceiptDate()),
                batch.getBatchType(),
                batch.getTotalCount(),
                batch.getActualCount(),
                batch.getReportId()
        );

        long batchId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        if (batchId < 1) {
            logger.error("Can't insert batch");
            return 0;
        }

        return batchId;
    }

    private void updateBatch(Batch batch) {
        Update update = context.update(EAV_BATCHES)
                .set(EAV_BATCHES.USER_ID, batch.getUserId())
                .set(EAV_BATCHES.CREDITOR_ID, batch.getCreditorId())
                .set(EAV_BATCHES.FILE_NAME, batch.getFileName())
                .set(EAV_BATCHES.HASH, batch.getHash())
                .set(EAV_BATCHES.SIGN, batch.getSign())
                .set(EAV_BATCHES.REP_DATE, DataUtils.convert(batch.getRepDate()))
                .set(EAV_BATCHES.RECEIPT_DATE, DataUtils.convertToTimestamp(batch.getReceiptDate()))
                .set(EAV_BATCHES.BATCH_TYPE, batch.getBatchType())
                .set(EAV_BATCHES.TOTAL_COUNT, batch.getTotalCount())
                .set(EAV_BATCHES.ACTUAL_COUNT, batch.getActualCount())
                .set(EAV_BATCHES.REPORT_ID, batch.getReportId())
                .where(EAV_BATCHES.ID.eq(batch.getId()));

        int updatedCount = updateWithStats(update.getSQL(), update.getBindValues().toArray());

        if (updatedCount < 1) {
            logger.error("Batch not found. Can't update.");
        }
    }

    private void loadBatch(Batch batch) {
        Select select = context.selectFrom(EAV_BATCHES).where(EAV_BATCHES.ID.eq(batch.getId()));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.E149));
        }

        if (rows.size() < 1) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.E150));
        }

        Map<String, Object> row = rows.get(0);

        if (row != null) {
            fillBatch(batch, row);
        } else {
            logger.error("Can't load batch, empty data set.");
        }
    }

    private Batch fillBatch(Batch batch, Map<String, Object> row) {
        batch.setId(((BigDecimal) row.get(EAV_BATCHES.ID.getName())).longValue());
        batch.setUserId(getNullSafeLong(row, EAV_BATCHES.USER_ID));
        batch.setCreditorId(getNullSafeLong(row, EAV_BATCHES.CREDITOR_ID));
        batch.setFileName((String) row.get(EAV_BATCHES.FILE_NAME.getName()));
        batch.setHash((String) row.get(EAV_BATCHES.HASH.getName()));
        batch.setStatusId(getNullSafeLong(row, EAV_BATCH_STATUSES.STATUS_ID));
        batch.setSign((String) row.get(EAV_BATCHES.SIGN.getName()));
        batch.setRepDate(DataUtils.convert((Timestamp) row.get(EAV_BATCHES.REP_DATE.getName())));
        batch.setReceiptDate(DataUtils.convert((Timestamp) row.get(EAV_BATCHES.RECEIPT_DATE.getName())));
        batch.setBatchType((String) row.get(EAV_BATCHES.BATCH_TYPE.getName()));
        batch.setTotalCount(getNullSafeLong(row, EAV_BATCHES.TOTAL_COUNT));
        batch.setActualCount(getNullSafeLong(row, EAV_BATCHES.ACTUAL_COUNT));
        batch.setReportId(getNullSafeLong(row, EAV_BATCHES.REPORT_ID));
        return batch;
    }

    private Long getNullSafeLong(Map<String, Object> row, Field field) {
        Object o = row.get(field.getName());
        return o == null ? 0L : ((BigDecimal) o).longValue();
    }

    @Override
    public void incrementActualCount(long batchId, long count) {
        Update update = context.update(EAV_BATCHES)
                .set(EAV_BATCHES.ACTUAL_COUNT, EAV_BATCHES.ACTUAL_COUNT.plus(count))
                .where(EAV_BATCHES.ID.eq(batchId));

        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }

    @Override
    public void clearActualCount(long batchId){
        Update update = context.update(EAV_BATCHES)
                .set(EAV_BATCHES.ACTUAL_COUNT, 0L)
                .where(EAV_BATCHES.ID.eq(batchId));

        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }
}
