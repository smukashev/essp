package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BATCHES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BATCH_STATUSES;

@Repository
public class BatchDaoImpl extends JDBCSupport implements IBatchDao
{
    private final Logger logger = LoggerFactory.getLogger(BatchDaoImpl.class);

    @Autowired
    private DSLContext context;

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
                EAV_BATCHES.BATCH_SIZE
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
                batch.getSize()
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
                .set(EAV_BATCHES.BATCH_SIZE, batch.getSize())
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
            batch.setSize(((BigDecimal) row.get(EAV_BATCHES.BATCH_SIZE.getName())).longValue());
        }
        else
        {
            logger.error("Can't load batch, empty data set.");
        }
    }

}
