package kz.bsbnb.usci.eav.persistance.db;

import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JDBCSupport {
    protected JdbcTemplate jdbcTemplate;

    private JDBCConfig config;

    @Autowired
    protected SQLQueriesStats sqlStats;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected class GenericInsertPreparedStatementCreator implements PreparedStatementCreator {
        String query;
        Object[] values;
        final String keyName = "id";

        public GenericInsertPreparedStatementCreator(String query, Object[] values) {
            this.query = query;
            this.values = values.clone();
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(query, new String[]{keyName});

            int i = 1;
            for (Object obj : values) ps.setObject(i++, obj);

            return ps;
        }
    }


    public boolean testConnection() {
        try {
            return !jdbcTemplate.getDataSource().getConnection().isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    protected JDBCConfig getConfig() {
        return config;
    }

    @Autowired
    public void setConfig(JDBCConfig config) {
        this.config = config;
    }

    protected int updateWithStats(String sql, Object... args) {
        long t1 = System.currentTimeMillis();
        int count = jdbcTemplate.update(sql, args);
        sqlStats.put(sql, (System.currentTimeMillis() - t1));

        return count;
    }

    protected List<Map<String, Object>> queryForListWithStats(String sql, Object... args) {
        long t1 = System.currentTimeMillis();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
        sqlStats.put(sql, (System.currentTimeMillis() - t1));

        return rows;
    }

    protected long insertWithId(String query, Object[] values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        long t1 = System.currentTimeMillis();
        jdbcTemplate.update(new GenericInsertPreparedStatementCreator(query, values), keyHolder);
        sqlStats.put(query, (System.currentTimeMillis() - t1));

        return keyHolder.getKey().longValue();
    }

}
