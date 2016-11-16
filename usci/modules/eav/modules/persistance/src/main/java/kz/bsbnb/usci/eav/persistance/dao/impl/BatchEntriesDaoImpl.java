package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.eav.persistance.dao.IBatchEntriesDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.Insert;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.BATCH_ENTRIES;

@Repository
public class BatchEntriesDaoImpl extends JDBCSupport implements IBatchEntriesDao {
    private final Logger logger = LoggerFactory.getLogger(BatchEntriesDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public BatchEntry load(long id) {
        BatchEntry batchEntry = new BatchEntry();

        Select select = context
                .select(BATCH_ENTRIES.ID,
                        BATCH_ENTRIES.USER_ID,
                        BATCH_ENTRIES.UPDATED_DATE,
                        BATCH_ENTRIES.REPORT_DATE,
                        BATCH_ENTRIES.VALUE)
                .from(BATCH_ENTRIES)
                .where(BATCH_ENTRIES.ID.equal(id));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E151));

        if (rows.size() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E152, id));

        Map<String, Object> row = rows.get(0);
        if (row != null) {
            batchEntry.setId(((BigDecimal) row.get(BATCH_ENTRIES.ID.getName())).longValue());
            batchEntry.setUserId(((BigDecimal) row.get(BATCH_ENTRIES.USER_ID.getName())).longValue());
            batchEntry.setUpdateDate(DataUtils.convert((Timestamp)
                    row.get(BATCH_ENTRIES.UPDATED_DATE.getName())));
            batchEntry.setRepDate(DataUtils.convert((Timestamp)
                    row.get(BATCH_ENTRIES.REPORT_DATE.getName())));
            batchEntry.setValue((String) row.get(BATCH_ENTRIES.VALUE.getName()));
        } else {
            logger.error("Can't load instance of BatchEntry, empty data set.");
        }

        return batchEntry;
    }

    @Override
    @Transactional
    public long save(BatchEntry batch) {
        long baseEntityId;

        Insert insert = context
                .insertInto(BATCH_ENTRIES)
                .set(BATCH_ENTRIES.USER_ID, batch.getUserId())
                .set(BATCH_ENTRIES.VALUE, batch.getValue())
                .set(BATCH_ENTRIES.REPORT_DATE, new Date(batch.getRepDate().getTime()))
                .set(BATCH_ENTRIES.UPDATED_DATE, DataUtils.convertToTimestamp(new java.util.Date()))
                .set(BATCH_ENTRIES.ENTITY_ID, batch.getEntityId())
                .set(BATCH_ENTRIES.IS_MAINTENANCE, DataUtils.convert(batch.getMaintenance()));


        baseEntityId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        return baseEntityId;
    }

    @Override
    public void remove(BatchEntry batch) {
        if (batch.getId() < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E153));

        DeleteConditionStep delete = context
                .delete(BATCH_ENTRIES)
                .where(BATCH_ENTRIES.ID.eq(batch.getId()));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public List<BatchEntry> getBatchEntriesByUserId(long userId) {
        ArrayList<BatchEntry> result = new ArrayList<BatchEntry>();

        Select select = context
                .select(BATCH_ENTRIES.ID,
                        BATCH_ENTRIES.UPDATED_DATE,
                        BATCH_ENTRIES.REPORT_DATE,
                        BATCH_ENTRIES.VALUE,
                        BATCH_ENTRIES.ENTITY_ID,
                        BATCH_ENTRIES.IS_MAINTENANCE)
                .from(BATCH_ENTRIES)
                .where(BATCH_ENTRIES.USER_ID.equal(userId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            BatchEntry batchEntry = new BatchEntry();

            batchEntry.setId(((BigDecimal) row.get(BATCH_ENTRIES.ID.getName())).longValue());
            batchEntry.setValue((String) row.get(BATCH_ENTRIES.VALUE.getName()));
            batchEntry.setUpdateDate(DataUtils.convert((Timestamp) row.get(BATCH_ENTRIES.UPDATED_DATE.getName())));
            batchEntry.setRepDate(DataUtils.convert((Timestamp) row.get(BATCH_ENTRIES.REPORT_DATE.getName())));
            if (row.get(BATCH_ENTRIES.ENTITY_ID.getName()) != null)
                batchEntry.setEntityId(((BigDecimal) row.get(BATCH_ENTRIES.ENTITY_ID.getName())).longValue());
            if (row.get(BATCH_ENTRIES.IS_MAINTENANCE.getName()) != null)
                batchEntry.setMaintenance(((BigDecimal) row.get(BATCH_ENTRIES.IS_MAINTENANCE.getName())).longValue() == 1);

            result.add(batchEntry);
        }

        return result;
    }

    @Override
    public void remove(List<Long> batchEntryIds) {
        DeleteConditionStep delete = context
                .delete(BATCH_ENTRIES)
                .where(BATCH_ENTRIES.ID.in(batchEntryIds));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }
}
