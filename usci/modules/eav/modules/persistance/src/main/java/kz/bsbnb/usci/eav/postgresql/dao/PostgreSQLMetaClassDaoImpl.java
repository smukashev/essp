package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.eav.model.base.ContainerTypes;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

//TODO: refactor all id casts!
//TODO: refactor all field names. Get field names from generated classes.

@Repository
public class PostgreSQLMetaClassDaoImpl extends JDBCSupport implements IMetaClassDao {
	private final Logger logger = LoggerFactory.getLogger(PostgreSQLMetaClassDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    private void loadAllClasses(List<MetaClass> metaClassList){
        SelectForUpdateStep select;



                select = context.select(
                        EAV_M_CLASSES.IS_DISABLED,
                        EAV_M_CLASSES.BEGIN_DATE,
                        EAV_M_CLASSES.ID,
                        EAV_M_CLASSES.NAME,
                        EAV_M_CLASSES.TITLE,
                        EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                        EAV_M_CLASSES.IS_IMMUTABLE,
                        EAV_M_CLASSES.IS_REFERENCE
                ).from(EAV_M_CLASSES).
                  orderBy(EAV_M_CLASSES.BEGIN_DATE.desc());


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());


        if (rows.size() < 1)
            return;//throw new IllegalArgumentException("Classes not found.");

        for (Map<String, Object> row : rows){

            MetaClass metaClass = new MetaClass();

            metaClass.setDisabled(((BigDecimal)row.get("is_disabled")).longValue() == 1);
            metaClass.setBeginDate(DataUtils.convert((Timestamp)row.get("begin_date")));
            metaClass.setId(((BigDecimal)row.get("id")).longValue());
            metaClass.setClassName((String)row.get("name"));
            metaClass.setClassTitle((String)row.get("title"));
            metaClass.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
            metaClass.setImmutable(((BigDecimal)row.get("is_immutable")).longValue() == 1);
            metaClass.setReference(((BigDecimal)row.get("is_reference")).longValue() == 1);
            loadAttributes(metaClass);
            metaClassList.add(metaClass);
        }
    }

    public List<MetaClass> loadAll(){

        List<MetaClass> metaClassList = new ArrayList<MetaClass>();
        loadAllClasses(metaClassList);

      return metaClassList;
    }

    private void loadClass(MetaClass metaClass, boolean beginDateStrict)
    {
        SelectForUpdateStep select;

        if(metaClass.getId() < 1)
        {
            if(metaClass.getClassName() == null)
                throw new IllegalArgumentException("Meta class does not have name or id. Can't load.");

            if(beginDateStrict)
            {
                select = context.select(
                        EAV_M_CLASSES.IS_DISABLED,
                        EAV_M_CLASSES.BEGIN_DATE,
                        EAV_M_CLASSES.ID,
                        EAV_M_CLASSES.NAME,
                        EAV_M_CLASSES.TITLE,
                        EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                        EAV_M_CLASSES.IS_IMMUTABLE,
                        EAV_M_CLASSES.IS_REFERENCE
                    ).from(EAV_M_CLASSES).
                    where(
                            EAV_M_CLASSES.NAME.equal(metaClass.getClassName())
                    ).and(
                        EAV_M_CLASSES.BEGIN_DATE.eq(DataUtils.convert(metaClass.getBeginDate()))
                ).and(
                        EAV_M_CLASSES.IS_DISABLED.equal(DataUtils.convert(false))
                ).orderBy(EAV_M_CLASSES.BEGIN_DATE.desc()).limit(1).offset(0);
            }
            else
            {
                select = context.select(
                        EAV_M_CLASSES.IS_DISABLED,
                        EAV_M_CLASSES.BEGIN_DATE,
                        EAV_M_CLASSES.ID,
                        EAV_M_CLASSES.NAME,
                        EAV_M_CLASSES.TITLE,
                        EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                        EAV_M_CLASSES.IS_IMMUTABLE,
                        EAV_M_CLASSES.IS_REFERENCE
                    ).from(EAV_M_CLASSES).
                    where(
                        EAV_M_CLASSES.NAME.equal(metaClass.getClassName())
                    ).and(
                        EAV_M_CLASSES.BEGIN_DATE.le(DataUtils.convert(metaClass.getBeginDate()))
                ).and(
                        EAV_M_CLASSES.IS_DISABLED.equal(DataUtils.convert(false))
                ).orderBy(EAV_M_CLASSES.BEGIN_DATE.desc()).limit(1).offset(0);
            }

        }
        else
        {
            select = context.select(
                    EAV_M_CLASSES.IS_DISABLED,
                    EAV_M_CLASSES.BEGIN_DATE,
                    EAV_M_CLASSES.ID,
                    EAV_M_CLASSES.NAME,
                    EAV_M_CLASSES.TITLE,
                    EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                    EAV_M_CLASSES.IS_IMMUTABLE,
                    EAV_M_CLASSES.IS_REFERENCE
                ).from(EAV_M_CLASSES).
                where(
                    EAV_M_CLASSES.ID.equal(metaClass.getId())
                ).limit(1).offset(0);
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalArgumentException("More then one class found. Can't load class : "
                    + metaClass.getClassName());

        if (rows.size() < 1)
            throw new IllegalArgumentException("Class not found. Can't load class : "
                    + metaClass.getClassName());

        Map<String, Object> row = rows.get(0);

        if(row != null) {
            metaClass.setDisabled(((BigDecimal)row.get("is_disabled")).longValue() == 1);
            metaClass.setBeginDate(DataUtils.convert((Timestamp)row.get("begin_date")));
            metaClass.setId(((BigDecimal)row.get("id")).longValue());
            metaClass.setClassName((String)row.get("name"));
            metaClass.setClassTitle((String) row.get("title"));
            metaClass.setComplexKeyType(ComplexKeyTypes.valueOf((String)row.get("complex_key_type")));
            metaClass.setImmutable(((BigDecimal)row.get("is_immutable")).longValue() == 1);
            metaClass.setReference(((BigDecimal)row.get("is_reference")).longValue() == 1);
        } else {
            logger.error("Can't load metaClass, empty data set.");
        }
    }
	
	private long createClass(MetaClass metaClass)
    {
        try
        {
            InsertOnDuplicateStep insert = context.insertInto(
                    EAV_M_CLASSES,
                    EAV_M_CLASSES.NAME,
                    EAV_M_CLASSES.TITLE,
                    EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                    EAV_M_CLASSES.BEGIN_DATE,
                    EAV_M_CLASSES.IS_DISABLED,
                    EAV_M_CLASSES.IS_IMMUTABLE,
                    EAV_M_CLASSES.IS_REFERENCE
                ).values(metaClass.getClassName(), metaClass.getClassTitle(),
                    metaClass.getComplexKeyType().toString(),
                    DataUtils.convert(metaClass.getBeginDate()),
                    DataUtils.convert(metaClass.isDisabled()),
                    DataUtils.convert(metaClass.isImmutable()),
                    DataUtils.convert(metaClass.isReference()));

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

        System.out.println("########################");
        System.out.println(metaClass.getClassName());
        System.out.println(metaClass.getClassTitle());
        System.out.println("########################");

        UpdateConditionStep update = context.update(EAV_M_CLASSES
            ).set(EAV_M_CLASSES.NAME, metaClass.getClassName()
            ).set(EAV_M_CLASSES.TITLE, metaClass.getClassTitle()
            ).set(EAV_M_CLASSES.COMPLEX_KEY_TYPE, metaClass.getComplexKeyType().toString()
            ).set(EAV_M_CLASSES.BEGIN_DATE, DataUtils.convert(metaClass.getBeginDate())
            ).set(EAV_M_CLASSES.IS_DISABLED, DataUtils.convert(metaClass.isDisabled())
            ).set(EAV_M_CLASSES.IS_REFERENCE, DataUtils.convert(metaClass.isReference())
            ).set(EAV_M_CLASSES.IS_IMMUTABLE, DataUtils.convert(metaClass.isImmutable())
            ).where(EAV_M_CLASSES.ID.eq(metaClass.getId()));

        logger.debug(update.toString());

        long t = 0;
        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        jdbcTemplate.update(update.getSQL(), update.getBindValues().toArray());
        if(sqlStats != null)
        {
            sqlStats.put(update.getSQL(), (System.nanoTime() - t) / 1000000);
        }
    }

    private long saveSet(IMetaType type, long parentId, int parentType, IMetaAttribute metaAttribute, String attributeName)
    {
        InsertOnDuplicateStep insert;
        long id;

        if(!type.isSet())
        {
            throw new IllegalStateException(attributeName + " is not an array.");
        }

        MetaSet metaSet = (MetaSet)type;

        if(metaSet.getMemberType().isSet())
        {
            insert = context.insertInto(
                    EAV_M_SET_OF_SETS,
                    EAV_M_SET_OF_SETS.CONTAINING_ID,
                    EAV_M_SET_OF_SETS.CONTAINER_TYPE,
                    EAV_M_SET_OF_SETS.NAME,
                    EAV_M_SET_OF_SETS.TITLE,
                    EAV_M_SET_OF_SETS.IS_KEY,
                    EAV_M_SET_OF_SETS.IS_NULLABLE,
                    EAV_M_SET_OF_SETS.ARRAY_KEY_TYPE,
                    EAV_M_SET_OF_SETS.IS_IMMUTABLE,
                    EAV_M_SET_OF_SETS.IS_REFERENCE
                ).values(parentId, parentType, attributeName, metaAttribute.getTitle(),
                    DataUtils.convert(metaAttribute.isKey()), DataUtils.convert(metaAttribute.isNullable()),
                    metaSet.getArrayKeyType().toString(),
                    DataUtils.convert(metaSet.isImmutable()), DataUtils.convert(metaSet.isReference()));

            logger.debug(insert.toString());

            long t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }

            id = insertWithId(insert.getSQL(), insert.getBindValues().toArray());
            metaSet.setId(id);

            if(sqlStats != null)
            {
                sqlStats.put(insert.getSQL(), (System.nanoTime() - t) / 1000000);
            }

            saveSet(metaSet.getMemberType(), id, ContainerTypes.SET, new MetaAttribute(false, false, null), "item");
        }
        else
        {
            if(metaSet.isComplex())
            {
                long innerId = save((MetaClass)metaSet.getMemberType());

                insert = context
                        .insertInto(EAV_M_COMPLEX_SET)
                        .set(EAV_M_COMPLEX_SET.CONTAINING_ID, parentId)
                        .set(EAV_M_COMPLEX_SET.CONTAINER_TYPE, parentType)
                        .set(EAV_M_COMPLEX_SET.NAME, attributeName)
                        .set(EAV_M_COMPLEX_SET.TITLE, metaAttribute.getTitle())
                        .set(EAV_M_COMPLEX_SET.IS_KEY, DataUtils.convert(metaAttribute.isKey()))
                        .set(EAV_M_COMPLEX_SET.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable()))
                        .set(EAV_M_COMPLEX_SET.CLASS_ID, innerId)
                        .set(EAV_M_COMPLEX_SET.ARRAY_KEY_TYPE, metaSet.getArrayKeyType().toString())
                        .set(EAV_M_COMPLEX_SET.IS_IMMUTABLE, DataUtils.convert(metaSet.isImmutable()))
                        .set(EAV_M_COMPLEX_SET.IS_REFERENCE, DataUtils.convert(metaSet.isReference()));
            }
            else
            {
                insert = context
                        .insertInto(EAV_M_SIMPLE_SET)
                        .set(EAV_M_SIMPLE_SET.CONTAINING_ID, parentId)
                        .set(EAV_M_SIMPLE_SET.CONTAINER_TYPE, parentType)
                        .set(EAV_M_SIMPLE_SET.NAME, attributeName)
                        .set(EAV_M_SIMPLE_SET.TITLE, metaAttribute.getTitle())
                        .set(EAV_M_SIMPLE_SET.TYPE_CODE, metaSet.getTypeCode().toString())
                        .set(EAV_M_SIMPLE_SET.IS_KEY, DataUtils.convert(metaAttribute.isKey()))
                        .set(EAV_M_SIMPLE_SET.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable()))
                        .set(EAV_M_SIMPLE_SET.ARRAY_KEY_TYPE, metaSet.getArrayKeyType().toString())
                        .set(EAV_M_SIMPLE_SET.IS_IMMUTABLE, DataUtils.convert(metaSet.isImmutable()))
                        .set(EAV_M_SIMPLE_SET.IS_REFERENCE, DataUtils.convert(metaSet.isReference()));
            }

            logger.debug(insert.toString());

            long t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }

            id = insertWithId(insert.getSQL(), insert.getBindValues().toArray());
            metaSet.setId(id);

            if(sqlStats != null)
            {
                sqlStats.put(insert.getSQL(), (System.nanoTime() - t) / 1000000);
            }
        }

        if (metaSet.isComplex()) {
            HashMap<String, ArrayList<String>> keyFilter = metaSet.getArrayKeyFilter();

            DeleteConditionStep deleteFilter =
                    context.delete(EAV_M_SET_KEY_FILTER).where(EAV_M_SET_KEY_FILTER.SET_ID.eq(id));

            long t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }

            jdbcTemplate.update(deleteFilter.getSQL(), deleteFilter.getBindValues().toArray());

            if(sqlStats != null)
            {
                sqlStats.put(deleteFilter.getSQL(), (System.nanoTime() - t) / 1000000);
            }

            for (String attrName : keyFilter.keySet()) {

                for (String val : keyFilter.get(attrName)) {
                    InsertOnDuplicateStep insertFilter = context.
                            insertInto(EAV_M_SET_KEY_FILTER,
                                    EAV_M_SET_KEY_FILTER.SET_ID,
                                    EAV_M_SET_KEY_FILTER.ATTR_NAME,
                                    EAV_M_SET_KEY_FILTER.VALUE).values(
                                        id,
                                        attrName,
                                        val
                                    );

                    t = 0;
                    if(sqlStats != null)
                    {
                        t = System.nanoTime();
                    }

                    insertWithId(insertFilter.getSQL(), insertFilter.getBindValues().toArray());

                    if(sqlStats != null)
                    {
                        sqlStats.put(insertFilter.getSQL(), (System.nanoTime() - t) / 1000000);
                    }
                }
            }
        }

        return id;
    }

    private long saveAttribute(IMetaType type, long parentId, int parentType, IMetaAttribute metaAttribute,
                               String attributeName, String attributeTitle)
    {
        //String query;
        //Object[] args;
        InsertOnDuplicateStep insert;

        if(type.isSet())
        {
            throw new IllegalStateException(attributeName + " is an array, single value expected.");
        }

        if(type.isComplex())
        {
            long innerId = save((MetaClass)type);

            insert = context.insertInto(
                    EAV_M_COMPLEX_ATTRIBUTES,
                    EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID,
                    EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE,
                    EAV_M_COMPLEX_ATTRIBUTES.NAME,
                    EAV_M_COMPLEX_ATTRIBUTES.TITLE,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_KEY,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_NULLABLE,
                    EAV_M_COMPLEX_ATTRIBUTES.CLASS_ID
                ).values(parentId, parentType, attributeName, attributeTitle,
                    DataUtils.convert(metaAttribute.isKey()), DataUtils.convert(metaAttribute.isNullable()),
                    innerId);
        }
        else
        {
            insert = context.insertInto(
                    EAV_M_SIMPLE_ATTRIBUTES,
                    EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID,
                    EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE,
                    EAV_M_SIMPLE_ATTRIBUTES.NAME,
                    EAV_M_SIMPLE_ATTRIBUTES.TITLE,
                    EAV_M_SIMPLE_ATTRIBUTES.TYPE_CODE,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_KEY,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_NULLABLE
                ).values(parentId, parentType, attributeName, attributeTitle,
                    ((MetaValue)type).getTypeCode().toString(),
                    DataUtils.convert(metaAttribute.isKey()), DataUtils.convert(metaAttribute.isNullable()));
        }

        logger.debug(insert.toString());

        long t = 0;
        if(sqlStats != null)
        {
            t = System.nanoTime();
        }

        long id = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        if(sqlStats != null)
        {
            sqlStats.put(insert.getSQL(), (System.nanoTime() - t) / 1000000);
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
                metaAttribute.setId(saveAttribute(metaType, meta.getId(), ContainerTypes.CLASS,
                        metaAttribute, typeName, metaAttribute.getTitle()));
            }
        }
    }

    private void updateAttributes(Set<String> updateNames, MetaClass meta, MetaClass dbMeta)
    {
        if(dbMeta.getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before attributes update in DB");
        }

        UpdateConditionStep update;

        for(String typeName : updateNames)
        {
            IMetaAttribute metaAttribute = meta.getMetaAttribute(typeName);

            if (meta.getMemberType(typeName).isSet()) {
                if(meta.getMemberType(typeName).isComplex())
                {
                    update = context.update(EAV_M_COMPLEX_SET
                    ).set(EAV_M_COMPLEX_SET.IS_KEY, DataUtils.convert(metaAttribute.isKey())
                    ).set(EAV_M_COMPLEX_SET.TITLE, metaAttribute.getTitle()
                    ).set(EAV_M_COMPLEX_SET.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable())
                    ).where(EAV_M_COMPLEX_SET.CONTAINING_ID.eq(dbMeta.getId())
                    ).and(EAV_M_COMPLEX_SET.CONTAINER_TYPE.eq(ContainerTypes.CLASS)
                    ).and(EAV_M_COMPLEX_SET.NAME.eq(typeName));

                    MetaSet metaSet = (MetaSet)meta.getMemberType(typeName);
                    long id = ((MetaSet)dbMeta.getMemberType(typeName)).getId();

                    HashMap<String, ArrayList<String>> keyFilter = metaSet.getArrayKeyFilter();

                    DeleteConditionStep deleteFilter =
                            context.delete(EAV_M_SET_KEY_FILTER).where(EAV_M_SET_KEY_FILTER.SET_ID.eq(id));

                    long t = 0;
                    if(sqlStats != null)
                    {
                        t = System.nanoTime();
                    }

                    jdbcTemplate.update(deleteFilter.getSQL(), deleteFilter.getBindValues().toArray());

                    if(sqlStats != null)
                    {
                        sqlStats.put(deleteFilter.getSQL(), (System.nanoTime() - t) / 1000000);
                    }

                    for (String attrName : keyFilter.keySet()) {
                        for(String val : keyFilter.get(attrName)) {
                            InsertOnDuplicateStep insertFilter = context.
                                    insertInto(EAV_M_SET_KEY_FILTER,
                                            EAV_M_SET_KEY_FILTER.SET_ID,
                                            EAV_M_SET_KEY_FILTER.ATTR_NAME,
                                            EAV_M_SET_KEY_FILTER.VALUE).values(
                                    id,
                                    attrName,
                                    val
                            );

                            t = 0;
                            if(sqlStats != null)
                            {
                                t = System.nanoTime();
                            }

                            insertWithId(insertFilter.getSQL(), insertFilter.getBindValues().toArray());

                            if(sqlStats != null)
                            {
                                sqlStats.put(insertFilter.getSQL(), (System.nanoTime() - t) / 1000000);
                            }
                        }
                    }
                }
                else
                {
                    update = context.update(EAV_M_SIMPLE_SET
                    ).set(EAV_M_SIMPLE_SET.IS_KEY, DataUtils.convert(metaAttribute.isKey())
                    ).set(EAV_M_SIMPLE_SET.TITLE, metaAttribute.getTitle()
                    ).set(EAV_M_SIMPLE_SET.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable())
                    ).where(EAV_M_SIMPLE_SET.CONTAINING_ID.eq(dbMeta.getId())
                    ).and(EAV_M_SIMPLE_SET.CONTAINER_TYPE.eq(ContainerTypes.CLASS)
                    ).and(EAV_M_SIMPLE_SET.NAME.eq(typeName));
                }
            } else {
                if(meta.getMemberType(typeName).isComplex())
                {
                    update = context.update(EAV_M_COMPLEX_ATTRIBUTES
                    ).set(EAV_M_COMPLEX_ATTRIBUTES.IS_KEY, DataUtils.convert(metaAttribute.isKey())
                    ).set(EAV_M_COMPLEX_ATTRIBUTES.TITLE, metaAttribute.getTitle()
                    ).set(EAV_M_COMPLEX_ATTRIBUTES.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable())
                    ).where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(dbMeta.getId())
                    ).and(EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE.eq(ContainerTypes.CLASS)
                    ).and(EAV_M_COMPLEX_ATTRIBUTES.NAME.eq(typeName));
                }
                else
                {
                    update = context.update(EAV_M_SIMPLE_ATTRIBUTES
                    ).set(EAV_M_SIMPLE_ATTRIBUTES.IS_KEY, DataUtils.convert(metaAttribute.isKey())
                    ).set(EAV_M_SIMPLE_ATTRIBUTES.TITLE, metaAttribute.getTitle()
                    ).set(EAV_M_SIMPLE_ATTRIBUTES.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable())
                    ).where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(dbMeta.getId())
                    ).and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(ContainerTypes.CLASS)
                    ).and(EAV_M_SIMPLE_ATTRIBUTES.NAME.eq(typeName));
                }
            }

            logger.debug(update.toString());

            long t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }
            jdbcTemplate.update(update.getSQL(), update.getBindValues().toArray());
            if(sqlStats != null)
            {
                sqlStats.put(update.getSQL(), (System.nanoTime() - t) / 1000000);
            }
        }
    }

    private void deleteAttributes(Set<String> deleteNames, MetaClass meta)
    {
        DeleteConditionStep delete;

        if(meta.getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before attributes deletion to DB");
        }

        for(String typeName : deleteNames)
        {
            if(meta.getMemberType(typeName).isComplex())
            {
                delete = context.delete(EAV_M_COMPLEX_ATTRIBUTES
                    ).where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(meta.getId())
                    ).and(EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE.eq(ContainerTypes.CLASS)
                    ).and(EAV_M_COMPLEX_ATTRIBUTES.NAME.eq(typeName));
            }
            else
            {
                delete = context.delete(EAV_M_SIMPLE_ATTRIBUTES
                    ).where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(meta.getId())
                    ).and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(ContainerTypes.CLASS)
                    ).and(EAV_M_SIMPLE_ATTRIBUTES.NAME.eq(typeName));
            }

            logger.debug(delete.toString());

            long t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }
            jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
            if(sqlStats != null)
            {
                sqlStats.put(delete.getSQL(), (System.nanoTime() - t) / 1000000);
            }
        }
    }

    void loadSimpleAttributes(IMetaContainer meta)
    {
        SelectForUpdateStep select = context.select(
                EAV_M_SIMPLE_ATTRIBUTES.ID,
                EAV_M_SIMPLE_ATTRIBUTES.NAME,
                EAV_M_SIMPLE_ATTRIBUTES.TITLE,
                EAV_M_SIMPLE_ATTRIBUTES.TYPE_CODE,
                EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE,
                EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID,
                EAV_M_SIMPLE_ATTRIBUTES.IS_KEY,
                EAV_M_SIMPLE_ATTRIBUTES.IS_NULLABLE
            ).from(EAV_M_SIMPLE_ATTRIBUTES
        ).where(
                EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(meta.getId())
        ).and(
                EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(meta.getType())
        );

        logger.debug(select.toString());

        long t = 0;
        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        if(sqlStats != null)
        {
            sqlStats.put(select.getSQL(), (System.nanoTime() - t) / 1000000);
        }

        for (Map<String, Object> row : rows) {
            MetaAttribute metaAttirubute = new MetaAttribute(
                    ((BigDecimal)row.get("id")).longValue(),
                    ((BigDecimal)row.get("is_key")).longValue() == 1,
                    ((BigDecimal)row.get("is_nullable")).longValue() == 1);

            metaAttirubute.setMetaType(new MetaValue(DataTypes.valueOf((String) row.get("type_code"))));
            metaAttirubute.setTitle((String) row.get("title"));

            meta.setMetaAttribute((String) row.get("name"), metaAttirubute);
        }
    }

    void loadSimpleArrays(IMetaContainer meta)
    {
        SelectForUpdateStep select = context.select(
                EAV_M_SIMPLE_SET.ID,
                EAV_M_SIMPLE_SET.NAME,
                EAV_M_SIMPLE_SET.TITLE,
                EAV_M_SIMPLE_SET.CONTAINER_TYPE,
                EAV_M_SIMPLE_SET.CONTAINING_ID,
                EAV_M_SIMPLE_SET.IS_KEY,
                EAV_M_SIMPLE_SET.IS_NULLABLE,
                EAV_M_SIMPLE_SET.TYPE_CODE,
                EAV_M_SIMPLE_SET.ARRAY_KEY_TYPE,
                EAV_M_SIMPLE_SET.IS_IMMUTABLE,
                EAV_M_SIMPLE_SET.IS_REFERENCE
            ).from(EAV_M_SIMPLE_SET
        ).where(EAV_M_SIMPLE_SET.CONTAINING_ID.eq(meta.getId())
        ).and(EAV_M_SIMPLE_SET.CONTAINER_TYPE.eq(meta.getType()));

        long t = 0;

        logger.debug(select.toString());

        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        List<Map<String, Object>>  rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        if(sqlStats != null)
        {
            sqlStats.put(select.getSQL(), (System.nanoTime() - t) / 1000000);
        }

        for (Map<String, Object> row : rows) {

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((BigDecimal)row.get("id")).longValue(),
                    ((BigDecimal)row.get("is_key")).longValue() == 1,
                    ((BigDecimal)row.get("is_nullable")).longValue() == 1);


            MetaSet metaSet = new MetaSet(new MetaValue(DataTypes.valueOf((String) row.get("type_code"))));
            metaSet.setId(((BigDecimal)row.get("id")).longValue());

            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));
            metaSet.setImmutable(((BigDecimal)row.get("is_immutable")).longValue() == 1);
            metaSet.setReference(((BigDecimal)row.get("is_reference")).longValue() == 1);

            metaAttribute.setMetaType(metaSet);
            metaAttribute.setTitle((String) row.get("title"));

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);


        }
    }

    void loadComplexAttributes(IMetaContainer meta)
    {
        SelectForUpdateStep select = context.select(
                EAV_M_COMPLEX_ATTRIBUTES.ID,
                EAV_M_COMPLEX_ATTRIBUTES.IS_KEY,
                EAV_M_COMPLEX_ATTRIBUTES.IS_NULLABLE,
                EAV_M_COMPLEX_ATTRIBUTES.NAME,
                EAV_M_COMPLEX_ATTRIBUTES.TITLE,
                EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE,
                EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID,
                EAV_M_COMPLEX_ATTRIBUTES.CLASS_ID,
                EAV_M_CLASSES.NAME.as("cname")
            ).from(EAV_M_COMPLEX_ATTRIBUTES).leftOuterJoin(EAV_M_CLASSES
        ).on(EAV_M_COMPLEX_ATTRIBUTES.CLASS_ID.eq(EAV_M_CLASSES.ID)
        ).where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(meta.getId())
        ).and(EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE.eq(meta.getType()));

        long t = 0;

        logger.debug(select.toString());

        if(sqlStats != null)
        {
            t = System.nanoTime();
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        if(sqlStats != null)
        {
            sqlStats.put(select.getSQL(), (System.nanoTime() - t) / 1000000);
        }

        for (Map<String, Object> row : rows) {
            MetaClass metaClass = load(((BigDecimal)row.get("class_id")).longValue());

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((BigDecimal)row.get("id")).longValue(),
                    ((BigDecimal)row.get("is_key")).longValue() == 1,
                    ((BigDecimal)row.get("is_nullable")).longValue() == 1);

            metaAttribute.setMetaType(metaClass);
            metaAttribute.setTitle((String) row.get("title"));

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);
        }
    }

    void loadComplexArrays(IMetaContainer meta)
    {
        SelectForUpdateStep select = context.select(
                EAV_M_COMPLEX_SET.ID,
                EAV_M_COMPLEX_SET.IS_NULLABLE,
                EAV_M_COMPLEX_SET.IS_KEY,
                EAV_M_COMPLEX_SET.NAME,
                EAV_M_COMPLEX_SET.TITLE,
                EAV_M_COMPLEX_SET.CLASS_ID,
                EAV_M_COMPLEX_SET.ARRAY_KEY_TYPE,
                EAV_M_COMPLEX_SET.CONTAINER_TYPE,
                EAV_M_COMPLEX_SET.CONTAINING_ID,
                EAV_M_COMPLEX_SET.IS_IMMUTABLE,
                EAV_M_COMPLEX_SET.IS_REFERENCE,
                EAV_M_CLASSES.NAME.as("cname")
            ).from(EAV_M_COMPLEX_SET
        ).leftOuterJoin(EAV_M_CLASSES).on(EAV_M_COMPLEX_SET.CLASS_ID.eq(EAV_M_CLASSES.ID)
        ).where(EAV_M_COMPLEX_SET.CONTAINING_ID.eq(meta.getId())
        ).and(EAV_M_COMPLEX_SET.CONTAINER_TYPE.eq(meta.getType()));
        long t = 0;

        logger.debug(select.toString());

        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        if(sqlStats != null)
        {
            sqlStats.put(select.getSQL(), (System.nanoTime() - t) / 1000000);
        }

        for (Map<String, Object> row : rows) {
            MetaClass metaClass = load(((BigDecimal)row.get("class_id")).longValue());

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((BigDecimal)row.get("id")).longValue(),
                    ((BigDecimal)row.get("is_key")).longValue() == 1,
                    ((BigDecimal)row.get("is_nullable")).longValue() == 1);

            MetaSet metaSet = new MetaSet(metaClass);
            metaSet.setId(((BigDecimal)row.get("id")).longValue());
            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));
            metaSet.setImmutable(((BigDecimal)row.get("is_immutable")).longValue() == 1);
            metaSet.setReference(((BigDecimal)row.get("is_reference")).longValue() == 1);

            SelectForUpdateStep selectFilters = context.select(
                    EAV_M_SET_KEY_FILTER.ATTR_NAME,
                    EAV_M_SET_KEY_FILTER.VALUE).from(EAV_M_SET_KEY_FILTER).
                    where(EAV_M_SET_KEY_FILTER.SET_ID.eq(metaSet.getId()));

            if(sqlStats != null)
            {
                t = System.nanoTime();
            }
            List<Map<String, Object>> filterRows = jdbcTemplate.queryForList(selectFilters.getSQL(),
                    selectFilters.getBindValues().toArray());
            if(sqlStats != null)
            {
                sqlStats.put(selectFilters.getSQL(), (System.nanoTime() - t) / 1000000);
            }

            for (Map<String, Object> filterRow : filterRows) {
                if (filterRow.get("attr_name") != null) {
                    metaSet.addArrayKeyFilter((String) filterRow.get("attr_name"),
                        (String) filterRow.get("value"));
                }
            }

            metaAttribute.setMetaType(metaSet);
            metaAttribute.setTitle((String) row.get("title"));

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);
        }
    }

    void loadArrayArrays(IMetaContainer meta)
    {
        SelectForUpdateStep select = context.select(
                EAV_M_SET_OF_SETS.ID,
                EAV_M_SET_OF_SETS.NAME,
                EAV_M_SET_OF_SETS.TITLE,
                EAV_M_SET_OF_SETS.IS_NULLABLE,
                EAV_M_SET_OF_SETS.IS_KEY,
                EAV_M_SET_OF_SETS.CONTAINER_TYPE,
                EAV_M_SET_OF_SETS.CONTAINING_ID,
                EAV_M_SET_OF_SETS.ARRAY_KEY_TYPE,
                EAV_M_SET_OF_SETS.IS_IMMUTABLE,
                EAV_M_SET_OF_SETS.IS_REFERENCE
            ).from(EAV_M_SET_OF_SETS
        ).where(EAV_M_SET_OF_SETS.CONTAINING_ID.eq(meta.getId())
        ).and(EAV_M_SET_OF_SETS.CONTAINER_TYPE.eq(meta.getType()));
        long t = 0;

        logger.debug(select.toString());

        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        List<Map<String, Object>>  rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());
        if(sqlStats != null)
        {
            sqlStats.put(select.getSQL(), (System.nanoTime() - t) / 1000000);
        }

        for (Map<String, Object> row : rows) {

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((BigDecimal)row.get("id")).longValue(),
                    ((BigDecimal)row.get("is_key")).longValue() == 1,
                    ((BigDecimal)row.get("is_nullable")).longValue() == 1);



            MetaSet metaSet = new MetaSet();

            metaSet.setId(((BigDecimal)row.get("id")).longValue());
            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));
            metaSet.setImmutable(((BigDecimal)row.get("is_immutable")).longValue() == 1);
            metaSet.setReference(((BigDecimal)row.get("is_reference")).longValue() == 1);

            loadSimpleArrays(metaSet);
            loadComplexArrays(metaSet);
            loadArrayArrays(metaSet);

            metaAttribute.setMetaType(metaSet);
            metaAttribute.setTitle((String) row.get("title"));

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

        Iterator<String> i = updateNames.iterator();
        while (i.hasNext())
        {
            String name = i.next();
            if(!meta.getMemberType(name).equals(dbMeta.getMemberType(name)))
            {
                deleteNames.add(name);
                addNames.add(name);
                i.remove();
            } else {
                if(meta.getMetaAttribute(name).equals(dbMeta.getMetaAttribute(name))) {
                    i.remove();
                }
            }
        }

        deleteAttributes(deleteNames, dbMeta);
        insertAttributes(addNames, meta);
        updateAttributes(updateNames, meta, dbMeta);

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
    public MetaClass load(String className, Date beginDate) {
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
        DeleteConditionStep delete = context.delete(EAV_M_SIMPLE_ATTRIBUTES
            ).where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(id)
        ).and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(type));

        logger.debug(delete.toString());

        long t = 0;
        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
        if(sqlStats != null)
        {
            sqlStats.put(delete.getSQL(), (System.nanoTime() - t) / 1000000);
        }

        delete = context.delete(EAV_M_COMPLEX_ATTRIBUTES
            ).where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(id)
            ).and(EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE.eq(type));

        logger.debug(delete.toString());

        t = 0;
        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
        if(sqlStats != null)
        {
            sqlStats.put(delete.getSQL(), (System.nanoTime() - t) / 1000000);
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

        long t = 0;

        for (String arrayName : metaClass.getArrayArrayAttributesNames())
        {
            removeSet(((MetaSet) metaClass.getMemberType(arrayName)));
        }

        //delete all class attributes
        removeAllAttributes(metaClass.getId(), ContainerTypes.CLASS);

        //delete class
        DeleteConditionStep delete = context.delete(EAV_M_CLASSES).where(EAV_M_CLASSES.ID.eq(metaClass.getId()));

        logger.debug(delete.toString());

        if(sqlStats != null)
        {
            t = System.nanoTime();
        }
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
        if(sqlStats != null)
        {
            sqlStats.put(delete.getSQL(), (System.nanoTime() - t) / 1000000);
        }
	}

    @SuppressWarnings("UnusedDeclaration")
    public DSLContext getDSLContext()
    {
        return context;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setDSLContext(DSLContext context)
    {
        this.context = context;
    }

    @Override
    public List<MetaClassName> getMetaClassesNames()
    {
        ArrayList<MetaClassName> metaClassNameList = new ArrayList<MetaClassName>();
        SelectForUpdateStep select;

        select = context.select(
                EAV_M_CLASSES.ID,
                EAV_M_CLASSES.NAME,
                EAV_M_CLASSES.TITLE
        ).from(EAV_M_CLASSES).
                orderBy(EAV_M_CLASSES.BEGIN_DATE.desc());


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());


        if (rows.size() < 1)
            throw new IllegalArgumentException("Classes not found.");

        for (Map<String, Object> row : rows){

            MetaClassName metaClassName = new MetaClassName();

            metaClassName.setId(((BigDecimal)row.get("id")).longValue());
            metaClassName.setClassName((String)row.get("name"));
            metaClassName.setClassTitle((String)row.get("title"));
            metaClassNameList.add(metaClassName);
        }

        return metaClassNameList;
    }
}
