package kz.bsbnb.usci.eav.postgresql.storage;

import kz.bsbnb.usci.eav.persistance.impl.db.JDBCConfig;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.tool.ddl.DDLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author a.tkachenko
 */
@Repository
public class PostgreSQLStorageImpl extends JDBCSupport implements IStorage {
	private final Logger logger = LoggerFactory.getLogger(PostgreSQLStorageImpl.class);

    private final static String COUNT_TABLE = "SELECT count(*) FROM %s";

    //TODO: Remove?
    //private final static String BATCH_FILES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL, file_data BYTEA NOT NULL, file_size double precision NOT NULL, file_name character varying(%d), batch_id int references %s(id) ON DELETE CASCADE, CONSTRAINT %s_primary_key_index PRIMARY KEY (id))";

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
                true);
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
        //todo: implement
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
}
