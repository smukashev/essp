package kz.bsbnb.usci.eav_persistance.postgresql.dao;

import kz.bsbnb.usci.eav_model.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav_model.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav_model.model.type.DataTypes;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav_model.util.SetUtils;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav_persistance.persistance.impl.db.JDBCSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
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
    private String DELETE_ALL_ATTRIBUTES;
    private String SELECT_SIMPLE_ATTRIBUTES;
    private String SELECT_SIMPLE_ARRAY;
    private String SELECT_COMPLEX_ARRAY;
    private String SELECT_COMPLEX_ATTRIBUTE;
    private String SELECT_ARRAY_ARRAY;
    private String INSERT_ARRAY_ARRAY;

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
        INSERT_COMPLEX_ARRAY = String.format("INSERT INTO %s (containing_id, name, is_key, is_nullable, class_id, array_key_type) VALUES  ( ?, ?, ?, ?, ?, ?) ", getConfig().getComplexSetTableName());
        INSERT_COMPLEX_ATTRIBUTE = String.format("INSERT INTO %s (containing_id, name, is_key, is_nullable, class_id) VALUES  ( ?, ?, ?, ?, ? ) ", getConfig().getComplexAttributesTableName());
        INSERT_SIMPLE_ARRAY = String.format("INSERT INTO %s (containing_id, name, type_code, is_key, is_nullable, array_key_type) VALUES  ( ?, ?, ?, ?, ?, ?) ", getConfig().getSimpleSetTableName());
        INSERT_SIMPLE_ATTRIBUTE = String.format("INSERT INTO %s (containing_id, name, type_code, is_key, is_nullable) VALUES  ( ?, ?, ?, ?, ?) ", getConfig().getSimpleAttributesTableName());

        DELETE_ATTRIBUTE = String.format("DELETE FROM %s WHERE containing_id = ? AND name = ? ", getConfig().getAttributesTableName());
        DELETE_ALL_ATTRIBUTES = String.format("DELETE FROM %s WHERE containing_id = ? ", getConfig().getAttributesTableName());

        SELECT_SIMPLE_ATTRIBUTES = String.format("SELECT * FROM ONLY %s WHERE containing_id = ?", getConfig().getSimpleAttributesTableName());
        SELECT_SIMPLE_ARRAY = String.format("SELECT * FROM %s WHERE containing_id = ?", getConfig().getSimpleSetTableName());
        SELECT_ARRAY_ARRAY = String.format("SELECT * FROM %s WHERE containing_id = ?", getConfig().getSetOfSetsTableName());
        SELECT_COMPLEX_ARRAY = String.format("SELECT ca.*, c.name cname FROM %s ca LEFT JOIN %s c ON ca.class_id = c.id  WHERE containing_id = ?", getConfig().getComplexSetTableName(), getConfig().getClassesTableName());
        SELECT_COMPLEX_ATTRIBUTE = String.format("SELECT ca.*, c.name cname FROM ONLY %s ca LEFT JOIN %s c ON ca.class_id = c.id  WHERE containing_id = ?", getConfig().getComplexAttributesTableName(), getConfig().getClassesTableName());

        INSERT_ARRAY_ARRAY = String.format("INSERT INTO %s (containing_id, name, is_key, is_nullable, array_key_type) VALUES  ( ?, ?, ?, ?, ?) ", getConfig().getSetOfSetsTableName());
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
        List<Map<String, Object>> rows = queryForListWithStats(query, args);

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
        try
        {
            long metaId = insertWithId(INSERT_CLASS_SQL,
                    new Object[] {
                            metaClass.getClassName(),
                            metaClass.getComplexKeyType().toString(),
                            metaClass.getBeginDate(),
                            metaClass.isDisabled()
                    });

            if(metaId < 1)
            {
                logger.error("Can't create class");
                return 0;
            }

            metaClass.setId(metaId);

            return metaId;
        }
        catch (DuplicateKeyException e)
        {
            logger.error("Duplicate name for class: " + metaClass.getClassName());
            throw new IllegalArgumentException("Duplicate name for class: " + metaClass.getClassName());
        }
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

    private long saveSet(IMetaType type, long parentId, IMetaAttribute metaAttribute, String attributeName)
    {
        String query;
        Object[] args;
        long id;

        if(!type.isArray())
        {
            throw new IllegalStateException(attributeName + " is not an array.");
        }

        MetaSet metaSet = (MetaSet)type;

        if(metaSet.getMemberType().isArray())
        {
            query = INSERT_ARRAY_ARRAY;

            args = new Object[] {parentId, attributeName,
                    metaAttribute.isKey(), metaAttribute.isNullable(),
                    metaSet.getArrayKeyType().toString()};

            logger.debug(query);

            long t = 0;
            if(sqlStats != null)
            {
                t = System.currentTimeMillis();
            }

            id = insertWithId(query, args);
            metaSet.setId(id);

            if(sqlStats != null)
            {
                sqlStats.put(query, System.currentTimeMillis() - t);
            }

            saveSet(metaSet.getMemberType(), id, new MetaAttribute(false, false, null), "item");
        }
        else
        {
            if(metaSet.isComplex())
            {
                long innerId = save((MetaClass)metaSet.getMemberType());

                query = INSERT_COMPLEX_ARRAY;

                args = new Object[] {parentId, attributeName, metaAttribute.isKey(), metaAttribute.isNullable(),
                        innerId, metaSet.getArrayKeyType().toString()};
            }
            else
            {
                query = INSERT_SIMPLE_ARRAY;

                args = new Object[] {parentId, attributeName, metaSet.getTypeCode().toString(),
                        metaAttribute.isKey(), metaAttribute.isNullable(),
                        metaSet.getArrayKeyType().toString()};
            }

            logger.debug(query);

            long t = 0;
            if(sqlStats != null)
            {
                t = System.currentTimeMillis();
            }

            id = insertWithId(query, args);
            metaSet.setId(id);

            if(sqlStats != null)
            {
                sqlStats.put(query, System.currentTimeMillis() - t);
            }
        }

        return id;
    }

    private long saveAttribute(IMetaType type, long parentId, IMetaAttribute metaAttribute, String attributeName)
    {
        String query;
        Object[] args;

        if(type.isArray())
        {
            throw new IllegalStateException(attributeName + " is an array, single value expected.");
        }

        if(type.isComplex())
        {
            long innerId = save((MetaClass)type);

            query = INSERT_COMPLEX_ATTRIBUTE;

            args = new Object[] {parentId, attributeName, metaAttribute.isKey(), metaAttribute.isNullable(),
                    innerId};
        }
        else
        {
            query = INSERT_SIMPLE_ATTRIBUTE;

            args = new Object[] {parentId, attributeName, ((MetaValue)type).getTypeCode().toString(),
                    metaAttribute.isKey(), metaAttribute.isNullable()};
        }

        logger.debug(query);

        long t = 0;
        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }

        long id = insertWithId(query, args);

        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }

        return id;
    }

    private void insertAttributes(Set<String> addNames, MetaClass meta)
    {
        if(meta.getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before attributes insertion to DB");
        }

        for(String typeName : addNames)
        {
            IMetaAttribute metaAttribute = meta.getMetaAttribute(typeName);
            IMetaType metaType = metaAttribute.getMetaType();

            if(metaType.isArray())
            {
                saveSet(metaType, meta.getId(), metaAttribute, typeName);
            }
            else
            {
                saveAttribute(metaType, meta.getId(), metaAttribute, typeName);
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

    void loadSimpleAttributes(IMetaContainer meta)
    {
        String query = SELECT_SIMPLE_ATTRIBUTES;

        logger.debug(query);

        long t = 0;
        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, meta.getId());
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
    }

    void loadSimpleArrays(IMetaContainer meta)
    {
        String query = SELECT_SIMPLE_ARRAY;
        long t = 0;

        logger.debug(query);

        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        List<Map<String, Object>>  rows = jdbcTemplate.queryForList(query, meta.getId());
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
            metaSet.setId(((Integer)row.get("id")).longValue());

            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));

            metaAttribute.setMetaType(metaSet);

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);


        }
    }

    void loadComplexAttributes(IMetaContainer meta)
    {
        String query = SELECT_COMPLEX_ATTRIBUTE;
        long t = 0;

        logger.debug(query);

        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, meta.getId());
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
    }

    void loadComplexArrays(IMetaContainer meta)
    {
        String query = SELECT_COMPLEX_ARRAY;
        long t = 0;

        logger.debug(query);

        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, meta.getId());
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
            metaSet.setId(((Integer)row.get("id")).longValue());
            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));

            metaAttribute.setMetaType(metaSet);

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);
        }
    }

    void loadArrayArrays(IMetaContainer meta)
    {
        String query = SELECT_ARRAY_ARRAY;
        long t = 0;

        logger.debug(query);

        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        List<Map<String, Object>>  rows = jdbcTemplate.queryForList(query, meta.getId());
        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }

        for (Map<String, Object> row : rows) {

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((Integer)row.get("id")).longValue(),
                    (Boolean)row.get("is_key"),
                    (Boolean)row.get("is_nullable"));



            MetaSet metaSet = new MetaSet();

            metaSet.setId(((Integer)row.get("id")).longValue());
            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));

            loadSimpleArrays(metaSet);
            loadComplexArrays(metaSet);
            loadArrayArrays(metaSet);

            metaAttribute.setMetaType(metaSet);

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);


        }
    }

    void loadAttributes(MetaClass meta) {
        if(meta.getId() < 1)
        {
            throw new IllegalStateException("Can't load atributes of metaclass without id!");
        }

        meta.removeMembers();

        loadSimpleAttributes(meta);
        loadSimpleArrays(meta);
        loadComplexAttributes(meta);
        loadComplexArrays(meta);
        loadArrayArrays(meta);
    }

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
	    	throw new IllegalArgumentException("Can't determine meta id");
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

    private void removeAllAttributes(long id)
    {
        String query = DELETE_ALL_ATTRIBUTES;

        logger.debug(query);

        long t = 0;
        if(sqlStats != null)
        {
            t = System.currentTimeMillis();
        }
        jdbcTemplate.update(query, id);
        if(sqlStats != null)
        {
            sqlStats.put(query, System.currentTimeMillis() - t);
        }
    }

    private void removeSet(MetaSet set)
    {
        if (set.getMemberType().isArray())
        {
            removeSet((MetaSet)set.getMemberType());
            removeAllAttributes(set.getId());
        }
        else
        {
            if (set.getMemberType().isComplex())
            {
                remove((MetaClass)set.getMemberType());
            }
        }
    }

	@Override
    @Transactional
	public void remove(MetaClass metaClass) {
		if(metaClass.getId() < 1)
        {
            throw new IllegalArgumentException("Can't remove MetaClass without id");
        }

        String query;
        long t = 0;

        for (String arrayName : metaClass.getArrayArrayAttributesNames())
        {
            removeSet(((MetaSet)metaClass.getMemberType(arrayName)));
        }

        //delete all class attributes
        removeAllAttributes(metaClass.getId());

        //delete class
        query = DELETE_CLASS_BY_ID;

        logger.debug(query);

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
}
