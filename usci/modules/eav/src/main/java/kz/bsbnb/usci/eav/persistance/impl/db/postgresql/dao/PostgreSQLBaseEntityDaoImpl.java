package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBatchRepository;
import kz.bsbnb.usci.eav.model.batchdata.IBatchValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.IMetaClassRepository;
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
    private String SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL;
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
            String queryForSimpleValues;
            String queryForSimpleArraysValues;

            switch(dataType)
            {
                case INTEGER: {
                    queryForSimpleValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getIntegerValuesTableName(), getConfig().getSimpleAttributesTableName());
                    queryForSimpleArraysValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getIntegerArrayValuesTableName(), getConfig().getSimpleArrayTableName());
                    break;
                }
                case DATE: {
                    queryForSimpleValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getDateValuesTableName(), getConfig().getSimpleAttributesTableName());
                    queryForSimpleArraysValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getDateArrayValuesTableName(), getConfig().getSimpleArrayTableName());
                    break;
                }
                case STRING: {
                    queryForSimpleValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getStringValuesTableName(), getConfig().getSimpleAttributesTableName());
                    queryForSimpleArraysValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getStringArrayValuesTableName(), getConfig().getSimpleArrayTableName());
                    break;
                }
                case BOOLEAN: {
                    queryForSimpleValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBooleanValuesTableName(), getConfig().getSimpleAttributesTableName());
                    queryForSimpleArraysValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getBooleanArrayValuesTableName(), getConfig().getSimpleArrayTableName());
                    break;
                }
                case DOUBLE: {
                    queryForSimpleValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getDoubleValuesTableName(), getConfig().getSimpleAttributesTableName());
                    queryForSimpleArraysValues = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                            getConfig().getDoubleArrayValuesTableName(), getConfig().getSimpleArrayTableName());
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }
            loadSimpleValues(baseEntity, queryForSimpleValues);
            loadSimpleArraysValues(baseEntity, queryForSimpleArraysValues);
        }

        // complex attribute values
        loadComplexValues(baseEntity, String.format(SELECT_COMPLEX_VALUES_BY_ENTITY_ID_SQL,
                getConfig().getComplexValuesTableName(), getConfig().getComplexAttributesTableName()));

        // complex arrays values
        loadComplexArraysValues(baseEntity, String.format(SELECT_COMPLEX_VALUES_BY_ENTITY_ID_SQL,
                getConfig().getComplexArrayValuesTableName(), getConfig().getComplexArrayTableName()));

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
                                getConfig().getIntegerValuesTableName());
                        break;
                    }
                    case DATE: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getDateValuesTableName());
                        break;
                    }
                    case STRING: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getStringValuesTableName());
                        break;
                    }
                    case BOOLEAN: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBooleanValuesTableName());
                        break;
                    }
                    case DOUBLE: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getDoubleValuesTableName());
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
                                getConfig().getIntegerArrayValuesTableName());
                        break;
                    }
                    case DATE: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getDateArrayValuesTableName());
                        break;
                    }
                    case STRING: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getStringArrayValuesTableName());
                        break;
                    }
                    case BOOLEAN: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getBooleanArrayValuesTableName());
                        break;
                    }
                    case DOUBLE: {
                        query = String.format(INSERT_SIMPLE_VALUE_SQL,
                                getConfig().getDoubleArrayValuesTableName());
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Unknown type.");
                }
                insertSimpleArraysValues(baseEntity, attributeNames, query);
            }
        }

        Set<String> attributeNames = null;

        //complex attribute values
        attributeNames = baseEntity.getPresentComplexAttributeNames();
        if (!attributeNames.isEmpty())
        {
            insertComplexValues(baseEntity, attributeNames, String.format(INSERT_COMPLEX_VALUE_SQL,
                    getConfig().getComplexValuesTableName()));
        }

        attributeNames = baseEntity.getPresentComplexArrayAttributeNames();
        if (!attributeNames.isEmpty())
        {
            insertComplexArraysValues(baseEntity, attributeNames, String.format(INSERT_COMPLEX_VALUE_SQL,
                    getConfig().getComplexArrayValuesTableName()));
        }

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

            IMetaType metaType = meta.getMemberType(attributeNameForInsert);
            MetaValue metaValue = (MetaValue)metaType;

            IBatchValue batchValue = baseEntity.getBatchValue(attributeNameForInsert);

            Object[] insertArgs = new Object[] {
                    baseEntity.getId(),
                    batchValue.getBatch().getId(),
                    metaValue.getId(),
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

            IBatchValue batchValue = baseEntity.getBatchValue(attributeNameForInsert);

            IMetaType metaType = meta.getMemberType(attributeNameForInsert);
            MetaClassHolder metaClassHolder = (MetaClassHolder)metaType;

            long childBaseEntityId = save((BaseEntity)batchValue.getValue());

            Object[] insertArgs = new Object[] {baseEntity.getId(), batchValue.getBatch().getId(),
                    metaClassHolder.getId(), batchValue.getIndex(), childBaseEntityId};

            batchArgs.add(insertArgs);
        }

        logger.debug(query);
        batchUpdateWithStats(query, batchArgs);
    }

    private void insertComplexArraysValues(BaseEntity baseEntity, Set<String> attributeNames, String query)
    {
        MetaClass meta = baseEntity.getMeta();

        Iterator<String> it = attributeNames.iterator();
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            List<IBatchValue> batchValues = baseEntity.getBatchValueArray(attributeNameForInsert);
            Iterator<IBatchValue> batchValueIt = batchValues.iterator();

            while (batchValueIt.hasNext())
            {
                IBatchValue batchValue = batchValueIt.next();

                IMetaType metaType = meta.getMemberType(attributeNameForInsert);
                MetaClassArray metaClassArray = (MetaClassArray)metaType;

                long childBaseEntityId = save((BaseEntity)batchValue.getValue());

                Object[] insertArgs = new Object[] {baseEntity.getId(), batchValue.getBatch().getId(),
                        metaClassArray.getId(), batchValue.getIndex(), childBaseEntityId};

                batchArgs.add(insertArgs);
            }
        }

        logger.debug(query);
        batchUpdateWithStats(query, batchArgs);
    }

    private void insertSimpleArraysValues(BaseEntity baseEntity, Set<String> attributeNames, String query)
    {
        MetaClass metaClass = baseEntity.getMeta();

        int i = 0;
        Iterator<String> it = attributeNames.iterator();
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            IMetaType metaType = metaClass.getMemberType(attributeNameForInsert);
            MetaValueArray metaValue = (MetaValueArray)metaType;

            List<IBatchValue> batchValues = baseEntity.getBatchValueArray(attributeNameForInsert);
            Iterator<IBatchValue> valueIt = batchValues.iterator();
            while (valueIt.hasNext()) {
                IBatchValue batchValue = valueIt.next();
                Object[] insertArgs = new Object[] {
                        baseEntity.getId(),
                        batchValue.getBatch().getId(),
                        metaValue.getId(),
                        batchValue.getIndex(),
                        batchValue.getValue()
                };
                batchArgs.add(insertArgs);
                i++;
            }

        }

        logger.debug(query);
        batchUpdateWithStats(query, batchArgs);
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

            baseEntity.set((String) row.get("attribute_name"), batch, (Long) row.get("index"), row.get("value"));
        }
    }

    public void loadSimpleArraysValues(BaseEntity baseEntity, String query)
    {
        logger.debug(query);
        List<Map<String, Object>> rows = queryForListWithStats(query, baseEntity.getId());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get("batch_id"));

            baseEntity.addToArray((String) row.get("attribute_name"), batch, (Long) row.get("index"), row.get("value"));
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

            baseEntity.set((String) row.get("attribute_name"), batch, (Long) row.get("index"), childBaseEntity);
        }
    }

    private void loadComplexArraysValues(BaseEntity baseEntity, String query)
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

            baseEntity.addToArray((String) row.get("attribute_name"), batch, (Long) row.get("index"), childBaseEntity);
        }
    }

}
