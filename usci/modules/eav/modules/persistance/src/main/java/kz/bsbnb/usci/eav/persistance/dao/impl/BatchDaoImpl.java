package kz.bsbnb.usci.eav.persistance.dao.impl;

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
import java.sql.*;
import java.sql.Date;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BATCHES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BATCH_STATUSES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_ENTITY_STATUS_PARAMS;

@Repository
public class BatchDaoImpl extends JDBCSupport implements IBatchDao
{
    private final Logger logger = LoggerFactory.getLogger(BatchDaoImpl.class);

    @Autowired
    private DSLContext context;

    @Autowired
    private IEavGlobalDao eavGlobalDao;

    @Override
    public Batch load(long id) {
        if (id < 0) {
            return null;
        }

        Batch batch = new Batch(new Date(System.currentTimeMillis()));
        batch.setId(id);

        loadBatch(batch);

        return batch;
    }

    @Override
    @Transactional
    public long save(Batch batch) {
        long baseEntityId = 0;

        if (batch.getId() < 1) {
            baseEntityId = insertBatch(batch);
            batch.setId(baseEntityId);
        } else {
            updateBatch(batch);
            baseEntityId = batch.getId();
        }

        return baseEntityId;
    }

    @Override
    public List<Batch> getPendingBatchList() {
        // TODO maks check

        EavGlobal statusCompleted = eavGlobalDao.get(BatchStatuses.COMPLETED.type(), BatchStatuses.COMPLETED.code());

        Select select = context.select(
                EAV_BATCHES.ID,
                EAV_BATCHES.USER_ID,
                EAV_BATCHES.CREDITOR_ID,
                EAV_BATCHES.FILE_NAME,
                EAV_BATCHES.HASH,
                EAV_BATCHES.SIGN,
                EAV_BATCHES.REP_DATE,
                EAV_BATCHES.RECEIPT_DATE,
                EAV_BATCHES.BEGIN_DATE,
                EAV_BATCHES.END_DATE,
                EAV_BATCHES.BATCH_TYPE,
                EAV_BATCHES.TOTAL_COUNT,
                EAV_BATCHES.ACTUAL_COUNT,
                EAV_BATCHES.REPORT_ID
        ).from(EAV_BATCHES).join(
                context.select(
                        EAV_BATCH_STATUSES.BATCH_ID,
                        EAV_BATCH_STATUSES.STATUS_ID,
                        DSL.rowNumber().over()
                                .partitionBy(EAV_BATCH_STATUSES.BATCH_ID)
                                .orderBy(EAV_BATCH_STATUSES.RECEIPT_DATE.desc()).as("num")
                ).from(EAV_BATCH_STATUSES).asTable("bs")
        ).on(EAV_BATCHES.ID.eq(EAV_BATCH_STATUSES.BATCH_ID))
        .where(DSL.field("\"bs\".\"num\"").eq(1)).and(DSL.field("\"bs\".STATUS_ID").eq(statusCompleted.getId()));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Batch> pendingBatchList = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            pendingBatchList.add(fillBatch(new Batch(), row));
        }

        return pendingBatchList;
    }

    @Override
    public List<Batch> getBatchListToSign(long userId) {
        // TODO maks check

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

    @Override
    public Map<String, String> getEntityStatusParams(long entityStatusId) {
        // TODO maks check

        Select select = context.selectFrom(EAV_ENTITY_STATUS_PARAMS)
                .where(EAV_ENTITY_STATUS_PARAMS.ENTITY_STATUS_ID.eq(entityStatusId));

        Map<String, String> params = new HashMap<>();

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            params.put(
                    (String) row.get(EAV_ENTITY_STATUS_PARAMS.KEY.getName()),
                    (String) row.get(EAV_ENTITY_STATUS_PARAMS.VALUE.getName())
            );
        }

        return params;
    }

    @Override
    public void addEntityStatusParam(long entityStatusId, String key, String value) {
        // TODO maks check

        Insert insert = context.insertInto(EAV_ENTITY_STATUS_PARAMS,
                EAV_ENTITY_STATUS_PARAMS.ENTITY_STATUS_ID,
                EAV_ENTITY_STATUS_PARAMS.KEY,
                EAV_ENTITY_STATUS_PARAMS.VALUE
        ).values(
                entityStatusId,
                key,
                value
        );

        long id = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        if(id < 1)
        {
            logger.error("Can't insert entity status param");
        }
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
                EAV_BATCHES.BEGIN_DATE,
                EAV_BATCHES.END_DATE,
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
                DataUtils.convertToTimestamp(batch.getBeginDate()),
                DataUtils.convertToTimestamp(batch.getEndDate()),
                batch.getBatchType(),
                batch.getTotalCount(),
                batch.getActualCount(),
                batch.getReportId()
        );

        long batchId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        if(batchId < 1)
        {
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
                .set(EAV_BATCHES.BEGIN_DATE, DataUtils.convertToTimestamp(batch.getBeginDate()))
                .set(EAV_BATCHES.END_DATE, DataUtils.convertToTimestamp(batch.getEndDate()))
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

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More than one batch found. Can't load.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalArgumentException("Batch not found. Can't load.");
        }

        Map<String, Object> row = rows.get(0);

        if(row != null)
        {
            fillBatch(batch, row);
        }
        else
        {
            logger.error("Can't load batch, empty data set.");
        }
    }

    private Batch fillBatch(Batch batch, Map<String, Object> row) {
        batch.setId(((BigDecimal)row.get(EAV_BATCHES.ID.getName())).longValue());
        batch.setUserId(((BigDecimal) row.get(EAV_BATCHES.USER_ID.getName())).longValue());
        batch.setCreditorId(((BigDecimal) row.get(EAV_BATCHES.CREDITOR_ID.getName())).longValue());
        batch.setFileName((String) row.get(EAV_BATCHES.FILE_NAME.getName()));
        batch.setHash((String) row.get(EAV_BATCHES.HASH.getName()));
        batch.setSign((String) row.get(EAV_BATCHES.SIGN.getName()));
        batch.setRepDate(DataUtils.convert((Timestamp) row.get(EAV_BATCHES.REP_DATE.getName())));
        batch.setReceiptDate(DataUtils.convert((Timestamp) row.get(EAV_BATCHES.RECEIPT_DATE.getName())));
        batch.setBeginDate(DataUtils.convert((Timestamp) row.get(EAV_BATCHES.BEGIN_DATE.getName())));
        batch.setEndDate(DataUtils.convert((Timestamp) row.get(EAV_BATCHES.END_DATE.getName())));
        batch.setBatchType((String) row.get(EAV_BATCHES.BATCH_TYPE.getName()));
        batch.setTotalCount(((BigDecimal) row.get(EAV_BATCHES.TOTAL_COUNT.getName())).longValue());
        batch.setActualCount(((BigDecimal) row.get(EAV_BATCHES.ACTUAL_COUNT.getName())).longValue());
        batch.setReportId(((BigDecimal) row.get(EAV_BATCHES.REPORT_ID.getName())).longValue());
        return batch;
    }

}
