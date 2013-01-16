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
	    
	    String query = "SELECT a.id, a.name, a.type_code, a.is_key, a.is_nullable FROM " + 
				classesTableName + " c, " + attributesTableName + " a " + 
                " WHERE c.name = \'" + metaClassName + "\'";
	    
	    logger.debug(query);
	    
	    List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
		for (Map<String, Object> row : rows) {
	    	meta.setId((Integer)row.get("id"));
	    	meta.setType((String)row.get("name"), 
	                new Type(
	                    DataTypes.valueOf((String)row.get("type_code")),
	                    (Boolean)row.get("is_key"),
	                    (Boolean)row.get("is_nullable")
	                ));
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
                    " (class_id, name, type_code, is_key, is_nullable) VALUES (" +
                    metaId + ", " +
                    "\'" + typeName + "\', " +
                    "\'" + meta.getType(typeName).getTypeCode() + "\', " +
                    "\'" + meta.getType(typeName).isKey() + "\', " +
                    "\'" + meta.getType(typeName).isNullable() + "\' " +
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
	                    " is_nullable = \'" + meta.getType(typeName).isNullable() + "\' " +
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
