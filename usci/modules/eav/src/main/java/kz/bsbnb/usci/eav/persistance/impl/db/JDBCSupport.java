package kz.bsbnb.usci.eav.persistance.impl.db;

import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public void setDataSource(DataSource dataSource)
    {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected class GenericInsertPreparedStatementCreator implements PreparedStatementCreator {
        private final Logger logger = LoggerFactory.getLogger(GenericInsertPreparedStatementCreator.class);

        String query;
        Object[] values;
        String keyName = "id";

        public GenericInsertPreparedStatementCreator(String query, Object[] values) {
            this.query = query;
            this.values = values;
        }

        public GenericInsertPreparedStatementCreator(String query, Object[] values, String keyName) {
            this.query = query;
            this.values = values;
            this.keyName = keyName;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(
                    query, new String[] {keyName});

            int i = 1;
            for (Object obj : values)
            {
                ps.setObject(i++, obj);
            }

            logger.debug(ps.toString());

            return ps;
        }
    }


    public boolean testConnection()
	{
		try
        {
			return !jdbcTemplate.getDataSource().getConnection().isClosed();
		}
        catch (SQLException e)
        {
			return false;
		}
	}

    protected JDBCConfig getConfig()
    {
        return config;
    }

    @Autowired
    public void setConfig(JDBCConfig config) {
        this.config = config;
    }

    protected int updateWithStats(String sql, Object... args) {
        long t = 0;

        if(sqlStats != null)
            t = System.currentTimeMillis();

        int count = jdbcTemplate.update(sql, args);

        if(sqlStats != null)
            sqlStats.put(sql, System.currentTimeMillis() - t);

        return count;
    }

    protected int[] batchUpdateWithStats(String sql, List<Object[]> batchArgs)
    {
        long t = 0;
        if(sqlStats != null)
            t = System.currentTimeMillis();

        int[] counts = jdbcTemplate.batchUpdate(sql, batchArgs);

        if(sqlStats != null)
            //TODO: Может нужно делить время на количество операций
            sqlStats.put(sql, System.currentTimeMillis() - t);

        return counts;
    }

    protected List<Map<String, Object>> queryForListWithStats(String sql, Object... args)
    {
        long t = 0;
        if(sqlStats != null)
            t = System.currentTimeMillis();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);

        if(sqlStats != null)
            sqlStats.put(sql, System.currentTimeMillis() - t);

        return rows;
    }

    protected long insertWithId(String query, Object[] values)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(new GenericInsertPreparedStatementCreator(query, values), keyHolder);

        return keyHolder.getKey().longValue();
    }
}
