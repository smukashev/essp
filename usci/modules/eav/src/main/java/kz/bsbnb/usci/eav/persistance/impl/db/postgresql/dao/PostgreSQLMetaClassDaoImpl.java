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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class PostgreSQLMetaClassDaoImpl extends JDBCSupport implements IMetaClassDao {
	final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaClassDaoImpl.class);

    private String INSERT_CLASS_SQL;
    private String SELECT_CLASS_BY_NAME;
    private String SELECT_CLASS_BY_ID;
    private String UPDATE_CLASS_BY_ID;
    private String DELETE_CLASS_BY_ID;
    private String INSERT_COMPLEX_ARRAY;
    private String INSERT_COMPLEX_ATTRIBUTE;
    private String INSERT_SIMPLE_ARRAY;
    private String INSERT_SIMPLE_ATTRIBUTE;
    private String DELETE_ATTRIBUTE;
    private String SELECT_SIMPLE_ATTRIBUTES;
    private String SELECT_SIMPLE_ARRAY;
    private String SELECT_COMPLEX_ARRAY;
    private String SELECT_COMPLEX_ATTRIBUTE;


    @PostConstruct
    public void init()
    {
        INSERT_CLASS_SQL = String.format("INSERT INTO %s (name, begin_date, is_disabled) VALUES ( ?, ?, ? )", getConfig().getClassesTableName());
        SELECT_CLASS_BY_NAME = String.format("SELECT * FROM %s WHERE name = ? ", getConfig().getClassesTableName());
        SELECT_CLASS_BY_ID = String.format("SELECT * FROM %s WHERE id = ? ", getConfig().getClassesTableName());
        UPDATE_CLASS_BY_ID = String.format("UPDATE %s SET  name = ?,  begin_date = ?,  is_disabled = ?  WHERE id = ?", getConfig().getClassesTableName());
        DELETE_CLASS_BY_ID = String.format("DELETE FROM %s WHERE id = ?", getConfig().getClassesTableName());
        INSERT_COMPLEX_ARRAY = String.format("INSERT INTO %s (containing_class_id, name, is_key, is_nullable, complex_key_type, class_id, array_key_type) VALUES  ( ?, ?, ?, ?, ?, ?, ?) ", getConfig().getComplexArrayTableName());
        INSERT_COMPLEX_ATTRIBUTE = String.format("INSERT INTO %s (containing_class_id, name, is_key, is_nullable, complex_key_type, class_id) VALUES  ( ?, ?, ?, ?, ?, ?) ", getConfig().getComplexAttributesTableName());
        INSERT_SIMPLE_ARRAY = String.format("INSERT INTO %s (containing_class_id, name, type_code, is_key, is_nullable, array_key_type) VALUES  ( ?, ?, ?, ?, ?, ?) ", getConfig().getSimpleArrayTableName());
        INSERT_SIMPLE_ATTRIBUTE = String.format("INSERT INTO %s (containing_class_id, name, type_code, is_key, is_nullable) VALUES  ( ?, ?, ?, ?, ?) ", getConfig().getSimpleAttributesTableName());
        DELETE_ATTRIBUTE = String.format("DELETE FROM %s WHERE containing_class_id = ? AND name = ? ", getConfig().getAttributesTableName());
        SELECT_SIMPLE_ATTRIBUTES = String.format("SELECT * FROM %s WHERE containing_class_id = ?", getConfig().getSimpleAttributesTableName());
        SELECT_SIMPLE_ARRAY = String.format("SELECT * FROM %s WHERE containing_class_id = ?", getConfig().getSimpleArrayTableName());
        SELECT_COMPLEX_ARRAY = String.format("SELECT ca.*, c.name cname FROM %s ca LEFT JOIN %s c ON ca.class_id = c.id  WHERE containing_class_id = ?", getConfig().getComplexAttributesTableName(), getConfig().getClassesTableName());
        SELECT_COMPLEX_ATTRIBUTE = String.format("SELECT ca.*, c.name cname FROM %s ca LEFT JOIN %s c ON ca.class_id = c.id  WHERE containing_class_id = ?", getConfig().getComplexArrayTableName(), getConfig().getClassesTableName());
    }
	
	class InsertMetaClassPreparedStatementCreator implements PreparedStatementCreator {
		MetaClass metaClass;
		
	    public InsertMetaClassPreparedStatementCreator(MetaClass metaClass) {
			this.metaClass = metaClass;
		}

		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
	        PreparedStatement ps = con.prepareStatement(
	        		INSERT_CLASS_SQL, new String[] {"id"});
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
        Object[] args;

        if(metaClass.getId() < 1)
        {
            if(metaClass.getClassName() == null)
            {
                throw new IllegalArgumentException("Meta class does not have name or id. Can't load.");
            }

            query = SELECT_CLASS_BY_NAME;

            args = new Object[] { metaClass.getClassName() };
        }
        else
        {
            query = SELECT_CLASS_BY_ID;

            args = new Object[] {metaClass.getId()};
        }

        logger.debug(query);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, args);

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

        return metaId;
    }

    private void updateClass(MetaClass metaClass)
    {
        if(metaClass.getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have id to be updated");
        }

        String query;

        query = UPDATE_CLASS_BY_ID;

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

        for(String typeName : addNames)
        {
            IMetaType metaType = meta.getMemberType(typeName);
            Object[] args;

            if(metaType.isComplex())
            {
                if(metaType.isArray())
                {
                    MetaClassArray metaClassArray = (MetaClassArray)metaType;

                    long innerId = save(metaClassArray.getMembersType());

                    query = INSERT_COMPLEX_ARRAY;

                    args = new Object[] {meta.getId(), typeName, metaClassArray.isKey(), metaClassArray.isNullable(),
                            metaClassArray.getComplexKeyType().toString(), innerId,
                            metaClassArray.getArrayKeyType().toString()};
                }
                else
                {
                    MetaClass metaClass = (MetaClass)metaType;

                    long innerId = save(metaClass);

                    query = INSERT_COMPLEX_ATTRIBUTE;

                    args = new Object[] {meta.getId(), typeName, metaClass.isKey(), metaClass.isNullable(),
                            metaClass.getComplexKeyType().toString(), innerId};
                }
            }
            else
            {
                if(metaType.isArray())
                {
                    MetaValueArray metaValueArray = (MetaValueArray)metaType;

                    query = INSERT_SIMPLE_ARRAY;

                    args = new Object[] {meta.getId(), typeName, metaValueArray.getTypeCode().toString(),
                            metaValueArray.isKey(), metaValueArray.isNullable(),
                            metaValueArray.getArrayKeyType().toString()};
                }
                else
                {
                    MetaValue metaValue = (MetaValue)metaType;

                    query = INSERT_SIMPLE_ATTRIBUTE;

                    args = new Object[] {meta.getId(), typeName, metaValue.getTypeCode().toString(),
                            metaValue.isKey(), metaValue.isNullable()};
                }
            }

            logger.debug(query);

            jdbcTemplate.update(query, args);
        }
    }

    private void deleteAttributes(Set<String> deleteNames, MetaClass meta)
    {
        String query;
        if(meta.getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before attributes deletion to DB");
        }

        for(String typeName : deleteNames)
        {
            query = DELETE_ATTRIBUTE;

            logger.debug(query);

            jdbcTemplate.update(query, meta.getId(), typeName);
        }
    }

    public void loadAttributes(MetaClass meta) {
        if(meta.getId() < 1)
            return;

        long id = meta.getId();
        meta.removeMembers();

        //load simple attributes
        String query = SELECT_SIMPLE_ATTRIBUTES;

        logger.debug(query);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, id);
        for (Map<String, Object> row : rows) {
            MetaValue attribute = new MetaValue(
                    DataTypes.valueOf((String) row.get("type_code")),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable"));

            meta.setMemberType((String)row.get("name"), attribute);
        }

        //load simple attributes arrays
        query = SELECT_SIMPLE_ARRAY;

        logger.debug(query);

        rows = jdbcTemplate.queryForList(query, id);
        for (Map<String, Object> row : rows) {
            MetaValueArray attribute = new MetaValueArray(
                    DataTypes.valueOf((String) row.get("type_code")),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable"));

            attribute.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));

            meta.setMemberType((String)row.get("name"), attribute);
        }

        //load complex attributes
        query = SELECT_COMPLEX_ARRAY;

        logger.debug(query);

        rows = jdbcTemplate.queryForList(query, id);
        for (Map<String, Object> row : rows) {
            MetaClass attribute = load((Integer)row.get("class_id"));

            attribute.setClassName((String)row.get("cname"));
            attribute.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
            attribute.setKey((Boolean)row.get("is_key"));
            attribute.setNullable((Boolean) row.get("is_nullable"));

            meta.setMemberType((String)row.get("name"), attribute);
        }

        //load complex attributes
        query = SELECT_COMPLEX_ATTRIBUTE;

        logger.debug(query);

        rows = jdbcTemplate.queryForList(query, id);
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
	public void remove(MetaClass metaClass) {
		if(metaClass.getId() < 1)
        {
            throw new IllegalArgumentException("Can't remove MetaClass without id");
        }

        String query;

        query = DELETE_CLASS_BY_ID;

        logger.debug(query);
        jdbcTemplate.update(query, metaClass.getId());
	}
}
