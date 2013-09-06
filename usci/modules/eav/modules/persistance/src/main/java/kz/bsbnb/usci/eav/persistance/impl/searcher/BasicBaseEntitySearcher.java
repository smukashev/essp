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
import kz.bsbnb.usci.eav.util.DateUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * Used to compare BaseEntity in memory, and to retrieve BaseEntities from storage by example.
 */
@Component
public class BasicBaseEntitySearcher extends JDBCSupport implements IBaseEntitySearcher
{
    Logger logger = LoggerFactory.getLogger(BasicBaseEntitySearcher.class);

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
        try
        {
            return findAll(entity).get(0);
        }
        catch (Exception e)
        {
            logger.debug("No entities found");
            return null;
        }
    }

    public SelectConditionStep generateSQL(IBaseEntity entity, String entityName)
    {
        MetaClass meta = entity.getMeta();

        String the_name = (entityName == null ? "root" : "e_" + entityName);

        SelectJoinStep joins = context.select(EAV_BE_ENTITIES.as(the_name).ID).from(EAV_BE_ENTITIES.as(the_name));

        if (meta == null)
        {
            throw new IllegalArgumentException("MetaData can't be null");
        }

        Set<String> names = meta.getMemberNames();

        for(String name : names)
        {
            IMetaAttribute attribute = meta.getMetaAttribute(name);
            IMetaType type = meta.getMemberType(name);

            logger.debug("Attribute: " + name);
            if(!attribute.isKey())
            {
                logger.debug("It's not a key! So skipped.");
                continue;
            }

            logger.debug("It's a key!");

            IBaseValue value = entity.safeGetValue(name);

            if(value == null)
            {
                //throw new IllegalArgumentException("Key attribute " + name + " can't be null");
                logger.warn("Key is null! So Skipped.");
            }

            if (!type.isSet())
            {
                if (!type.isComplex())
                {
                    MetaValue simple_value = (MetaValue)type;

                    switch (simple_value.getTypeCode())
                    {
                        case BOOLEAN:
                            joins = joins.leftOuterJoin(EAV_BE_BOOLEAN_VALUES.as(name)).
                                    on(EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_BOOLEAN_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_BOOLEAN_VALUES.as(name).ATTRIBUTE_ID.equal(attribute.getId())));
                            break;
                        case DATE:
                            joins = joins.leftOuterJoin(EAV_BE_DATE_VALUES.as(name)).
                                    on(EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_DATE_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_DATE_VALUES.as(name).ATTRIBUTE_ID.equal(attribute.getId())));
                            break;
                        case DOUBLE:
                            joins = joins.leftOuterJoin(EAV_BE_DOUBLE_VALUES.as(name)).
                                    on(EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_DOUBLE_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_DOUBLE_VALUES.as(name).ATTRIBUTE_ID.equal(attribute.getId())));
                            break;
                        case INTEGER:
                            joins = joins.leftOuterJoin(EAV_BE_INTEGER_VALUES.as(name)).
                                    on(EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_INTEGER_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_INTEGER_VALUES.as(name).ATTRIBUTE_ID.equal(attribute.getId())));

                            break;
                        case STRING:
                            joins = joins.leftOuterJoin(EAV_BE_STRING_VALUES.as(name)).
                                    on(EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_STRING_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_STRING_VALUES.as(name).ATTRIBUTE_ID.equal(attribute.getId())));
                            break;
                        default:
                            throw new IllegalStateException("Unknown data type: " + simple_value.getTypeCode() +
                                    " for attribute: " + name);
                    }
                }
                else
                {
                    joins = joins.leftOuterJoin(EAV_BE_COMPLEX_VALUES.as(name)).
                            on(EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_COMPLEX_VALUES.as(name).ENTITY_ID).
                                    and(EAV_BE_COMPLEX_VALUES.as(name).ATTRIBUTE_ID.equal(attribute.getId())));
                }
            }
            else
            {
                if (!type.isComplex())
                {
                    MetaValue simple_value = (MetaValue)type;

                    joins = joins.leftOuterJoin(EAV_BE_ENTITY_SIMPLE_SETS.as(name)).
                            on(EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_ENTITY_SIMPLE_SETS.as(name).ENTITY_ID).
                                    and(EAV_BE_ENTITY_SIMPLE_SETS.as(name).ATTRIBUTE_ID.equal(attribute.getId())));
                }
                else
                {
                    joins = joins.join(EAV_BE_ENTITY_COMPLEX_SETS.as("s_" + name)).
                            on(EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_ENTITY_COMPLEX_SETS.as("s_" + name).ENTITY_ID).
                                    and(EAV_BE_ENTITY_COMPLEX_SETS.as("s_" + name).ATTRIBUTE_ID.equal(attribute.getId())))
                            .join(EAV_BE_COMPLEX_SET_VALUES.as(name)).on(
                                    EAV_BE_ENTITY_COMPLEX_SETS.as("s_" + name).SET_ID.
                                            equal(EAV_BE_COMPLEX_SET_VALUES.as(name).SET_ID)
                            );
                }
            }
        }

        logger.debug("Searcher SQL after joins generated: " + joins.toString());

        SelectConditionStep where;

        if (entityName != null)
        {
            where = joins.where(
                (EAV_BE_ENTITIES.as(the_name).CLASS_ID.equal(meta.getId())).and(
                        EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_COMPLEX_VALUES.as(entityName).ENTITY_VALUE_ID)
                    )
                );
        }
        else
        {
            where = joins.where(EAV_BE_ENTITIES.as(the_name).CLASS_ID.equal(meta.getId()));
        }

        Condition condition = null;

        for(String name : names)
        {
            IMetaAttribute attribute = meta.getMetaAttribute(name);
            IMetaType type = meta.getMemberType(name);

            logger.debug("Attribute: " + name);
            if(!attribute.isKey())
            {
                logger.debug("It's not a key! So skipped.");
                continue;
            }

            logger.debug("It's a key!");

            IBaseValue value = entity.safeGetValue(name);

            if(value == null)
            {
                throw new IllegalArgumentException("Key attribute " + name + " can't be null");
            }

            boolean and = (meta.getComplexKeyType() == ComplexKeyTypes.ALL);

            if (!type.isSet())
            {
                if (!type.isComplex())
                {
                    MetaValue simple_value = (MetaValue)type;

                    switch (simple_value.getTypeCode())
                    {
                        case BOOLEAN:
                            Boolean actual_boolean_value = (Boolean)value.getValue();
                            where = where.and(EAV_BE_BOOLEAN_VALUES.as(name).VALUE.equal(actual_boolean_value));
                            break;
                        case DATE:
                            Date actual_date_value = DateUtils.convert((java.util.Date)value.getValue());
                            if (condition == null)
                            {
                                condition = EAV_BE_DATE_VALUES.as(name).VALUE.equal(actual_date_value);
                            }
                            else
                            {
                                if (and)
                                    condition = condition.and(EAV_BE_DATE_VALUES.as(name).VALUE.equal(actual_date_value));
                                else
                                    condition = condition.or(EAV_BE_DATE_VALUES.as(name).VALUE.equal(actual_date_value));
                            }
                            break;
                        case DOUBLE:
                            Double actual_double_value = (Double)value.getValue();
                            if (condition == null)
                            {
                                condition = EAV_BE_DOUBLE_VALUES.as(name).VALUE.equal(actual_double_value);
                            }
                            else
                            {
                                if (and)
                                    condition = condition.and(EAV_BE_DOUBLE_VALUES.as(name).VALUE.equal(actual_double_value));
                                else
                                    condition = condition.or(EAV_BE_DOUBLE_VALUES.as(name).VALUE.equal(actual_double_value));
                            }
                            break;
                        case INTEGER:
                            Integer actual_integer_value = (Integer)value.getValue();
                            if (condition == null)
                            {
                                condition = EAV_BE_INTEGER_VALUES.as(name).VALUE.equal(actual_integer_value);
                            }
                            else
                            {
                                if (and)
                                    condition = condition.and(EAV_BE_INTEGER_VALUES.as(name).VALUE.equal(actual_integer_value));
                                else
                                    condition = condition.or(EAV_BE_INTEGER_VALUES.as(name).VALUE.equal(actual_integer_value));
                            }
                            break;
                        case STRING:
                            String actual_string_value = (String)value.getValue();
                            if (condition == null)
                            {
                                condition = EAV_BE_STRING_VALUES.as(name).VALUE.equal(actual_string_value);
                            }
                            else
                            {
                                if (and)
                                    condition = condition.and(EAV_BE_STRING_VALUES.as(name).VALUE.equal(actual_string_value));
                                else
                                    condition = condition.or(EAV_BE_STRING_VALUES.as(name).VALUE.equal(actual_string_value));
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unknown data type: " + simple_value.getTypeCode() +
                                    " for attribute: " + name);
                    }
                }
                else
                {
                    BaseEntity actual_complex_value = (BaseEntity)value.getValue();

                    SelectConditionStep innerSQL = generateSQL(actual_complex_value, name);

                    if (condition == null)
                    {
                        condition = DSL.exists(innerSQL);
                    }
                    else
                    {
                        if (and)
                            condition = condition.andExists(innerSQL);
                        else
                            condition = condition.orExists(innerSQL);
                    }
                }
            }
            else
            {
                if (!type.isComplex())
                {
                    MetaSet simple_value = (MetaSet)type;

                    switch (simple_value.getTypeCode())
                    {
                        case BOOLEAN:
                            SelectConditionStep outerBooleanSQL = null;

                            BaseSet actual_set_value = (BaseSet)value.getValue();

                            for (IBaseValue actSetElementValue : actual_set_value.get())
                            {
                                Boolean actualBooleanValue = (Boolean)actSetElementValue.getValue();

                                SelectConditionStep innerSQL = context.select(
                                        EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").ID
                                ).from(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values"))
                                        .where(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").SET_ID.equal(
                                                EAV_BE_ENTITY_SIMPLE_SETS.as(name).SET_ID
                                        )).and(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").VALUE.equal(actualBooleanValue));

                                if (outerBooleanSQL == null) {
                                    outerBooleanSQL = innerSQL;
                                }
                                else {
                                    outerBooleanSQL.unionAll(innerSQL);
                                }
                            }


                            if (condition == null)
                            {
                                if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                {
                                    condition = context.select(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").ID.
                                        count()).from(outerBooleanSQL).asField(name + "_values_c").eq(actual_set_value.get().
                                        size());
                                }
                                else
                                {
                                    condition = context.select(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").ID.
                                            count()).from(outerBooleanSQL).asField(name + "_values_c").ge(0);
                                }
                            }
                            else
                            {
                                if (and)
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.and(context.select(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerBooleanSQL).asField(name + "_values_c").eq(actual_set_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.and(context.select(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerBooleanSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                                else
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.or(context.select(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerBooleanSQL).asField(name + "_values_c").eq(actual_set_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.or(context.select(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerBooleanSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                            }
                            break;
                        case DATE:
                            SelectConditionStep outerDateSQL = null;

                            BaseSet actual_date_value = (BaseSet)value.getValue();

                            for (IBaseValue actDateElementValue : actual_date_value.get())
                            {
                                java.sql.Date actualDateValue = DateUtils.convert((java.util.Date)actDateElementValue.
                                        getValue());

                                SelectConditionStep innerSQL = context.select(
                                        EAV_BE_DATE_SET_VALUES.as(name + "_values").ID
                                ).from(EAV_BE_DATE_SET_VALUES.as(name + "_values"))
                                        .where(EAV_BE_DATE_SET_VALUES.as(name + "_values").SET_ID.equal(
                                                EAV_BE_ENTITY_SIMPLE_SETS.as(name).SET_ID
                                        )).and(EAV_BE_DATE_SET_VALUES.as(name + "_values").VALUE.equal(actualDateValue));

                                if (outerDateSQL == null) {
                                    outerDateSQL = innerSQL;
                                }
                                else {
                                    outerDateSQL.unionAll(innerSQL);
                                }
                            }


                            if (condition == null)
                            {
                                if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                {
                                    condition = context.select(EAV_BE_DATE_SET_VALUES.as(name + "_values").ID.
                                            count()).from(outerDateSQL).asField(name + "_values_c").eq(actual_date_value.get().
                                            size());
                                }
                                else
                                {
                                    condition = context.select(EAV_BE_DATE_SET_VALUES.as(name + "_values").ID.
                                            count()).from(outerDateSQL).asField(name + "_values_c").ge(0);
                                }
                            }
                            else
                            {
                                if (and)
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.and(context.select(EAV_BE_DATE_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerDateSQL).asField(name + "_values_c").eq(actual_date_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.and(context.select(EAV_BE_DATE_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerDateSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                                else
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.or(context.select(EAV_BE_DATE_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerDateSQL).asField(name + "_values_c").eq(actual_date_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.or(context.select(EAV_BE_DATE_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerDateSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                            }

                            break;
                        case DOUBLE:
                            SelectConditionStep outerDoubleSQL = null;

                            BaseSet actual_double_value = (BaseSet)value.getValue();

                            for (IBaseValue actDoubleElementValue : actual_double_value.get())
                            {
                                Double actualDoubleValue =(Double)actDoubleElementValue.
                                        getValue();

                                SelectConditionStep innerSQL = context.select(
                                        EAV_BE_DOUBLE_SET_VALUES.as(name + "_values").ID
                                ).from(EAV_BE_DOUBLE_SET_VALUES.as(name + "_values"))
                                        .where(EAV_BE_DOUBLE_SET_VALUES.as(name + "_values").SET_ID.equal(
                                                EAV_BE_ENTITY_SIMPLE_SETS.as(name).SET_ID
                                        )).and(EAV_BE_DOUBLE_SET_VALUES.as(name + "_values").VALUE.equal(actualDoubleValue));

                                if (outerDoubleSQL == null) {
                                    outerDoubleSQL = innerSQL;
                                }
                                else {
                                    outerDoubleSQL.unionAll(innerSQL);
                                }
                            }


                            if (condition == null)
                            {
                                if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                {
                                    condition = context.select(EAV_BE_DOUBLE_SET_VALUES.as(name + "_values").ID.
                                            count()).from(outerDoubleSQL).asField(name + "_values_c").eq(actual_double_value.get().
                                            size());
                                }
                                else
                                {
                                    condition = context.select(EAV_BE_DOUBLE_SET_VALUES.as(name + "_values").ID.
                                            count()).from(outerDoubleSQL).asField(name + "_values_c").ge(0);
                                }
                            }
                            else
                            {
                                if (and)
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.and(context.select(EAV_BE_DOUBLE_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerDoubleSQL).asField(name + "_values_c").eq(actual_double_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.and(context.select(EAV_BE_DOUBLE_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerDoubleSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                                else
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.or(context.select(EAV_BE_DOUBLE_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerDoubleSQL).asField(name + "_values_c").eq(actual_double_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.or(context.select(EAV_BE_DOUBLE_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerDoubleSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                            }

                            break;
                        case INTEGER:
                            SelectConditionStep outerIntegerSQL = null;

                            BaseSet actual_integer_value = (BaseSet)value.getValue();

                            for (IBaseValue actIntegerElementValue : actual_integer_value.get())
                            {
                                Long actualIntegerValue = ((Integer)actIntegerElementValue.
                                        getValue()).longValue();

                                SelectConditionStep innerSQL = context.select(
                                        EAV_BE_INTEGER_SET_VALUES.as(name + "_values").ID
                                ).from(EAV_BE_INTEGER_SET_VALUES.as(name + "_values"))
                                        .where(EAV_BE_INTEGER_SET_VALUES.as(name + "_values").SET_ID.equal(
                                                EAV_BE_ENTITY_SIMPLE_SETS.as(name).SET_ID
                                        )).and(EAV_BE_INTEGER_SET_VALUES.as(name + "_values").VALUE.equal(actualIntegerValue));

                                if (outerIntegerSQL == null) {
                                    outerIntegerSQL = innerSQL;
                                }
                                else {
                                    outerIntegerSQL.unionAll(innerSQL);
                                }
                            }


                            if (condition == null)
                            {
                                if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                {
                                    condition = context.select(EAV_BE_INTEGER_SET_VALUES.as(name + "_values").ID.
                                            count()).from(outerIntegerSQL).asField(name + "_values_c").eq(actual_integer_value.get().
                                            size());
                                }
                                else
                                {
                                    condition = context.select(EAV_BE_INTEGER_SET_VALUES.as(name + "_values").ID.
                                            count()).from(outerIntegerSQL).asField(name + "_values_c").ge(0);
                                }
                            }
                            else
                            {
                                if (and)
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.and(context.select(EAV_BE_INTEGER_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerIntegerSQL).asField(name + "_values_c").eq(actual_integer_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.and(context.select(EAV_BE_INTEGER_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerIntegerSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                                else
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.or(context.select(EAV_BE_INTEGER_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerIntegerSQL).asField(name + "_values_c").eq(actual_integer_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.or(context.select(EAV_BE_INTEGER_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerIntegerSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                            }

                            break;
                        case STRING:
                            SelectConditionStep outerStringSQL = null;

                            BaseSet actual_string_value = (BaseSet)value.getValue();

                            for (IBaseValue actStringElementValue : actual_string_value.get())
                            {
                                String actualStringValue = (String)actStringElementValue.
                                        getValue();

                                SelectConditionStep innerSQL = context.select(
                                        EAV_BE_STRING_SET_VALUES.as(name + "_values").ID
                                ).from(EAV_BE_STRING_SET_VALUES.as(name + "_values"))
                                        .where(EAV_BE_STRING_SET_VALUES.as(name + "_values").SET_ID.equal(
                                                EAV_BE_ENTITY_SIMPLE_SETS.as(name).SET_ID
                                        )).and(EAV_BE_STRING_SET_VALUES.as(name + "_values").VALUE.equal(actualStringValue));

                                if (outerStringSQL == null) {
                                    outerStringSQL = innerSQL;
                                }
                                else {
                                    outerStringSQL.unionAll(innerSQL);
                                }
                            }


                            if (condition == null)
                            {
                                if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                {
                                    condition = context.select(EAV_BE_STRING_SET_VALUES.as(name + "_values").ID.
                                            count()).from(outerStringSQL).asField(name + "_values_c").eq(actual_string_value.get().
                                            size());
                                }
                                else
                                {
                                    condition = context.select(EAV_BE_STRING_SET_VALUES.as(name + "_values").ID.
                                            count()).from(outerStringSQL).asField(name + "_values_c").ge(0);
                                }
                            }
                            else
                            {
                                if (and)
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.and(context.select(EAV_BE_STRING_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerStringSQL).asField(name + "_values_c").eq(actual_string_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.and(context.select(EAV_BE_STRING_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerStringSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                                else
                                {
                                    if (simple_value.getArrayKeyType() == ComplexKeyTypes.ALL)
                                    {
                                        condition = condition.or(context.select(EAV_BE_STRING_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerStringSQL).asField(name + "_values_c").eq(actual_string_value.get().
                                                size()));
                                    }
                                    else
                                    {
                                        condition = condition.or(context.select(EAV_BE_STRING_SET_VALUES.as(name + "_values").ID.
                                                count()).from(outerStringSQL).asField(name + "_values_c").ge(0));
                                    }
                                }
                            }

                            break;
                        default:
                            throw new IllegalStateException("Unknown data type: " + simple_value.getTypeCode() +
                                    " for attribute: " + name);
                    }
                }
                else
                {
                    SelectConditionStep outerComplexSQL = null;

                    BaseSet actual_set_value = (BaseSet)value.getValue();

                    for (IBaseValue actSetElementValue : actual_set_value.get())
                    {
                        BaseEntity actualBaseEntityValue = (BaseEntity)actSetElementValue.getValue();

                        SelectConditionStep innerSQL = generateSQL(actualBaseEntityValue, name);

                        if (outerComplexSQL == null) {
                            outerComplexSQL = innerSQL;
                        }
                        else {
                            outerComplexSQL.unionAll(innerSQL);
                        }
                    }

                    MetaClass simple_value = (MetaClass)(((MetaSet)type).getMemberType());

                    Table<Record> nested = outerComplexSQL.asTable(name + "_values");

                    if (condition == null)
                    {
                        if (simple_value.getComplexKeyType() == ComplexKeyTypes.ALL)
                        {
                            condition = DSL.val(actual_set_value.get().size()).eq(
                                    context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                    count()).from(nested));
                        }
                        else
                        {
                            condition = DSL.val(actual_set_value.get().size()).le(
                                    context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                            count()).from(nested));
                        }
                    }
                    else
                    {
                        if (and)
                        {
                            if (simple_value.getComplexKeyType() == ComplexKeyTypes.ALL)
                            {
                                condition = condition.and(DSL.val(actual_set_value.get().size()).eq(
                                        context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                                count()).from(nested)));
                            }
                            else
                            {
                                condition = condition.and(DSL.val(actual_set_value.get().size()).le(
                                        context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                                count()).from(nested)));
                            }
                        }
                        else
                        {
                            if (simple_value.getComplexKeyType() == ComplexKeyTypes.ALL)
                            {
                                condition = condition.or(DSL.val(actual_set_value.get().size()).eq(
                                        context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                                count()).from(nested)));
                            }
                            else
                            {
                                condition = condition.or(DSL.val(actual_set_value.get().size()).le(
                                        context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                                count()).from(nested)));
                            }
                        }
                    }
                    //break;
                }
            }
        }

        if(condition != null) {
            where = where.and(condition);
        } else {
            logger.warn("No key attributes in entity.");
        }

        logger.debug("Searcher SQL after conditions generated: " + where.toString());

        return where;
    }

    @Override
    public ArrayList<Long> findAll(BaseEntity entity)
    {
        MetaClass meta = entity.getMeta();
        java.util.Date reportDate =  entity.getReportDate();
        ArrayList<Long> result = new ArrayList<Long>();

        SelectConditionStep where = generateSQL(entity, null);

        List<Map<String, Object>> rows = queryForListWithStats(where.getSQL(), where.getBindValues().toArray());

        for (Map<String, Object> row : rows)
        {
            BaseEntity resultEntity = new BaseEntity(meta, reportDate);

            result.add((Long)row.get(EAV_BE_ENTITIES.ID.getName()));
        }

        logger.debug("Result size: " + result.size());

        return result;
    }
}
