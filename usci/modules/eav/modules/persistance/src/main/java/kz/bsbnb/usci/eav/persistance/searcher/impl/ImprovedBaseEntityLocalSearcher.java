package kz.bsbnb.usci.eav.persistance.searcher.impl;

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
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Component
public class ImprovedBaseEntityLocalSearcher extends JDBCSupport {
    private final Logger logger = LoggerFactory.getLogger(ImprovedBaseEntityLocalSearcher.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    public Long findSingleWithParent(BaseEntity entity, BaseEntity parentEntity) {
        if (entity.getValueCount() == 0)
            return null;

        SelectConditionStep select = generateSQL(entity, null, parentEntity);

        if (select != null) {
            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(),
                    select.getBindValues().toArray());

            if (rows.size() > 1)
                throw new IllegalStateException("Found more than one row(" +
                    entity.getMeta().getClassName() + "), " + entity);

            return ((BigDecimal) rows.get(0).get("inner_id")).longValue();
        }

        return null;
    }

    SelectConditionStep generateSQL(IBaseEntity entity, String entityName, BaseEntity parentEntity) {
        return generateSQL(entity, entityName, parentEntity, null);
    }

    SelectConditionStep generateSQL(IBaseEntity entity, String entityName, BaseEntity parentEntity,
                                    HashMap<String, ArrayList<String>> arrayKeyFilter) {
        MetaClass metaClass = entity.getMeta();
        String entityAlias = (entityName == null ? "root" : "e_" + entityName);

        SelectJoinStep joins = context.select(EAV_BE_ENTITIES.as(entityAlias).ID.as("inner_id")).
                from(EAV_BE_ENTITIES.as(entityAlias));

        if (metaClass == null)
            throw new IllegalArgumentException("MetaData can't be null");

        Condition condition = null;

        for (String name : metaClass.getMemberNames()) {
            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(name);
            IMetaType memberType = metaClass.getMemberType(name);

            if (metaAttribute.isKey()) {
                IBaseValue baseValue = entity.safeGetValue(name);

                if ((baseValue == null || baseValue.getValue() == null) &&
                        (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)) {
                    logger.warn("Key attribute " + name + " can't be null. MetaClass: " +
                            entity.getMeta().getClassName());
                    continue;
                }

                if ((baseValue == null || baseValue.getValue() == null) &&
                        (metaClass.getComplexKeyType() == ComplexKeyTypes.ANY))
                    continue;

                if (!memberType.isSet()) {
                    if (!memberType.isComplex()) {
                        generateJoins(joins, entityAlias, name, memberType, metaAttribute);

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
                                throw new IllegalStateException("Unknown data type: " + metaValue.getTypeCode() +
                                        " for attribute: " + name);
                        }
                    } else {
                        BaseEntity childBaseEntity = (BaseEntity) baseValue.getValue();
                        //Long childBaseEntityId = findSingle(childBaseEntity);
                        Long childBaseEntityId = searcherPool.getSearcher(childBaseEntity.getMeta().getClassName()).
                                findSingle(childBaseEntity);

                        if (childBaseEntityId == null) {
                            if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                                return null;
                        } else {
                            generateJoins(joins, entityAlias, name, memberType, metaAttribute);

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
                    if (baseSet.get().size() > 0) {
                        if (!memberType.isComplex()) {
                            throw new UnsupportedOperationException("Not yet implemented.");
                        } else {

                            MetaClass childMetaClass = (MetaClass) metaSet.getMemberType();
                            List<Long> childBaseEntityIds = new ArrayList<Long>();
                            for (IBaseValue childBaseValue : baseSet.get()) {
                                BaseEntity childBaseEntity = (BaseEntity) childBaseValue.getValue();

                                Long childBaseEntityId = searcherPool.getSearcher(childBaseEntity.getMeta().
                                        getClassName()).findSingle(childBaseEntity);

                                if (childBaseEntityId != null) {
                                    childBaseEntityIds.add(childBaseEntityId);
                                } else {
                                    if (metaSet.getArrayKeyType() == ComplexKeyTypes.ALL) {
                                        return null;
                                    }
                                }
                            }

                            if (childBaseEntityIds.size() > 0) {
                                String className = childMetaClass.getClassName();
                                // String childEntityAlias = "e_" + className;
                                String setValueAlias = "sv_" + className;
                                // String setAlias = "s_" + className;
                                String entitySetAlias = "es_" + className;
                                Select select;

                                if (metaSet.getArrayKeyType() == ComplexKeyTypes.ANY) {
                                    select = context.select(
                                            EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID)
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
                                                    condition.and(DSL.exists(select)) :
                                                    condition.or(DSL.exists(select));
                                } else {
                                    Condition setCondition = null;

                                    for (Long childBaseEntityId : childBaseEntityIds) {
                                        select = context.select(
                                                EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID)
                                                .from(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                                .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                                .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ATTRIBUTE_ID.
                                                        equal(metaAttribute.getId()))
                                                .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.
                                                        equal(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.
                                                        equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID.
                                                        equal(childBaseEntityId));

                                        setCondition = setCondition == null ? DSL.exists(select) :
                                                setCondition.and(DSL.exists(select));
                                    }

                                    condition = condition == null ? setCondition :
                                            metaClass.getComplexKeyType() == ComplexKeyTypes.ALL ?
                                                    condition.and(setCondition) : condition.or(setCondition);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Local variables
        String complexValueAlias = "cv_parent";
        joins = joins.join(EAV_BE_COMPLEX_VALUES.as(complexValueAlias)).
                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                        equal(EAV_BE_COMPLEX_VALUES.as(complexValueAlias).ENTITY_VALUE_ID).
                        and(EAV_BE_COMPLEX_VALUES.as(complexValueAlias).ENTITY_ID.
                                equal(parentEntity.getId())));


        SelectConditionStep where = joins.where(EAV_BE_ENTITIES.as(entityAlias).CLASS_ID.equal(metaClass.getId()))
                .and(EAV_BE_ENTITIES.as(entityAlias).DELETED.equal(DataUtils.convert(false)));

        if (condition != null) {
            where = where.and(condition);
        } else {
            logger.warn("No key attributes in entity.");
            return null;
        }

        logger.debug("Searcher SQL after conditions generated: " + where.toString());

        return where;
    }

    private SelectJoinStep generateJoins(SelectJoinStep joins, String entityAlias, String name, IMetaType type,
                                         IMetaAttribute attribute) {
        String valueAlias = "v_" + name;
        if (!type.isSet()) {
            if (!type.isComplex()) {
                MetaValue metaValue = (MetaValue) type;

                switch (metaValue.getTypeCode()) {
                    case BOOLEAN:
                        joins = joins.join(EAV_BE_BOOLEAN_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ATTRIBUTE_ID.
                                                equal(attribute.getId())));
                        break;
                    case DATE:
                        joins = joins.join(EAV_BE_DATE_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_DATE_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_DATE_VALUES.as(valueAlias).ATTRIBUTE_ID.
                                                equal(attribute.getId())));
                        break;
                    case DOUBLE:
                        joins = joins.join(EAV_BE_DOUBLE_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_DOUBLE_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_DOUBLE_VALUES.as(valueAlias).ATTRIBUTE_ID.
                                                equal(attribute.getId())));
                        break;
                    case INTEGER:
                        joins = joins.join(EAV_BE_INTEGER_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_INTEGER_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_INTEGER_VALUES.as(valueAlias).ATTRIBUTE_ID.
                                                equal(attribute.getId())));
                        break;
                    case STRING:
                        joins = joins.join(EAV_BE_STRING_VALUES.as(valueAlias)).
                                on(EAV_BE_ENTITIES.as(entityAlias).ID.
                                        equal(EAV_BE_STRING_VALUES.as(valueAlias).ENTITY_ID).
                                        and(EAV_BE_STRING_VALUES.as(valueAlias).ATTRIBUTE_ID.
                                                equal(attribute.getId())));
                        break;
                    default:
                        throw new IllegalStateException("Unknown data type: " + metaValue.getTypeCode() +
                                " for attribute: " + name);
                }
            } else {
                joins = joins.join(EAV_BE_COMPLEX_VALUES.as(valueAlias)).
                        on(EAV_BE_ENTITIES.as(entityAlias).ID.equal(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_ID).
                                and(EAV_BE_COMPLEX_VALUES.as(valueAlias).ATTRIBUTE_ID.equal(attribute.getId())));
            }
        }

        return joins;
    }
}
