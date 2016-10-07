package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.eav.persistance.generated.tables.records.EavMClassesRecord;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.*;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Repository
public class MetaClassDaoImpl extends JDBCSupport implements IMetaClassDao {
    private final Logger logger = LoggerFactory.getLogger(MetaClassDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    private void loadAllClasses(List<MetaClass> metaClassList) {
        SelectForUpdateStep select;

        select = context.select(
                EAV_M_CLASSES.ID,
                EAV_M_CLASSES.BEGIN_DATE,
                EAV_M_CLASSES.NAME,
                EAV_M_CLASSES.TITLE,
                EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                EAV_M_CLASSES.PARENT_IS_KEY,
                EAV_M_CLASSES.IS_CLOSABLE,
                EAV_M_CLASSES.IS_DISABLED,
                EAV_M_CLASSES.IS_REFERENCE)
                .from(EAV_M_CLASSES)
                .orderBy(EAV_M_CLASSES.BEGIN_DATE.desc());

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() < 1) return;

        for (Map<String, Object> row : rows) {
            MetaClass metaClass = new MetaClass();

            setMetaProperties(metaClass, row);
            loadAttributes(metaClass);

            metaClassList.add(metaClass);
        }
    }

    private void setMetaProperties(MetaClass metaClass, Map<String, Object> row) {
        metaClass.setDisabled(((BigDecimal) row.get("is_disabled")).longValue() == 1);
        metaClass.setBeginDate(DataUtils.convert((Timestamp) row.get("begin_date")));
        metaClass.setId(((BigDecimal) row.get("id")).longValue());
        metaClass.setClassName((String) row.get("name"));
        metaClass.setClassTitle((String) row.get("title"));
        metaClass.setComplexKeyType(ComplexKeyTypes.valueOf((String) row.get("complex_key_type")));
        metaClass.setReference(((BigDecimal) row.get("is_reference")).longValue() == 1);
        metaClass.setParentIsKey(((BigDecimal) row.get("parent_is_key")).longValue() == 1);
        metaClass.setClosable(((BigDecimal) row.get("is_closable")).longValue() == 1);
    }

    public List<MetaClass> loadAll() {
        List<MetaClass> metaClassList = new ArrayList<>();
        loadAllClasses(metaClassList);

        return metaClassList;
    }

    private void loadClass(MetaClass metaClass, boolean beginDateStrict) {
        SelectForUpdateStep select;

        if (metaClass.getId() < 1) {
            if (metaClass.getClassName() == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E162));

            if (beginDateStrict) {
                select = context.select(
                        EAV_M_CLASSES.IS_DISABLED,
                        EAV_M_CLASSES.BEGIN_DATE,
                        EAV_M_CLASSES.ID,
                        EAV_M_CLASSES.NAME,
                        EAV_M_CLASSES.TITLE,
                        EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                        EAV_M_CLASSES.PARENT_IS_KEY,
                        EAV_M_CLASSES.IS_CLOSABLE,
                        EAV_M_CLASSES.IS_REFERENCE)
                        .from(EAV_M_CLASSES)
                        .where(EAV_M_CLASSES.NAME.equal(metaClass.getClassName()))
                        .and(EAV_M_CLASSES.BEGIN_DATE.equal(DataUtils.convert(metaClass.getBeginDate())))
                        .and(EAV_M_CLASSES.IS_DISABLED.equal(DataUtils.convert(false)))
                        .orderBy(EAV_M_CLASSES.BEGIN_DATE.desc()).limit(1).offset(0);
            } else {
                select = context.select(
                        EAV_M_CLASSES.IS_DISABLED,
                        EAV_M_CLASSES.BEGIN_DATE,
                        EAV_M_CLASSES.ID,
                        EAV_M_CLASSES.NAME,
                        EAV_M_CLASSES.TITLE,
                        EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                        EAV_M_CLASSES.PARENT_IS_KEY,
                        EAV_M_CLASSES.IS_CLOSABLE,
                        EAV_M_CLASSES.IS_REFERENCE)
                        .from(EAV_M_CLASSES)
                        .where(EAV_M_CLASSES.NAME.equal(metaClass.getClassName()))
                        .and(EAV_M_CLASSES.BEGIN_DATE.le(DataUtils.convert(metaClass.getBeginDate())))
                        .and(EAV_M_CLASSES.IS_DISABLED.equal(DataUtils.convert(false)))
                        .orderBy(EAV_M_CLASSES.BEGIN_DATE.desc()).limit(1).offset(0);
            }

        } else {
            select = context.select(
                    EAV_M_CLASSES.IS_DISABLED,
                    EAV_M_CLASSES.BEGIN_DATE,
                    EAV_M_CLASSES.ID,
                    EAV_M_CLASSES.NAME,
                    EAV_M_CLASSES.TITLE,
                    EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                    EAV_M_CLASSES.PARENT_IS_KEY,
                    EAV_M_CLASSES.IS_CLOSABLE,
                    EAV_M_CLASSES.IS_REFERENCE)
                    .from(EAV_M_CLASSES)
                    .where(EAV_M_CLASSES.ID.equal(metaClass.getId()));
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E82, metaClass.getClassName()));

        if (rows.size() < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E163, metaClass.getClassName()));

        Map<String, Object> row = rows.get(0);

        if (row != null) {
            setMetaProperties(metaClass, row);
        } else {
            logger.error("Can't load metaClass, empty data set.");
        }
    }

    // This method only for meta editor portlet
    private void loadAllClass(MetaClass metaClass, boolean beginDateStrict) {
        SelectForUpdateStep select;

        if (metaClass.getId() < 1) {
            if (metaClass.getClassName() == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E162));

            if (beginDateStrict) {
                select = context.select(
                        EAV_M_CLASSES.IS_DISABLED,
                        EAV_M_CLASSES.BEGIN_DATE,
                        EAV_M_CLASSES.ID,
                        EAV_M_CLASSES.NAME,
                        EAV_M_CLASSES.TITLE,
                        EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                        EAV_M_CLASSES.PARENT_IS_KEY,
                        EAV_M_CLASSES.IS_CLOSABLE,
                        EAV_M_CLASSES.IS_REFERENCE)
                        .from(EAV_M_CLASSES)
                        .where(EAV_M_CLASSES.NAME.equal(metaClass.getClassName()))
                        .and(EAV_M_CLASSES.BEGIN_DATE.eq(DataUtils.convert(metaClass.getBeginDate())))
                        .orderBy(EAV_M_CLASSES.BEGIN_DATE.desc())
                        .limit(1)
                        .offset(0);
            } else {
                select = context.select(
                        EAV_M_CLASSES.IS_DISABLED,
                        EAV_M_CLASSES.BEGIN_DATE,
                        EAV_M_CLASSES.ID,
                        EAV_M_CLASSES.NAME,
                        EAV_M_CLASSES.TITLE,
                        EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                        EAV_M_CLASSES.PARENT_IS_KEY,
                        EAV_M_CLASSES.IS_CLOSABLE,
                        EAV_M_CLASSES.IS_REFERENCE)
                        .from(EAV_M_CLASSES)
                        .where(EAV_M_CLASSES.NAME.equal(metaClass.getClassName()))
                        .and(EAV_M_CLASSES.BEGIN_DATE.le(DataUtils.convert(metaClass.getBeginDate())))
                        .orderBy(EAV_M_CLASSES.BEGIN_DATE.desc()).limit(1).offset(0);
            }

        } else {
            select = context.select(
                    EAV_M_CLASSES.IS_DISABLED,
                    EAV_M_CLASSES.BEGIN_DATE,
                    EAV_M_CLASSES.ID,
                    EAV_M_CLASSES.NAME,
                    EAV_M_CLASSES.TITLE,
                    EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                    EAV_M_CLASSES.PARENT_IS_KEY,
                    EAV_M_CLASSES.IS_CLOSABLE,
                    EAV_M_CLASSES.IS_REFERENCE)
                    .from(EAV_M_CLASSES)
                    .where(EAV_M_CLASSES.ID.equal(metaClass.getId()))
                    .limit(1)
                    .offset(0);
        }

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E83, metaClass.getClassName()));

        if (rows.size() < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E163, metaClass.getClassName()));

        Map<String, Object> row = rows.get(0);

        if (row != null) {
            setMetaProperties(metaClass, row);
        } else {
            logger.error("Can't load metaClass, empty data set.");
        }
    }

    private long createClass(MetaClass metaClass) {
        InsertOnDuplicateStep insert = context.insertInto(
                EAV_M_CLASSES,
                EAV_M_CLASSES.NAME,
                EAV_M_CLASSES.TITLE,
                EAV_M_CLASSES.COMPLEX_KEY_TYPE,
                EAV_M_CLASSES.BEGIN_DATE,
                EAV_M_CLASSES.IS_DISABLED,
                EAV_M_CLASSES.PARENT_IS_KEY,
                EAV_M_CLASSES.IS_CLOSABLE,
                EAV_M_CLASSES.IS_REFERENCE
        ).values(metaClass.getClassName(), metaClass.getClassTitle(),
                metaClass.getComplexKeyType().toString(),
                DataUtils.convert(metaClass.getBeginDate()),
                DataUtils.convert(metaClass.isDisabled()),
                DataUtils.convert(metaClass.parentIsKey()),
                DataUtils.convert(metaClass.isClosable()),
                DataUtils.convert(metaClass.isReference()));

        logger.debug(insert.toString());
        long metaId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        if (metaId < 1)
            throw new IllegalStateException(Errors.compose(Errors.E158));

        metaClass.setId(metaId);

        return metaId;
    }

    private void updateClass(MetaClass metaClass) {
        if (metaClass.getId() < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E170));

        UpdateConditionStep update = context.update(EAV_M_CLASSES).
                set(EAV_M_CLASSES.NAME, metaClass.getClassName()).
                set(EAV_M_CLASSES.TITLE, metaClass.getClassTitle()).
                set(EAV_M_CLASSES.COMPLEX_KEY_TYPE, metaClass.getComplexKeyType().toString()).
                set(EAV_M_CLASSES.BEGIN_DATE, DataUtils.convert(metaClass.getBeginDate())).
                set(EAV_M_CLASSES.IS_DISABLED, DataUtils.convert(metaClass.isDisabled())).
                set(EAV_M_CLASSES.IS_REFERENCE, DataUtils.convert(metaClass.isReference())).
                set(EAV_M_CLASSES.PARENT_IS_KEY, DataUtils.convert(metaClass.parentIsKey())).
                set(EAV_M_CLASSES.IS_CLOSABLE, DataUtils.convert(metaClass.isClosable())).
                where(EAV_M_CLASSES.ID.eq(metaClass.getId()));

        jdbcTemplate.update(update.getSQL(), update.getBindValues().toArray());
    }

    private long saveSet(IMetaType type, long parentId, int parentType, IMetaAttribute metaAttribute, String attributeName) {
        InsertOnDuplicateStep insert;
        long id;

        if (!type.isSet())
            throw new IllegalStateException(Errors.compose(Errors.E168, attributeName));

        MetaSet metaSet = (MetaSet) type;

        if (metaSet.isComplex()) {
            long innerId = save((MetaClass) metaSet.getMemberType());

            insert = context
                    .insertInto(EAV_M_COMPLEX_SET)
                    .set(EAV_M_COMPLEX_SET.CONTAINING_ID, parentId)
                    .set(EAV_M_COMPLEX_SET.CONTAINER_TYPE, parentType)
                    .set(EAV_M_COMPLEX_SET.NAME, attributeName)
                    .set(EAV_M_COMPLEX_SET.TITLE, metaAttribute.getTitle())
                    .set(EAV_M_COMPLEX_SET.IS_KEY, DataUtils.convert(metaAttribute.isKey()))
                    .set(EAV_M_COMPLEX_SET.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable()))
                    .set(EAV_M_COMPLEX_SET.IS_CUMULATIVE, DataUtils.convert(metaAttribute.isCumulative()))
                    .set(EAV_M_COMPLEX_SET.IS_FINAL, DataUtils.convert(metaAttribute.isFinal()))
                    .set(EAV_M_COMPLEX_SET.IS_IMMUTABLE, DataUtils.convert(metaAttribute.isImmutable()))
                    .set(EAV_M_COMPLEX_SET.IS_REQUIRED, DataUtils.convert(metaAttribute.isRequired()))
                    .set(EAV_M_COMPLEX_SET.CLASS_ID, innerId)
                    .set(EAV_M_COMPLEX_SET.ARRAY_KEY_TYPE, metaSet.getArrayKeyType().toString())
                    .set(EAV_M_COMPLEX_SET.IS_DISABLED, DataUtils.convert(metaAttribute.isDisabled()))
                    .set(EAV_M_COMPLEX_SET.IS_REFERENCE, DataUtils.convert(metaSet.isReference()));
        } else {
            insert = context
                    .insertInto(EAV_M_SIMPLE_SET)
                    .set(EAV_M_SIMPLE_SET.CONTAINING_ID, parentId)
                    .set(EAV_M_SIMPLE_SET.CONTAINER_TYPE, parentType)
                    .set(EAV_M_SIMPLE_SET.NAME, attributeName)
                    .set(EAV_M_SIMPLE_SET.TITLE, metaAttribute.getTitle())
                    .set(EAV_M_SIMPLE_SET.TYPE_CODE, metaSet.getTypeCode().toString())
                    .set(EAV_M_SIMPLE_SET.IS_KEY, DataUtils.convert(metaAttribute.isKey()))
                    .set(EAV_M_SIMPLE_SET.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable()))
                    .set(EAV_M_SIMPLE_SET.IS_CUMULATIVE, DataUtils.convert(metaAttribute.isCumulative()))
                    .set(EAV_M_SIMPLE_SET.IS_FINAL, DataUtils.convert(metaAttribute.isFinal()))
                    .set(EAV_M_SIMPLE_SET.IS_REQUIRED, DataUtils.convert(metaAttribute.isRequired()))
                    .set(EAV_M_SIMPLE_SET.IS_IMMUTABLE, DataUtils.convert(metaAttribute.isImmutable()))
                    .set(EAV_M_SIMPLE_SET.ARRAY_KEY_TYPE, metaSet.getArrayKeyType().toString())
                    .set(EAV_M_SIMPLE_SET.IS_DISABLED, DataUtils.convert(metaAttribute.isDisabled()))
                    .set(EAV_M_SIMPLE_SET.IS_REFERENCE, DataUtils.convert(metaSet.isReference()));
        }

        id = insertWithId(insert.getSQL(), insert.getBindValues().toArray());
        metaSet.setId(id);

        return id;
    }

    private long saveAttribute(IMetaType type, long parentId, int parentType, IMetaAttribute metaAttribute,
                               String attributeName, String attributeTitle) {
        InsertOnDuplicateStep insert;

        if (type.isSet())
            throw new IllegalStateException(Errors.compose(Errors.E167, attributeName));

        if (type.isComplex()) {
            long innerId = save((MetaClass) type);

            insert = context.insertInto(
                    EAV_M_COMPLEX_ATTRIBUTES,
                    EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID,
                    EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE,
                    EAV_M_COMPLEX_ATTRIBUTES.NAME,
                    EAV_M_COMPLEX_ATTRIBUTES.TITLE,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_KEY,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_OPTIONAL_KEY,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_NULLABLE_KEY,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_NULLABLE,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_IMMUTABLE,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_REQUIRED,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_FINAL,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_REQUIRED,
                    EAV_M_COMPLEX_ATTRIBUTES.IS_DISABLED,
                    EAV_M_COMPLEX_ATTRIBUTES.CLASS_ID
            ).values(
                    parentId,
                    parentType,
                    attributeName,
                    attributeTitle,
                    DataUtils.convert(metaAttribute.isKey()),
                    DataUtils.convert(metaAttribute.isOptionalKey()),
                    DataUtils.convert(metaAttribute.isNullableKey()),
                    DataUtils.convert(metaAttribute.isNullable()),
                    DataUtils.convert(metaAttribute.isImmutable()),
                    DataUtils.convert(metaAttribute.isRequired()),
                    DataUtils.convert(metaAttribute.isFinal()),
                    DataUtils.convert(metaAttribute.isRequired()),
                    DataUtils.convert(metaAttribute.isDisabled()),
                    innerId);
        } else {
            insert = context.insertInto(
                    EAV_M_SIMPLE_ATTRIBUTES,
                    EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID,
                    EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE,
                    EAV_M_SIMPLE_ATTRIBUTES.NAME,
                    EAV_M_SIMPLE_ATTRIBUTES.TITLE,
                    EAV_M_SIMPLE_ATTRIBUTES.TYPE_CODE,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_KEY,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_OPTIONAL_KEY,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_NULLABLE_KEY,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_NULLABLE,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_IMMUTABLE,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_FINAL,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_REQUIRED,
                    EAV_M_SIMPLE_ATTRIBUTES.IS_DISABLED
            ).values(parentId,
                    parentType,
                    attributeName,
                    attributeTitle,
                    ((MetaValue) type).getTypeCode().toString(),
                    DataUtils.convert(metaAttribute.isKey()),
                    DataUtils.convert(metaAttribute.isOptionalKey()),
                    DataUtils.convert(metaAttribute.isNullableKey()),
                    DataUtils.convert(metaAttribute.isNullable()),
                    DataUtils.convert(metaAttribute.isImmutable()),
                    DataUtils.convert(metaAttribute.isFinal()),
                    DataUtils.convert(metaAttribute.isRequired()),
                    DataUtils.convert(metaAttribute.isDisabled()));
        }

        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    private void insertAttributes(Set<String> addNames, MetaClass meta) {
        if (meta.getId() < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E161));

        for (String typeName : addNames) {
            IMetaAttribute metaAttribute = meta.getMetaAttribute(typeName);
            IMetaType metaType = metaAttribute.getMetaType();

            if (metaType.isSet()) {
                saveSet(metaType, meta.getId(), MetaContainerTypes.META_CLASS, metaAttribute, typeName);
            } else {
                long attributeId = saveAttribute(metaType, meta.getId(), MetaContainerTypes.META_CLASS,
                        metaAttribute, typeName, metaAttribute.getTitle());

                metaAttribute.setId(attributeId);
            }
        }
    }

    private void updateAttributes(Set<String> updateNames, MetaClass meta, MetaClass dbMeta) {
        if (dbMeta.getId() < 1) {
            throw new IllegalArgumentException(Errors.compose(Errors.E169));
        }

        UpdateConditionStep update;

        for (String typeName : updateNames) {
            IMetaAttribute metaAttribute = meta.getMetaAttribute(typeName);

            if (meta.getMemberType(typeName).isSet()) {
                if (meta.getMemberType(typeName).isComplex()) {
                    update = context.update(EAV_M_COMPLEX_SET)
                            .set(EAV_M_COMPLEX_SET.TITLE, metaAttribute.getTitle())
                            .set(EAV_M_COMPLEX_SET.IS_KEY, DataUtils.convert(metaAttribute.isKey()))
                            .set(EAV_M_COMPLEX_SET.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable()))
                            .set(EAV_M_COMPLEX_SET.IS_CUMULATIVE, DataUtils.convert(metaAttribute.isCumulative()))
                            .set(EAV_M_COMPLEX_SET.IS_FINAL, DataUtils.convert(metaAttribute.isFinal()))
                            .set(EAV_M_COMPLEX_SET.IS_IMMUTABLE, DataUtils.convert(metaAttribute.isImmutable()))
                            .set(EAV_M_COMPLEX_SET.IS_DISABLED, DataUtils.convert(metaAttribute.isDisabled()))
                            .set(EAV_M_COMPLEX_SET.IS_REQUIRED, DataUtils.convert(metaAttribute.isRequired()))
                            .where(EAV_M_COMPLEX_SET.CONTAINING_ID.eq(dbMeta.getId()))
                            .and(EAV_M_COMPLEX_SET.CONTAINER_TYPE.eq(MetaContainerTypes.META_CLASS))
                            .and(EAV_M_COMPLEX_SET.NAME.eq(typeName));
                } else {
                    update = context.update(EAV_M_SIMPLE_SET)
                            .set(EAV_M_SIMPLE_SET.TITLE, metaAttribute.getTitle())
                            .set(EAV_M_SIMPLE_SET.IS_KEY, DataUtils.convert(metaAttribute.isKey()))
                            .set(EAV_M_SIMPLE_SET.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable()))
                            .set(EAV_M_SIMPLE_SET.IS_CUMULATIVE, DataUtils.convert(metaAttribute.isCumulative()))
                            .set(EAV_M_SIMPLE_SET.IS_FINAL, DataUtils.convert(metaAttribute.isFinal()))
                            .set(EAV_M_SIMPLE_SET.IS_IMMUTABLE, DataUtils.convert(metaAttribute.isImmutable()))
                            .set(EAV_M_SIMPLE_SET.IS_DISABLED, DataUtils.convert(metaAttribute.isDisabled()))
                            .set(EAV_M_SIMPLE_SET.IS_REQUIRED, DataUtils.convert(metaAttribute.isRequired()))
                            .where(EAV_M_SIMPLE_SET.CONTAINING_ID.eq(dbMeta.getId()))
                            .and(EAV_M_SIMPLE_SET.CONTAINER_TYPE.eq(MetaContainerTypes.META_CLASS))
                            .and(EAV_M_SIMPLE_SET.NAME.eq(typeName));
                }
            } else {
                if (meta.getMemberType(typeName).isComplex()) {
                    update = context.update(EAV_M_COMPLEX_ATTRIBUTES)
                            .set(EAV_M_COMPLEX_ATTRIBUTES.IS_KEY, DataUtils.convert(metaAttribute.isKey()))
                            .set(EAV_M_COMPLEX_ATTRIBUTES.IS_OPTIONAL_KEY, DataUtils.convert(metaAttribute.isOptionalKey()))
                            .set(EAV_M_COMPLEX_ATTRIBUTES.IS_NULLABLE_KEY, DataUtils.convert(metaAttribute.isNullableKey()))
                            .set(EAV_M_COMPLEX_ATTRIBUTES.TITLE, metaAttribute.getTitle())
                            .set(EAV_M_COMPLEX_ATTRIBUTES.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable()))
                            .set(EAV_M_COMPLEX_ATTRIBUTES.IS_IMMUTABLE, DataUtils.convert(metaAttribute.isImmutable()))
                            .set(EAV_M_COMPLEX_ATTRIBUTES.IS_FINAL, DataUtils.convert(metaAttribute.isFinal()))
                            .set(EAV_M_COMPLEX_ATTRIBUTES.IS_REQUIRED, DataUtils.convert(metaAttribute.isRequired()))
                            .set(EAV_M_COMPLEX_ATTRIBUTES.IS_DISABLED, DataUtils.convert(metaAttribute.isDisabled()))
                            .where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(dbMeta.getId()))
                            .and(EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE.eq(MetaContainerTypes.META_CLASS))
                            .and(EAV_M_COMPLEX_ATTRIBUTES.NAME.eq(typeName));
                } else {
                    update = context.update(EAV_M_SIMPLE_ATTRIBUTES)
                            .set(EAV_M_SIMPLE_ATTRIBUTES.IS_KEY, DataUtils.convert(metaAttribute.isKey()))
                            .set(EAV_M_SIMPLE_ATTRIBUTES.IS_OPTIONAL_KEY, DataUtils.convert(metaAttribute.isOptionalKey()))
                            .set(EAV_M_SIMPLE_ATTRIBUTES.IS_NULLABLE_KEY, DataUtils.convert(metaAttribute.isNullableKey()))
                            .set(EAV_M_SIMPLE_ATTRIBUTES.TITLE, metaAttribute.getTitle())
                            .set(EAV_M_SIMPLE_ATTRIBUTES.IS_NULLABLE, DataUtils.convert(metaAttribute.isNullable()))
                            .set(EAV_M_SIMPLE_ATTRIBUTES.IS_IMMUTABLE, DataUtils.convert(metaAttribute.isImmutable()))
                            .set(EAV_M_SIMPLE_ATTRIBUTES.IS_FINAL, DataUtils.convert(metaAttribute.isFinal()))
                            .set(EAV_M_SIMPLE_ATTRIBUTES.IS_REQUIRED, DataUtils.convert(metaAttribute.isRequired()))
                            .set(EAV_M_SIMPLE_ATTRIBUTES.IS_DISABLED, DataUtils.convert(metaAttribute.isDisabled()))
                            .where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(dbMeta.getId()))
                            .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(MetaContainerTypes.META_CLASS))
                            .and(EAV_M_SIMPLE_ATTRIBUTES.NAME.eq(typeName));
                }
            }

            logger.debug(update.toString());

            long t = System.currentTimeMillis();

            jdbcTemplate.update(update.getSQL(), update.getBindValues().toArray());

            sqlStats.put(update.getSQL(), (System.currentTimeMillis() - t));
        }
    }

    private void deleteAttributes(Set<String> deleteNames, MetaClass meta) {
        DeleteConditionStep delete;

        if (meta.getId() < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E159));

        for (String typeName : deleteNames) {
            if (meta.getMemberType(typeName).isComplex()) {
                if (!meta.getMemberType(typeName).isSet()) {
                    delete = context.delete(EAV_M_COMPLEX_ATTRIBUTES)
                            .where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(meta.getId()))
                            .and(EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE.eq(MetaContainerTypes.META_CLASS))
                            .and(EAV_M_COMPLEX_ATTRIBUTES.NAME.eq(typeName));
                } else {
                    delete = context.delete(EAV_M_COMPLEX_SET)
                            .where(EAV_M_COMPLEX_SET.CONTAINING_ID.eq(meta.getId()))
                            .and(EAV_M_COMPLEX_SET.CONTAINER_TYPE.eq(MetaContainerTypes.META_CLASS))
                            .and(EAV_M_COMPLEX_SET.NAME.eq(typeName));
                }
            } else {
                if (!meta.getMemberType(typeName).isSet()) {
                    delete = context.delete(EAV_M_SIMPLE_ATTRIBUTES)
                            .where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(meta.getId()))
                            .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(MetaContainerTypes.META_CLASS))
                            .and(EAV_M_SIMPLE_ATTRIBUTES.NAME.eq(typeName));
                } else {
                    delete = context.delete(EAV_M_SIMPLE_SET)
                            .where(EAV_M_SIMPLE_SET.CONTAINING_ID.eq(meta.getId()))
                            .and(EAV_M_SIMPLE_SET.CONTAINER_TYPE.eq(MetaContainerTypes.META_CLASS))
                            .and(EAV_M_SIMPLE_SET.NAME.eq(typeName));
                }
            }

            logger.debug(delete.toString());

            long t = 0;
            if (sqlStats != null) t = System.currentTimeMillis();

            jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());

            if (sqlStats != null)
                sqlStats.put(delete.getSQL(), (System.currentTimeMillis() - t));
        }
    }

    private void loadSimpleAttributes(IMetaContainer meta) {
        SelectForUpdateStep select = context.select(
                EAV_M_SIMPLE_ATTRIBUTES.ID,
                EAV_M_SIMPLE_ATTRIBUTES.NAME,
                EAV_M_SIMPLE_ATTRIBUTES.TITLE,
                EAV_M_SIMPLE_ATTRIBUTES.TYPE_CODE,
                EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE,
                EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID,
                EAV_M_SIMPLE_ATTRIBUTES.IS_KEY,
                EAV_M_SIMPLE_ATTRIBUTES.IS_OPTIONAL_KEY,
                EAV_M_SIMPLE_ATTRIBUTES.IS_NULLABLE_KEY,
                EAV_M_SIMPLE_ATTRIBUTES.IS_NULLABLE,
                EAV_M_SIMPLE_ATTRIBUTES.IS_IMMUTABLE,
                EAV_M_SIMPLE_ATTRIBUTES.IS_FINAL,
                EAV_M_SIMPLE_ATTRIBUTES.IS_REQUIRED,
                EAV_M_SIMPLE_ATTRIBUTES.IS_DISABLED)
                .from(EAV_M_SIMPLE_ATTRIBUTES)
                .where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.equal(meta.getId()))
                .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.equal(meta.getType()));

        logger.debug(select.toString());

        long t = System.currentTimeMillis();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());

       sqlStats.put(select.getSQL(), (System.currentTimeMillis() - t));

        for (Map<String, Object> row : rows) {
            MetaAttribute metaAttribute = new MetaAttribute(
                    ((BigDecimal) row.get("id")).longValue(),
                    ((BigDecimal) row.get("is_key")).longValue() == 1,
                    ((BigDecimal) row.get("is_nullable")).longValue() == 1);

            metaAttribute.setOptionalKey(((BigDecimal) row.get("is_optional_key")).longValue() == 1);
            metaAttribute.setNullableKey(((BigDecimal) row.get("is_nullable_key")).longValue() == 1);
            metaAttribute.setFinal(((BigDecimal) row.get("is_final")).longValue() == 1);
            metaAttribute.setRequired(((BigDecimal) row.get("is_required")).longValue() == 1);
            metaAttribute.setImmutable(((BigDecimal) row.get("is_immutable")).longValue() == 1);
            metaAttribute.setDisabled(((BigDecimal) row.get("is_disabled")).longValue() == 1);
            metaAttribute.setMetaType(new MetaValue(DataTypes.valueOf((String) row.get("type_code"))));
            metaAttribute.setTitle((String) row.get("title"));

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);
        }
    }

    private void loadSimpleArrays(IMetaContainer meta) {
        SelectForUpdateStep select = context.select(
                EAV_M_SIMPLE_SET.ID,
                EAV_M_SIMPLE_SET.NAME,
                EAV_M_SIMPLE_SET.TITLE,
                EAV_M_SIMPLE_SET.CONTAINER_TYPE,
                EAV_M_SIMPLE_SET.CONTAINING_ID,
                EAV_M_SIMPLE_SET.IS_KEY,
                EAV_M_SIMPLE_SET.IS_NULLABLE,
                EAV_M_SIMPLE_SET.IS_CUMULATIVE,
                EAV_M_SIMPLE_SET.IS_IMMUTABLE,
                EAV_M_SIMPLE_SET.IS_REQUIRED,
                EAV_M_SIMPLE_SET.TYPE_CODE,
                EAV_M_SIMPLE_SET.ARRAY_KEY_TYPE,
                EAV_M_SIMPLE_SET.IS_REFERENCE,
                EAV_M_SIMPLE_SET.IS_DISABLED)
                .from(EAV_M_SIMPLE_SET)
                .where(EAV_M_SIMPLE_SET.CONTAINING_ID.eq(meta.getId()))
                .and(EAV_M_SIMPLE_SET.CONTAINER_TYPE.eq(meta.getType()));

        long t = System.currentTimeMillis();

        logger.debug(select.toString());

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());

        sqlStats.put(select.getSQL(), (System.currentTimeMillis() - t));

        for (Map<String, Object> row : rows) {

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((BigDecimal) row.get("id")).longValue(),
                    ((BigDecimal) row.get("is_key")).longValue() == 1,
                    ((BigDecimal) row.get("is_nullable")).longValue() == 1);

            metaAttribute.setImmutable(((BigDecimal) row.get("is_immutable")).longValue() == 1);
            metaAttribute.setCumulative(((BigDecimal) row.get("is_cumulative")).longValue() == 1);
            metaAttribute.setDisabled(((BigDecimal) row.get("is_disabled")).longValue() == 1);


            MetaSet metaSet = new MetaSet(new MetaValue(DataTypes.valueOf((String) row.get("type_code"))));
            metaSet.setId(((BigDecimal) row.get("id")).longValue());

            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));
            metaSet.setReference(((BigDecimal) row.get("is_reference")).longValue() == 1);

            metaAttribute.setMetaType(metaSet);
            metaAttribute.setTitle((String) row.get("title"));

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);
        }
    }

    private void loadComplexAttributes(IMetaContainer meta) {
        SelectForUpdateStep select = context.select(
                EAV_M_COMPLEX_ATTRIBUTES.ID,
                EAV_M_COMPLEX_ATTRIBUTES.IS_KEY,
                EAV_M_COMPLEX_ATTRIBUTES.IS_OPTIONAL_KEY,
                EAV_M_COMPLEX_ATTRIBUTES.IS_NULLABLE_KEY,
                EAV_M_COMPLEX_ATTRIBUTES.IS_NULLABLE,
                EAV_M_COMPLEX_ATTRIBUTES.IS_FINAL,
                EAV_M_COMPLEX_ATTRIBUTES.IS_REQUIRED,
                EAV_M_COMPLEX_ATTRIBUTES.IS_IMMUTABLE,
                EAV_M_COMPLEX_ATTRIBUTES.NAME,
                EAV_M_COMPLEX_ATTRIBUTES.TITLE,
                EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE,
                EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID,
                EAV_M_COMPLEX_ATTRIBUTES.CLASS_ID,
                EAV_M_CLASSES.NAME.as("cname"),
                EAV_M_COMPLEX_ATTRIBUTES.IS_DISABLED)
                .from(EAV_M_COMPLEX_ATTRIBUTES).leftOuterJoin(EAV_M_CLASSES)
                .on(EAV_M_COMPLEX_ATTRIBUTES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                .where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(meta.getId()))
                .and(EAV_M_COMPLEX_ATTRIBUTES.CONTAINER_TYPE.eq(meta.getType()));

        long t = System.currentTimeMillis();

        logger.debug(select.toString());

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());

        sqlStats.put(select.getSQL(), (System.currentTimeMillis() - t));

        for (Map<String, Object> row : rows) {
            MetaClass metaClass = load(((BigDecimal) row.get("class_id")).longValue());

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((BigDecimal) row.get("id")).longValue(),
                    ((BigDecimal) row.get("is_key")).longValue() == 1,
                    ((BigDecimal) row.get("is_nullable")).longValue() == 1);

            metaAttribute.setOptionalKey(((BigDecimal) row.get("is_optional_key")).longValue() == 1);
            metaAttribute.setNullableKey(((BigDecimal) row.get("is_nullable_key")).longValue() == 1);
            metaAttribute.setImmutable(((BigDecimal) row.get("is_immutable")).longValue() == 1);
            metaAttribute.setFinal(((BigDecimal) row.get("is_final")).longValue() == 1);
            metaAttribute.setRequired(((BigDecimal) row.get("is_required")).longValue() == 1);
            metaAttribute.setDisabled(((BigDecimal) row.get("is_disabled")).longValue() == 1);
            metaAttribute.setMetaType(metaClass);
            metaAttribute.setTitle((String) row.get("title"));

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);
        }
    }

    private void loadComplexArrays(IMetaContainer meta) {
        SelectForUpdateStep select = context.select(
                EAV_M_COMPLEX_SET.ID,
                EAV_M_COMPLEX_SET.IS_NULLABLE,
                EAV_M_COMPLEX_SET.IS_CUMULATIVE,
                EAV_M_COMPLEX_SET.IS_IMMUTABLE,
                EAV_M_COMPLEX_SET.IS_KEY,
                EAV_M_COMPLEX_SET.IS_FINAL,
                EAV_M_COMPLEX_SET.IS_REQUIRED,
                EAV_M_COMPLEX_SET.NAME,
                EAV_M_COMPLEX_SET.TITLE,
                EAV_M_COMPLEX_SET.CLASS_ID,
                EAV_M_COMPLEX_SET.ARRAY_KEY_TYPE,
                EAV_M_COMPLEX_SET.CONTAINER_TYPE,
                EAV_M_COMPLEX_SET.CONTAINING_ID,
                EAV_M_COMPLEX_SET.IS_REFERENCE,
                EAV_M_CLASSES.NAME.as("cname"),
                EAV_M_COMPLEX_SET.IS_DISABLED)
                .from(EAV_M_COMPLEX_SET)
                .leftOuterJoin(EAV_M_CLASSES).on(EAV_M_COMPLEX_SET.CLASS_ID.equal(EAV_M_CLASSES.ID))
                .where(EAV_M_COMPLEX_SET.CONTAINING_ID.equal(meta.getId()))
                .and(EAV_M_COMPLEX_SET.CONTAINER_TYPE.equal(meta.getType()));

        long t = System.currentTimeMillis();

        logger.debug(select.toString());

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());

        sqlStats.put(select.getSQL(), (System.currentTimeMillis() - t));

        for (Map<String, Object> row : rows) {
            MetaClass metaClass = load(((BigDecimal) row.get("class_id")).longValue());

            MetaAttribute metaAttribute = new MetaAttribute(
                    ((BigDecimal) row.get("id")).longValue(),
                    ((BigDecimal) row.get("is_key")).longValue() == 1,
                    ((BigDecimal) row.get("is_nullable")).longValue() == 1);

            metaAttribute.setImmutable(((BigDecimal) row.get("is_immutable")).longValue() == 1);
            metaAttribute.setCumulative(((BigDecimal) row.get("is_cumulative")).longValue() == 1);
            metaAttribute.setFinal(((BigDecimal) row.get("is_final")).longValue() == 1);
            metaAttribute.setDisabled(((BigDecimal) row.get("is_disabled")).longValue() == 1);
            metaAttribute.setRequired(((BigDecimal) row.get("is_required")).longValue() == 1);

            MetaSet metaSet = new MetaSet(metaClass);
            metaSet.setId(((BigDecimal) row.get("id")).longValue());
            metaSet.setArrayKeyType(ComplexKeyTypes.valueOf((String) row.get("array_key_type")));
            metaSet.setReference(((BigDecimal) row.get("is_reference")).longValue() == 1);

            metaAttribute.setMetaType(metaSet);
            metaAttribute.setTitle((String) row.get("title"));

            meta.setMetaAttribute((String) row.get("name"), metaAttribute);
        }
    }

    private void loadAttributes(MetaClass meta) {
        if (meta.getId() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E164));

        meta.removeMembers();

        loadSimpleAttributes(meta);
        loadSimpleArrays(meta);
        loadComplexAttributes(meta);
        loadComplexArrays(meta);
    }

    @Transactional
    public long save(MetaClass meta) {
        MetaClass dbMeta = new MetaClass(meta);

        try {
            loadClass(dbMeta, true);
            loadAttributes(dbMeta);
            meta.setId(dbMeta.getId());
            updateClass(meta);
        } catch (IllegalArgumentException e) {
            logger.debug("Class: " + meta.getClassName() + " not found.");
            createClass(dbMeta);
            dbMeta.removeMembers();
            meta.setId(dbMeta.getId());
            logger.debug("Class: " + meta.getClassName() + " created.");
        }

        if (dbMeta.getId() < 1) {
            throw new IllegalArgumentException(Errors.compose(Errors.E166));
        }

        Set<String> oldNames = dbMeta.getMemberNames();
        Set<String> newNames = meta.getMemberNames();

        Set<String> updateNames = SetUtils.intersection(oldNames, newNames);
        Set<String> deleteNames = SetUtils.difference(oldNames, newNames);
        Set<String> addNames = SetUtils.difference(newNames, oldNames);

        Iterator<String> i = updateNames.iterator();
        while (i.hasNext()) {
            String name = i.next();
            if (!meta.getMemberType(name).equals(dbMeta.getMemberType(name))) {
                deleteNames.add(name);
                addNames.add(name);
                i.remove();
            } else {
                if (meta.getMetaAttribute(name).equals(dbMeta.getMetaAttribute(name))) {
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
    public MetaClass loadDisabled(String className) {
        MetaClass meta = new MetaClass(className);

        loadAllClass(meta, false);
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
        if (id < 1)
            return null;

        MetaClass meta = new MetaClass();
        meta.setId(id);

        loadClass(meta, false);
        loadAttributes(meta);

        return meta;
    }

    @Override
    public List<Long> loadContaining(long id) {
        List<Long> containingList = new ArrayList<>();

        if (id < 1)
            return null;

        Select selectComplexSetIds = context
                .select(EAV_M_COMPLEX_SET.CONTAINING_ID)
                .from(EAV_M_COMPLEX_SET)
                .where(EAV_M_COMPLEX_SET.CLASS_ID.equal(id));

        List<Map<String, Object>> complexSetIds = jdbcTemplate.queryForList(selectComplexSetIds.getSQL(),
                selectComplexSetIds.getBindValues().toArray());

        Select selectComplexIds = context
                .select(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID)
                .from(EAV_M_COMPLEX_ATTRIBUTES)
                .where(EAV_M_COMPLEX_ATTRIBUTES.CLASS_ID.equal(id));

        List<Map<String, Object>> complexIds = jdbcTemplate.queryForList(selectComplexIds.getSQL(),
                selectComplexIds.getBindValues().toArray());

        for (Map<String, Object> complexSetId : complexSetIds)
            containingList.add(Long.parseLong(complexSetId.get("CONTAINING_ID").toString()));

        for (Map<String, Object> complexId : complexIds)
            containingList.add(Long.parseLong(complexId.get("CONTAINING_ID").toString()));

        return containingList;
    }

    @Override
    @Transactional
    public void remove(MetaClass metaClass) {
        if (metaClass.getId() < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E165));

        long t = System.currentTimeMillis();

        UpdateConditionStep<EavMClassesRecord> delete = context
                .update(EAV_M_CLASSES)
                .set(EAV_M_CLASSES.IS_DISABLED, new Byte("1"))
                .where(EAV_M_CLASSES.ID.equal(metaClass.getId()));

        logger.debug(delete.toString());

        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());

        if (sqlStats != null) sqlStats.put(delete.getSQL(), (System.currentTimeMillis() - t));
    }

    @SuppressWarnings("UnusedDeclaration")
    public DSLContext getDSLContext() {
        return context;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setDSLContext(DSLContext context) {
        this.context = context;
    }

    @Override
    public List<MetaClassName> getMetaClassesNames() {
        return getMetaClassesNames(false);
    }

    @Override
    public List<MetaClassName> getRefNames() {
        return getMetaClassesNames(true);
    }

    public List<MetaClassName> getMetaClassesNames(boolean refs) {

        ArrayList<MetaClassName> metaClassNameList = new ArrayList<>();
        SelectForUpdateStep select;
        SelectJoinStep join;

        join = context.select(EAV_M_CLASSES.ID,
                EAV_M_CLASSES.NAME,
                EAV_M_CLASSES.TITLE,
                EAV_M_CLASSES.IS_DISABLED,
                EAV_M_CLASSES.IS_REFERENCE)
                .from(EAV_M_CLASSES);

        if (refs)
            select = join.where(EAV_M_CLASSES.IS_REFERENCE.eq((byte) 1));
        else
            select = join;

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E160));

        for (Map<String, Object> row : rows) {

            MetaClassName metaClassName = new MetaClassName();

            metaClassName.setId(((BigDecimal) row.get("id")).longValue());
            metaClassName.setClassName((String) row.get("name"));
            metaClassName.setClassTitle((String) row.get("title"));
            metaClassName.setDisabled(((BigDecimal) row.get("is_disabled")).longValue() == 1);
            metaClassName.setReference(((BigDecimal) row.get("is_reference")).longValue() == 1);
            metaClassNameList.add(metaClassName);
        }

        return metaClassNameList;
    }
}
