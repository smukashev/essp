package kz.bsbnb.usci.eav.storage.dao.impl;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractDBDao {

	protected JdbcTemplate jdbcTemplate;

	@Autowired
    public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


	@Value("${ds.table.prefix}") 
	protected String tablePrefix;
	
	protected String classesTableName;
    protected String attributesTableName;
    protected String entitiesTableName;

	
	public static final int classNameLength = 64;
	public static final int typeNameLength = 64;
	public static final int typeCodeLength = 16;
	
	@PostConstruct
	public void init()
	{
		classesTableName = tablePrefix + "classes";
	    attributesTableName = tablePrefix + "attributes";
	    entitiesTableName = tablePrefix + "entities";
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}
}