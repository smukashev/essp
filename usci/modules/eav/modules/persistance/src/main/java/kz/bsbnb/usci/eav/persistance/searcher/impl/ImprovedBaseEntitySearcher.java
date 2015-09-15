package kz.bsbnb.usci.eav.persistance.searcher.impl;

import kz.bsbnb.usci.eav.comparator.impl.IdentificationDocComparator;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Component
public class ImprovedBaseEntitySearcher extends JDBCSupport implements IBaseEntitySearcher {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

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

        SelectJoinStep joins = context.select(EAV_BE_ENTITIES.as(entityAlias).ID.as("inner_id")).
                from(EAV_BE_ENTITIES.as(entityAlias));

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
                    throw new IllegalArgumentException("Ключевой атрибут(" + name + ") не может быть пустым. " +
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
                                            EAV_BE_BOOLEAN_VALUES.as(valueAlias).VALUE.
                                                    equal(DataUtils.convert(booleanValue)) :
                                            condition.and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).VALUE.
                                                    equal(DataUtils.convert(booleanValue)));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_BOOLEAN_VALUES.as(valueAlias))
                                            .where(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ENTITY_ID.
                                                    equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                            .and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).VALUE.
                                                    equal(DataUtils.convert(booleanValue)));

                                    condition = condition == null ? DSL.exists(select) :
                                            condition.or(DSL.exists(select));
                                }
                                break;
                            case DATE:
                                Date dateValue = DataUtils.convert((java.util.Date) baseValue.getValue());
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_DATE_VALUES.as(valueAlias).VALUE.
                                                    equal(dateValue) :
                                            condition.and(EAV_BE_DATE_VALUES.as(valueAlias).VALUE.
                                                    equal(dateValue));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_DATE_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_DATE_VALUES.as(valueAlias))
                                            .where(EAV_BE_DATE_VALUES.as(valueAlias).ENTITY_ID.
                                                    equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                            .and(EAV_BE_DATE_VALUES.as(valueAlias).VALUE.
                                                    equal(dateValue));

                                    condition = condition == null ? DSL.exists(select) :
                                            condition.or(DSL.exists(select));
                                }
                                break;
                            case DOUBLE:
                                Double doubleValue = (Double) baseValue.getValue();
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_DOUBLE_VALUES.as(valueAlias).VALUE.equal(doubleValue) :
                                            condition.and(EAV_BE_DOUBLE_VALUES.as(valueAlias).VALUE.
                                                    equal(doubleValue));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_DOUBLE_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_DOUBLE_VALUES.as(valueAlias))
                                            .where(EAV_BE_DOUBLE_VALUES.as(valueAlias).ENTITY_ID.
                                                    equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                            .and(EAV_BE_DOUBLE_VALUES.as(valueAlias).VALUE.
                                                    equal(doubleValue));

                                    condition = condition == null ? DSL.exists(select) :
                                            condition.or(DSL.exists(select));
                                }
                                break;
                            case INTEGER:
                                Integer integerValue = (Integer) baseValue.getValue();
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_INTEGER_VALUES.as(valueAlias).VALUE.
                                                    equal(integerValue) :
                                            condition.and(EAV_BE_INTEGER_VALUES.as(valueAlias).VALUE.
                                                    equal(integerValue));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_INTEGER_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_INTEGER_VALUES.as(valueAlias))
                                            .where(EAV_BE_INTEGER_VALUES.as(valueAlias).ENTITY_ID.
                                                    equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                            .and(EAV_BE_INTEGER_VALUES.as(valueAlias).VALUE.
                                                    equal(integerValue));

                                    condition = condition == null ? DSL.exists(select) :
                                            condition.or(DSL.exists(select));
                                }
                                break;
                            case STRING:
                                String stringValue = (String) baseValue.getValue();
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL) {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_STRING_VALUES.as(valueAlias).VALUE.equal(stringValue) :
                                            condition.and(EAV_BE_STRING_VALUES.as(valueAlias).VALUE.
                                                    equal(stringValue));
                                } else {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_STRING_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_STRING_VALUES.as(valueAlias))
                                            .where(EAV_BE_STRING_VALUES.as(valueAlias).ENTITY_ID.
                                                    equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                            .and(EAV_BE_STRING_VALUES.as(valueAlias).VALUE.
                                                    equal(stringValue));

                                    condition = condition == null ? DSL.exists(select) :
                                            condition.or(DSL.exists(select));
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
                                        condition.and(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_VALUE_ID.
                                                equal(childBaseEntityId));
                            } else {
                                String valueAlias = "v";
                                Select select = context
                                        .select(EAV_BE_COMPLEX_VALUES.as(valueAlias).ID)
                                        .from(EAV_BE_COMPLEX_VALUES.as(valueAlias))
                                        .where(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_ID.
                                                equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                        .and(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_VALUE_ID.
                                                equal(childBaseEntityId));

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

                    if (childMetaClass.getClassName().equals("document")) {
                        List<IBaseValue> baseValues = new ArrayList<>(baseSet.get());
                        Collections.sort(baseValues, new IdentificationDocComparator());

                        String className = childMetaClass.getClassName();
                        String setValueAlias = "sv_" + className;
                        String entitySetAlias = "es_" + className;

                        Long entityValueId = 0L;

                        Select select;

                        boolean identified = false;

                        for (IBaseValue val : baseValues) {
                            BaseEntity document = (BaseEntity) val.getValue();
                            BaseEntity docType = (BaseEntity) document.getBaseValue("doc_type").getValue();
                            boolean is_identification = (boolean) docType.getBaseValue("is_identification").getValue();

                            if (((BaseEntity) val.getValue()).getId() == 0) {
                                if (is_identification) identified = true;
                                continue;
                            }

                            if (!is_identification) continue;

                            identified = true;

                            select = context.select(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID)
                                    .from(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                    .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                    .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ATTRIBUTE_ID.
                                            equal(metaAttribute.getId()))
                                    .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.
                                            equal(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                    .where(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID.
                                            eq(((BaseEntity) val.getValue()).getId()));

                            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(),
                                    select.getBindValues().toArray());

                            if (rows.size() > 1)
                                throw new IllegalStateException("Найдено больше одного документа(" +
                                        entity.getMeta().getClassName() + ");");

                            if (rows.size() == 1) {
                                entityValueId = ((BigDecimal) rows.get(0).get(EAV_BE_COMPLEX_SET_VALUES.
                                        as(setValueAlias).ENTITY_VALUE_ID.getName())).longValue();
                                break;
                            }
                        }

                        if (!identified)
                            throw new IllegalStateException("Нет идентификационных документов;");

                        if (entityValueId > 0) {
                            // TODO: remove repeated code
                            select = context.select(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID)
                                    .from(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                    .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                    .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ATTRIBUTE_ID.
                                            equal(metaAttribute.getId()))
                                    .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.
                                            equal(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                    .where(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID.
                                            eq(entityValueId).
                                            and(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.
                                                    equal(EAV_BE_ENTITIES.as(entityAlias).ID)));

                            condition = condition == null ? DSL.exists(select) :
                                    metaClass.getComplexKeyType() == ComplexKeyTypes.ALL ?
                                            condition.and(DSL.exists(select)) : condition.or(DSL.exists(select));
                        }
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

                        if (childBaseEntityIds.size() == 0)
                            throw new IllegalStateException("Ни один элемент ключевого массива " +
                                    "не был идентифицирован;");

                        String className = childMetaClass.getClassName();
                        String setValueAlias = "sv_" + className;
                        String entitySetAlias = "es_" + className;
                        Select select;

                        if (metaSet.getArrayKeyType() == ComplexKeyTypes.ANY) {
                            select = context.select(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID)
                                    .from(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                    .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                    .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ATTRIBUTE_ID.
                                            equal(metaAttribute.getId()))
                                    .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.
                                            equal(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                    .where(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.
                                            equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                    .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID.
                                            in(childBaseEntityIds));

                            condition = condition == null ? DSL.exists(select) :
                                    metaClass.getComplexKeyType() == ComplexKeyTypes.ALL ?
                                            condition.and(DSL.exists(select)) : condition.or(DSL.exists(select));
                        } else {
                            Collections.sort(childBaseEntityIds);

                            select = context.select(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID
                            ).from(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                    .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                    .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).
                                            ATTRIBUTE_ID.eq(metaAttribute.getId()))
                                    .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).
                                            SET_ID.eq(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                    .where(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).
                                            ENTITY_ID.eq(EAV_BE_ENTITIES.as(entityAlias).ID)
                                            .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).
                                                    ENTITY_VALUE_ID.in(childBaseEntityIds)));

                            condition = condition == null ? DSL.exists(select) :
                                    metaClass.getComplexKeyType() == ComplexKeyTypes.ALL ?
                                            condition.and(DSL.exists(select)) : condition.or(DSL.exists(select));
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
