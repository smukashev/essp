package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IEavGlobalDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BATCHES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BATCH_STATUSES;

@Repository
public class BatchDaoImpl extends JDBCSupport implements IBatchDao {
    private final Logger logger = LoggerFactory.getLogger(BatchDaoImpl.class);

    @Autowired
    private DSLContext context;

    @Autowired
    private IEavGlobalDao eavGlobalDao;


    private static final String WAITING_FOR_SIGNATURE = "WAITING_FOR_SIGNATURE";

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
        EavGlobal statusCancelled = eavGlobalDao.get(BatchStatuses.CANCELLED.type(), BatchStatuses.CANCELLED.code());

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
                                .orderBy(EAV_BATCH_STATUSES.RECEIPT_DATE.desc(), EAV_BATCH_STATUSES.STATUS_ID.desc()).as("num"))
                        .from(EAV_BATCH_STATUSES).where(EAV_BATCH_STATUSES.BATCH_ID.notIn(context.selectDistinct(EAV_BATCH_STATUSES.BATCH_ID).from(EAV_BATCH_STATUSES).where(EAV_BATCH_STATUSES.STATUS_ID.eq(statusCompleted.getId()).or(EAV_BATCH_STATUSES.STATUS_ID.eq(statusError.getId())).or(EAV_BATCH_STATUSES.STATUS_ID.eq(statusCancelled.getId()))))).asTable("bs")).
                on(EAV_BATCHES.ID.eq(DSL.field("\"bs\".\"BATCH_ID\"", Long.class)))
                .where(DSL.field("\"bs\".\"num\"").eq(1))
                        .and(EAV_BATCHES.IS_DISABLED.eq(DataUtils.convert(false)))
                        .and(EAV_BATCHES.IS_MAINTENANCE.eq(DataUtils.convert(false))
                                .or(EAV_BATCHES.IS_MAINTENANCE.eq(DataUtils.convert(true)).and(EAV_BATCHES.IS_MAINTENANCE_APPROVED.eq(DataUtils.convert(true))))).orderBy(EAV_BATCHES.ID);
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Batch> pendingBatchList = new ArrayList<>();

        for (Map<String, Object> row : rows)
            pendingBatchList.add(fillBatch(new Batch(), row));

        return pendingBatchList;
    }

    @Override
    public List<Batch> getBatchListToSign(long creditorId) {
        EavGlobal statusCompleted = eavGlobalDao.get(BatchStatuses.COMPLETED.type(), BatchStatuses.COMPLETED.code());
        EavGlobal statusSign = eavGlobalDao.get(BatchStatuses.WAITING_FOR_SIGNATURE.type(), BatchStatuses.WAITING_FOR_SIGNATURE.code());
        EavGlobal statusError = eavGlobalDao.get(BatchStatuses.ERROR.type(), BatchStatuses.ERROR.code());

        Select select = context.select().from(EAV_BATCHES)
                        .join(context.select(DSL.max(EAV_BATCH_STATUSES.STATUS_ID).as("STATUS_ID"), EAV_BATCH_STATUSES.BATCH_ID)
                                .from(EAV_BATCH_STATUSES)
                                .where(EAV_BATCH_STATUSES.STATUS_ID.ne(statusCompleted.getId()))
                                .groupBy(EAV_BATCH_STATUSES.BATCH_ID).asTable("bs"))
                        .on(EAV_BATCHES.ID.eq(DSL.field("\"bs\".\"BATCH_ID\"", Long.class)))
                .where(EAV_BATCHES.CREDITOR_ID.eq(creditorId))
                .and(EAV_BATCHES.SIGN.isNull())
                .andNotExists(context.select().from(EAV_BATCH_STATUSES)
                        .where((EAV_BATCH_STATUSES.STATUS_ID.eq(statusCompleted.getId())).or(EAV_BATCH_STATUSES.STATUS_ID.eq(statusError.getId())))
                        .and(EAV_BATCH_STATUSES.BATCH_ID.eq(DSL.field("\"bs\".\"BATCH_ID\"", Long.class))))

                .and(DSL.field("\"bs\".STATUS_ID").eq(statusSign.getId()));
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

    @Override
    public List<Batch> getAll(java.util.Date repDate, List<Creditor> creditorsList) {
        SelectWhereStep select = context.selectFrom(EAV_BATCHES);

//        select.limit(100);

        // one creditor must exists!
        select.where(EAV_BATCHES.CREDITOR_ID.eq(creditorsList.get(0).getId()));
        for (int i = 1; i < creditorsList.size(); i++) {
            select.where().or(EAV_BATCHES.CREDITOR_ID.eq(creditorsList.get(i).getId()));
        }

        if (repDate != null) {
            select.where().and(EAV_BATCHES.REP_DATE.eq(DataUtils.convert(repDate)));
        }

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Batch> batchList = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            batchList.add(fillBatch(new Batch(), row));
        }

        return batchList;
    }

    @Override
    public List<Batch> getAll(java.util.Date repDate, List<Creditor> creditorsList, int firstIndex, int count) {
        SelectWhereStep select = context.selectFrom(EAV_BATCHES);

        // one creditor must exists!
        select.where(EAV_BATCHES.CREDITOR_ID.eq(creditorsList.get(0).getId()));
        for (int i = 1; i < creditorsList.size(); i++) {
            select.where().or(EAV_BATCHES.CREDITOR_ID.eq(creditorsList.get(i).getId()));
        }

        if (repDate != null) {
            select.where().and(EAV_BATCHES.REP_DATE.eq(DataUtils.convert(repDate)));
        }

        select.orderBy(EAV_BATCHES.RECEIPT_DATE.desc());

        select.limit(count).offset(firstIndex);


        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Batch> batchList = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            batchList.add(fillBatch(new Batch(), row));
        }

        return batchList;
    }

    @Override
    public int getBatchCount(List<Creditor> creditorsList, Date reportDate) {
        SelectWhereStep select = context
                .select(DSL.count(EAV_BATCHES.ID).as("batch_count"))
                .from(EAV_BATCHES);

        for (int i = 1; i < creditorsList.size(); i++) {
            select.where().or(EAV_BATCHES.CREDITOR_ID.eq(creditorsList.get(i).getId()));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());


        return ((BigDecimal) rows.get(0).get("batch_count")).intValue();
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
                .set(EAV_BATCHES.IS_MAINTENANCE, DataUtils.convert(batch.isMaintenance()))
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
            throw new IllegalArgumentException(Errors.compose(Errors.E149));
        }

        if (rows.size() < 1) {
            throw new IllegalArgumentException(Errors.compose(Errors.E150));
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
        batch.setMaintenance(getNullSafeLong(row, EAV_BATCHES.IS_MAINTENANCE) == 1);
        batch.setMaintenanceApproved(getNullSafeLong(row, EAV_BATCHES.IS_MAINTENANCE_APPROVED) == 1);
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


    @Override
    public List<Batch> getMaintenanceBatches(Date reportDate) {
        SelectConditionStep select = context.selectFrom(EAV_BATCHES)
                .where(EAV_BATCHES.IS_MAINTENANCE.eq(DataUtils.convert(true)))
                .and(EAV_BATCHES.IS_MAINTENANCE_APPROVED.eq(DataUtils.convert(false)));

        if(reportDate != null)
            select = select.and(EAV_BATCHES.REP_DATE.eq(DataUtils.convert(reportDate)));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Batch> maintenanceBatches = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            maintenanceBatches.add(fillBatch(new Batch() , row));
        }

        return maintenanceBatches;
    }

    @Override
    public void approveMaintenance(List<Long> approvedBatchIds) {
        Update update = context.update(EAV_BATCHES)
                .set(EAV_BATCHES.IS_MAINTENANCE_APPROVED, DataUtils.convert(true))
                .where(EAV_BATCHES.ID.in(approvedBatchIds));

        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }
}
