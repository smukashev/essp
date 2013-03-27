package kz.bsbnb.usci.eav.persistance.impl.searcher;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import org.jooq.Condition;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.Executor;
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
    private Executor sqlGenerator;

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public BaseEntity findSingle(BaseEntity entity)
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

    private SelectConditionStep generateSQL(BaseEntity entity)
    {
        MetaClass meta = entity.getMeta();

        SelectJoinStep joins = sqlGenerator.select(EAV_ENTITIES.ID).from(EAV_ENTITIES);

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
                throw new IllegalArgumentException("Key attribute " + name + " can't be null");
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
                                    on(EAV_ENTITIES.ID.equal(EAV_BE_BOOLEAN_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_BOOLEAN_VALUES.as(name).ATTRIBUTE_ID.equal((int) attribute.getId())));
                            break;
                        case DATE:
                            joins = joins.leftOuterJoin(EAV_BE_DATE_VALUES.as(name)).
                                    on(EAV_ENTITIES.ID.equal(EAV_BE_DATE_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_DATE_VALUES.as(name).ATTRIBUTE_ID.equal((int)attribute.getId())));
                            break;
                        case DOUBLE:
                            joins = joins.leftOuterJoin(EAV_BE_DOUBLE_VALUES.as(name)).
                                    on(EAV_ENTITIES.ID.equal(EAV_BE_DOUBLE_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_DOUBLE_VALUES.as(name).ATTRIBUTE_ID.equal((int)attribute.getId())));
                            break;
                        case INTEGER:
                            joins = joins.leftOuterJoin(EAV_BE_INTEGER_VALUES.as(name)).
                                    on(EAV_ENTITIES.ID.equal(EAV_BE_INTEGER_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_INTEGER_VALUES.as(name).ATTRIBUTE_ID.equal((int)attribute.getId())));

                            break;
                        case STRING:
                            joins = joins.leftOuterJoin(EAV_BE_STRING_VALUES.as(name)).
                                    on(EAV_ENTITIES.ID.equal(EAV_BE_STRING_VALUES.as(name).ENTITY_ID).
                                            and(EAV_BE_STRING_VALUES.as(name).ATTRIBUTE_ID.equal((int)attribute.getId())));
                            break;
                        default:
                            throw new IllegalStateException("Unknown data type: " + simple_value.getTypeCode() +
                                    " for attribute: " + name);
                    }
                }
                else
                {
                    joins = joins.leftOuterJoin(EAV_BE_COMPLEX_VALUES.as(name)).
                            on(EAV_ENTITIES.ID.equal(EAV_BE_COMPLEX_VALUES.as(name).ENTITY_ID).
                                    and(EAV_BE_COMPLEX_VALUES.as(name).ATTRIBUTE_ID.equal((int)attribute.getId())));
                }
            }
        }

        logger.debug("Searcher SQL after joins generated: " + joins.toString());

        SelectConditionStep where = joins.where(EAV_ENTITIES.CLASS_ID.equal((int)meta.getId()));

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
                            Date actual_date_value = (Date)value.getValue();
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

                    SelectConditionStep innerSQL = generateSQL(actual_complex_value);

                    if (condition == null)
                    {
                        condition = EAV_BE_COMPLEX_VALUES.as(name).ENTITY_VALUE_ID.in(innerSQL);
                    }
                    else
                    {
                        if (and)
                            condition = condition.and(EAV_BE_COMPLEX_VALUES.as(name).ENTITY_VALUE_ID.in(innerSQL));
                        else
                            condition = condition.or(EAV_BE_COMPLEX_VALUES.as(name).ENTITY_VALUE_ID.in(innerSQL));
                    }
                }
            }
        }

        where = where.and(condition);

        logger.debug("Searcher SQL after conditions generated: " + where.toString());

        return where;
    }

    @Override
    public ArrayList<BaseEntity> findAll(BaseEntity entity)
    {
        MetaClass meta = entity.getMeta();
        ArrayList<BaseEntity> result = new ArrayList<BaseEntity>();

        SelectConditionStep where = generateSQL(entity);

        List<Map<String, Object>> rows = queryForListWithStats(where.getSQL(), where.getBindValues().toArray());

        for (Map<String, Object> row : rows)
        {
            BaseEntity resultEntity = new BaseEntity(meta);
            resultEntity.setId((Integer)row.get(EAV_ENTITIES.ID.getName()));

            result.add(resultEntity);
        }

        logger.debug("Result size: " + result.size());

        return result;
    }
}
