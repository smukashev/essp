package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClassArray;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValueArray;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class PostgreSQLMetaClassDaoImpl extends JDBCSupport implements IMetaClassDao {
	final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaClassDaoImpl.class);
	
	class InsertMetaClassPreparedStatementCreator implements PreparedStatementCreator {
		MetaClass metaClass;
		
	    public InsertMetaClassPreparedStatementCreator(MetaClass metaClass) {
			this.metaClass = metaClass;
		}

		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
	        PreparedStatement ps = con.prepareStatement(
	        		"INSERT INTO " + getConfig().getClassesTableName() +
	                " (name, begin_date, is_disabled) VALUES ( ?, ?, ? )", new String[] {"id"});
	        ps.setString(1, metaClass.getClassName());
            ps.setTimestamp(2, metaClass.getBeginDate());
            ps.setBoolean(3, metaClass.isDisabled());
	        
	        logger.debug(ps.toString());
	        
	        return ps;
	    }
	}

    private void loadClass(MetaClass metaClass)
    {
        String query;
        if(metaClass.getId() < 1)
        {
            if(metaClass.getClassName() == null)
            {
                throw new IllegalArgumentException("Meta class does not have name or id. Can't load.");
            }

            query = "SELECT * FROM " + getConfig().getClassesTableName() +
                    " WHERE name = '" + metaClass.getClassName() + "' ";
        }
        else
        {
            query = "SELECT * FROM " + getConfig().getClassesTableName() +
                    " WHERE id = " + metaClass.getId();
        }

        logger.debug(query);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one class found. Can't load.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalArgumentException("Class not found. Can't load.");
        }

        Map<String, Object> row = rows.get(0);

        if(row != null)
        {
            metaClass.setDisabled((Boolean)row.get("is_disabled"));
            metaClass.setBeginDate((Timestamp)row.get("begin_date"));
            metaClass.setId((Integer)row.get("id"));
            metaClass.setClassName((String)row.get("name"));
        }
        else
        {
            logger.error("Can't load metaClass, empty data set.");
        }
    }
	
	private long createClass(MetaClass metaClass)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try
        {
            jdbcTemplate.update(new InsertMetaClassPreparedStatementCreator(metaClass), keyHolder);
        }
        catch (DuplicateKeyException e)
        {
            logger.error("Duplicate name for class: " + metaClass.getClassName());
            throw new IllegalArgumentException("Duplicate name for class: " + metaClass.getClassName());
        }

        long metaId = keyHolder.getKey().longValue();

        metaClass.setId(metaId);

        if(metaId < 1)
        {
            logger.error("Can't create class");
            return 0;
        }
//        {
//            logger.error("Can't create class with name: " + metaClass.getClassName() +
//                    ", because there is a class with such name, with id: " + metaId);
//            throw new IllegalArgumentException("Can't create class with name: " + metaClass.getClassName() +
//                    ", because there is a class with such name, with id: " + metaId);
//        }

        return metaId;
    }

    private void updateClass(MetaClass metaClass)
    {
        if(metaClass.getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have id to be updated");
        }

        String query;

        query = "UPDATE " + getConfig().getClassesTableName() + " SET " +
                " name = ?, " +
                " begin_date = ?, " +
                " is_disabled = ? " +
                " WHERE id = ?";

        Object[] args = {metaClass.getClassName(), metaClass.getBeginDate(), metaClass.isDisabled(), metaClass.getId()};

        logger.debug(query);
        jdbcTemplate.update(query, args);
    }

    private void insertAttributes(Set<String> addNames, MetaClass meta)
    {
        String query;
        if(meta.getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before attributes insertion to DB");
        }
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

                    query = "INSERT INTO " + getConfig().getComplexArrayTableName() +
                            " (containing_class_id, name, is_key, is_nullable, complex_key_type, class_id, array_key_type) VALUES " +
                            " ( " +
                            meta.getId()                               + " , " +
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

                    query = "INSERT INTO " + getConfig().getComplexAttributesTableName() +
                            " (containing_class_id, name, is_key, is_nullable, complex_key_type, class_id) VALUES " +
                            " ( " +
                            meta.getId()                          + " , " +
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

                    query = "INSERT INTO " + getConfig().getSimpleArrayTableName() +
                            " (containing_class_id, name, type_code, is_key, is_nullable, array_key_type) VALUES " +
                            " ( " +
                            meta.getId()                             + " , " +
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

                    query = "INSERT INTO " + getConfig().getSimpleAttributesTableName() +
                            " (containing_class_id, name, type_code, is_key, is_nullable) VALUES " +
                            " ( " +
                            meta.getId()                   + " , " +
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
    }

    private void deleteAttributes(Set<String> deleteNames, MetaClass meta)
    {
        String query;
        if(meta.getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before attributes deletion to DB");
        }

        //TODO: Add bulk delete by id list
        for(String typeName : deleteNames)
        {
            query = "DELETE FROM " + getConfig().getAttributesTableName() +
                    " WHERE containing_class_id = " +
                    meta.getId() + " AND name = " +
                    "\'" + typeName + "\' ";

            logger.debug(query);

            jdbcTemplate.execute(query);
        }
    }

    public void loadAttributes(MetaClass meta) {
        if(meta.getId() < 1)
            return;

        long id = meta.getId();
        meta.removeMembers();

        //load simple attributes
        String query = "SELECT * FROM " +
                getConfig().getSimpleAttributesTableName() +
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
                getConfig().getSimpleArrayTableName() +
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
        query = "SELECT ca.*, c.name cname FROM " +
                getConfig().getComplexAttributesTableName() + " ca LEFT JOIN " + getConfig().getClassesTableName() +
                    " c ON ca.class_id = c.id " +
                " WHERE containing_class_id = " + id;

        logger.debug(query);

        rows = jdbcTemplate.queryForList(query);
        for (Map<String, Object> row : rows) {
            MetaClass attribute = load((Integer)row.get("class_id"));

            attribute.setClassName((String)row.get("cname"));
            attribute.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
            attribute.setKey((Boolean)row.get("is_key"));
            attribute.setNullable((Boolean) row.get("is_nullable"));

            meta.setMemberType((String)row.get("name"), attribute);
        }

        //load complex attributes
        query = "SELECT ca.*, c.name cname FROM " +
                getConfig().getComplexArrayTableName() + " ca LEFT JOIN " + getConfig().getClassesTableName() +
                    " c ON ca.class_id = c.id " +
                " WHERE containing_class_id = " + id;

        logger.debug(query);

        rows = jdbcTemplate.queryForList(query);
        for (Map<String, Object> row : rows) {
            MetaClass attribute = load((Integer)row.get("class_id"));

            attribute.setClassName((String)row.get("cname"));
            attribute.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
            attribute.setKey((Boolean)row.get("is_key"));
            attribute.setNullable((Boolean) row.get("is_nullable"));

            MetaClassArray metaClassArray = new MetaClassArray(attribute);

            meta.setMemberType((String)row.get("name"), metaClassArray);
        }
    }

    //TODO: add active period to classes
	@Transactional
	public long save(MetaClass meta) {
	    String query;
        MetaClass dbMeta = new MetaClass(meta);

        try
        {
            loadClass(dbMeta);
            loadAttributes(dbMeta);
            meta.setId(dbMeta.getId());
            updateClass(meta);
        }
        catch(IllegalArgumentException e)
        {
            logger.debug("Class: " + meta.getClassName() + " not found.");
            createClass(dbMeta);
            dbMeta.removeMembers();
            meta.setId(dbMeta.getId());
            logger.debug("Class: " + meta.getClassName() + " not created.");
        }

	    if(dbMeta.getId() < 1)
	    {
	    	throw new IllegalArgumentException("Can't determine metadata id");
	    }

	    Set<String> oldNames = dbMeta.getMemberNames();
	    Set<String> newNames = meta.getMemberNames();
	    
	    Set<String> updateNames = SetUtils.intersection(oldNames, newNames);
	    Set<String> deleteNames = SetUtils.difference(oldNames, newNames);
	    Set<String> addNames = SetUtils.difference(newNames, oldNames);

        for (String name : updateNames)
        {
            if(!meta.getMemberType(name).equals(dbMeta.getMemberType(name)))
            {
                deleteNames.add(name);
                addNames.add(name);
            }
        }

        deleteAttributes(deleteNames, dbMeta);
        insertAttributes(addNames, meta);

	    return dbMeta.getId();
	}

	@Override
	public MetaClass load(String className) {
		MetaClass meta = new MetaClass(className);

        loadClass(meta);
        loadAttributes(meta);

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
        if(id < 1)
            return null;

        MetaClass meta = new MetaClass();
        meta.setId(id);

        loadClass(meta);
        loadAttributes(meta);

        return meta;
	}

	@Override
	public void remove(long id) {
		// TODO Auto-generated method stub
		
	}
}
