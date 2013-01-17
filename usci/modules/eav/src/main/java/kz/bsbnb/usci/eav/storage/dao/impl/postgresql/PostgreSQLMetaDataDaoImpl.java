package kz.bsbnb.usci.eav.storage.dao.impl.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.MetaData;
import kz.bsbnb.usci.eav.model.metadata.Type;
import kz.bsbnb.usci.eav.storage.dao.IMetaDataDao;
import kz.bsbnb.usci.eav.storage.dao.impl.AbstractDBDao;
import kz.bsbnb.usci.eav.util.SetUtils;

@Repository
public class PostgreSQLMetaDataDaoImpl extends AbstractDBDao implements IMetaDataDao {
	final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaDataDaoImpl.class);
	
	class InsertMetaDataPreparedStatementCreator implements PreparedStatementCreator {
		String className;
		
	    public InsertMetaDataPreparedStatementCreator(String className) {
			this.className = className;
		}

		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
	        PreparedStatement ps = con.prepareStatement(
	        		"INSERT INTO " + classesTableName +
	                " (name) VALUES ( ? )", new String[] {"id"});
	        ps.setString(1, className);
	        
	        logger.debug(ps.toString());
	        
	        return ps;
	    }
	}
	
	public MetaData loadMetaData(String metaClassName) {
		MetaData meta = new MetaData(metaClassName);
	    
	    String query = "SELECT a.id, a.name, a.type_code, a.is_key, a.is_nullable, a.is_array, " +
	    		"a.array_key_type, a.complex_key_type FROM " + 
				classesTableName + " c, " + attributesTableName + " a " + 
                " WHERE c.name = \'" + metaClassName + "\'";
	    
	    logger.debug(query);
	    
	    List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
		for (Map<String, Object> row : rows) {
	    	Type t = new Type(
                    DataTypes.valueOf((String)row.get("type_code")),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable")
                );
	    	t.setArray((Boolean)row.get("is_array"));
	    	t.setArrayKeyType(ComplexKeyTypes.valueOf((String)row.get("array_key_type")));
	    	t.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
			meta.setId((Integer)row.get("id"));
	    	meta.setType((String)row.get("name"), 
	                t);
	    }
	    
	    return meta;
	}
	
	private long getMetaDataId(MetaData meta)
	{
		long metaId = 0;
	    
	    String query = "SELECT id FROM " + classesTableName + 
	    		" WHERE name = \'" + meta.getClassName() + "\' limit 1"; 
	    		
	    logger.debug(query);
	    
	    try {
	    	metaId = jdbcTemplate.queryForInt(query);
	    } catch (IncorrectResultSizeDataAccessException e) {
	    	metaId = 0;
	    }
	    
	    if(metaId == 0)
	    {
	    	KeyHolder keyHolder = new GeneratedKeyHolder();
		    
	    	jdbcTemplate.update(new InsertMetaDataPreparedStatementCreator(meta.getClassName()), keyHolder);
		    
            metaId = keyHolder.getKey().longValue();
            
            if(metaId == 0)
	        {
	        	logger.error("Can't get className id");
	        }
	    }
	    
	    return metaId;
	}
	
	@Transactional
	public boolean saveMetaData(MetaData meta) {
	    long metaId = meta.getId();
	    String query = "";
	    
	    if(meta.getId() < 1)
	    {
	    	metaId = getMetaDataId(meta);
	    	meta.setId(metaId);
	    }
	    
	    if(metaId < 1)
	    {
	    	throw new IllegalArgumentException("Can't determine metadata id");
	    }
	    
	    MetaData oldMeta = loadMetaData(meta.getClassName());
	    
	    Set<String> oldNames = oldMeta.getTypeNames();
	    Set<String> newNames = meta.getTypeNames();
	    
	    Set<String> updateNames = SetUtils.intersection(oldNames, newNames);
	    Set<String> deleteNames = SetUtils.difference(oldNames, newNames);
	    Set<String> addNames = SetUtils.difference(newNames, oldNames);
	    
	    for(String typeName : addNames)
	    {
	    	query = "INSERT INTO " + attributesTableName +
                    " (class_id, name, type_code, is_key, is_nullable, " +
                    "is_array, array_key_type, complex_key_type) VALUES (" +
                    metaId + ", " +
                    "\'" + typeName + "\', " +
                    "\'" + meta.getType(typeName).getTypeCode() + "\', " +
                    "\'" + meta.getType(typeName).isKey() + "\', " +
                    "\'" + meta.getType(typeName).isNullable() + "\', " +
                    "\'" + meta.getType(typeName).isArray() + "\', " +
                    "\'" + meta.getType(typeName).getArrayKeyType() + "\', " +
                    "\'" + meta.getType(typeName).getComplexKeyType() + "\' " +
                    		")";
	    	
	    	logger.debug(query);
	    	
	    	jdbcTemplate.execute(query);
	    }
	    
	    //TODO: Add bulk delete
	    for(String typeName : deleteNames)
	    {
	    	query = "DELETE FROM " + attributesTableName +
                    " where class_id = " +
                    metaId + " and name = " +
                    "\'" + typeName + "\'";
	    	
	    	logger.debug(query);
	    	
	    	jdbcTemplate.execute(query);
	    }
	    
	    for(String typeName : updateNames)
	    {
	    	if(!meta.getType(typeName).equals(oldMeta.getType(typeName)))
	    	{
		    	query = "UPDATE " + attributesTableName +
	                    " set type_code = \'" + meta.getType(typeName).getTypeCode() + "\', " +  
	                    " is_key = \'" + meta.getType(typeName).isKey() + "\', " +
	                    " is_nullable = \'" + meta.getType(typeName).isNullable() + "\', " +
	                    " is_array = \'" + meta.getType(typeName).isArray() + "\', " +
	                    " array_key_type = \'" + meta.getType(typeName).getArrayKeyType() + "\', " +
	                    " complex_key_type = \'" + meta.getType(typeName).getComplexKeyType() + "\' " +
	                    " where class_id = " +
	    	            	metaId + " and name = " +
	    	                "\'" + typeName + "\'";
		    	
		    	logger.debug(query);
		    	
		    	jdbcTemplate.execute(query);
	    	}
	    }
	    
	    return true;
	}
}
