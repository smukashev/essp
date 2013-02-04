package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Repository
public class PostgreSQLBatchDaoImpl extends JDBCSupport
        implements IBatchDao {
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBatchDaoImpl.class);

    private String INSERT_BATCH_SQL;
    private String DELETE_BATCH_BY_ID_SQL;
    private String SELECT_BATCH_BY_ID_SQL;

    @PostConstruct
    public void init()
    {
        INSERT_BATCH_SQL = String.format("INSERT INTO %s (receipt_date) VALUES ( ? )", getConfig().getBatchesTableName());
        DELETE_BATCH_BY_ID_SQL = String.format("DELETE FROM %s WHERE id = ?", getConfig().getBatchesTableName());
        SELECT_BATCH_BY_ID_SQL = String.format("SELECT * FROM %s WHERE id = ?", getConfig().getBatchesTableName());
    }

    private class InsertBatchPreparedStatementCreator
            implements PreparedStatementCreator {
        private Batch batch;

        public InsertBatchPreparedStatementCreator(Batch batch) {
            this.batch = batch;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(
                    INSERT_BATCH_SQL, new String[] {"id"});
            ps.setTimestamp(1, batch.getReceiptDate());

            logger.debug(ps.toString());

            return ps;
        }
    }

    @Override
    public Batch load(long id) {
        if (id < 0) {
            return null;
        }

        Batch batch = new Batch();
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
        }

        return baseEntityId;
    }

    @Override
    public void remove(Batch batch) {
        if(batch.getId() < 1)
        {
            throw new IllegalArgumentException("Can't remove Batch without id");
        }

        logger.debug(DELETE_BATCH_BY_ID_SQL);
        updateWithStats(DELETE_BATCH_BY_ID_SQL, batch.getId());
    }

    private long insertBatch(Batch batch) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new InsertBatchPreparedStatementCreator(batch), keyHolder);

        long baseEntityId = keyHolder.getKey().longValue();

        if(baseEntityId < 1)
        {
            logger.error("Can't insert batch");
            return 0;
        }

        return baseEntityId;
    }

    private void loadBatch(Batch batch) {
        logger.debug(SELECT_BATCH_BY_ID_SQL);
        List<Map<String, Object>> rows = queryForList(SELECT_BATCH_BY_ID_SQL, batch.getId());

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one batch found. Can't load.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalArgumentException("Batch not found. Can't load.");
        }

        Map<String, Object> row = rows.get(0);

        if(row != null)
        {
            batch.setId((Integer)row.get("id"));
            logger.debug("CLASS: " + row.get("receipt_date").getClass());
            batch.setReceiptDate((Timestamp)row.get("receipt_date"));
        }
        else
        {
            logger.error("Can't load batch, empty data set.");
        }
    }

}
