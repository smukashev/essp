package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaAttribute;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
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
	private final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaClassDaoImpl.class);

    private String INSERT_CLASS_SQL;
    private String SELECT_CLASS_BY_NAME;
    private String SELECT_CLASS_BY_NAME_STRICT;
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
        INSERT_CLASS_SQL = String.format("INSERT INTO %s (name, complex_key_type, begin_date, is_disabled) VALUES ( ?, ?, ?, ? )", getConfig().getClassesTableName());
        // SELECT_CLASS_BY_NAME = String.format("SELECT * FROM %s WHERE name = ? and begin_date <= ? and is_disabled = FALSE ORDER BY begin_date DESC LIMIT 1", getConfig().getClassesTableName());
        SELECT_CLASS_BY_NAME = String.format("SELECT * FROM %s WHERE name = ? and begin_date <= ? ORDER BY begin_date DESC LIMIT 1", getConfig().getClassesTableName());
        SELECT_CLASS_BY_NAME_STRICT = String.format("SELECT * FROM %s WHERE name = ? and begin_date = ? ORDER BY begin_date DESC LIMIT 1", getConfig().getClassesTableName());
        SELECT_CLASS_BY_ID = String.format("SELECT * FROM %s WHERE id = ?", getConfig().getClassesTableName());
        UPDATE_CLASS_BY_ID = String.format("UPDATE %s SET  name = ?,  complex_key_type = ?, begin_date = ?,  is_disabled = ?  WHERE id = ?", getConfig().getClassesTableName());
        DELETE_CLASS_BY_ID = String.format("DELETE FROM %s WHERE id = ?", getConfig().getClassesTableName());
        INSERT_COMPLEX_ARRAY = String.format("INSERT INTO %s (containing_class_id, name, is_key, is_nullable, class_id, array_key_type) VALUES  ( ?, ?, ?, ?, ?, ?) ", getConfig().getComplexArrayTableName());
        INSERT_COMPLEX_ATTRIBUTE = String.format("INSERT INTO %s (containing_class_id, name, is_key, is_nullable, class_id) VALUES  ( ?, ?, ?, ?, ? ) ", getConfig().getComplexAttributesTableName());
        INSERT_SIMPLE_ARRAY = String.format("INSERT INTO %s (containing_class_id, name, type_code, is_key, is_nullable, array_key_type) VALUES  ( ?, ?, ?, ?, ?, ?) ", getConfig().getSimpleArrayTableName());
        INSERT_SIMPLE_ATTRIBUTE = String.format("INSERT INTO %s (containing_class_id, name, type_code, is_key, is_nullable) VALUES  ( ?, ?, ?, ?, ?) ", getConfig().getSimpleAttributesTableName());
        DELETE_ATTRIBUTE = String.format("DELETE FROM %s WHERE containing_class_id = ? AND name = ? ", getConfig().getAttributesTableName());
        SELECT_SIMPLE_ATTRIBUTES = String.format("SELECT * FROM ONLY %s WHERE containing_class_id = ?", getConfig().getSimpleAttributesTableName());
        SELECT_SIMPLE_ARRAY = String.format("SELECT * FROM %s WHERE containing_class_id = ?", getConfig().getSimpleArrayTableName());
        SELECT_COMPLEX_ARRAY = String.format("SELECT ca.*, c.name cname FROM %s ca LEFT JOIN %s c ON ca.class_id = c.id  WHERE containing_class_id = ?", getConfig().getComplexArrayTableName(), getConfig().getClassesTableName());
        SELECT_COMPLEX_ATTRIBUTE = String.format("SELECT ca.*, c.name cname FROM ONLY %s ca LEFT JOIN %s c ON ca.class_id = c.id  WHERE containing_class_id = ?", getConfig().getComplexAttributesTableName(), getConfig().getClassesTableName());
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
            ps.setString(2, metaClass.getComplexKeyType().toString());
            ps.setTimestamp(3, metaClass.getBeginDate());
            ps.setBoolean(4, metaClass.isDisabled());
	        
	        logger.debug(ps.toString());
	        
	        return ps;
	    }
	}

    private void loadClass(MetaClass metaClass, boolean beginDateStrict)
    {
        String query;
        Object[] args;

        if(metaClass.getId() < 1)
        {
            if(metaClass.getClassName() == null)
                throw new IllegalArgumentException("Meta class does not have name or id. Can't load.");

            if(beginDateStrict)
            {
                query = SELECT_CLASS_BY_NAME_STRICT;
            }
            else
            {
                query = SELECT_CLASS_BY_NAME;
            }

            args = new Object[] { metaClass.getClassName(), metaClass.getBeginDate() };
        }
        else
        {
            query = SELECT_CLASS_BY_ID;

            args = new Object[] { metaClass.getId() };
        }

        logger.debug(query);

        long t = 0;
        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, args);
        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }

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
            metaClass.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
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

        Object[] args = {
                metaClass.getClassName(),
                metaClass.getComplexKeyType().toString(),
                metaClass.getBeginDate(),
                metaClass.isDisabled(),
                metaClass.getId()
        };

        logger.debug(query);

        long t = 0;
        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        jdbcTemplate.update(query, args);
        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }
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
            IMetaAttribute metaAttribute = meta.getMetaAttribute(typeName);
            Object[] args;

            IMetaType metaType = metaAttribute.getMetaType();

            if(metaType.isComplex())
            {
                if(metaType.isArray())
                {
                    MetaSet metaSet = (MetaSet)metaType;

                    long innerId = save((MetaClass)metaSet.getMemberType());

                    query = INSERT_COMPLEX_ARRAY;

                    args = new Object[] {meta.getId(), typeName, metaAttribute.isKey(), metaAttribute.isNullable(),
                            innerId, metaSet.getArrayKeyType().toString()};
                }
                else
                {
                    MetaClass metaClass = (MetaClass)metaType;

                    long innerId = save(metaClass);

                    query = INSERT_COMPLEX_ATTRIBUTE;

                    args = new Object[] {meta.getId(), typeName, metaAttribute.isKey(), metaAttribute.isNullable(),
                            innerId};
                }
            }
            else
            {
                if(metaType.isArray())
                {
                    MetaSet metaValueArray = (MetaSet)metaType;

                    query = INSERT_SIMPLE_ARRAY;

                    args = new Object[] {meta.getId(), typeName, metaValueArray.getTypeCode().toString(),
                            metaAttribute.isKey(), metaAttribute.isNullable(),
                            metaValueArray.getArrayKeyType().toString()};
                }
                else
                {
                    MetaValue metaValue = (MetaValue)metaType;

                    query = INSERT_SIMPLE_ATTRIBUTE;

                    args = new Object[] {meta.getId(), typeName, metaValue.getTypeCode().toString(),
                            metaAttribute.isKey(), metaAttribute.isNullable()};
                }
            }

            logger.debug(query);

            long t = 0;
            if(sqlStats != null)
            {
                t = System.currentTimeMillis();
            }
            jdbcTemplate.update(query, args);
            if(sqlStats != null)
            {
                sqlStats.put(query, System.currentTimeMillis() - t);
            }
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

            long t = 0;
            if(sqlStats != null)
            {
                t = System.currentTimeMillis();
            }
            jdbcTemplate.update(query, meta.getId(), typeName);
            if(sqlStats != null)
            {
                sqlStats.put(query, System.currentTimeMillis() - t);
            }
        }
    }

    void loadAttributes(MetaClass meta) {
        if(meta.getId() < 1)
            return;

        long id = meta.getId();
        meta.removeMembers();

        //load simple attributes
        String query = SELECT_SIMPLE_ATTRIBUTES;

        logger.debug(query);

        long t = 0;
        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, id);
        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }

        for (Map<String, Object> row : rows) {
            MetaAttribute metaAttirubute = new MetaAttribute(
                    ((Integer)row.get("id")).longValue(),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable"));

            metaAttirubute.setMetaType(new MetaValue(DataTypes.valueOf((String) row.get("type_code"))));

            meta.setMetaAttribute((String) row.get("name"), metaAttirubute);
        }

        //load simple attributes arrays
        query = SELECT_SIMPLE_ARRAY;

        logger.debug(query);

        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        rows = jdbcTemplate.queryForList(query, id);
        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }

        for (Map<String, Object> row : rows) {

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((Integer)row.get("id")).longValue(),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable"));


            MetaSet metaSet = new MetaSet(new MetaValue(DataTypes.valueOf((String) row.get("type_code"))));

            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));

            metaAttribute.setMetaType(metaSet);

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);


        }

        //load complex attributes
        query = SELECT_COMPLEX_ATTRIBUTE;

        logger.debug(query);

        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        rows = jdbcTemplate.queryForList(query, id);
        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }

        for (Map<String, Object> row : rows) {
            MetaClass metaClass = load((Integer)row.get("class_id"));

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((Integer)row.get("id")).longValue(),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable"));

            metaAttribute.setMetaType(metaClass);

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);
        }

        //load complex attributes array
        query = SELECT_COMPLEX_ARRAY;

        logger.debug(query);

        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        rows = jdbcTemplate.queryForList(query, id);
        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }

        for (Map<String, Object> row : rows) {
            MetaClass metaClass = load((Integer)row.get("class_id"));

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((Integer)row.get("id")).longValue(),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable"));

            MetaSet metaSet = new MetaSet(metaClass);
            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));

            metaAttribute.setMetaType(metaSet);

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);
        }
    }

    //TODO: addToArray active period to classes
	@Transactional
	public long save(MetaClass meta) {
        MetaClass dbMeta = new MetaClass(meta);

        try
        {
            loadClass(dbMeta, true);
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
            logger.debug("Class: " + meta.getClassName() + " created.");
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

        loadClass(meta, false);
        loadAttributes(meta);

	    return meta;
	}

    @Override
    public MetaClass load(String className, Timestamp beginDate) {
        MetaClass meta = new MetaClass(className, beginDate);

        loadClass(meta, false);
        loadAttributes(meta);

        return meta;
    }

	@Override
	public MetaClass load(long id) {
        if(id < 1)
            return null;

        MetaClass meta = new MetaClass();
        meta.setId(id);

        loadClass(meta, false);
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

        long t = 0;
        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        jdbcTemplate.update(query, metaClass.getId());
        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }
	}

    public SQLQueriesStats getSqlStats() {
        return sqlStats;
    }

    public void setSqlStats(SQLQueriesStats sqlStats) {
        this.sqlStats = sqlStats;
    }
}
