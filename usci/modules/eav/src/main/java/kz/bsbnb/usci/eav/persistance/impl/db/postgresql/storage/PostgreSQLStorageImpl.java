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
	
	@Override
	public void initialize() {
        //add unique constraint on name
	    String query = "CREATE TABLE IF NOT EXISTS " + getConfig().getClassesTableName() +
	                    " (" + 
	                      "id serial NOT NULL, " +
                          "begin_date TIMESTAMP WITH TIME ZONE NOT NULL, " +
                          "is_disabled BOOLEAN NOT NULL, " +
	                      "name character varying(" + getConfig().getClassNameLength()  + ") NOT NULL," +
	                      "CONSTRAINT " + getConfig().getClassesTableName() + "_primary_key_index PRIMARY KEY (id ), " +
                          "UNIQUE (name) " +
	                    ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //----------------------------------------------
	    //basic attribute
        //TODO: add unique constraint on containing_class_id and name
	    query = "CREATE TABLE IF NOT EXISTS " + getConfig().getAttributesTableName() +
                " (" + 
                  "id serial NOT NULL," +
                  "containing_class_id int references " + getConfig().getClassesTableName() + "(id) ON DELETE CASCADE, " +
                  "name character varying(" + getConfig().getAttributeNameLength() + ") NOT NULL," +
                  "is_key boolean NOT NULL," +
                  "is_nullable boolean NOT NULL," +
                  "CONSTRAINT " + getConfig().getAttributesTableName() + "_primary_key_index PRIMARY KEY (id ) " +
                ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //simple attributes
	    query = "CREATE TABLE IF NOT EXISTS " + getConfig().getSimpleAttributesTableName() +
                " (" + 
                  "type_code character varying(" + getConfig().getTypeCodeLength() + ")" +
                ") INHERITS (" + getConfig().getAttributesTableName() + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    //complex attributes
	    query = "CREATE TABLE IF NOT EXISTS " + getConfig().getComplexAttributesTableName() +
                " (" + 
                  "complex_key_type character varying(" + getConfig().getComplexKeyTypeCodeLength() + "), " +
                  "class_id int references " + getConfig().getClassesTableName() + "(id) ON DELETE CASCADE " +
                ") INHERITS (" + getConfig().getAttributesTableName() + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //array
	    query = "CREATE TABLE IF NOT EXISTS " + getConfig().getArrayTableName() +
                " (" + 
                  "array_key_type character varying(" + getConfig().getArrayKeyTypeCodeLength() + ")" +
                ") INHERITS (" + getConfig().getAttributesTableName() + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    //simple array
	    query = "CREATE TABLE IF NOT EXISTS " + getConfig().getSimpleArrayTableName() +
                " () INHERITS (" + getConfig().getArrayTableName() + ", " + getConfig().getSimpleAttributesTableName() + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //complex array
	    query = "CREATE TABLE IF NOT EXISTS " + getConfig().getComplexArrayTableName() +
                " () INHERITS (" + getConfig().getArrayTableName() + ", " + getConfig().getComplexAttributesTableName() + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //------------------------------------------------
	    query = "CREATE TABLE IF NOT EXISTS " + getConfig().getEntitiesTableName() +
                " (" + 
                  "id serial NOT NULL," +
                  "class_id int references " + getConfig().getClassesTableName() + "(id) ON DELETE CASCADE, " +
                  "CONSTRAINT " + getConfig().getEntitiesTableName() + "_primary_key_index PRIMARY KEY (id )" +
                ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    query = "CREATE TABLE IF NOT EXISTS " + getConfig().getArrayKeyFilterTableName() +
                " (" + 
                  "id serial NOT NULL," + 
                  "attribute_id int references " + getConfig().getAttributesTableName() + "(id) ON DELETE CASCADE, " +
                  "attribute_name character varying(" + getConfig().getAttributeNameLength() + ") NOT NULL," +
                  "CONSTRAINT " + getConfig().getArrayKeyFilterTableName() + "_primary_key_index PRIMARY KEY (id )" +
                ")";

		logger.debug(query);
		
		jdbcTemplate.execute(query);
		
		query = "CREATE TABLE IF NOT EXISTS " + getConfig().getArrayKeyFilterValuesTableName() +
                " (" + 
                  "id serial NOT NULL," + 
                  "filter_id int references " + getConfig().getArrayKeyFilterTableName() + "(id) ON DELETE CASCADE, " +
                  "value character varying(" + getConfig().getArrayKeyFilterValueLength() + ") NOT NULL," +
                  "CONSTRAINT " + getConfig().getArrayKeyFilterValuesTableName() + "_primary_key_index PRIMARY KEY (id )" +
                ")";

		logger.debug(query);
		
		jdbcTemplate.execute(query);
	}

	@Override
	public void clear() {
		String query = "DROP TABLE IF EXISTS " + getConfig().getArrayKeyFilterValuesTableName();
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = "DROP TABLE IF EXISTS " + getConfig().getArrayKeyFilterTableName();
	    logger.debug(query);
	    jdbcTemplate.execute(query);
		query = "DROP TABLE IF EXISTS " + getConfig().getAttributesTableName() + " CASCADE";
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = "DROP TABLE IF EXISTS " + getConfig().getEntitiesTableName();
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = "DROP TABLE IF EXISTS " + getConfig().getClassesTableName();
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	}

	@Override
	public void empty() {
		// TODO Auto-generated method stub
		
	}
}
