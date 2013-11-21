package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchEntriesDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.Insert;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static kz.bsbnb.eav.persistance.generated.Tables.AUDIT_EVENT;

@Repository
public class PostgreSQLBatchEntriesDaoImpl extends JDBCSupport implements IBatchEntriesDao
{
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBatchEntriesDaoImpl.class);

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
                        BATCH_ENTRIES.VALUE)
                .from(BATCH_ENTRIES)
                .where(BATCH_ENTRIES.ID.equal(id));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one BatchEntry found.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalStateException(String.format("BatchEntry with identifier {0} was not found.", id));
        }

        Map<String, Object> row = rows.get(0);
        if(row != null)
        {
            batchEntry.setId(((BigDecimal)row.get(BATCH_ENTRIES.ID.getName())).longValue());
            batchEntry.setUserId(((BigDecimal) row.get(BATCH_ENTRIES.USER_ID.getName())).longValue());
            batchEntry.setUpdateDate(DataUtils.convert((Timestamp)
                    row.get(BATCH_ENTRIES.UPDATED_DATE.getName())));
            batchEntry.setValue((String) row.get(BATCH_ENTRIES.VALUE.getName()));
        }
        else
        {
            logger.error("Can't load instance of BatchEntry, empty data set.");
        }

        return batchEntry;
    }

    @Override
    @Transactional
    public long save(BatchEntry batch) {
        long baseEntityId = 0;

        Insert insert = context
                .insertInto(BATCH_ENTRIES)
                .set(BATCH_ENTRIES.USER_ID, batch.getUserId())
                .set(BATCH_ENTRIES.VALUE, batch.getValue())
                .set(BATCH_ENTRIES.UPDATED_DATE,
                        new Date(Calendar.getInstance().getTimeInMillis()));


        baseEntityId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        return baseEntityId;
    }

    @Override
    public void remove(BatchEntry batch) {
        if(batch.getId() < 1)
        {
            throw new IllegalArgumentException("Can't remove BatchEntry without id");
        }

        DeleteConditionStep delete = context
                .delete(BATCH_ENTRIES)
                .where(BATCH_ENTRIES.ID.eq(batch.getId()));

        logger.debug(delete.toString());
        updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public List<BatchEntry> getBatchEntriesByUserId(long userId)
    {
        ArrayList<BatchEntry> result = new ArrayList <BatchEntry>();

        Select select = context
                .select(BATCH_ENTRIES.ID,
                        BATCH_ENTRIES.UPDATED_DATE)
                .from(BATCH_ENTRIES)
                .where(BATCH_ENTRIES.USER_ID.equal(userId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            BatchEntry batchEntry = new BatchEntry();

            batchEntry.setId(((BigDecimal)row.get(BATCH_ENTRIES.ID.getName())).longValue());
            batchEntry.setUpdateDate(DataUtils.convert((Timestamp)
                    row.get(BATCH_ENTRIES.UPDATED_DATE.getName())));

            result.add(batchEntry);
        }

        return result;
    }
}
