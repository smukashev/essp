package kz.bsbnb.usci.eav.persistance.searcher.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * Used to compare BaseEntity in memory, and to retrieve BaseEntities from storage by example.
 */
//@Component
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
        List<Long> ids = findAll(entity);

        if (ids.size() > 1) {
            //throw new RuntimeException("Found more than one instance of BaseEntity. Needed one.");
        }

        return ids.size() == 1 ? ids.get(0) : null;
    }

    public SelectConditionStep generateSQL(IBaseEntity entity, String entityName) {
        return generateSQL(entity, entityName, null);
    }

    private SelectJoinStep generateJoins(SelectJoinStep joins, String the_name, String name, IMetaType type,
                               IMetaAttribute attribute) {
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

        return joins;
    }

    private Condition generateSimpleConditions(Condition condition, String name, IMetaType type, IBaseValue value,
                                               boolean and) {

        if (type instanceof MetaClass)
            throw new IllegalStateException("Can't convert class: " + ((MetaClass) type).getClassName() + " to simple value");

        MetaValue simple_value = (MetaValue)type;

        switch (simple_value.getTypeCode())
        {
            case BOOLEAN:
                Boolean actual_boolean_value = (Boolean)value.getValue();
                if (condition == null)
                {
                    condition = EAV_BE_BOOLEAN_VALUES.as(name).VALUE.equal(DataUtils.convert(actual_boolean_value));
                }
                else
                {
                    if (and)
                        condition = condition.and(EAV_BE_BOOLEAN_VALUES.as(name).VALUE.equal(DataUtils.convert(actual_boolean_value)));
                    else
                        condition = condition.or(EAV_BE_BOOLEAN_VALUES.as(name).VALUE.equal(DataUtils.convert(actual_boolean_value)));
                }
                break;
            case DATE:
                Date actual_date_value = DataUtils.convert((java.util.Date) value.getValue());
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

        return condition;
    }

    private Condition generateComplexCondition(Condition condition, String name, IBaseValue value, boolean and) {
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

        return condition;
    }

    private Condition generateSimpleArrayCondition(Condition condition, String name, IMetaType type, IBaseValue value,
                                                   boolean and) {
        MetaSet simple_value = (MetaSet)type;

        switch (simple_value.getTypeCode())
        {
            case BOOLEAN:
                Select outerBooleanSQL = null;

                BaseSet actual_set_value = (BaseSet)value.getValue();

                for (IBaseValue actSetElementValue : actual_set_value.get())
                {
                    Boolean actualBooleanValue = (Boolean)actSetElementValue.getValue();

                    SelectConditionStep innerSQL = context.select(
                            EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").ID
                    ).from(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values"))
                            .where(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").SET_ID.equal(
                                    EAV_BE_ENTITY_SIMPLE_SETS.as(name).SET_ID
                            )).and(EAV_BE_BOOLEAN_SET_VALUES.as(name + "_values").VALUE.equal(DataUtils.convert(actualBooleanValue)));

                    if (outerBooleanSQL == null) {
                        outerBooleanSQL = innerSQL;
                    }
                    else {
                        outerBooleanSQL = outerBooleanSQL.unionAll(innerSQL);
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
                Select outerDateSQL = null;

                BaseSet actual_date_value = (BaseSet)value.getValue();

                for (IBaseValue actDateElementValue : actual_date_value.get())
                {
                    java.sql.Date actualDateValue = DataUtils.convert((java.util.Date) actDateElementValue.
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
                        outerDateSQL = outerDateSQL.unionAll(innerSQL);
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
                Select outerDoubleSQL = null;

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
                        outerDoubleSQL = outerDoubleSQL.unionAll(innerSQL);
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
                Select outerIntegerSQL = null;

                BaseSet actual_integer_value = (BaseSet)value.getValue();

                for (IBaseValue actIntegerElementValue : actual_integer_value.get())
                {
                    Integer actualIntegerValue = (Integer)actIntegerElementValue.getValue();

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
                        outerIntegerSQL = outerIntegerSQL.unionAll(innerSQL);
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
                Select outerStringSQL = null;

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
                        outerStringSQL = outerStringSQL.unionAll(innerSQL);
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

        return condition;
    }

    private Condition generateComplexArrayCondition(Condition condition, String name, IMetaType type, IBaseValue value,
                                                    boolean and) throws ParseException
    {
        Select outerComplexSQL = null;

        BaseSet actual_set_value = (BaseSet)value.getValue();

        MetaSet simple_set = (MetaSet)type;

        for (IBaseValue actSetElementValue : actual_set_value.get())
        {
            BaseEntity actualBaseEntityValue = (BaseEntity)actSetElementValue.getValue();

            SelectConditionStep innerSQL = generateSQL(actualBaseEntityValue, name,
                    simple_set.getArrayKeyFilter());

            if (outerComplexSQL == null) {
                outerComplexSQL = innerSQL;
            }
            else {
                outerComplexSQL = outerComplexSQL.unionAll(innerSQL);
            }
        }

        MetaClass simple_value = (MetaClass)(((MetaSet)type).getMemberType());

        Table<Record> nested = outerComplexSQL.asTable(name + "_values");

        if (condition == null)
        {
            if (simple_value.getComplexKeyType() == ComplexKeyTypes.ALL)
            {
                condition = DSL.val(actual_set_value.sizeWithFilter(simple_set.getArrayKeyFilter())).lessOrEqual(
                        context.select(nested.field("inner_id").
                                count()).from(nested).where(((Field<Long>) nested.field("inner_id")).
                                equal(EAV_BE_COMPLEX_VALUES.as(name).ENTITY_VALUE_ID)));
            }
            else
            {
                condition = DSL.val(0).le(
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
                    condition = condition.and(DSL.val(actual_set_value.sizeWithFilter(simple_set.getArrayKeyFilter())).
                            lessOrEqual(
                            context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                    count()).from(nested)));
                }
                else
                {
                    condition = condition.and(DSL.val(0).le(
                            context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                    count()).from(nested)));
                }
            }
            else
            {
                if (simple_value.getComplexKeyType() == ComplexKeyTypes.ALL)
                {
                    condition = condition.or(DSL.val(actual_set_value.sizeWithFilter(simple_set.getArrayKeyFilter())).
                            lessOrEqual(
                            context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                    count()).from(nested)));
                }
                else
                {
                    condition = condition.or(DSL.val(0).le(
                            context.select(nested.field(EAV_BE_COMPLEX_SET_VALUES.ID.getName()).
                                    count()).from(nested)));
                }
            }
        }

        return condition;
    }

    public SelectConditionStep generateSQL(IBaseEntity entity, String entityName, HashMap<String, ArrayList<String>> arrayKeyFilter)
    {
        MetaClass meta = entity.getMeta();

        String the_name = (entityName == null ? "root" : "e_" + entityName);

        SelectJoinStep joins = context.select(EAV_BE_ENTITIES.as(the_name).ID.as("inner_id")).from(EAV_BE_ENTITIES.as(the_name));

        if (meta == null) {
            throw new IllegalArgumentException("MetaData can't be null");
        }

        Set<String> names = meta.getMemberNames();

        for(String name : names) {
            IMetaAttribute attribute = meta.getMetaAttribute(name);
            IMetaType type = meta.getMemberType(name);

            logger.debug("Attribute: " + name);
            if(!attribute.isKey()) {
                logger.debug("It's not a key! So skipped.");
                continue;
            }

            logger.debug("It's a key!");

            IBaseValue value = entity.safeGetValue(name);

            if(value == null) {
                //throw new IllegalArgumentException("Key attribute " + name + " can't be null");
                logger.warn("Key is null! So Skipped.");
            }

            generateJoins(joins, the_name, name, type, attribute);
        }

        if (arrayKeyFilter != null && arrayKeyFilter.size() > 0) {
            int filterNumber = 1;
            for (String attrName : arrayKeyFilter.keySet()) {
                IMetaAttribute attribute = meta.getMetaAttribute(attrName);
                IMetaType type = meta.getMemberType(attrName);

                generateJoins(joins, the_name, attrName + filterNumber++, type, attribute);
            }
        }

        logger.debug("Searcher SQL after joins generated: " + joins.toString());

        SelectConditionStep where;

        if (entityName != null) {
            where = joins.where(
                    (EAV_BE_ENTITIES.as(the_name).CLASS_ID.equal(meta.getId()))//.and(
                    //EAV_BE_ENTITIES.as(the_name).ID.equal(EAV_BE_COMPLEX_VALUES.as(entityName).ENTITY_VALUE_ID)
                    //)
            );
        } else {
            where = joins.where(EAV_BE_ENTITIES.as(the_name).CLASS_ID.equal(meta.getId()));
        }

        Condition condition = null;

        for(String name : names) {
            IMetaAttribute attribute = meta.getMetaAttribute(name);
            IMetaType type = meta.getMemberType(name);

            logger.debug("Attribute: " + name);
            if(!attribute.isKey()) {
                logger.debug("It's not a key! So skipped.");
                continue;
            }

            logger.debug("It's a key!");

            IBaseValue value = entity.safeGetValue(name);

            if(value == null) {
                //System.out.println("Entity: " + entity.toString());
                throw new IllegalArgumentException("Key attribute " + name + " can't be null. MetaClass: " +
                        entity.getMeta().getClassName());
            }

            boolean and = (meta.getComplexKeyType() == ComplexKeyTypes.ALL);

            if (!type.isSet()) {
                if (!type.isComplex()) {
                    condition = generateSimpleConditions(condition, name, type, value, and);
                } else {
                    condition = generateComplexCondition(condition, name, value, and);
                }
            }
            else
            {
                BaseSet baseSet = (BaseSet)value.getValue();
                if (baseSet.get().size() > 0) {
                    if (!type.isComplex()) {
                        condition = generateSimpleArrayCondition(condition, name, type, value, and);
                    } else {
                        try
                        {
                            condition = generateComplexArrayCondition(condition, name, type, value, and);
                        } catch (ParseException e)
                        {
                            throw new IllegalArgumentException("Error in array key filter: " + e.getMessage());
                        }
                    }
                }
            }
        }

        if (arrayKeyFilter != null && arrayKeyFilter.size() > 0) {
            int filterNumber = 1;
            Condition innerCondition = null;
            for (String attrName : arrayKeyFilter.keySet()) {
                IMetaType type = meta.getMemberType(attrName);

                IBaseValue value = entity.safeGetValue(attrName);

                if(value == null) {
                    throw new IllegalArgumentException("Key attribute " + attrName + " can't be null, " +
                            "it is used in array filter");
                }

                BaseValue bv = ((BaseValue)value).clone();
                for (String val : arrayKeyFilter.get(attrName)) {
                    bv.setValue(val);

                    innerCondition = generateSimpleConditions(innerCondition, attrName + filterNumber, type, bv, false);
                }
                filterNumber++;
            }

            if (innerCondition != null) {
                condition = condition.and(innerCondition);
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

        //System.out.println("Gen sql: " + where.toString());

        List<Map<String, Object>> rows = queryForListWithStats(where.getSQL(), where.getBindValues().toArray());

        for (Map<String, Object> row : rows)
        {
            //BaseEntity resultEntity = new BaseEntity(meta, reportDate);

            result.add(((BigDecimal)row.get("inner_id")).longValue());
        }

        logger.debug("Result size: " + result.size());

        return result;
    }
}