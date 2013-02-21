package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.BaseSet;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.model.batchdata.IBatchRepository;
import kz.bsbnb.usci.eav.model.batchdata.impl.BaseValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaAttribute;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * @author a.motov
 */
@Repository
public class PostgreSQLBaseEntityDaoImpl extends JDBCSupport implements IBaseEntityDao
{
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBaseEntityDaoImpl.class);

    private String INSERT_ENTITY_SQL;
    private String SELECT_ENTITY_BY_ID_SQL;
    private String DELETE_ENTITY_BY_ID_SQL;
    private String INSERT_SIMPLE_VALUE_SQL;
    private String INSERT_COMPLEX_VALUE_SQL;
    private String INSERT_SET_SQL;
    private String SELECT_SETS_BY_ENTITY_ID_SQL;
    private String SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL;
    private String SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL;
    private String SELECT_COMPLEX_VALUES_BY_ENTITY_ID_SQL;

    @Autowired
    IBatchRepository batchRepository;

    @Autowired
    IMetaClassRepository metaClassRepository;

    @PostConstruct
    public void init()
    {
        INSERT_ENTITY_SQL = String.format("INSERT INTO %s (class_id) VALUES ( ? )", getConfig().getEntitiesTableName());
        SELECT_ENTITY_BY_ID_SQL = String.format(
                "SELECT e.id, " +
                       "e.class_id, " +
                       "c.name as class_name " +
                  "FROM %s e, " +
                       "%s c " +
                 "WHERE e.id = ? " +
                   "AND e.class_id = c.id",
                getConfig().getEntitiesTableName(), getConfig().getClassesTableName());
        DELETE_ENTITY_BY_ID_SQL = String.format("DELETE FROM %s WHERE id = ?", getConfig().getEntitiesTableName());

        INSERT_SIMPLE_VALUE_SQL = "INSERT INTO %s (entity_id, batch_id, attribute_id, index, value) VALUES ( ?, ?, ?, ?, ? )";
        INSERT_COMPLEX_VALUE_SQL = "INSERT INTO %s (entity_id, batch_id, attribute_id, index, entity_value_id) VALUES ( ?, ?, ?, ?, ? )";
        INSERT_SET_SQL = String.format("INSERT INTO %s (entity_id, batch_id, attribute_id, index) VALUES ( ?, ?, ?, ? )",
                getConfig().getBaseSimpleSetsTableName());

        SELECT_SETS_BY_ENTITY_ID_SQL = String.format(
                "SELECT ss.batch_id, " +
                       "sa.name as attribute_name, " +
                       "ss.index " +
                  "FROM %s ss, " +
                       "%s sa " +
                 "WHERE ss.entity_id = ? " +
                   "AND ss.attribute_id = sa.id",
                getConfig().getBaseSimpleSetsTableName(), getConfig().getSimpleArrayTableName());

        SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL =
                "SELECT v.batch_id, " +
                       "v.attribute_id, " +
                       "sa.name as attribute_name, " +
                       "v.index, " +
                       "v.value " +
                  "FROM %s v, " +
                       "%s sa " +
                 "WHERE v.entity_id = ? " +
                   "AND v.attribute_id = sa.id";

        SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL =
                "SELECT sav.batch_id, " +
                       "sav.index, " +
                       "sav.value " +
                  "FROM %s sav " +
                 "WHERE sav.set_id in (?) ";

        SELECT_COMPLEX_VALUES_BY_ENTITY_ID_SQL =
                "SELECT cv.batch_id, " +
                       "ca.name as attribute_name, " +
                       "cv.index, " +
                       "cv.entity_value_id " +
                  "FROM %s cv, " +
                       "%s ca " +
                 "WHERE cv.entity_id = ? " +
                   "AND cv.attribute_id = ca.id ";
    }

    class InsertBaseEntityPreparedStatementCreator implements PreparedStatementCreator {
        BaseEntity baseEntity;

        public InsertBaseEntityPreparedStatementCreator(BaseEntity baseEntity) {
            this.baseEntity = baseEntity;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(
                    INSERT_ENTITY_SQL, new String[] {"id"});
            ps.setLong(1, baseEntity.getMeta().getId());

            logger.debug(ps.toString());

            return ps;
        }
    }

    @Override
    public BaseEntity load(long id)
    {
        if(id < 1)
        {
            throw new IllegalArgumentException("Does not have id. Can't load.");
        }

        List<Map<String, Object>> rows = queryForListWithStats(SELECT_ENTITY_BY_ID_SQL, id);

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one base entity found. Can't load.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalArgumentException("Class not found. Can't load.");
        }

        Map<String, Object> row = rows.get(0);
        BaseEntity baseEntity = null;

        if(row != null)
        {
            MetaClass meta = metaClassRepository.getMetaClass((String) row.get("class_name"));

            baseEntity = new BaseEntity(meta);
            baseEntity.setId((Integer)row.get("id"));
        }
        else
        {
            logger.error("Can't load BaseEntity, empty data set.");
        }

        // simple attribute values
        for (DataTypes dataType: DataTypes.values()) {
            String query;

            switch(dataType)
            {
                case INTEGER: {
                    query = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBaseIntegerValuesTableName(), getConfig().getSimpleAttributesTableName());
                    break;
                }
                case DATE: {
                    query = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBaseDateValuesTableName(), getConfig().getSimpleAttributesTableName());
                    break;
                }
                case STRING: {
                    query = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBaseStringValuesTableName(), getConfig().getSimpleAttributesTableName());
                    break;
                }
                case BOOLEAN: {
                    query = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBaseBooleanValuesTableName(), getConfig().getSimpleAttributesTableName());
                    break;
                }
                case DOUBLE: {
                    query = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBaseDoubleValuesTableName(), getConfig().getSimpleAttributesTableName());
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }
            loadSimpleValues(baseEntity, query);
        }

        // simple sets values
        loadSimpleArraysValues(baseEntity);

        // complex attribute values
        loadComplexValues(baseEntity, String.format(SELECT_COMPLEX_VALUES_BY_ENTITY_ID_SQL,
                getConfig().getBaseComplexValuesTableName(), getConfig().getComplexAttributesTableName()));

        // complex arrays values
        /*loadComplexArraysValues(baseEntity, String.format(SELECT_COMPLEX_VALUES_BY_ENTITY_ID_SQL,
                getConfig().getBaseComplexSetValuesTableName(), getConfig().getComplexArrayTableName()));*/

        return baseEntity;
    }

    @Override
    @Transactional
    public long save(BaseEntity baseEntity)
    {
        if(baseEntity.getMeta() == null)
        {
            throw new IllegalArgumentException("MetaClass must be set in the BaseEntity before entity insertion to DB.");
        }

        long baseEntityId = 0;
        if (baseEntity.getId() < 1)
        {
            baseEntityId = insertBaseEntity(baseEntity);
            baseEntity.setId(baseEntityId);
        }

        for (DataTypes dataType: DataTypes.values())
        {

            // simple values
            Set<String> attributeNames = baseEntity.getPresentSimpleAttributeNames(dataType);
            if (!attributeNames.isEmpty())
            {
                String query;
                switch(dataType)
                {
                    case INTEGER: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseIntegerValuesTableName());
                        break;
                    }
                    case DATE: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseDateValuesTableName());
                        break;
                    }
                    case STRING: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseStringValuesTableName());
                        break;
                    }
                    case BOOLEAN: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseBooleanValuesTableName());
                        break;
                    }
                    case DOUBLE: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseDoubleValuesTableName());
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unknown type.");
                }
                insertSimpleValues(baseEntity, attributeNames, query);
            }

            // simple array values
            attributeNames = baseEntity.getPresentSimpleArrayAttributeNames(dataType);
            if (!attributeNames.isEmpty())
            {
                String query;
                switch(dataType)
                {
                    case INTEGER: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseIntegerSetValuesTableName());
                        break;
                    }
                    case DATE: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseDateSetValuesTableName());
                        break;
                    }
                    case STRING: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseStringSetValuesTableName());
                        break;
                    }
                    case BOOLEAN: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseBooleanSetValuesTableName());
                        break;
                    }
                    case DOUBLE: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBaseDoubleSetValuesTableName());
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unknown type.");
                }
                //insertSimpleSetsValues(baseEntity, attributeNames, query);

            }
        }

        Set<String> attributeNames = null;

        //complex attribute values
        attributeNames = baseEntity.getPresentComplexAttributeNames();
        if (!attributeNames.isEmpty())
        {
            insertComplexValues(baseEntity, attributeNames, String.format(INSERT_COMPLEX_VALUE_SQL,
                    getConfig().getBaseComplexValuesTableName()));
        }

        /*attributeNames = baseEntity.getPresentComplexArrayAttributeNames();
        if (!attributeNames.isEmpty())
        {
            insertComplexArraysValues(baseEntity, attributeNames, String.format(INSERT_COMPLEX_VALUE_SQL,
                    getConfig().getBaseComplexSetValuesTableName()));
        }*/


        return baseEntityId;
    }

    @Override
    public void remove(BaseEntity baseEntity)
    {
        if(baseEntity.getId() < 1)
        {
            throw new IllegalArgumentException("Can't remove BaseEntity without id.");
        }

        updateWithStats(DELETE_ENTITY_BY_ID_SQL, baseEntity.getId());
    }

    @Override
    public BaseEntity load(BaseEntity baseEntity, boolean eager)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private long insertBaseEntity(BaseEntity baseEntity)
    {
        if(baseEntity.getMeta().getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must have an id filled before entity insertion to DB.");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new InsertBaseEntityPreparedStatementCreator(baseEntity), keyHolder);

        long baseEntityId = keyHolder.getKey().longValue();

        if(baseEntityId < 1)
        {
            logger.error("Can't insert entity");
            return 0;
        }

        return baseEntityId;
    }

    private void insertSimpleValues(BaseEntity baseEntity, Set<String> attributeNames, String query)
    {
        MetaClass meta = baseEntity.getMeta();

        Iterator<String> it = attributeNames.iterator();
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeNameForInsert);


            IBaseValue batchValue = baseEntity.getBaseValue(attributeNameForInsert);

            Object[] insertArgs = new Object[] {
                    baseEntity.getId(),
                    batchValue.getBatch().getId(),
                    metaAttribute.getId(), // todo: check
                    batchValue.getIndex(),
                    batchValue.getValue()
            };

            batchArgs.add(insertArgs);
        }

        logger.debug(query);
        batchUpdateWithStats(query, batchArgs);
    }

    private void insertComplexValues(BaseEntity baseEntity, Set<String> attributeNames, String query)
    {
        MetaClass meta = baseEntity.getMeta();

        Iterator<String> it = attributeNames.iterator();
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            IBaseValue batchValue = baseEntity.getBaseValue(attributeNameForInsert);

            IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeNameForInsert);

            long childBaseEntityId = save((BaseEntity)batchValue.getValue());

            Object[] insertArgs = new Object[] {baseEntity.getId(), batchValue.getBatch().getId(),
                    metaAttribute.getId(), batchValue.getIndex(), childBaseEntityId};          // todo: check

            batchArgs.add(insertArgs);
        }

        logger.debug(query);
        batchUpdateWithStats(query, batchArgs);
    }

    private void insertComplexArraysValues(BaseEntity baseEntity, Set<String> attributeNames, String query)
    {
        // todo: implement
        /*MetaClass meta = baseEntity.getMeta();

        Iterator<String> it = attributeNames.iterator();
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            BaseSet baseSet = (BaseSet)baseEntity.getBatchValue(attributeNameForInsert);
            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> batchValueIt = baseValues.iterator();

            while (batchValueIt.hasNext())
            {
                IBaseValue batchValue = batchValueIt.next();
                IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeNameForInsert);
                long childBaseEntityId = save((BaseEntity)batchValue.getValue());

                Object[] insertArgs = new Object[] {baseEntity.getId(), batchValue.getBatch().getId(),
                        metaAttribute.getId(), batchValue.getIndex(), childBaseEntityId};

                batchArgs.add(insertArgs);
            }
        }

        logger.debug(query);
        batchUpdateWithStats(query, batchArgs);*/
    }

    private void insertSimpleSetsValues(BaseEntity baseEntity, Set<String> attributeNames, String query)
    {
        // todo: implement
        /*MetaClass metaClass = baseEntity.getMeta();

        int i = 0;
        Iterator<String> it = attributeNames.iterator();
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attributeNameForInsert);

            List<IBaseValue> batchValues = baseEntity.getBatchValueArray(attributeNameForInsert);
            Iterator<IBaseValue> valueIt = batchValues.iterator();
            while (valueIt.hasNext()) {
                IBaseValue batchValue = valueIt.next();
                Object[] insertArgs = new Object[] {
                        baseEntity.getId(),
                        batchValue.getBatch().getId(),
                        metaAttribute.getId(),
                        batchValue.getIndex(),
                        batchValue.getValue()
                };
                batchArgs.add(insertArgs);
                i++;
            }

        }

        logger.debug(query);
        batchUpdateWithStats(query, batchArgs);*/
    }

    public void loadSimpleValues(BaseEntity baseEntity, String query)
    {
        logger.debug(query);
        List<Map<String, Object>> rows = queryForListWithStats(query, baseEntity.getId());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get("batch_id"));

            baseEntity.put((String) row.get("attribute_name"), new BaseValue(batch, (Long) row.get("index"), row.get("value")));
        }
    }

    public void loadSimpleArraysValues(BaseEntity baseEntity)
    {
        MetaClass metaClass = baseEntity.getMeta();

        logger.debug(SELECT_SETS_BY_ENTITY_ID_SQL);
        List<Map<String, Object>> rowsSet = queryForListWithStats(SELECT_SETS_BY_ENTITY_ID_SQL, baseEntity.getId());

        Iterator<Map<String, Object>> itSet = rowsSet.iterator();
        while (itSet.hasNext())
        {
            Map<String, Object> rowSet = itSet.next();

            long setId = (Long)rowSet.get("set_id");
            String attributeName = (String)rowSet.get("attribute_name");

            IMetaType metaTypeSet = metaClass.getMemberType(attributeName);

            if (!metaTypeSet.isArray() && metaTypeSet.isComplex()) {
                throw new RuntimeException("Create an array of simple values ​​impossible. " +
                        "Query returned no valid data.");
            }

            MetaSet metaSet = (MetaSet)metaTypeSet;
            IMetaType metaTypeValue = metaSet.getMemberType();

            if (metaTypeValue.isArray() || metaTypeValue.isComplex()) {
                throw new RuntimeException("Create an array of simple values ​​impossible. " +
                        "Query returned no valid data.");
            }

            MetaValue metaValue = (MetaValue)metaTypeValue;
            DataTypes dataType = metaValue.getTypeCode();

            String query;
            switch(dataType)
            {
                case INTEGER: {
                    query = String.format(SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL,
                            getConfig().getBaseIntegerSetValuesTableName());
                    break;
                }
                case DATE: {
                    query = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBaseDateSetValuesTableName());
                    break;
                }
                case STRING: {
                    query = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBaseStringSetValuesTableName());
                    break;
                }
                case BOOLEAN: {
                    query = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBaseBooleanSetValuesTableName());
                    break;
                }
                case DOUBLE: {
                    query = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBaseDoubleSetValuesTableName());
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }

            BaseSet baseSet = new BaseSet(metaTypeValue);

            List<Map<String, Object>> rowsValue = queryForListWithStats(query, setId);
            Iterator<Map<String, Object>> itValue = rowsValue.iterator();
            while (itValue.hasNext())
            {
                Map<String, Object> rowValue = itSet.next();

                Batch batch = batchRepository.getBatch((Long)rowValue.get("batch_id"));
                baseSet.put(new BaseValue(batch, (Long)rowValue.get("index"), rowValue.get("value")));
            }

            Batch batch = batchRepository.getBatch((Long)rowSet.get("batch_id"));
            baseEntity.put(attributeName, new BaseValue(batch, (Long)rowSet.get("index"), baseSet));

        }
    }

    private void loadComplexValues(BaseEntity baseEntity, String query)
    {
        logger.debug(query);
        List<Map<String, Object>> rows =
                queryForListWithStats(query, baseEntity.getId());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get("batch_id"));
            long entityValueId = (Long)row.get("entity_value_id");
            BaseEntity childBaseEntity = load(entityValueId);

            baseEntity.put((String) row.get("attribute_name"), new BaseValue(batch, (Long) row.get("index"), childBaseEntity));
        }
    }

    private void loadComplexArraysValues(BaseEntity baseEntity, String query)
    {
        /*logger.debug(query);
        List<Map<String, Object>> rows =
                queryForListWithStats(query, baseEntity.getId());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get("batch_id"));
            long entityValueId = (Long)row.get("entity_value_id");
            BaseEntity childBaseEntity = load(entityValueId);

            baseEntity.addToArray((String) row.get("attribute_name"), new BaseValue(batch, (Long) row.get("index"), childBaseEntity));
        }*/
    }

}
