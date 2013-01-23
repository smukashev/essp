package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
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
	
	private long getMetaClassId(String className)
	{
		long metaId;
		
	    String query = "SELECT id FROM " + classesTableName +
	    		" WHERE name = \'" + className + "\' LIMIT 1";
	    		
	    logger.debug(query);
	    
	    try {
	    	metaId = jdbcTemplate.queryForInt(query);
	    } catch (IncorrectResultSizeDataAccessException e) {
	    	metaId = 0;
	    }
	    
	    if(metaId == 0)
	    {
	    	KeyHolder keyHolder = new GeneratedKeyHolder();
		    
	    	jdbcTemplate.update(new InsertMetaClassPreparedStatementCreator(className), keyHolder);
		    
            metaId = keyHolder.getKey().longValue();
            
            if(metaId == 0)
	        {
	        	logger.error("Can't get className id");
	        }
	    }
	    
	    return metaId;
	}
	
	@Transactional
	public long save(MetaClass meta) {
	    long metaId = meta.getId();
	    String query;
	    
	    if(meta.getId() < 1)
	    {
            metaId = getMetaClassId(meta.getClassName());
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

        //TODO: make bulk insert
	    for(String typeName : addNames)
	    {
	    	IMetaType metaType = meta.getMemberType(typeName);

            if(metaType.isComplex())
            {
                if(metaType.isArray())
                {
                    query = "";
                }
                else
                {
                    query = "";
                }
            }
            else
            {
                if(metaType.isArray())
                {
                    query = "";
                }
                else
                {
                    MetaValue metaValue = (MetaValue)metaType;

                    query = "INSERT INTO " + simpleAttributesTableName +
                        " (containing_class_id, name, type_code, is_key, is_nullable) VALUES " +
                        " ( " +
                                    metaId   +               " , " +
                            " '" + typeName                + "', " +
                            " '" + metaValue.getTypeCode() + "', " +
                            " '" + metaValue.isKey()       + "', " +
                            " '" + metaValue.isNullable()  + "'  " +
                   		" ) ";
                }
            }
	    	
//	    	query = "INSERT INTO " + attributesTableName +
//                    " (class_id, name, type_code, is_key, is_nullable, " +
//                    "is_array, array_key_type, complex_key_type) VALUES (" +
//                    metaId + ", " +
//                    "\'" + typeName + "\', " +
//                    (MetaTypeHelper.getDataType(metaType) == null ?
//                    	"NULL" : "'" + MetaTypeHelper.getDataType(metaType) + "'") + ", " +
//                    "\'" + meta.getMemberType(typeName).isKey() + "\', " +
//                    "\'" + meta.getMemberType(typeName).isNullable() + "\', " +
//                    "\'" + meta.getMemberType(typeName).isArray() + "\', " +
//                    (MetaTypeHelper.getArrayKeyType(metaType) == null ?
//                    	"NULL" : "'" + MetaTypeHelper.getArrayKeyType(metaType) + "'") + ", " +
//                    (MetaTypeHelper.getArrayKeyType(metaType) == null ?
//                    	"NULL" : "'" + MetaTypeHelper.getClassKeyType(metaType) + "'") + " " +
//                    		")";
//
	    	logger.debug(query);

	    	jdbcTemplate.execute(query);
	    }
	    
	    //TODO: Add bulk delete by id list
	    for(String typeName : deleteNames)
	    {
	    	query = "DELETE FROM " + attributesTableName +
                    " WHERE class_id = " +
                    metaId + " AND name = " +
                    "\'" + typeName + "\' CASCADE ";
	    	
	    	logger.debug(query);
	    	
	    	jdbcTemplate.execute(query);
	    }
	    
	    for(String typeName : updateNames)
	    {
	    	if(!meta.getMemberType(typeName).equals(oldMeta.getMemberType(typeName)))
	    	{
	    		IMetaType metaType = meta.getMemberType(typeName);
	    		
		    	query = "UPDATE " + attributesTableName +
	                    " SET type_code = " + (MetaTypeHelper.getDataType(metaType) == null ?
	                        	"NULL" : "'" + MetaTypeHelper.getDataType(metaType) + "'") + ", " +  
	                    " is_key = \'" + metaType.isKey() + "\', " +
	                    " is_nullable = \'" + metaType.isNullable() + "\', " +
	                    " is_array = \'" + metaType.isArray() + "\', " +
	                    " array_key_type = " + (MetaTypeHelper.getArrayKeyType(metaType) == null ? 
	                        	"NULL" : "'" + MetaTypeHelper.getArrayKeyType(metaType) + "'") + ", " +
	                    " complex_key_type = " + (MetaTypeHelper.getArrayKeyType(metaType) == null ? 
	                        	"NULL" : "'" + MetaTypeHelper.getClassKeyType(metaType) + "'") +
	                    " WHERE class_id = " +
	    	            	metaId + " AND name = " +
	    	                "\'" + typeName + "\'";
		    	
		    	logger.debug(query);
		    	
		    	jdbcTemplate.execute(query);
	    	}
	    }
	    
	    return metaId;
	}

	@Override
	public MetaClass load(String className) {
		long id = getMetaClassId(className);

        MetaClass meta = load(id);
        meta.setClassName(className);

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
        MetaClass meta = new MetaClass();

        //load simple attributes
        String query = "SELECT * FROM " +
                simpleAttributesTableName +
                " WHERE containing_class_id = " + id;

        logger.debug(query);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
        for (Map<String, Object> row : rows) {
            MetaValue attribute = new MetaValue(
                    DataTypes.valueOf((String) row.get("type_code")),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable"));

            meta.setId((Integer)row.get("id"));
            meta.setMemberType((String)row.get("name"), attribute);
        }

        return meta;
	}

	@Override
	public void remove(long id) {
		// TODO Auto-generated method stub
		
	}
}
