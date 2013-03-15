package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.eav.model.base.ContainerTypes;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.jooq.InsertOnDuplicateStep;
import org.jooq.SelectForUpdateStep;
import org.jooq.impl.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_CLASSES;

@Repository
public class PostgreSQLMetaClassDaoImpl extends JDBCSupport implements IMetaClassDao {
	private final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaClassDaoImpl.class);

    private String UPDATE_CLASS_BY_ID;
    private String DELETE_CLASS_BY_ID;
    private String INSERT_COMPLEX_ARRAY;
    private String INSERT_COMPLEX_ATTRIBUTE;
    private String INSERT_SIMPLE_ARRAY;
    private String INSERT_SIMPLE_ATTRIBUTE;
    private String DELETE_SIMPLE_ATTRIBUTE;
    private String DELETE_COMPLEX_ATTRIBUTE;
    private String DELETE_ALL_SIMPLE_ATTRIBUTES;
    private String DELETE_ALL_COMPLEX_ATTRIBUTES;
    private String SELECT_SIMPLE_ATTRIBUTES;
    private String SELECT_SIMPLE_ARRAY;
    private String SELECT_COMPLEX_ARRAY;
    private String SELECT_COMPLEX_ATTRIBUTE;
    private String SELECT_ARRAY_ARRAY;
    private String INSERT_ARRAY_ARRAY;

    @Autowired
    private Executor sqlGenerator;

    @PostConstruct
    public void init()
    {
        UPDATE_CLASS_BY_ID = String.format("UPDATE %s SET  name = ?,  complex_key_type = ?, begin_date = ?,  is_disabled = ?  WHERE id = ?", getConfig().getClassesTableName());
        DELETE_CLASS_BY_ID = String.format("DELETE FROM %s WHERE id = ?", getConfig().getClassesTableName());
        INSERT_COMPLEX_ARRAY = String.format("INSERT INTO %s (containing_id, container_type, name, is_key, is_nullable, class_id, array_key_type) VALUES  ( ?, ?, ?, ?, ?, ?, ?) ", getConfig().getComplexSetTableName());
        INSERT_COMPLEX_ATTRIBUTE = String.format("INSERT INTO %s (containing_id, container_type, name, is_key, is_nullable, class_id) VALUES  ( ?, ?, ?, ?, ?, ? ) ", getConfig().getComplexAttributesTableName());
        INSERT_SIMPLE_ARRAY = String.format("INSERT INTO %s (containing_id, container_type, name, type_code, is_key, is_nullable, array_key_type) VALUES  ( ?, ?, ?, ?, ?, ?, ?) ", getConfig().getSimpleSetTableName());
        INSERT_SIMPLE_ATTRIBUTE = String.format("INSERT INTO %s (containing_id, container_type, name, type_code, is_key, is_nullable) VALUES  ( ?, ?, ?, ?, ?, ?) ", getConfig().getSimpleAttributesTableName());

        DELETE_SIMPLE_ATTRIBUTE = String.format("DELETE FROM %s WHERE containing_id = ? AND container_type = ? AND name = ? ", getConfig().getSimpleAttributesTableName());
        DELETE_COMPLEX_ATTRIBUTE = String.format("DELETE FROM %s WHERE containing_id = ? AND container_type = ? AND name = ? ", getConfig().getComplexAttributesTableName());

        DELETE_ALL_SIMPLE_ATTRIBUTES = String.format("DELETE FROM %s WHERE containing_id = ? AND container_type = ? ", getConfig().getSimpleAttributesTableName());
        DELETE_ALL_COMPLEX_ATTRIBUTES = String.format("DELETE FROM %s WHERE containing_id = ? AND container_type = ? ", getConfig().getComplexAttributesTableName());

        SELECT_SIMPLE_ATTRIBUTES = String.format("SELECT * FROM ONLY %s WHERE containing_id = ? AND container_type = ? ", getConfig().getSimpleAttributesTableName());
        SELECT_SIMPLE_ARRAY = String.format("SELECT * FROM %s WHERE containing_id = ? AND container_type = ? ", getConfig().getSimpleSetTableName());
        SELECT_ARRAY_ARRAY = String.format("SELECT * FROM %s WHERE containing_id = ? AND container_type = ? ", getConfig().getSetOfSetsTableName());
        SELECT_COMPLEX_ARRAY = String.format("SELECT ca.*, c.name cname FROM %s ca LEFT JOIN %s c ON ca.class_id = c.id  WHERE containing_id = ? AND container_type = ? ", getConfig().getComplexSetTableName(), getConfig().getClassesTableName());
        SELECT_COMPLEX_ATTRIBUTE = String.format("SELECT ca.*, c.name cname FROM ONLY %s ca LEFT JOIN %s c ON ca.class_id = c.id  WHERE containing_id = ? AND container_type = ? ", getConfig().getComplexAttributesTableName(), getConfig().getClassesTableName());

        INSERT_ARRAY_ARRAY = String.format("INSERT INTO %s (containing_id, container_type, name, is_key, is_nullable, array_key_type) VALUES  ( ?, ?, ?, ?, ?, ?) ", getConfig().getSetOfSetsTableName());
    }
	
    private void loadClass(MetaClass metaClass, boolean beginDateStrict)
    {
        //String query;
        SelectForUpdateStep select;

        if(metaClass.getId() < 1)
        {
            if(metaClass.getClassName() == null)
                throw new IllegalArgumentException("Meta class does not have name or id. Can't load.");

            if(beginDateStrict)
            {
                select = sqlGenerator.select(
                        EAV_CLASSES.IS_DISABLED,
                        EAV_CLASSES.BEGIN_DATE,
                        EAV_CLASSES.ID,
                        EAV_CLASSES.NAME,
                        EAV_CLASSES.COMPLEX_KEY_TYPE
                ).from(EAV_CLASSES).
                where(
                    EAV_CLASSES.NAME.equal(metaClass.getClassName())
                ).and(
                    EAV_CLASSES.BEGIN_DATE.eq(metaClass.getBeginDate())
                ).and(
                    EAV_CLASSES.IS_DISABLED.equal(false)
                ).orderBy(EAV_CLASSES.BEGIN_DATE.desc()).limit(1).offset(0);
            }
            else
            {
                select = sqlGenerator.select(
                        EAV_CLASSES.IS_DISABLED,
                        EAV_CLASSES.BEGIN_DATE,
                        EAV_CLASSES.ID,
                        EAV_CLASSES.NAME,
                        EAV_CLASSES.COMPLEX_KEY_TYPE
                    ).from(EAV_CLASSES).
                    where(
                        EAV_CLASSES.NAME.equal(metaClass.getClassName())
                    ).and(
                        EAV_CLASSES.BEGIN_DATE.le(metaClass.getBeginDate())
                    ).and(
                        EAV_CLASSES.IS_DISABLED.equal(false)
                    ).orderBy(EAV_CLASSES.BEGIN_DATE.desc()).limit(1).offset(0);
            }

        }
        else
        {
            //todo: refactor cast
            select = sqlGenerator.select(
                    EAV_CLASSES.IS_DISABLED,
                    EAV_CLASSES.BEGIN_DATE,
                    EAV_CLASSES.ID,
                    EAV_CLASSES.NAME,
                    EAV_CLASSES.COMPLEX_KEY_TYPE
                ).from(EAV_CLASSES).
                where(
                    EAV_CLASSES.ID.equal((int) metaClass.getId())
                ).limit(1).offset(0);
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

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
            InsertOnDuplicateStep insert = sqlGenerator.insertInto(
                    EAV_CLASSES,
                    EAV_CLASSES.NAME,
                    EAV_CLASSES.COMPLEX_KEY_TYPE,
                    EAV_CLASSES.BEGIN_DATE,
                    EAV_CLASSES.IS_DISABLED
            ).values(metaClass.getClassName(),
                    metaClass.getComplexKeyType().toString(),
                    metaClass.getBeginDate(),
                    metaClass.isDisabled());

            logger.debug(insert.toString());
            long metaId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

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
            t = System.nanoTime();
        }
        jdbcTemplate.update(query, args);
        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
        }
    }

    private long saveSet(IMetaType type, long parentId, int parentType, IMetaAttribute metaAttribute, String attributeName)
    {
        String query;
        Object[] args;
        long id;

        if(!type.isSet())
        {
            throw new IllegalStateException(attributeName + " is not an array.");
        }

        MetaSet metaSet = (MetaSet)type;

        if(metaSet.getMemberType().isSet())
        {
            query = INSERT_ARRAY_ARRAY;

            args = new Object[] {parentId, parentType, attributeName,
                    metaAttribute.isKey(), metaAttribute.isNullable(),
                    metaSet.getArrayKeyType().toString()};

            logger.debug(query);

            long t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }

            id = insertWithId(query, args);
            metaSet.setId(id);

            if(sqlStats != null)
            {
                sqlStats.put(query, (System.nanoTime() - t) / 1000000);
            }

            saveSet(metaSet.getMemberType(), id, ContainerTypes.SET, new MetaAttribute(false, false, null), "item");
        }
        else
        {
            if(metaSet.isComplex())
            {
                long innerId = save((MetaClass)metaSet.getMemberType());

                query = INSERT_COMPLEX_ARRAY;

                args = new Object[] {parentId, parentType, attributeName, metaAttribute.isKey(), metaAttribute.isNullable(),
                        innerId, metaSet.getArrayKeyType().toString()};
            }
            else
            {
                query = INSERT_SIMPLE_ARRAY;

                args = new Object[] {parentId, parentType, attributeName, metaSet.getTypeCode().toString(),
                        metaAttribute.isKey(), metaAttribute.isNullable(),
                        metaSet.getArrayKeyType().toString()};
            }

            logger.debug(query);

            long t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }

            id = insertWithId(query, args);
            metaSet.setId(id);

            if(sqlStats != null)
            {
                sqlStats.put(query, (System.nanoTime() - t) / 1000000);
            }
        }

        return id;
    }

    private long saveAttribute(IMetaType type, long parentId, int parentType, IMetaAttribute metaAttribute, String attributeName)
    {
        String query;
        Object[] args;

        if(type.isSet())
        {
            throw new IllegalStateException(attributeName + " is an array, single value expected.");
        }

        if(type.isComplex())
        {
            long innerId = save((MetaClass)type);

            query = INSERT_COMPLEX_ATTRIBUTE;

            args = new Object[] {parentId, parentType, attributeName, metaAttribute.isKey(), metaAttribute.isNullable(),
                    innerId};
        }
        else
        {
            query = INSERT_SIMPLE_ATTRIBUTE;

            args = new Object[] {parentId, parentType, attributeName, ((MetaValue)type).getTypeCode().toString(),
                    metaAttribute.isKey(), metaAttribute.isNullable()};
        }

        logger.debug(query);

        long t = 0;
        if(sqlStats != null)
        {
            t = System.nanoTime();
        }

        long id = insertWithId(query, args);

        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
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

            if(metaType.isSet())
            {
                saveSet(metaType, meta.getId(), ContainerTypes.CLASS, metaAttribute, typeName);
            }
            else
            {
                saveAttribute(metaType, meta.getId(), ContainerTypes.CLASS, metaAttribute, typeName);
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
            if(meta.getMemberType(typeName).isComplex())
            {
                query = DELETE_COMPLEX_ATTRIBUTE;
            }
            else
            {
                query = DELETE_SIMPLE_ATTRIBUTE;
            }

            logger.debug(query);

            long t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }
            jdbcTemplate.update(query, meta.getId(), ContainerTypes.CLASS, typeName);
            if(sqlStats != null)
            {
                sqlStats.put(query, (System.nanoTime() - t) / 1000000);
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
            t = System.nanoTime();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, meta.getId(), meta.getType());
        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
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
            t = System.nanoTime();
        }
        List<Map<String, Object>>  rows = jdbcTemplate.queryForList(query, meta.getId(), meta.getType());
        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
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
            t = System.nanoTime();
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, meta.getId(), meta.getType());
        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
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
            t = System.nanoTime();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, meta.getId(), meta.getType());
        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
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
            t = System.nanoTime();
        }
        List<Map<String, Object>>  rows = jdbcTemplate.queryForList(query, meta.getId(), meta.getType());
        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
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

    private void removeAllAttributes(long id, int type)
    {
        String query = DELETE_ALL_SIMPLE_ATTRIBUTES;

        logger.debug(query);

        long t = 0;
        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        jdbcTemplate.update(query, id, type);
        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
        }

        query = DELETE_ALL_COMPLEX_ATTRIBUTES;

        logger.debug(query);

        t = 0;
        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        jdbcTemplate.update(query, id, type);
        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
        }
    }

    private void removeSet(MetaSet set)
    {
        if (set.getMemberType().isSet())
        {
            removeSet((MetaSet)set.getMemberType());
            removeAllAttributes(set.getId(), ContainerTypes.SET);
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
            removeSet(((MetaSet) metaClass.getMemberType(arrayName)));
        }

        //delete all class attributes
        removeAllAttributes(metaClass.getId(), ContainerTypes.CLASS);

        //delete class
        query = DELETE_CLASS_BY_ID;

        logger.debug(query);

        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        jdbcTemplate.update(query, metaClass.getId());
        if(sqlStats != null)
        {
            sqlStats.put(query, (System.nanoTime() - t) / 1000000);
        }
	}

    public Executor getSqlGenerator()
    {
        return sqlGenerator;
    }

    public void setSqlGenerator(Executor sqlGenerator)
    {
        this.sqlGenerator = sqlGenerator;
    }
}
