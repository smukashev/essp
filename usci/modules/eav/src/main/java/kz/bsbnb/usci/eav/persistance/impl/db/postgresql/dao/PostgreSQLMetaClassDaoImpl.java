package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClassArray;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValueArray;
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

    private String getMetaClassName(long id)
    {
        String name;

        String query = "SELECT name FROM " + classesTableName +
                " WHERE id = " + id + " LIMIT 1";

        logger.debug(query);

        try {
            name = jdbcTemplate.queryForObject(query, String.class);
        } catch (IncorrectResultSizeDataAccessException e) {
            name = null;
        }

        return name;
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
                    MetaClassArray metaClassArray = (MetaClassArray)metaType;

                    long innerId = save(metaClassArray.getMembersType());

                    query = "INSERT INTO " + complexArrayTableName +
                            " (containing_class_id, name, is_key, is_nullable, complex_key_type, class_id, array_key_type) VALUES " +
                            " ( " +
                                   metaId                              + " , " +
                            " '" + typeName                            + "', " +
                            " '" + metaClassArray.isKey()              + "', " +
                            " '" + metaClassArray.isNullable()         + "', " +
                            " '" + metaClassArray.getComplexKeyType()  + "', " +
                            "  " + innerId                             + " , " +
                            " '" + metaClassArray.getArrayKeyType()    + "'  " +
                            " ) ";
                }
                else
                {
                    MetaClass metaClass = (MetaClass)metaType;

                    long innerId = save(metaClass);

                    query = "INSERT INTO " + complexAttributesTableName +
                            " (containing_class_id, name, is_key, is_nullable, complex_key_type, class_id) VALUES " +
                            " ( " +
                                   metaId                         + " , " +
                            " '" + typeName                       + "', " +
                            " '" + metaClass.isKey()              + "', " +
                            " '" + metaClass.isNullable()         + "', " +
                            " '" + metaClass.getComplexKeyType()  + "', " +
                            "  " + innerId                        + "   " +
                            " ) ";
                }
            }
            else
            {
                if(metaType.isArray())
                {
                    MetaValueArray metaValueArray = (MetaValueArray)metaType;

                    query = "INSERT INTO " + simpleArrayTableName +
                            " (containing_class_id, name, type_code, is_key, is_nullable, array_key_type) VALUES " +
                            " ( " +
                                   metaId                            + " , " +
                            " '" + typeName                          + "', " +
                            " '" + metaValueArray.getTypeCode()      + "', " +
                            " '" + metaValueArray.isKey()            + "', " +
                            " '" + metaValueArray.isNullable()       + "', " +
                            " '" + metaValueArray.getArrayKeyType()  + "'  " +
                            " ) ";
                }
                else
                {
                    MetaValue metaValue = (MetaValue)metaType;

                    query = "INSERT INTO " + simpleAttributesTableName +
                        " (containing_class_id, name, type_code, is_key, is_nullable) VALUES " +
                        " ( " +
                                    metaId                 + " , " +
                            " '" + typeName                + "', " +
                            " '" + metaValue.getTypeCode() + "', " +
                            " '" + metaValue.isKey()       + "', " +
                            " '" + metaValue.isNullable()  + "'  " +
                   		" ) ";
                }
            }
	    	
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
        meta.setId(id);
        meta.setClassName(getMetaClassName(id));

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

            meta.setMemberType((String)row.get("name"), attribute);
        }

        //load simple attributes arrays
        query = "SELECT * FROM " +
                simpleArrayTableName +
                " WHERE containing_class_id = " + id;

        logger.debug(query);

        rows = jdbcTemplate.queryForList(query);
        for (Map<String, Object> row : rows) {
            MetaValueArray attribute = new MetaValueArray(
                    DataTypes.valueOf((String) row.get("type_code")),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable"));

            attribute.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));

            meta.setMemberType((String)row.get("name"), attribute);
        }

        //load complex attributes
        query = "SELECT * FROM " +
                complexAttributesTableName +
                " WHERE containing_class_id = " + id;

        logger.debug(query);

        rows = jdbcTemplate.queryForList(query);
        for (Map<String, Object> row : rows) {
            MetaClass attribute = load((Integer)row.get("class_id"));

            attribute.setClassName(getMetaClassName((Integer)row.get("class_id")));
            attribute.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
            attribute.setKey((Boolean)row.get("is_key"));
            attribute.setNullable((Boolean) row.get("is_nullable"));

            meta.setMemberType((String)row.get("name"), attribute);
        }

        //load complex attributes
        query = "SELECT * FROM " +
                complexArrayTableName +
                " WHERE containing_class_id = " + id;

        logger.debug(query);

        rows = jdbcTemplate.queryForList(query);
        for (Map<String, Object> row : rows) {
            MetaClass attribute = load((Integer)row.get("class_id"));

            attribute.setClassName(getMetaClassName((Integer)row.get("class_id")));
            attribute.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
            attribute.setKey((Boolean)row.get("is_key"));
            attribute.setNullable((Boolean) row.get("is_nullable"));

            MetaClassArray metaClassArray = new MetaClassArray(attribute);

            meta.setMemberType((String)row.get("name"), metaClassArray);
        }

        return meta;
	}

	@Override
	public void remove(long id) {
		// TODO Auto-generated method stub
		
	}
}
