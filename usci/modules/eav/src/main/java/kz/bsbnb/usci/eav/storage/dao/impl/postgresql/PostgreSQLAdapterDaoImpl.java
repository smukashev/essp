package kz.bsbnb.usci.eav.storage.dao.impl.postgresql;



import kz.bsbnb.usci.eav.storage.dao.IAdapterDao;
import kz.bsbnb.usci.eav.storage.dao.impl.AbstractDBDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;


/**
 *
 * @author a.tkachenko
 */
@Repository
public class PostgreSQLAdapterDaoImpl extends AbstractDBDao implements IAdapterDao {
	final Logger logger = LoggerFactory.getLogger(PostgreSQLAdapterDaoImpl.class);
	
	public void createStructure() {
	    String query = "CREATE TABLE IF NOT EXISTS " + classesTableName + 
	                    " (" + 
	                      "id serial NOT NULL," + 
	                      "name character varying(" + classNameLength  + ") NOT NULL," + 
	                      "CONSTRAINT " + classesTableName + "_primary_key_index PRIMARY KEY (id )" + 
	                    ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
	    query = "CREATE TABLE IF NOT EXISTS " + attributesTableName + 
                " (" + 
                  "id serial NOT NULL," +
                  "class_id int references " + classesTableName + "(id) ON DELETE CASCADE, " +
                  "name character varying(" + attributeNameLength  + ") NOT NULL," + 
                  "type_code character varying(" + typeCodeLength  + ") NOT NULL," +
                  "is_key boolean NOT NULL," +
                  "is_nullable boolean NOT NULL," +
                  "is_array boolean NOT NULL," +
                  "array_key_type character varying(" + arrayKeyTypeCodeLength  + ")," +
                  "complex_key_type character varying(" + complexKeyTypeCodeLength  + ")," +
                  "CONSTRAINT " + attributesTableName + "_primary_key_index PRIMARY KEY (id )" + 
                ")";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	    
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

	public void dropStructure() {
		String query = "DROP TABLE IF EXISTS " + arrayKeyFilterValuesTableName;
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = "DROP TABLE IF EXISTS " + arrayKeyFilterTableName;
	    logger.debug(query);
	    jdbcTemplate.execute(query);
		query = "DROP TABLE IF EXISTS " + attributesTableName;
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = "DROP TABLE IF EXISTS " + entitiesTableName;
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	    query = "DROP TABLE IF EXISTS " + classesTableName;
	    logger.debug(query);
	    jdbcTemplate.execute(query);
	}

	public boolean testConnection() {
	    String query = "SELECT 1";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	
	    return true;
	}
}
