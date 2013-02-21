package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.storage;



import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


/**
 *
 * @author a.tkachenko
 */
@Repository
public class PostgreSQLStorageImpl extends JDBCSupport implements IStorage {
	private final Logger logger = LoggerFactory.getLogger(PostgreSQLStorageImpl.class);

    private final static String ENTITIES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL,class_id int references %s(id) ON DELETE CASCADE, CONSTRAINT %s_primary_key_index PRIMARY KEY (id ))";
    private final static String DROP_TABLE = "DROP TABLE IF EXISTS %s";
    private final static String DROP_TABLE_CASCADE = "DROP TABLE IF EXISTS %s CASCADE";
    private final static String COUNT_TABLE = "SELECT count(*) FROM %s";

    private final static String BATCHES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL, receipt_date TIMESTAMP WITH TIME ZONE NOT NULL, begin_date TIMESTAMP WITH TIME ZONE, end_date TIMESTAMP WITH TIME ZONE, CONSTRAINT %s_primary_key_index PRIMARY KEY (id))";
    private final static String BATCH_FILES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL, file_data BYTEA NOT NULL, file_size double precision NOT NULL, file_name character varying(%d), batch_id int references %s(id) ON DELETE CASCADE, CONSTRAINT %s_primary_key_index PRIMARY KEY (id))";

    private final static String VALUES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL, entity_id int references %s(id) ON DELETE CASCADE, batch_id bigint references %s(id) ON DELETE CASCADE, attribute_id int references %s(id) ON DELETE CASCADE, index bigint, CONSTRAINT %s_primary_key_index PRIMARY KEY (id))";

    private final static String DATE_VALUES_TABLE = "CREATE TABLE IF NOT EXISTS %s (value DATE, CONSTRAINT %s_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES %s (id) ON DELETE CASCADE) INHERITS (%s)";
    private final static String DOUBLE_VALUES_TABLE = "CREATE TABLE IF NOT EXISTS %s (value double precision, CONSTRAINT %s_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES %s (id) ON DELETE CASCADE) INHERITS (%s)";
    private final static String INTEGER_VALUES_TABLE = "CREATE TABLE IF NOT EXISTS %s (value integer, CONSTRAINT %s_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES %s (id) ON DELETE CASCADE) INHERITS (%s)";
    private final static String BOOLEAN_VALUES_TABLE = "CREATE TABLE IF NOT EXISTS %s (value boolean, CONSTRAINT %s_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES %s (id) ON DELETE CASCADE) INHERITS (%s)";
    private final static String STRING_VALUES_TABLE = "CREATE TABLE IF NOT EXISTS %s (value character varying(%d), CONSTRAINT %s_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES %s (id) ON DELETE CASCADE) INHERITS (%s)";

    private final static String COMPLEX_VALUES_TABLE = "CREATE TABLE IF NOT EXISTS %s (entity_value_id bigint references %s(id), CONSTRAINT %s_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_batch_id_fkey FOREIGN KEY (batch_id) REFERENCES %s (id) ON DELETE CASCADE, CONSTRAINT %s_attribute_id_fkey FOREIGN KEY (attribute_id) REFERENCES %s (id) ON DELETE CASCADE) INHERITS (%s)";

//    private void createIndexes()
//    {
//        List<Map<String, Object>> rows = jdbcTemplate.queryForList(INDEXES_QUERY);
//
//        for (Map<String, Object> row : rows)
//        {
//            String query = (String)row.get("query");
//            logger.debug(query);
//            jdbcTemplate.update(query);
//        }
//    }

	@Override
	public void initialize() {
        //addToArray unique constraint on name
	    String query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "id serial NOT NULL, " +
                        "complex_key_type character varying(%d), " +
                        "begin_date TIMESTAMP WITH TIME ZONE NOT NULL, " +
                        "is_disabled BOOLEAN NOT NULL, " +
                        "name character varying(%d) NOT NULL, " +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id ), " +
                        "UNIQUE (name, begin_date) " +
                        ")",
                getConfig().getClassesTableName(),
                getConfig().getComplexKeyTypeCodeLength(),
                getConfig().getClassNameLength(),
                getConfig().getClassesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //----------------------------------------------
	    //basic attribute
	    query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "id serial NOT NULL, " +
                        "containing_id int, " +
                        "name character varying(%d) NOT NULL, " +
                        "is_key boolean NOT NULL, " +
                        "is_nullable boolean NOT NULL, " +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id ), " +
                        "UNIQUE (containing_id, name) " +
                        ")",
                getConfig().getAttributesTableName(),
                getConfig().getAttributeNameLength(),
                getConfig().getAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //simple attributes
	    query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "type_code character varying(%d), " +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id) " +
                        ") " +
                        "INHERITS (%s)",
                getConfig().getSimpleAttributesTableName(),
                getConfig().getTypeCodeLength(),
                getConfig().getSimpleAttributesTableName(),
                getConfig().getAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    //complex attributes
	    query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "class_id int, " +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id) " +
                        ") " +
                        "INHERITS (%s)",
                getConfig().getComplexAttributesTableName(),
                getConfig().getComplexAttributesTableName(),
                getConfig().getAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //array
	    query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "array_key_type character varying(%d), " +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id) " +
                        ") " +
                        "INHERITS (%s)",
                getConfig().getArrayTableName(),
                getConfig().getArrayKeyTypeCodeLength(),
                getConfig().getArrayTableName(),
                getConfig().getAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    //simple array
	    query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id) " +
                        ") " +
                        "INHERITS (%s, %s)",
                getConfig().getSimpleArrayTableName(),
                getConfig().getSimpleArrayTableName(),
                getConfig().getArrayTableName(), getConfig().getSimpleAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);

	    //complex array
	    query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id)" +
                        ") " +
                        "INHERITS (%s, %s)",
                getConfig().getComplexArrayTableName(),
                getConfig().getComplexArrayTableName(),
                getConfig().getArrayTableName(), getConfig().getComplexAttributesTableName());

	    logger.debug(query);

	    jdbcTemplate.execute(query);

        //array of array
        query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id) " +
                        ") " +
                        "INHERITS (%s)",
                getConfig().getArrayArrayTableName(),
                getConfig().getArrayArrayTableName(),
                getConfig().getArrayTableName());

        logger.debug(query);

        jdbcTemplate.execute(query);

        //batches
        query = String.format(BATCHES_TABLE, getConfig().getBatchesTableName(),
                getConfig().getBatchesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //entities
        query = String.format(ENTITIES_TABLE, getConfig().getEntitiesTableName(),
                getConfig().getClassesTableName(), getConfig().getEntitiesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //values
        query = String.format(VALUES_TABLE, getConfig().getValuesTableName(),
                getConfig().getEntitiesTableName(), getConfig().getBatchesTableName(),
                getConfig().getAttributesTableName(), getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //date values
        query = String.format(DATE_VALUES_TABLE, getConfig().getDateValuesTableName(),
                getConfig().getDateValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getDateValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getDateValuesTableName(), getConfig().getSimpleAttributesTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //double values
        query = String.format(DOUBLE_VALUES_TABLE, getConfig().getDoubleValuesTableName(),
                getConfig().getDoubleValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getDoubleValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getDoubleValuesTableName(), getConfig().getSimpleAttributesTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //integer values
        query = String.format(INTEGER_VALUES_TABLE, getConfig().getIntegerValuesTableName(),
                getConfig().getIntegerValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getIntegerValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getIntegerValuesTableName(), getConfig().getSimpleAttributesTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //boolean values
        query = String.format(BOOLEAN_VALUES_TABLE, getConfig().getBooleanValuesTableName(),
                getConfig().getBooleanValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getBooleanValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getBooleanValuesTableName(), getConfig().getSimpleAttributesTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //string values
        query = String.format(STRING_VALUES_TABLE, getConfig().getStringValuesTableName(),
                getConfig().getStringValueLength(),
                getConfig().getStringValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getStringValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getStringValuesTableName(), getConfig().getSimpleAttributesTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //complex values
        query = String.format(COMPLEX_VALUES_TABLE, getConfig().getComplexValuesTableName(),
                getConfig().getEntitiesTableName(),
                getConfig().getComplexValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getComplexValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getComplexValuesTableName(), getConfig().getComplexAttributesTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //date array values
        query = String.format(DATE_VALUES_TABLE, getConfig().getDateArrayValuesTableName(),
                getConfig().getDateArrayValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getDateArrayValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getDateArrayValuesTableName(), getConfig().getSimpleArrayTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //double array values
        query = String.format(DOUBLE_VALUES_TABLE, getConfig().getDoubleArrayValuesTableName(),
                getConfig().getDoubleArrayValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getDoubleArrayValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getDoubleArrayValuesTableName(), getConfig().getSimpleArrayTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //integer array values
        query = String.format(INTEGER_VALUES_TABLE, getConfig().getIntegerArrayValuesTableName(),
                getConfig().getIntegerArrayValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getIntegerArrayValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getIntegerArrayValuesTableName(), getConfig().getSimpleArrayTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //boolean array values
        query = String.format(BOOLEAN_VALUES_TABLE, getConfig().getBooleanArrayValuesTableName(),
                getConfig().getBooleanArrayValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getBooleanArrayValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getBooleanArrayValuesTableName(), getConfig().getSimpleArrayTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //string values
        query = String.format(STRING_VALUES_TABLE, getConfig().getStringArrayValuesTableName(),
                getConfig().getStringValueLength(),
                getConfig().getStringArrayValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getStringArrayValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getStringArrayValuesTableName(), getConfig().getSimpleArrayTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);

        //complex array values
        query = String.format(COMPLEX_VALUES_TABLE, getConfig().getComplexArrayValuesTableName(),
                getConfig().getEntitiesTableName(),
                getConfig().getComplexArrayValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getComplexArrayValuesTableName(), getConfig().getBatchesTableName(),
                getConfig().getComplexArrayValuesTableName(), getConfig().getComplexArrayTableName(),
                getConfig().getValuesTableName());
        logger.debug(query);

        jdbcTemplate.execute(query);
	    //------------------------------------------------
	    query = String.format(ENTITIES_TABLE, getConfig().getEntitiesTableName(), getConfig().getClassesTableName(), getConfig().getEntitiesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "id serial NOT NULL, " +
                        "attribute_id int, " +
                        "attribute_name character varying(%d) NOT NULL, " +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id ))",
                getConfig().getArrayKeyFilterTableName(),
                getConfig().getAttributeNameLength(),
                getConfig().getArrayKeyFilterTableName());

		logger.debug(query);
		
		jdbcTemplate.execute(query);
		
		query = String.format(
                "CREATE TABLE IF NOT EXISTS %s " +
                        "(" +
                        "id serial NOT NULL, " +
                        "filter_id int, " +
                        "value character varying(%d) NOT NULL, " +
                        "CONSTRAINT %s_primary_key_index PRIMARY KEY (id ))",
                getConfig().getArrayKeyFilterValuesTableName(),
                getConfig().getArrayKeyFilterValueLength(),
                getConfig().getArrayKeyFilterValuesTableName());

		logger.debug(query);
		
		jdbcTemplate.execute(query);

        //todo: add again
        //createIndexes();
	}

	@Override
	public void clear() {
		String query = String.format(DROP_TABLE, getConfig().getArrayKeyFilterValuesTableName());
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = String.format(DROP_TABLE, getConfig().getArrayKeyFilterTableName());
	    logger.debug(query);
	    jdbcTemplate.execute(query);
        query = String.format(DROP_TABLE_CASCADE, getConfig().getValuesTableName());
        logger.debug(query);
        jdbcTemplate.execute(query);
        query = String.format(DROP_TABLE, getConfig().getEntitiesTableName());
        logger.debug(query);
        jdbcTemplate.execute(query);
		query = String.format(DROP_TABLE_CASCADE, getConfig().getAttributesTableName());
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = String.format(DROP_TABLE, getConfig().getEntitiesTableName());
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = String.format(DROP_TABLE, getConfig().getClassesTableName());
	    logger.debug(query);
        jdbcTemplate.execute(query);
        query = String.format(DROP_TABLE, getConfig().getBatchesTableName());
        logger.debug(query);
        jdbcTemplate.execute(query);
	}

	@Override
	public void empty() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public boolean isClean() {
        String query = String.format(COUNT_TABLE, getConfig().getArrayKeyFilterValuesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getArrayKeyFilterValuesTableName() + " is not clean.");
            return false;
        }

        //array key filter
        query = String.format(COUNT_TABLE, getConfig().getArrayKeyFilterTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getArrayKeyFilterTableName() + " is not clean.");
            return false;
        }

        //attributes
        query = String.format(COUNT_TABLE, getConfig().getAttributesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getAttributesTableName() + " is not clean.");
            return false;
        }

        //entities
        query = String.format(COUNT_TABLE, getConfig().getEntitiesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getEntitiesTableName() + " is not clean.");
            return false;
        }

        //classes
        query = String.format(COUNT_TABLE, getConfig().getClassesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getClassesTableName() + " is not clean.");
            return false;
        }

        //batches
        query = String.format(COUNT_TABLE, getConfig().getBatchesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getBatchesTableName() + " is not clean.");
            return false;
        }

        //values
        query = String.format(COUNT_TABLE, getConfig().getValuesTableName());
        logger.debug(query);
        if(jdbcTemplate.queryForInt(query) > 0)
        {
            logger.debug("Table " + getConfig().getValuesTableName() + " is not clean.");
            return false;
        }

        return true;
    }
}
