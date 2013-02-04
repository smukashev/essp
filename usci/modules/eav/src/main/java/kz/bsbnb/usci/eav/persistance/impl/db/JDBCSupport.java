package kz.bsbnb.usci.eav.persistance.impl.db;

import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

public class JDBCSupport {
	protected JdbcTemplate jdbcTemplate;

    private JDBCConfig config;

    @Autowired
    protected SQLQueriesStats sqlStats;

	@Autowired
    public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
	public boolean testConnection()
	{
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
        long t = 0;
        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        int count = jdbcTemplate.update(sql, args);
        if(sqlStats != null)
        {
            sqlStats.put(sql, System.currentTimeMillis() - t);
        }
        return count;
    }

}
