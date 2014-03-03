package kz.bsbnb.usci.eav.persistance.impl.searcher;

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
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * Used to compare BaseEntity in memory, and to retrieve BaseEntities from storage by example.
 */
@Component
public class ImprovedBaseEntitySearcher extends JDBCSupport implements IBaseEntitySearcher
{
    Logger logger = LoggerFactory.getLogger(ImprovedBaseEntitySearcher.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public Long findSingle(BaseEntity entity)
    {
        if (entity.getId() > 0)
            return entity.getId();

        List<Long> ids = findAll(entity);

        if (ids.size() > 1) {
            //throw new RuntimeException("Found more than one instance of BaseEntity. Needed one.");
        }

        Long id = ids.size() == 1 ? ids.get(0) : null;

        if (id != null)
            entity.setId(id);

        return id;
    }

    public SelectConditionStep generateSQL(IBaseEntity entity, String entityName) {
        return generateSQL(entity, entityName, null);
    }

    private SelectJoinStep generateJoins(SelectJoinStep joins, String entityAlias, String name, IMetaType type,
                               IMetaAttribute attribute) {

        String valueAlias = "v_" + name;
        if (!type.isSet())
        {
            if (!type.isComplex())
            {
                MetaValue metaValue = (MetaValue)type;

                switch (metaValue.getTypeCode())
                {
                    case BOOLEAN:
                        joins = joins.join(EAV_BE_BOOLEAN_VALUES.as(valueAlias)).
                                        on(EAV_BE_ENTITIES.as(entityAlias).ID.equal(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ENTITY_ID).
                                                and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ATTRIBUTE_ID.equal(attribute.getId())));
                        break;
                    case DATE:
                        joins = joins.join(EAV_BE_DATE_VALUES.as(valueAlias)).
                                        on(EAV_BE_ENTITIES.as(entityAlias).ID.equal(EAV_BE_DATE_VALUES.as(valueAlias).ENTITY_ID).
                                                and(EAV_BE_DATE_VALUES.as(valueAlias).ATTRIBUTE_ID.equal(attribute.getId())));
                        break;
                    case DOUBLE:
                        joins = joins.join(EAV_BE_DOUBLE_VALUES.as(valueAlias)).
                                        on(EAV_BE_ENTITIES.as(entityAlias).ID.equal(EAV_BE_DOUBLE_VALUES.as(valueAlias).ENTITY_ID).
                                                and(EAV_BE_DOUBLE_VALUES.as(valueAlias).ATTRIBUTE_ID.equal(attribute.getId())));
                        break;
                    case INTEGER:
                        joins = joins.join(EAV_BE_INTEGER_VALUES.as(valueAlias)).
                                        on(EAV_BE_ENTITIES.as(entityAlias).ID.equal(EAV_BE_INTEGER_VALUES.as(valueAlias).ENTITY_ID).
                                                and(EAV_BE_INTEGER_VALUES.as(valueAlias).ATTRIBUTE_ID.equal(attribute.getId())));

                        break;
                    case STRING:
                        joins = joins.join(EAV_BE_STRING_VALUES.as(valueAlias)).
                                        on(EAV_BE_ENTITIES.as(entityAlias).ID.equal(EAV_BE_STRING_VALUES.as(valueAlias).ENTITY_ID).
                                                and(EAV_BE_STRING_VALUES.as(valueAlias).ATTRIBUTE_ID.equal(attribute.getId())));
                        break;
                    default:
                        throw new IllegalStateException("Unknown data type: " + metaValue.getTypeCode() +
                                " for attribute: " + name);
                }
            }
            else
            {
                joins = joins.join(EAV_BE_COMPLEX_VALUES.as(valueAlias)).
                        on(EAV_BE_ENTITIES.as(entityAlias).ID.equal(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_ID).
                                and(EAV_BE_COMPLEX_VALUES.as(valueAlias).ATTRIBUTE_ID.equal(attribute.getId())));
            }
        }

        return joins;
    }

    public SelectConditionStep generateSQL(IBaseEntity entity, String entityName, HashMap<String, ArrayList<String>> arrayKeyFilter)
    {
        MetaClass metaClass = entity.getMeta();

        String entityAlias = (entityName == null ? "root" : "e_" + entityName);

        SelectJoinStep joins = context.select(EAV_BE_ENTITIES.as(entityAlias).ID.as("inner_id")).from(EAV_BE_ENTITIES.as(entityAlias));

        if (metaClass == null) {
            throw new IllegalArgumentException("MetaData can't be null");
        }

        Set<String> names = metaClass.getMemberNames();

        Condition condition = null;
        if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
        {
            for(String name : names) {
                IMetaAttribute metaAttribute = metaClass.getMetaAttribute(name);
                IMetaType memberType = metaClass.getMemberType(name);
                if(metaAttribute.isKey()) {
                    IBaseValue baseValue = entity.safeGetValue(name);
                    if(baseValue == null) {
                        throw new IllegalArgumentException("Key attribute " + name + " can't be null. MetaClass: " +
                                entity.getMeta().getClassName());
                    }

                    if (!memberType.isSet()) {
                        if (!memberType.isComplex()) {
                            generateJoins(joins, entityAlias, name, memberType, metaAttribute);

                            MetaValue metaValue = (MetaValue)memberType;
                            switch (metaValue.getTypeCode())
                            {
                                case BOOLEAN:
                                    Boolean booleanValue = (Boolean)baseValue.getValue();
                                    if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        String valueAlias = "v_" + name;
                                        condition = condition == null ?
                                                EAV_BE_BOOLEAN_VALUES.as(valueAlias).VALUE.equal(DataUtils.convert(booleanValue)) :
                                                condition.and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).VALUE.equal(DataUtils.convert(booleanValue)));
                                    }
                                    else
                                    {
                                        String valueAlias = "v";
                                        Select select = context
                                                .select(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ID)
                                                .from(EAV_BE_BOOLEAN_VALUES.as(valueAlias))
                                                .where(EAV_BE_BOOLEAN_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_BOOLEAN_VALUES.as(valueAlias).VALUE.equal(DataUtils.convert(booleanValue)));
                                        condition = condition == null ? DSL.exists(select) : condition.or(DSL.exists(select));
                                    }
                                    break;
                                case DATE:
                                    Date dateValue = DataUtils.convert((java.util.Date)baseValue.getValue());
                                    if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        String valueAlias = "v_" + name;
                                        condition = condition == null ?
                                                EAV_BE_DATE_VALUES.as(valueAlias).VALUE.equal(dateValue) :
                                                condition.and(EAV_BE_DATE_VALUES.as(valueAlias).VALUE.equal(dateValue));
                                    }
                                    else
                                    {
                                        String valueAlias = "v";
                                        Select select = context
                                                .select(EAV_BE_DATE_VALUES.as(valueAlias).ID)
                                                .from(EAV_BE_DATE_VALUES.as(valueAlias))
                                                .where(EAV_BE_DATE_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_DATE_VALUES.as(valueAlias).VALUE.equal(dateValue));
                                        condition = condition == null ? DSL.exists(select) : condition.or(DSL.exists(select));
                                    }
                                    break;
                                case DOUBLE:
                                    Double doubleValue = (Double)baseValue.getValue();
                                    if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        String valueAlias = "v_" + name;
                                        condition = condition == null ?
                                                EAV_BE_DOUBLE_VALUES.as(valueAlias).VALUE.equal(doubleValue) :
                                                condition.and(EAV_BE_DOUBLE_VALUES.as(valueAlias).VALUE.equal(doubleValue));
                                    }
                                    else
                                    {
                                        String valueAlias = "v";
                                        Select select = context
                                                .select(EAV_BE_DOUBLE_VALUES.as(valueAlias).ID)
                                                .from(EAV_BE_DOUBLE_VALUES.as(valueAlias))
                                                .where(EAV_BE_DOUBLE_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_DOUBLE_VALUES.as(valueAlias).VALUE.equal(doubleValue));
                                        condition = condition == null ? DSL.exists(select) : condition.or(DSL.exists(select));
                                    }
                                    break;
                                case INTEGER:
                                    Integer integerValue = (Integer)baseValue.getValue();
                                    if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        String valueAlias = "v_" + name;
                                        condition = condition == null ?
                                                EAV_BE_INTEGER_VALUES.as(valueAlias).VALUE.equal(integerValue) :
                                                condition.and(EAV_BE_INTEGER_VALUES.as(valueAlias).VALUE.equal(integerValue));
                                    }
                                    else
                                    {
                                        String valueAlias = "v";
                                        Select select = context
                                                .select(EAV_BE_INTEGER_VALUES.as(valueAlias).ID)
                                                .from(EAV_BE_INTEGER_VALUES.as(valueAlias))
                                                .where(EAV_BE_INTEGER_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_INTEGER_VALUES.as(valueAlias).VALUE.equal(integerValue));
                                        condition = condition == null ? DSL.exists(select) : condition.or(DSL.exists(select));
                                    }
                                    break;
                                case STRING:
                                    String stringValue = (String)baseValue.getValue();
                                    if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        String valueAlias = "v_" + name;
                                        condition = condition == null ?
                                                EAV_BE_STRING_VALUES.as(valueAlias).VALUE.equal(stringValue) :
                                                condition.and(EAV_BE_STRING_VALUES.as(valueAlias).VALUE.equal(stringValue));
                                    }
                                    else
                                    {
                                        String valueAlias = "v";
                                        Select select = context
                                                .select(EAV_BE_STRING_VALUES.as(valueAlias).ID)
                                                .from(EAV_BE_STRING_VALUES.as(valueAlias))
                                                .where(EAV_BE_STRING_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_STRING_VALUES.as(valueAlias).VALUE.equal(stringValue));
                                        condition = condition == null ? DSL.exists(select) : condition.or(DSL.exists(select));
                                    }
                                    break;
                                default:
                                    throw new IllegalStateException("Unknown data type: " + metaValue.getTypeCode() +
                                            " for attribute: " + name);
                            }
                        } else {
                            BaseEntity childBaseEntity = (BaseEntity)baseValue.getValue();
                            Long childBaseEntityId = findSingle(childBaseEntity);
                            if (childBaseEntityId == null)
                            {
                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                                {
                                    return null;
                                }
                            }
                            else
                            {
                                generateJoins(joins, entityAlias, name, memberType, metaAttribute);

                                if (metaClass.getComplexKeyType() == ComplexKeyTypes.ALL)
                                {
                                    String valueAlias = "v_" + name;
                                    condition = condition == null ?
                                            EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_VALUE_ID.equal(childBaseEntityId) :
                                            condition.and(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_VALUE_ID.equal(childBaseEntityId));
                                }
                                else
                                {
                                    String valueAlias = "v";
                                    Select select = context
                                            .select(EAV_BE_COMPLEX_VALUES.as(valueAlias).ID)
                                            .from(EAV_BE_COMPLEX_VALUES.as(valueAlias))
                                            .where(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                            .and(EAV_BE_COMPLEX_VALUES.as(valueAlias).ENTITY_VALUE_ID.equal(childBaseEntityId));
                                    condition = condition == null ? DSL.exists(select) : condition.or(DSL.exists(select));
                                }
                            }
                        }
                    }
                    else
                    {
                        BaseSet baseSet = (BaseSet)baseValue.getValue();
                        MetaSet metaSet = (MetaSet)memberType;
                        if (baseSet.get().size() > 0) {
                            if (!memberType.isComplex()) {
                                throw new UnsupportedOperationException("Not yet implemented.");
                            } else {

                                MetaClass childMetaClass = (MetaClass)metaSet.getMemberType();
                                List<Long> childBaseEntityIds = new ArrayList<Long>();
                                for (IBaseValue childBaseValue : baseSet.get())
                                {
                                    BaseEntity childBaseEntity = (BaseEntity)childBaseValue.getValue();
                                    Long childBaseEntityId = findSingle(childBaseEntity);

                                    if (childBaseEntityId != null)
                                    {
                                        childBaseEntityIds.add(childBaseEntityId);
                                    }
                                    else
                                    {
                                        if (metaSet.getArrayKeyType() == ComplexKeyTypes.ALL)
                                        {
                                            return null;
                                        }
                                    }
                                }

                                if (childBaseEntityIds.size() > 0)
                                {
                                    String className = childMetaClass.getClassName();
                                    String childEntityAlias = "e_" + className;
                                    String setValueAlias = "sv_" + className;
                                    String setAlias = "s_" + className;
                                    String entitySetAlias = "es_" + className;
                                    Select select = null;

                                    if (metaSet.getArrayKeyType() == ComplexKeyTypes.ANY)
                                    {
                                        select = context.select(EAV_BE_ENTITIES.as(childEntityAlias).ID)
                                                .from(EAV_BE_ENTITIES.as(childEntityAlias))
                                                .join(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                                .on(EAV_BE_ENTITIES.as(childEntityAlias).ID.equal(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID))
                                                .join(EAV_BE_SETS.as(setAlias))
                                                .on(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.equal(EAV_BE_SETS.as(setAlias).ID))
                                                .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                                .on(EAV_BE_SETS.as(setAlias).ID.equal(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_ENTITIES.as(childEntityAlias).ID.in(childBaseEntityIds));

                                                /*
                                                .from(EAV_BE_SETS.as(setAlias))
                                                .join(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                                .on(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.equal(EAV_BE_SETS.as(setAlias).ID))
                                                .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                                .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID.eq(EAV_BE_SETS.as(setAlias).ID))
                                                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID.in(childBaseEntityIds));
                                                */

                                        condition = condition == null ? DSL.exists(select) :
                                                metaClass.getComplexKeyType() == ComplexKeyTypes.ALL ?
                                                        condition.and(DSL.exists(select)) : condition.or(DSL.exists(select));
                                    }
                                    else
                                    {
                                        select = context.selectCount()
                                                .from(EAV_BE_ENTITIES.as(childEntityAlias))
                                                .join(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                                .on(EAV_BE_ENTITIES.as(childEntityAlias).ID.equal(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID))
                                                .join(EAV_BE_SETS.as(setAlias))
                                                .on(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.equal(EAV_BE_SETS.as(setAlias).ID))
                                                .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                                .on(EAV_BE_SETS.as(setAlias).ID.equal(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID))
                                                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_ENTITIES.as(childEntityAlias).ID.in(childBaseEntityIds));

                                                /*
                                                .from(EAV_BE_SETS.as(setAlias))
                                                .join(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias))
                                                .on(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).SET_ID.equal(EAV_BE_SETS.as(setAlias).ID))
                                                .join(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias))
                                                .on(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).SET_ID.eq(EAV_BE_SETS.as(setAlias).ID))
                                                .where(EAV_BE_ENTITY_COMPLEX_SETS.as(entitySetAlias).ENTITY_ID.equal(EAV_BE_ENTITIES.as(entityAlias).ID))
                                                .and(EAV_BE_COMPLEX_SET_VALUES.as(setValueAlias).ENTITY_VALUE_ID.in(childBaseEntityIds));
                                                */

                                        condition = condition == null ? DSL.val(childBaseEntityIds.size()).lessOrEqual(select):
                                                metaClass.getComplexKeyType() == ComplexKeyTypes.ALL ?
                                                        condition.and(DSL.val(childBaseEntityIds.size()).lessOrEqual(select)) :
                                                        condition.or(DSL.val(childBaseEntityIds.size()).lessOrEqual(select));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SelectConditionStep where = joins.where(EAV_BE_ENTITIES.as(entityAlias).CLASS_ID.equal(metaClass.getId()));

        if(condition != null) {
            where = where.and(condition);

            //System.out.println("Generation SQL for class name " + metaClass.getClassName() + ": " + where.toString());
            //System.out.println("BaseEntity: ");
            //System.out.println(entity.toString());
        } else {
            logger.warn("No key attributes in entity.");

            //System.out.println("Generation SQL for class name " + metaClass.getClassName() + ": " + where.toString());
            //System.out.println("BaseEntity: ");
            //System.out.println(entity.toString());

            return null;
        }

        logger.debug("Searcher SQL after conditions generated: " + where.toString());

        return where;
    }

    @Override
    public ArrayList<Long> findAll(BaseEntity baseEntity)
    {
        ArrayList<Long> result = new ArrayList<Long>();
        //System.out.println("################");
        //System.out.println(baseEntity.toString());
        SelectConditionStep select = generateSQL(baseEntity, null);

        if (select != null)
        {
            long t1 = System.currentTimeMillis();
            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
            long t2 = System.currentTimeMillis() - t1;

            //System.out.println("[searcher]: " + t2 + " (" + baseEntity.getMeta().getClassName() + ")");

            for (Map<String, Object> row : rows)
            {
                result.add(((BigDecimal)row.get("inner_id")).longValue());
            }
        }

        logger.debug("Result size: " + result.size());

        return result;
    }
}
