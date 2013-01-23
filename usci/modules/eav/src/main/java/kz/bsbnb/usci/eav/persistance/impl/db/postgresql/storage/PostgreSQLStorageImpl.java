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
	    String query = "CREATE TABLE IF NOT EXISTS " + classesTableName + 
	                    " (" + 
	                      "id serial NOT NULL," + 
	                      "name character varying(" + classNameLength  + ") NOT NULL," + 
	                      "CONSTRAINT " + classesTableName + "_primary_key_index PRIMARY KEY (id )" + 
	                    ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //----------------------------------------------
	    //basic attribute
        //TODO: add unique constraint on containing_class_id and name
	    query = "CREATE TABLE IF NOT EXISTS " + attributesTableName + 
                " (" + 
                  "id serial NOT NULL," +
                  "containing_class_id int references " + classesTableName + "(id) ON DELETE CASCADE, " +
                  "name character varying(" + attributeNameLength  + ") NOT NULL," + 
                  "is_key boolean NOT NULL," +
                  "is_nullable boolean NOT NULL," +
                  "CONSTRAINT " + attributesTableName + "_primary_key_index PRIMARY KEY (id )" + 
                ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //simple attributes
	    query = "CREATE TABLE IF NOT EXISTS " + simpleAttributesTableName + 
                " (" + 
                  "type_code character varying(" + typeCodeLength  + ")" +
                ") INHERITS (" + attributesTableName + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    //complex attributes
	    query = "CREATE TABLE IF NOT EXISTS " + complexAttributesTableName + 
                " (" + 
                  "complex_key_type character varying(" + complexKeyTypeCodeLength  + "), " +
                  "class_id int references " + classesTableName + "(id) ON DELETE CASCADE " +
                ") INHERITS (" + attributesTableName + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //array
	    query = "CREATE TABLE IF NOT EXISTS " + arrayTableName + 
                " (" + 
                  "array_key_type character varying(" + arrayKeyTypeCodeLength  + ")" +
                ") INHERITS (" + attributesTableName + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    //simple array
	    query = "CREATE TABLE IF NOT EXISTS " + simpleArrayTableName + 
                " () INHERITS (" + arrayTableName + ", " + simpleAttributesTableName + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //complex array
	    query = "CREATE TABLE IF NOT EXISTS " + complexArrayTableName + 
                " () INHERITS (" + arrayTableName + ", " + complexAttributesTableName + ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    //------------------------------------------------
	    query = "CREATE TABLE IF NOT EXISTS " + entitiesTableName + 
                " (" + 
                  "id serial NOT NULL," +
                  "class_id int references " + classesTableName + "(id) ON DELETE CASCADE, " +
                  "CONSTRAINT " + entitiesTableName + "_primary_key_index PRIMARY KEY (id )" + 
                ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    query = "CREATE TABLE IF NOT EXISTS " + arrayKeyFilterTableName + 
                " (" + 
                  "id serial NOT NULL," + 
                  "attribute_id int references " + attributesTableName + "(id) ON DELETE CASCADE, " +
                  "attribute_name character varying(" + attributeNameLength  + ") NOT NULL," + 
                  "CONSTRAINT " + arrayKeyFilterTableName + "_primary_key_index PRIMARY KEY (id )" + 
                ")";

		logger.debug(query);
		
		jdbcTemplate.execute(query);
		
		query = "CREATE TABLE IF NOT EXISTS " + arrayKeyFilterValuesTableName + 
                " (" + 
                  "id serial NOT NULL," + 
                  "filter_id int references " + arrayKeyFilterTableName + "(id) ON DELETE CASCADE, " +
                  "value character varying(" + arrayKeyFilterValueLength  + ") NOT NULL," + 
                  "CONSTRAINT " + arrayKeyFilterValuesTableName + "_primary_key_index PRIMARY KEY (id )" + 
                ")";

		logger.debug(query);
		
		jdbcTemplate.execute(query);
	}

	@Override
	public void clear() {
		String query = "DROP TABLE IF EXISTS " + arrayKeyFilterValuesTableName;
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = "DROP TABLE IF EXISTS " + arrayKeyFilterTableName;
	    logger.debug(query);
	    jdbcTemplate.execute(query);
		query = "DROP TABLE IF EXISTS " + attributesTableName + " CASCADE";
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = "DROP TABLE IF EXISTS " + entitiesTableName;
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = "DROP TABLE IF EXISTS " + classesTableName;
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	}

	@Override
	public void empty() {
		// TODO Auto-generated method stub
		
	}
}
