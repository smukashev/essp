package kz.bsbnb.usci.eav.persistance.storage.impl;

import kz.bsbnb.usci.eav.persistance.db.JDBCConfig;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.tool.ddl.DDLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author a.tkachenko
 */
@Repository
public class StorageImpl extends JDBCSupport implements IStorage {
	private final Logger logger = LoggerFactory.getLogger(StorageImpl.class);

    private final static String COUNT_TABLE = "SELECT count(*) FROM %s";

    @Override
	public void initialize() {
        URL dbConfigFileUrl = this.getClass().getClassLoader().getResource(getConfig().getSchema());

        if(dbConfigFileUrl == null)
        {
            logger.error("Can't find db config file: " + getConfig().getSchema());
            throw new IllegalStateException("Can't find db config file: " + getConfig().getSchema());
        }

        DDLHelper.changeDatabase(jdbcTemplate.getDataSource(),
                DDLHelper.readDatabaseFromXML(dbConfigFileUrl.getFile()),
                false);
	}

	@Override
	public void clear() {
        URL dbConfigFileUrl = this.getClass().getClassLoader().getResource(getConfig().getSchema());

        if(dbConfigFileUrl == null)
        {
            logger.error("Can't find db config file: " + getConfig().getSchema());
            throw new IllegalStateException("Can't find db config file: " + getConfig().getSchema());
        }

        DDLHelper.dropDatabase(jdbcTemplate.getDataSource(),
                DDLHelper.readDatabaseFromXML(dbConfigFileUrl.getFile()));
    }

	@Override
	public void empty() {
        String query = "SELECT table_name from all_tables where table_name like 'EAV%'";
        List<String> list = jdbcTemplate.queryForList(query, String.class);
        for(String s : list){
            jdbcTemplate.update("delete from " + s);
        }

        String seq = "SELECT object_name from all_objects where object_type  = 'SEQUENCE' " +
                "and object_name like 'SEQ_EAV%'" +
                "and owner = (select user from dual)";

        list = jdbcTemplate.queryForList(seq, String.class);

        for(String s: list){
            jdbcTemplate.update("drop sequence " + s);
            jdbcTemplate.update("create sequence " + s + " minvalue 1 maxvalue 9999999999999999999999999 " +
                    "start with 1 increment by 1 cache 20");
        }
    }

    @Override
    public boolean isClean() {
        //TODO: add base entity tables
        String query = String.format(COUNT_TABLE, getConfig().getArrayKeyFilterValuesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForLong(query) > 0)
        {
            logger.debug("Table " + getConfig().getArrayKeyFilterValuesTableName() + " is not clean.");
            return false;
        }

        //array key filter
        query = String.format(COUNT_TABLE, getConfig().getArrayKeyFilterTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForLong(query) > 0)
        {
            logger.debug("Table " + getConfig().getArrayKeyFilterTableName() + " is not clean.");
            return false;
        }

        //attributes
        query = String.format(COUNT_TABLE, getConfig().getAttributesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForLong(query) > 0)
        {
            logger.debug("Table " + getConfig().getAttributesTableName() + " is not clean.");
            return false;
        }

        //entities
        query = String.format(COUNT_TABLE, getConfig().getEntitiesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForLong(query) > 0)
        {
            logger.debug("Table " + getConfig().getEntitiesTableName() + " is not clean.");
            return false;
        }

        //classes
        query = String.format(COUNT_TABLE, getConfig().getClassesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForLong(query) > 0)
        {
            logger.debug("Table " + getConfig().getClassesTableName() + " is not clean.");
            return false;
        }

        //batches
        query = String.format(COUNT_TABLE, getConfig().getBatchesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForLong(query) > 0)
        {
            logger.debug("Table " + getConfig().getBatchesTableName() + " is not clean.");
            return false;
        }

        return true;
    }

    @Override
    public HashMap<String, Long> tableCounts() {
        //Obtain the Class instance
        Class jdbcConfig = JDBCConfig.class;

        HashMap<String, Long> res = new HashMap<String, Long>();

        //Get the methods
        Method[] methods = jdbcConfig.getDeclaredMethods();

        //Loop through the methods and print out their names
        for (Method method : methods) {
            try
            {
                if(method.getName().endsWith("TableName"))
                {
                    String tableName = (String)method.invoke(getConfig());
                    String query = String.format(COUNT_TABLE, tableName);
                    long count = jdbcTemplate.queryForLong(query);
                    logger.debug("Table " + tableName + ": " + count);

                    res.put(tableName, count);
                }
            } catch (Exception e) {
                logger.error("Can't call method " + method.getName() + " with error: " + e.getMessage());
            }
        }

        return res;
    }

    @Override
    public boolean simpleSql(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
