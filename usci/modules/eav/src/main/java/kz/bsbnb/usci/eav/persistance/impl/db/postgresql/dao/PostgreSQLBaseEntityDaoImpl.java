package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.batchdata.IBatchRepository;
import kz.bsbnb.usci.eav.model.batchdata.IBatchValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClassHolder;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
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
public class PostgreSQLBaseEntityDaoImpl extends JDBCSupport
            implements IBaseEntityDao {
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBaseEntityDaoImpl.class);

    private String INSERT_ENTITY_SQL;
    private String SELECT_ENTITY_BY_ID_SQL;
    private String DELETE_ENTITY_BY_ID_SQL;
    private String INSERT_SIMPLE_VALUE_SQL;
    private String INSERT_DATE_VALUE_SQL;
    private String INSERT_DOUBLE_VALUE_SQL;
    private String INSERT_INTEGER_VALUE_SQL;
    private String INSERT_BOOLEAN_VALUE_SQL;
    private String INSERT_STRING_VALUE_SQL;
    private String INSERT_COMPLEX_VALUE_SQL;
    private String SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL;
    private String SELECT_DATE_VALUES_BY_ENTITY_ID_SQL;
    private String SELECT_DOUBLE_VALUES_BY_ENTITY_ID_SQL;
    private String SELECT_INTEGER_VALUES_BY_ENTITY_ID_SQL;
    private String SELECT_BOOLEAN_VALUES_BY_ENTITY_ID_SQL;
    private String SELECT_STRING_VALUES_BY_ENTITY_ID_SQL;
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
        INSERT_DATE_VALUE_SQL = String.format(INSERT_SIMPLE_VALUE_SQL, getConfig().getDateValuesTableName());
        INSERT_DOUBLE_VALUE_SQL = String.format(INSERT_SIMPLE_VALUE_SQL, getConfig().getDoubleValuesTableName());
        INSERT_INTEGER_VALUE_SQL = String.format(INSERT_SIMPLE_VALUE_SQL, getConfig().getIntegerValuesTableName());
        INSERT_BOOLEAN_VALUE_SQL = String.format(INSERT_SIMPLE_VALUE_SQL, getConfig().getBooleanValuesTableName());
        INSERT_STRING_VALUE_SQL = String.format(INSERT_SIMPLE_VALUE_SQL, getConfig().getStringValuesTableName());
        INSERT_COMPLEX_VALUE_SQL = String.format("INSERT INTO %s (entity_id, batch_id, attribute_id, index, entity_value_id) VALUES ( ?, ?, ?, ?, ? )", getConfig().getComplexValuesTableName());

        SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL =
                "SELECT savpp.batch_id, " +
                       "savpp.attribute_name, " +
                       "savpp.index, " +
                       "savpp.value " +
                  "FROM (SELECT (rank() over(PARTITION BY sav.attribute_id ORDER BY sav.batch_id DESC)) AS num_pp, " +
                               "sav.* " +
                          "FROM (SELECT v.batch_id, " +
                                       "v.attribute_id, " +
                                       "sa.name as attribute_name, " +
                                       "v.index, " +
                                       "v.value " +
                                  "FROM %s v, " +
                                       "%s sa " +
                                 "WHERE v.entity_id = ? " +
                                   "AND v.attribute_id = sa.id) sav " +
                         ") savpp " +
                " WHERE savpp.num_pp = 1";

        SELECT_DATE_VALUES_BY_ENTITY_ID_SQL = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                getConfig().getDateValuesTableName(), getConfig().getSimpleAttributesTableName());
        SELECT_DOUBLE_VALUES_BY_ENTITY_ID_SQL = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                getConfig().getDoubleValuesTableName(), getConfig().getSimpleAttributesTableName());
        SELECT_INTEGER_VALUES_BY_ENTITY_ID_SQL = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                getConfig().getIntegerValuesTableName(), getConfig().getSimpleAttributesTableName());
        SELECT_BOOLEAN_VALUES_BY_ENTITY_ID_SQL = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                getConfig().getBooleanValuesTableName(), getConfig().getSimpleAttributesTableName());
        SELECT_STRING_VALUES_BY_ENTITY_ID_SQL = String.format(SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL,
                getConfig().getStringValuesTableName(), getConfig().getSimpleAttributesTableName());

        SELECT_COMPLEX_VALUES_BY_ENTITY_ID_SQL = String.format(
                "SELECT cvpp.batch_id, " +
                       "ca.name as attribute_name, " +
                       "cvpp.index, " +
                       "cvpp.entity_value_id " +
                  "FROM (SELECT (rank() over(PARTITION BY cv.attribute_id ORDER BY cv.batch_id DESC)) AS num_pp, " +
                               "cv.batch_id, " +
                               "cv.attribute_id, " +
                               "cv.index, " +
                               "cv.entity_value_id " +
                          "FROM %s cv " +
                         "WHERE cv.entity_id = ?) cvpp, " +
                       "%s e, " +
                       "%s ca " +
                 "WHERE cvpp.num_pp = 1 " +
                   "AND cvpp.attribute_id = ca.id " +
                   "AND cvpp.entity_value_id = e.id",
                getConfig().getComplexValuesTableName(), getConfig().getEntitiesTableName(),
                getConfig().getComplexAttributesTableName()
        );
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
                case INTEGER: query = SELECT_INTEGER_VALUES_BY_ENTITY_ID_SQL; break;
                case DATE: query = SELECT_DATE_VALUES_BY_ENTITY_ID_SQL; break;
                case STRING: query = SELECT_STRING_VALUES_BY_ENTITY_ID_SQL; break;
                case BOOLEAN: query = SELECT_BOOLEAN_VALUES_BY_ENTITY_ID_SQL; break;
                case DOUBLE: query = SELECT_DOUBLE_VALUES_BY_ENTITY_ID_SQL; break;
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }
            loadSimpleValues(baseEntity, query);
        }

        // complex attribute values
        loadComplexValues(baseEntity);

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

        // simple attribute values
        for (DataTypes dataType: DataTypes.values())
        {
            Set<String> attributeNames = baseEntity.getPresentSimpleAttributeNames(dataType);

            if (!attributeNames.isEmpty())
            {
                String query;
                switch(dataType)
                {
                    case INTEGER: query = INSERT_INTEGER_VALUE_SQL; break;
                    case DATE: query = INSERT_DATE_VALUE_SQL; break;
                    case STRING: query = INSERT_STRING_VALUE_SQL; break;
                    case BOOLEAN: query = INSERT_BOOLEAN_VALUE_SQL; break;
                    case DOUBLE: query = INSERT_DOUBLE_VALUE_SQL; break;
                    default:
                        throw new IllegalArgumentException("Unknown type.");
                }
                insertSimpleValues(baseEntity, attributeNames, query);
            }
        }

        //complex attribute values
        Set<String> attributeNames = baseEntity.getPresentComplexAttributeNames();
        if (!attributeNames.isEmpty())
        {
            insertComplexValue(baseEntity, attributeNames);
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

    private void insertComplexValue(BaseEntity baseEntity, Set<String> attributeNames)
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

            Object[] insertArgs = new Object[] {
                    baseEntity.getId(),
                    batchValue.getBatch().getId(),
                    metaClassHolder.getId(),
                    batchValue.getIndex(),
                    childBaseEntityId
            };

            batchArgs.add(insertArgs);
        }

        logger.debug(INSERT_COMPLEX_VALUE_SQL);
        batchUpdateWithStats(INSERT_COMPLEX_VALUE_SQL, batchArgs);
    }

    /*private void insertComplexValueArrays(BaseEntity baseEntity, Set<String> attributeNames)
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
    }*/

    public void loadSimpleValues(BaseEntity baseEntity, String query)
    {
        logger.debug(query);
        List<Map<String, Object>> rows = queryForListWithStats(query, baseEntity.getId());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            String attributeName = (String) row.get("attribute_name");
            Batch batch = batchRepository.getBatch((Long)row.get("batch_id"));
            long index = (Long) row.get("index");
            Object value = row.get("value");

            baseEntity.set(
                    attributeName,
                    batch,
                    index,
                    value
            );
        }
    }

    private void loadComplexValues(BaseEntity baseEntity)
    {
        logger.debug(SELECT_COMPLEX_VALUES_BY_ENTITY_ID_SQL);
        List<Map<String, Object>> rows =
                queryForListWithStats(SELECT_COMPLEX_VALUES_BY_ENTITY_ID_SQL, baseEntity.getId());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            String attributeName = (String) row.get("attribute_name");
            Batch batch = batchRepository.getBatch((Long)row.get("batch_id"));
            long index = (Long) row.get("index");
            long entityValueId = (Long)row.get("entity_value_id");
            BaseEntity childBaseEntity = load(entityValueId);

            baseEntity.set(
                    attributeName,
                    batch,
                    index,
                    childBaseEntity
            );
        }
    }



}
