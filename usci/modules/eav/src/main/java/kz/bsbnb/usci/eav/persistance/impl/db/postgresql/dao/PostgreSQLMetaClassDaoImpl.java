package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.MetaTypeHelper;
import kz.bsbnb.usci.eav.util.SetUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostgreSQLMetaClassDaoImpl extends JDBCSupport implements IMetaClassDao {
	final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaClassDaoImpl.class);
	
	class InsertMetaClassPreparedStatementCreator implements PreparedStatementCreator {
		String className;
		
	    public InsertMetaClassPreparedStatementCreator(String className) {
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
	
	private long getMetaClassId(MetaClass meta)
	{
		long metaId = 0;
		
		if(meta.getId() > 0)
			return meta.getId(); 
	    
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
		    
	    	jdbcTemplate.update(new InsertMetaClassPreparedStatementCreator(meta.getClassName()), keyHolder);
		    
            metaId = keyHolder.getKey().longValue();
            
            if(metaId == 0)
	        {
	        	logger.error("Can't get className id");
	        }
	    }
	    
	    return metaId;
	}
	
	@Transactional
	public boolean saveMetaClass(MetaClass meta) {
	    long metaId = meta.getId();
	    String query = "";
	    
	    if(meta.getId() < 1)
	    {
	    	metaId = getMetaClassId(meta);
	    	meta.setId(metaId);
	    }
	    
	    if(metaId < 1)
	    {
	    	throw new IllegalArgumentException("Can't determine metadata id");
	    }
	    
	    MetaClass oldMeta = load(metaId);
	    
	    Set<String> oldNames = oldMeta.getMemberNames();
	    Set<String> newNames = meta.getMemberNames();
	    
	    Set<String> updateNames = SetUtils.intersection(oldNames, newNames);
	    Set<String> deleteNames = SetUtils.difference(oldNames, newNames);
	    Set<String> addNames = SetUtils.difference(newNames, oldNames);
	    
	    for(String typeName : addNames)
	    {
	    	IMetaType metaType = meta.getMemberType(typeName);
	    	
	    	query = "INSERT INTO " + attributesTableName +
                    " (class_id, name, type_code, is_key, is_nullable, " +
                    "is_array, array_key_type, complex_key_type) VALUES (" +
                    metaId + ", " +
                    "\'" + typeName + "\', " +
                    (MetaTypeHelper.getDataType(metaType) == null ? 
                    	"NULL" : "'" + MetaTypeHelper.getDataType(metaType) + "'") + ", " +
                    "\'" + meta.getMemberType(typeName).isKey() + "\', " +
                    "\'" + meta.getMemberType(typeName).isNullable() + "\', " +
                    "\'" + meta.getMemberType(typeName).isArray() + "\', " +
                    (MetaTypeHelper.getArrayKeyType(metaType) == null ? 
                    	"NULL" : "'" + MetaTypeHelper.getArrayKeyType(metaType) + "'") + ", " +
                    (MetaTypeHelper.getArrayKeyType(metaType) == null ? 
                    	"NULL" : "'" + MetaTypeHelper.getClassKeyType(metaType) + "'") + " " +
                    		")";
	    	
	    	logger.debug(query);
	    	
	    	jdbcTemplate.execute(query);
	    }
	    
	    //TODO: Add bulk delete by id list
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
	    	if(!meta.getMemberType(typeName).equals(oldMeta.getMemberType(typeName)))
	    	{
	    		IMetaType metaType = meta.getMemberType(typeName);
	    		
		    	query = "UPDATE " + attributesTableName +
	                    " set type_code = " + (MetaTypeHelper.getDataType(metaType) == null ? 
	                        	"NULL" : "'" + MetaTypeHelper.getDataType(metaType) + "'") + ", " +  
	                    " is_key = \'" + metaType.isKey() + "\', " +
	                    " is_nullable = \'" + metaType.isNullable() + "\', " +
	                    " is_array = \'" + metaType.isArray() + "\', " +
	                    " array_key_type = " + (MetaTypeHelper.getArrayKeyType(metaType) == null ? 
	                        	"NULL" : "'" + MetaTypeHelper.getArrayKeyType(metaType) + "'") + ", " +
	                    " complex_key_type = " + (MetaTypeHelper.getArrayKeyType(metaType) == null ? 
	                        	"NULL" : "'" + MetaTypeHelper.getClassKeyType(metaType) + "'") +
	                    " where class_id = " +
	    	            	metaId + " and name = " +
	    	                "\'" + typeName + "\'";
		    	
		    	logger.debug(query);
		    	
		    	jdbcTemplate.execute(query);
	    	}
	    }
	    
	    return true;
	}

	@Override
	public MetaClass load(String className) {
		MetaClass meta = new MetaClass(className);
	    
	    String query = "SELECT a.id, a.name, a.type_code, a.is_key, a.is_nullable, a.is_array, " +
	    		"a.array_key_type, a.complex_key_type, ak.id akid, ak.attribute_name akname FROM " + 
				classesTableName + " c LEFT JOIN " + attributesTableName + " a ON c.id = a.class_id " +
				" LEFT JOIN " + arrayKeyFilterTableName + " ak ON a.id = ak.attribute_id " +  
                " WHERE c.name = \'" + className + "\' " +
                "and a.id IS NOT NULL";
	    
	    logger.debug(query);
	    
	    List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
		/*for (Map<String, Object> row : rows) {
			IMetaType attribute;
			boolean isArray = (Boolean)row.get("is_array");
			boolean isClass = row.get("type_code") == null ? true : false;
			
			if (isArray)
			{
				if(isClass)
				{
					attribute = new MetaClassArray(query, isClass, isClass);
				}
				else
				{
					
				}
			}
			else
			{
				if(isClass)
				{
					
				}
				else
				{
					
				}
			}
			
	    	MetaValue t = new MetaValue(
                    DataTypes.valueOf((String)row.get("type_code")),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable")
                );
	    	t.setArrayKeyType(ComplexKeyTypes.valueOf((String)row.get("array_key_type")));
	    	t.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
	    	
	    	if(row.get("akid") != null)
	    	{
	    		//TODO: add list of filter values from db
	    		t.addArrayKeyFilterValues((String)row.get("akname"), null);
	    	}
	    	
			meta.setId((Integer)row.get("id"));
	    	meta.setType((String)row.get("name"), t);
	    }*/
	    
	    return meta;
	}

	@Override
	public boolean testConnection() {
	    String query = "SELECT 1";
	    
	    logger.debug(query);
	    
	    jdbcTemplate.execute(query);
	
	    return true;
	}

	@Override
	public MetaClass load(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long save(MetaClass persitable) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void remove(long id) {
		// TODO Auto-generated method stub
		
	}
}
