package kz.bsbnb.usci.eav.persistance.impl.db;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

public class JDBCSupport {
	protected JdbcTemplate jdbcTemplate;

    private JDBCConfig config;
	
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
}
