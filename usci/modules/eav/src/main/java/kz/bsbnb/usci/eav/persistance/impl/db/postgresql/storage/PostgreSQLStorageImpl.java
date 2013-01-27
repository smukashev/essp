package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.storage;



import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;


/**
 *
 * @author a.tkachenko
 */
@Repository
public class PostgreSQLStorageImpl extends JDBCSupport implements IStorage {
	final Logger logger = LoggerFactory.getLogger(PostgreSQLStorageImpl.class);

    final static String CLASSES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL, begin_date TIMESTAMP WITH TIME ZONE NOT NULL, is_disabled BOOLEAN NOT NULL, name character varying(%d) NOT NULL,CONSTRAINT %s_primary_key_index PRIMARY KEY (id ), UNIQUE (name) )";
    final static String ATTRIBUTES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL,containing_class_id int references %s(id) ON DELETE CASCADE, name character varying(%d) NOT NULL,is_key boolean NOT NULL,is_nullable boolean NOT NULL,CONSTRAINT %s_primary_key_index PRIMARY KEY (id ), UNIQUE (containing_class_id, name) )";
    final static String SIMPLE_ATTRIBUTES_TABLE = "CREATE TABLE IF NOT EXISTS %s (type_code character varying(%d)) INHERITS (%s)";
    final static String COMPLEX_ATTRIBUTES_TABLE = "CREATE TABLE IF NOT EXISTS %s (complex_key_type character varying(%d), class_id int references %s(id) ON DELETE CASCADE ) INHERITS (%s)";
    final static String ARRAY_TABLE = "CREATE TABLE IF NOT EXISTS %s (array_key_type character varying(%d)) INHERITS (%s)";
    final static String SIMPLE_ARRAY_TABLE = "CREATE TABLE IF NOT EXISTS %s () INHERITS (%s, %s)";
    final static String COMPLEX_ARRAY_TABLE = "CREATE TABLE IF NOT EXISTS %s () INHERITS (%s, %s)";
    final static String ENTITIES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL,class_id int references %s(id) ON DELETE CASCADE, CONSTRAINT %s_primary_key_index PRIMARY KEY (id ))";
    final static String ARRAY_KEY_FILTER_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL,attribute_id int references %s(id) ON DELETE CASCADE, attribute_name character varying(%d) NOT NULL,CONSTRAINT %s_primary_key_index PRIMARY KEY (id ))";
    final static String ARRAY_KEY_FILTER_VALUES_TABLE = "CREATE TABLE IF NOT EXISTS %s (id serial NOT NULL,filter_id int references %s(id) ON DELETE CASCADE, value character varying(%d) NOT NULL,CONSTRAINT %s_primary_key_index PRIMARY KEY (id ))";
    final static String DROP_TABLE = "DROP TABLE IF EXISTS %s";
    final static String DROP_TABLE_CASCADE = "DROP TABLE IF EXISTS %s CASCADE";

	@Override
	public void initialize() {
        //add unique constraint on name
	    String query = String.format(CLASSES_TABLE,
                getConfig().getClassesTableName(), getConfig().getClassNameLength(), getConfig().getClassesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //----------------------------------------------
	    //basic attribute
	    query = String.format(ATTRIBUTES_TABLE, getConfig().getAttributesTableName(), getConfig().getClassesTableName(), getConfig().getAttributeNameLength(), getConfig().getAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //simple attributes
	    query = String.format(SIMPLE_ATTRIBUTES_TABLE, getConfig().getSimpleAttributesTableName(), getConfig().getTypeCodeLength(), getConfig().getAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    //complex attributes
	    query = String.format(COMPLEX_ATTRIBUTES_TABLE, getConfig().getComplexAttributesTableName(), getConfig().getComplexKeyTypeCodeLength(), getConfig().getClassesTableName(), getConfig().getAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //array
	    query = String.format(ARRAY_TABLE, getConfig().getArrayTableName(), getConfig().getArrayKeyTypeCodeLength(), getConfig().getAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    //simple array
	    query = String.format(SIMPLE_ARRAY_TABLE, getConfig().getSimpleArrayTableName(), getConfig().getArrayTableName(), getConfig().getSimpleAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //complex array
	    query = String.format(COMPLEX_ARRAY_TABLE, getConfig().getComplexArrayTableName(), getConfig().getArrayTableName(), getConfig().getComplexAttributesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //------------------------------------------------
	    query = String.format(ENTITIES_TABLE, getConfig().getEntitiesTableName(), getConfig().getClassesTableName(), getConfig().getEntitiesTableName());
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    query = String.format(ARRAY_KEY_FILTER_TABLE, getConfig().getArrayKeyFilterTableName(), getConfig().getAttributesTableName(), getConfig().getAttributeNameLength(), getConfig().getArrayKeyFilterTableName());

		logger.debug(query);
		
		jdbcTemplate.execute(query);
		
		query = String.format(ARRAY_KEY_FILTER_VALUES_TABLE, getConfig().getArrayKeyFilterValuesTableName(), getConfig().getArrayKeyFilterTableName(), getConfig().getArrayKeyFilterValueLength(), getConfig().getArrayKeyFilterValuesTableName());

		logger.debug(query);
		
		jdbcTemplate.execute(query);
	}

	@Override
	public void clear() {
		String query = String.format(DROP_TABLE, getConfig().getArrayKeyFilterValuesTableName());
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = String.format(DROP_TABLE, getConfig().getArrayKeyFilterTableName());
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
	}

	@Override
	public void empty() {
		// TODO Auto-generated method stub
		
	}
}
