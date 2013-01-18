package kz.bsbnb.usci.eav.persistance.impl.db;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

public class JDBCSupport {
	protected JdbcTemplate jdbcTemplate;
	
	@Value("${ds.table.prefix}") 
	protected String tablePrefix;
	
	protected String classesTableName;
	protected String arrayKeyFilterTableName;
	protected String arrayKeyFilterValuesTableName;
    protected String attributesTableName;
    protected String entitiesTableName;

	
	public static final int classNameLength = 64;
	public static final int attributeNameLength = 64;
	public static final int typeCodeLength = 16;
	public static final int arrayKeyTypeCodeLength = 16;
	public static final int complexKeyTypeCodeLength = 16;
	public static final int arrayKeyFilterValueLength = 128;
	
	@PostConstruct
	public void init()
	{
		classesTableName = tablePrefix + "classes";
	    attributesTableName = tablePrefix + "attributes";
	    entitiesTableName = tablePrefix + "entities";
	    arrayKeyFilterTableName = tablePrefix + "array_key_filter";
		arrayKeyFilterValuesTableName = tablePrefix + "array_key_filter_values";
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

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
}
