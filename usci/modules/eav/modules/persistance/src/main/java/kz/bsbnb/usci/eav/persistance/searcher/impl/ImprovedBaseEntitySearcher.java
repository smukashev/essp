package kz.bsbnb.usci.eav.persistance.searcher.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.exceptions.KnownException;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Component
public class ImprovedBaseEntitySearcher extends JDBCSupport implements IBaseEntitySearcher {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    private final Logger logger = LoggerFactory.getLogger(ImprovedBaseEntitySearcher.class);

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public Long findSingle(BaseEntity entity, Long creditorId) {
        if (entity.getId() > 0)
            return entity.getId();

        if (entity.getValueCount() == 0)
            return null;

        SelectConditionStep select = generateSQL(entity, creditorId, null);

        if (select != null) {
            logger.debug(select.toString());
            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(),
                    select.getBindValues().toArray());

            if (rows.size() > 1)
                throw new IllegalStateException("Найдено больше одной записи(" +
                        entity.getMeta().getClassName() + "), " + entity);

            if (rows.size() < 1)
                return null;

            return ((BigDecimal) rows.get(0).get("inner_id")).longValue();
        }

        return null;
    }

    public SelectConditionStep generateSQL(IBaseEntity entity, Long creditorId, String entityName) {
        return generateSQL(entity, creditorId, entityName, null);
    }

    public SelectConditionStep generateSQL(IBaseEntity entity, Long creditorId, String entityName,
                                           HashMap<String, ArrayList<String>> arrayKeyFilter) {
        MetaClass metaClass = entity.getMeta();
        String entityAlias = (entityName == null ? "root" : "e_" + entityName);

        SelectJoinStep joins = context.select(EAV_BE_ENTITIES.as(entityAlias).ID.as("inner_id"))
                .from(EAV_BE_ENTITIES.as(entityAlias));

        if (metaClass == null)
            throw new IllegalArgumentException("Метакласс не может быть NULL;");

        Set<String> names = metaClass.getMemberNames();

        Condition condition = null;
        for (String name : names) {
            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(name);
            IMetaType memberType = metaClass.getMemberType(name);

            if (metaAttribute.isKey()) {
                IBaseValue baseValue = entity.safeGetValue(name);

                if ((baseValue == null || baseValue.getValue() == null) &&
                        metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                    throw new KnownException("Ключевой атрибут(" + name + ") не может быть пустым. " +
                            "Мета класс: " + entity.getMeta().getClassName() + ";");


                if ((baseValue == null || baseValue.getValue() == null) &&
                        (metaClass.getComplexKeyType() == ComplexKeyTypes.ANY)) continue;

                if (!memberType.isSet()) {
                    if (!memberType.isComplex()) {
                        generateJoins(joins, entityAlias, name, memberType, creditorId, metaAttribute);

                        MetaValue metaValue = (MetaValue) memberType;
                        switch (metaValue.getTypeCode()) {
                            case BOOLEAN:
                                Boolean booleanValue = (Boolean) baseValue.getValue();
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_BOOLEAN_VALUES.as(valueAlias).VALUE.equal(DataUtils.convert(booleanValue))
                                                    .and(EAV_BE_BOOLEAN_VALUES.IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_BOOLEAN_VALUES.IS_LAST.equal(DataUtils.convert(true))) :
                                            condition.and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).VALUE.equal(DataUtils.convert(booleanValue)))
                                                    .and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true)));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_BOOLEAN_VALUES.as(valueAlias))
                                            .where(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID)
                                                    .and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).VALUE.equal(DataUtils.convert(booleanValue)))
                                                    .and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true))));

                                    condition = condition == null ? DSL.exists(select) : condition.or(DSL.exists(select));
                                }
                                break;
                            case DATE:
                                Date dateValue = DataUtils.convert((java.util.Date) baseValue.getValue());
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_DATE_VALUES.as(valueAlias).VALUE.equal(dateValue)
                                                    .and(EAV_BE_DATE_VALUES.IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_DATE_VALUES.IS_LAST.equal(DataUtils.convert(true))) :
                                            condition.and(EAV_BE_DATE_VALUES.as(valueAlias).VALUE.equal(dateValue))
                                                    .and(EAV_BE_DATE_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_DATE_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true)));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_DATE_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_DATE_VALUES.as(valueAlias))
                                            .where(EAV_BE_DATE_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID)
                                                    .and(EAV_BE_DATE_VALUES.as(valueAlias).VALUE.equal(dateValue))
                                                    .and(EAV_BE_DATE_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_DATE_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true))));

                                    condition = condition == null ? DSL.exists(select) :
                                            condition.or(DSL.exists(select));
                                }
                                break;
                            case DOUBLE:
                                Double doubleValue = (Double) baseValue.getValue();
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_DOUBLE_VALUES.as(valueAlias).VALUE.equal(doubleValue)
                                                    .and(EAV_BE_DOUBLE_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_DOUBLE_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true))) :
                                            condition.and(EAV_BE_DOUBLE_VALUES.as(valueAlias).VALUE.equal(doubleValue))
                                                    .and(EAV_BE_DOUBLE_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_DOUBLE_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true)));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_DOUBLE_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_DOUBLE_VALUES.as(valueAlias))
                                            .where(EAV_BE_DOUBLE_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID)
                                                    .and(EAV_BE_DOUBLE_VALUES.as(valueAlias).VALUE.equal(doubleValue))
                                                    .and(EAV_BE_DOUBLE_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_DOUBLE_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true))));

                                    condition = condition == null ? DSL.exists(select) :
                                            condition.or(DSL.exists(select));
                                }
                                break;
                            case INTEGER:
                                Integer integerValue = (Integer) baseValue.getValue();
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_INTEGER_VALUES.as(valueAlias).VALUE.equal(integerValue)
                                                    .and(EAV_BE_INTEGER_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_INTEGER_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true))) :
                                            condition.and(EAV_BE_INTEGER_VALUES.as(valueAlias).VALUE.equal(integerValue))
                                                    .and(EAV_BE_INTEGER_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_INTEGER_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true)));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_INTEGER_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_INTEGER_VALUES.as(valueAlias))
                                            .where(EAV_BE_INTEGER_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID)
                                                    .and(EAV_BE_INTEGER_VALUES.as(valueAlias).VALUE.equal(integerValue))
                                                    .and(EAV_BE_INTEGER_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_INTEGER_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true))));

                                    condition = condition == null ? DSL.exists(select) : condition.or(DSL.exists(select));
                                }
                                break;
                            case STRING:
                                String stringValue = (String) baseValue.getValue();
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_STRING_VALUES.as(valueAlias).VALUE.equal(stringValue)
                                                    .and(EAV_BE_STRING_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_STRING_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true))) :
                                            condition.and(EAV_BE_STRING_VALUES.as(valueAlias).VALUE.equal(stringValue))
                                                    .and(EAV_BE_STRING_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_STRING_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true)));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_STRING_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_STRING_VALUES.as(valueAlias))
                                            .where(EAV_BE_STRING_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID)
                                                    .and(EAV_BE_STRING_VALUES.as(valueAlias).VALUE.equal(stringValue))
                                                    .and(EAV_BE_STRING_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                    .and(EAV_BE_STRING_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true))));

                                    condition = condition == null ? DSL.exists(select) : condition.or(DSL.exists(select));
                                }
                                break;
                            default:
                                throw new IllegalStateException("Неизвестный тип данных: " + metaValue.getTypeCode() +
                                        " для атрибута: " + name + ";");
                        }
                    } else {
                        BaseEntity childBaseEntity = (BaseEntity) baseValue.getValue();
                        Long childBaseEntityId = searcherPool.getSearcher(childBaseEntity.getMeta().getClassName()).
                                findSingle(childBaseEntity, creditorId);

                        if (childBaseEntityId == null) {
                            if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                                return null;
                        } else {
                            generateJoins(joins, entityAlias, name, memberType, creditorId, metaAttribute);

                            if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                String valueAlias = "v_" + name;
                                condition = condition == null ?
                                        EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_VALUE_ID.equal(childBaseEntityId) :
                                        condition.and(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_VALUE_ID.equal(childBaseEntityId))
                                                .and(EAV_BE_COMPLEX_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                .and(EAV_BE_COMPLEX_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true)));
                            } else {
                                String valueAlias = "v";
                                Select select = context
                                        .select(EAV_BE_COMPLEX_VALUES.as(valueAlias).ID).from(EAV_BE_COMPLEX_VALUES.as(valueAlias))
                                        .where(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID)
                                                .and(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_VALUE_ID.equal(childBaseEntityId))
                                                .and(EAV_BE_COMPLEX_VALUES.as(valueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                                .and(EAV_BE_COMPLEX_VALUES.as(valueAlias).IS_LAST.equal(DataUtils.convert(true))));

                                condition = condition == null ? DSL.exists(select) :
                                        condition.or(DSL.exists(select));
                            }
                        }
                    }
                } else {
                    BaseSet baseSet = (BaseSet) baseValue.getValue();
                    MetaSet metaSet = (MetaSet) memberType;
                    MetaClass childMetaClass = (MetaClass) metaSet.getMemberType();

                    if (baseSet.get().size() == 0)
                        throw new UnsupportedOperationException("Массив должен содержать элементы(" +
                                (childMetaClass).getClassName() + ";");

                    if (!memberType.isComplex())
                        throw new UnsupportedOperationException("Простой массив не может быть ключевым(" +
                                childMetaClass.getClassName() + ");");

                    String metaClassName = entity.getMeta().getClassName();

                    /* Обработка документов */
                    if (childMetaClass.getClassName().equals("document") && (metaClassName.equals("subject"))) {
                        List<IBaseValue> baseValues = new ArrayList<>(baseSet.get());
                        List<Long> identificationValues = new ArrayList<>();

                        String className = childMetaClass.getClassName();
                        String setValueAlias = "sv_" + className;
                        String entitySetAlias = "es_" + className;

                        Select select;

                        for (IBaseValue val : baseValues) {
                            BaseEntity document = (BaseEntity) val.getValue();
                            long docTypeId = ((BaseEntity) document.getBaseValue("doc_type").getValue()).getId();

                            IBaseEntity docType = baseEntityLoadDao.load(docTypeId);

                            boolean is_identification = (boolean) docType.getBaseValue("is_identification").getValue();

                            if (is_identification && document.getId() == 0)
                                return null;

                            if (is_identification)
                                identificationValues.add(document.getId());
                        }

                        MetaClass metaDocType = (MetaClass) childMetaClass.getMemberType("doc_type");

                        Collections.sort(identificationValues);

                        String identificationCondition = StringUtils.
                                arrayToDelimitedString(identificationValues.toArray(), ",");

                        String entitiesAlias = "sebe";
                        String complexValuesAlias = "sebcv";
                        String booleanValuesAlias = "sebbv";

                        Select smallSelect = context.select(EAV_BE_ENTITIES.as(entitiesAlias).ID)
                                .from(EAV_BE_ENTITIES.as(entitiesAlias))
                                .join(EAV_BE_COMPLEX_VALUES.as(complexValuesAlias)).
                                        on(EAV_BE_COMPLEX_VALUES.as(complexValuesAlias).ENTITY_ID.eq(
                                                EAV_BE_ENTITIES.as(entitiesAlias).ID)
                                                .and(EAV_BE_COMPLEX_VALUES.as(complexValuesAlias).ATTRIBUTE_ID.eq(
                                                        childMetaClass.getMetaAttribute("doc_type").getId())))
                                .join(EAV_BE_BOOLEAN_VALUES.as(booleanValuesAlias)).
                                        on(EAV_BE_BOOLEAN_VALUES.as(booleanValuesAlias).ENTITY_ID.eq(
                                                EAV_BE_COMPLEX_VALUES.as(complexValuesAlias).ENTITY_VALUE_ID)
                                                .and(EAV_BE_BOOLEAN_VALUES.as(booleanValuesAlias).ATTRIBUTE_ID.eq(
                                                        metaDocType.getMetaAttribute("is_identification").getId())
                                                        .and(EAV_BE_BOOLEAN_VALUES.as(booleanValuesAlias).VALUE.eq(
                                                                DataUtils.convert(true)))))
                                .where(EAV_BE_ENTITIES.as(entitiesAlias).CLASS_ID.eq(childMetaClass.getId()));

                        select = context.select(
                                DSL.field("listagg(\"" + setValueAlias + "\".\"ENTITY_VALUE_ID\", ',') " +
                                        "within group (order by \"" + setValueAlias + "\".\"ENTITY_VALUE_ID\" asc)")
                        ).from(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ATTRIBUTE_ID.eq(metaAttribute.getId()))
                                .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.eq(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                .where(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID.in(smallSelect)
                                        .and(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.eq(EAV_BE_ENTITIES.as(entityAlias).ID))
                                        .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                        .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).IS_LAST.equal(DataUtils.convert(true))));

                        Condition setCondition = select.asField().eq(identificationCondition);

                        condition = condition == null ? setCondition :
                                metaClass.getComplexKeyType() == ComplexKeyTypes.ALL ?
                                        condition.and(setCondition) : condition.or(setCondition);
                    } else {
                        List<Long> childBaseEntityIds = new ArrayList<>();

                        for (IBaseValue childBaseValue : baseSet.get()) {
                            BaseEntity childBaseEntity = (BaseEntity) childBaseValue.getValue();

                            Long childBaseEntityId = searcherPool.getSearcher(childBaseEntity.getMeta().
                                    getClassName()).findSingle(childBaseEntity, creditorId);

                            if (childBaseEntityId != null) {
                                childBaseEntityIds.add(childBaseEntityId);
                            } else {
                                if (metaSet.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    return null;
                            }
                        }

                        /* Ни один элемент ключевого массива не был идентифицирован */
                        if (childBaseEntityIds.size() == 0)
                            return null;

                        String className = childMetaClass.getClassName();
                        String setValueAlias = "sv_" + className;
                        String entitySetAlias = "es_" + className;
                        Select select;

                        if (metaSet.getArrayKeyType() == ComplexKeyTypes.ANY) {
                            select = context.select(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID)
                                    .from(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                    .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                    .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ATTRIBUTE_ID.equal(metaAttribute.getId()))
                                    .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.equal(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                    .where(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID)
                                            .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID.in(childBaseEntityIds))
                                            .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                            .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).IS_LAST.equal(DataUtils.convert(true))));

                            condition = condition == null ? DSL.exists(select) :
                                    metaClass.getComplexKeyType() == ComplexKeyTypes.ALL ?
                                            condition.and(DSL.exists(select)) : condition.or(DSL.exists(select));
                        } else {
                            Collections.sort(childBaseEntityIds);
                            String sChildBaseEntityIds = StringUtils.arrayToDelimitedString(childBaseEntityIds.toArray(), ", ");

                            select = context.select(
                                    DSL.field("listagg(\"" + setValueAlias + "\".\"ENTITY_VALUE_ID\", ', ') " +
                                            "within group (order by \"" + setValueAlias + "\".\"ENTITY_VALUE_ID\" asc)")
                            ).from(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                    .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                    .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ATTRIBUTE_ID.eq(metaAttribute.getId()))
                                    .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.eq(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                    .where(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.eq(EAV_BE_ENTITIES.as(entityAlias).ID)
                                            .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).IS_CLOSED.equal(DataUtils.convert(false)))
                                            .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).IS_LAST.equal(DataUtils.convert(true))));

                            Condition setCondition = select.asField().eq(sChildBaseEntityIds);

                            condition = condition == null ? setCondition :
                                    metaClass.getComplexKeyType() == ComplexKeyTypes.ALL ?
                                            condition.and(setCondition) : condition.or(setCondition);
                        }
                    }
                }
            }
        }

        SelectConditionStep where = joins.where(EAV_BE_ENTITIES.as(entityAlias).CLASS_ID.equal(metaClass.getId()))
                .and(EAV_BE_ENTITIES.as(entityAlias).DELETED.equal(DataUtils.convert(false)));

        if (condition != null) {
            where = where.and(condition);
        } else {
            return null;
        }

        return where;
    }

    private SelectJoinStep generateJoins(SelectJoinStep joins, String entityAlias, String name, IMetaType type,
                                         Long creditorId, IMetaAttribute attribute) {
        String valueAlias = "v_" + name;
        if (!type.isSet()) {
            if (!type.isComplex()) {
                MetaValue metaValue = (MetaValue) type;

                switch (metaValue.getTypeCode()) {
                    case BOOLEAN:
                        joins = joins.join(EAV_BE_BOOLEAN_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).CREDITOR_ID.equal(creditorId).
                                                and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ATTRIBUTE_ID.
                                                        equal(attribute.getId()))));
                        break;
                    case DATE:
                        joins = joins.join(EAV_BE_DATE_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_DATE_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_DATE_VALUES.as(valueAlias).CREDITOR_ID.equal(creditorId)).
                                        and(EAV_BE_DATE_VALUES.as(valueAlias).ATTRIBUTE_ID.equal(attribute.getId())));
                        break;
                    case DOUBLE:
                        joins = joins.join(EAV_BE_DOUBLE_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_DOUBLE_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_DOUBLE_VALUES.as(valueAlias).CREDITOR_ID.
                                                equal(creditorId)).
                                        and(EAV_BE_DOUBLE_VALUES.as(valueAlias).ATTRIBUTE_ID.
                                                equal(attribute.getId())));
                        break;
                    case INTEGER:
                        joins = joins.join(EAV_BE_INTEGER_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_INTEGER_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_INTEGER_VALUES.as(valueAlias).CREDITOR_ID.
                                                equal(creditorId)).
                                        and(EAV_BE_INTEGER_VALUES.as(valueAlias).ATTRIBUTE_ID.
                                                equal(attribute.getId())));
                        break;
                    case STRING:
                        joins = joins.join(EAV_BE_STRING_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_STRING_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_STRING_VALUES.as(valueAlias).CREDITOR_ID.
                                                equal(creditorId)).
                                        and(EAV_BE_STRING_VALUES.as(valueAlias).ATTRIBUTE_ID.
                                                equal(attribute.getId())));
                        break;
                    default:
                        throw new IllegalStateException("Неизвестный тип данных: " + metaValue.getTypeCode() +
                                " для атрибута: " + name);
                }
            } else {
                joins = joins.join(EAV_BE_COMPLEX_VALUES.as(valueAlias)).
                        on(EAV_BE_ENTITIES.as(entityAlias).ID.equal(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_ID).
                                and(EAV_BE_COMPLEX_VALUES.as(valueAlias).ATTRIBUTE_ID.equal(attribute.getId())));
            }
        }

        return joins;
    }

    @Override
    public ArrayList<Long> findAll(BaseEntity baseEntity, Long creditorId) {
        ArrayList<Long> result = new ArrayList<>();

        if (baseEntity.getValueCount() == 0)
            return result;

        SelectConditionStep select = generateSQL(baseEntity, creditorId, null);

        if (select != null) {
            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(),
                    select.getBindValues().toArray());

            for (Map<String, Object> row : rows)
                result.add(((BigDecimal) row.get("inner_id")).longValue());
        }

        Collections.sort(result);

        return result;
    }
}
