package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.meta.impl.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DateUtils;
import org.jooq.SelectForUpdateStep;
import org.jooq.impl.Executor;
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * @author a.motov
 */
@Repository
public class PostgreSQLBaseEntityDaoImpl extends JDBCSupport implements IBaseEntityDao
{
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBaseEntityDaoImpl.class);

    private String INSERT_ENTITY_SQL;
    private String DELETE_ENTITY_BY_ID_SQL;

    private String INSERT_SIMPLE_VALUE_SQL;
    private String UPDATE_SIMPLE_VALUE_SQL;
    private String SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL;

    private String INSERT_COMPLEX_VALUE_SQL;

    private String INSERT_SET_SQL;
    private String SELECT_ENTITY_SETS_BY_ENTITY_ID_SQL;
    private String SELECT_SETS_OF_SETS_BY_PARENT_SET_ID_SQL;

    private String INSERT_ENTITY_SET;
    private String INSERT_SET_OF_SETS;

    private String INSERT_SIMPLE_SET_VALUE_SQL;
    private String SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL;

    private String INSERT_COMPLEX_SET_VALUE_SQL;
    private String SELECT_COMPLEX_SET_VALUES_BY_SET_ID_SQL;

    @Autowired
    IBatchRepository batchRepository;

    @Autowired
    IMetaClassRepository metaClassRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private Executor sqlGenerator;

    @PostConstruct
    public void init()
    {
        INSERT_ENTITY_SQL = String.format("INSERT INTO %s (class_id) VALUES ( ? )", getConfig().getEntitiesTableName());
        DELETE_ENTITY_BY_ID_SQL = String.format("DELETE FROM %s WHERE id = ?", getConfig().getEntitiesTableName());

        INSERT_SIMPLE_VALUE_SQL = "INSERT INTO %s (entity_id, batch_id, attribute_id, index, rep_date, value) VALUES ( ?, ?, ?, ?, ?, ? )";
        UPDATE_SIMPLE_VALUE_SQL = "UPDATE %s sv SET sv.batch_id = ?, sv.index = ?, value = ? WHERE sv.id = ?";
        SELECT_SIMPLE_VALUES_BY_ENTITY_ID_SQL =
                "SELECT v.batch_id, " +
                       "v.attribute_id, " +
                       "sa.name as attribute_name, " +
                       "v.index, " +
                       "v.rep_date, " +
                       "v.value " +
                  "FROM %s v, " +
                       "%s sa " +
                 "WHERE v.entity_id = ? " +
                   "AND v.attribute_id = sa.id";

        INSERT_COMPLEX_VALUE_SQL = String.format("INSERT INTO %s (entity_id, batch_id, attribute_id, index, rep_date, entity_value_id) VALUES ( ?, ?, ?, ?, ?, ? )",
                getConfig().getBaseComplexValuesTableName());

        INSERT_SET_SQL = "INSERT INTO %s (batch_id, index, rep_date) VALUES ( ?, ?, ? )";

        INSERT_ENTITY_SET = "INSERT INTO %s (entity_id, attribute_id, set_id) VALUES ( ?, ?, ? )";
        SELECT_ENTITY_SETS_BY_ENTITY_ID_SQL =
                "SELECT s.id, " +
                       "s.batch_id, " +
                       "a.name as attribute_name, " +
                       "s.index, " +
                       "s.rep_date " +
                  "FROM %s a, " +
                       "%s es, " +
                       "%s s " +
                 "WHERE es.entity_id = ? " +
                   "AND es.attribute_id = a.id " +
                   "AND es.set_id = s.id";

        INSERT_SET_OF_SETS = "INSERT INTO %s (parent_set_id, child_set_id) VALUES ( ?, ? )";
        SELECT_SETS_OF_SETS_BY_PARENT_SET_ID_SQL =
                "SELECT s.id, " +
                       "s.batch_id, " +
                       "s.index, " +
                       "s.rep_date " +
                  "FROM %s ss, " +
                       "%s s " +
                 "WHERE ss.parent_set_id = ? " +
                   "AND ss.child_set_id = s.id";

        INSERT_SIMPLE_SET_VALUE_SQL = "INSERT INTO %s (set_id, batch_id, index, rep_date, value) VALUES ( ?, ?, ?, ?, ? )";
        SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL = "SELECT sav.batch_id, sav.index, sav.value, sav.rep_date FROM %s sav WHERE sav.set_id in (?) ";

        INSERT_COMPLEX_SET_VALUE_SQL = String.format("INSERT INTO %s (set_id, batch_id, index, rep_date, entity_value_id) VALUES ( ?, ?, ?, ?, ? )",
                getConfig().getBaseComplexSetValuesTableName());
        SELECT_COMPLEX_SET_VALUES_BY_SET_ID_SQL = String.format("SELECT cav.batch_id, cav.index, cav.rep_date, cav.entity_value_id FROM %s cav WHERE cav.set_id in (?) ",
                getConfig().getBaseComplexSetValuesTableName());

    }

    class InsertBaseEntityPreparedStatementCreator implements PreparedStatementCreator {
        BaseEntity baseEntity;

        public InsertBaseEntityPreparedStatementCreator(BaseEntity baseEntity) {
            this.baseEntity = baseEntity;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException
        {
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

        SelectForUpdateStep select = sqlGenerator
                .select(EAV_ENTITIES.ID, EAV_ENTITIES.CLASS_ID)
                .from(EAV_ENTITIES)
                .where(EAV_ENTITIES.ID.equal((int)id));
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

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
            MetaClass meta = metaClassRepository.getMetaClass((Integer)row.get(EAV_ENTITIES.CLASS_ID.getName()));

            baseEntity = new BaseEntity(meta);
            baseEntity.setId((Integer)row.get(EAV_ENTITIES.ID.getName()));
        }
        else
        {
            logger.error("Can't load BaseEntity, empty data set.");
        }

        // simple values
        loadIntegerValues(baseEntity);
        loadDateValues(baseEntity);
        loadStringValues(baseEntity);
        loadBooleanValues(baseEntity);
        loadDoubleValues(baseEntity);

        // complex values
        loadComplexValues(baseEntity);

        // entity sets
        loadEntitySets(baseEntity);

        return baseEntity;
    }

    @Override
    @Transactional
    public void update(BaseEntity baseEntity) {
        if (baseEntity.getId() < 1)
        {
            throw new IllegalArgumentException("BaseEntity must contain the id.");
        }


    }

    @Override
    @Transactional
    public long save(BaseEntity baseEntity)
    {
        if(baseEntity.getMeta() == null)
        {
            throw new IllegalArgumentException("MetaClass must be set before entity insertion to DB.");
        }

        if(baseEntity.getMeta().getId() < 1)
        {
            throw new IllegalArgumentException("MetaClass must contain the id before entity insertion to DB.");
        }

        if (baseEntity.getId() < 1)
        {
            long baseEntityId = insertBaseEntity(baseEntity);
            baseEntity.setId(baseEntityId);
        }

        MetaClass meta = baseEntity.getMeta();
        Set<String> entityAttributeNames = baseEntity.getAttributeNames();

        Map<DataTypes, Set<String>> simpleAttributeNames = new HashMap<DataTypes, Set<String>>();
        Set<String> complexAttributeNames = new HashSet<String>();

        Iterator<String> it = entityAttributeNames.iterator();
        while (it.hasNext())
        {
            String attributeName = it.next();

            IMetaType metaType = meta.getMemberType(attributeName);
            if (metaType.isSet())
            {
                insertEntitySet(baseEntity, attributeName);
            }
            else
            {
                if (metaType.isComplex())
                {
                    complexAttributeNames.add(attributeName);
                }
                else
                {
                    MetaValue metaValue = (MetaValue)metaType;
                    DataTypes type = metaValue.getTypeCode();

                    if (simpleAttributeNames.containsKey(type))
                    {
                        simpleAttributeNames.get(type).add(attributeName);
                    }
                    else
                    {
                        Set<String> attributes = new HashSet<String>();
                        attributes.add(attributeName);

                        simpleAttributeNames.put(type, attributes);
                    }
                }
            }
        }

        for (DataTypes dataType: DataTypes.values())
        {
            // simple values
            if (simpleAttributeNames.containsKey(dataType))
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
                insertSimpleValues(baseEntity, simpleAttributeNames.get(dataType), query);
            }
        }

        if (!complexAttributeNames.isEmpty())
        {
            insertComplexValues(baseEntity, complexAttributeNames);
        }

        return baseEntity.getId();
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
                    metaAttribute.getId(),
                    batchValue.getIndex(),
                    batchValue.getRepDate(),
                    batchValue.getValue()
            };

            batchArgs.add(insertArgs);
        }

        logger.debug(query);
        batchUpdateWithStats(query, batchArgs);
    }

    private void updateSimpleValues(BaseEntity baseEntity, Set<String> attributeNames, String query) {

    }

    private void insertComplexValues(BaseEntity baseEntity, Set<String> attributeNames)
    {
        MetaClass meta = baseEntity.getMeta();

        Iterator<String> it = attributeNames.iterator();
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        while (it.hasNext())
        {
            String attributeNameForInsert = it.next();

            IBaseValue batchValue = baseEntity.getBaseValue(attributeNameForInsert);

            IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeNameForInsert);

            long childBaseEntityId = save((BaseEntity) batchValue.getValue());

            Object[] insertArgs = new Object[] {baseEntity.getId(), batchValue.getBatch().getId(),
                    metaAttribute.getId(), batchValue.getIndex(), batchValue.getRepDate(), childBaseEntityId};          // todo: check

            batchArgs.add(insertArgs);
        }

        logger.debug(INSERT_COMPLEX_VALUE_SQL);
        batchUpdateWithStats(INSERT_COMPLEX_VALUE_SQL, batchArgs);
    }

    private void insertEntitySet(BaseEntity baseEntity, String attributeName)
    {
        MetaClass meta = baseEntity.getMeta();
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeName);
        IMetaType metaType = metaAttribute.getMetaType();
        MetaSet metaSet = (MetaSet)metaType;

        if (metaAttribute.getId() < 1)
        {
            throw new IllegalArgumentException("MetaAttribute does not contain an id. " +
                    "The set can not be saved.");
        }

        IBaseValue baseValue = baseEntity.getBaseValue(attributeName);
        BaseSet baseSet = (BaseSet)baseValue.getValue();
        IMetaType metaTypeValue = baseSet.getMemberType();

        long setId;
        if (metaTypeValue.isComplex())
        {
            setId = insertComplexSet(baseValue, metaSet);
        }
        else
        {
            setId = insertSimpleSet(baseValue, metaSet);
        }

        String query;
        if (metaTypeValue.isSet())
        {
            query = String.format(INSERT_ENTITY_SET, getConfig().getBaseEntitySetOfSetsTableName());
        }
        else
        {
            if (metaTypeValue.isComplex())
            {
                query = String.format(INSERT_ENTITY_SET, getConfig().getBaseEntityComplexSetsTableName());
            }
            else
            {
                query = String.format(INSERT_ENTITY_SET, getConfig().getBaseEntitySimpleSetsTableName());
            }
        }
        insertWithId(query, new Object[] {baseEntity.getId(), metaAttribute.getId(), setId});
    }

    private long insertSimpleSet(IBaseValue baseValue, MetaSet metaSet)
    {

        BaseSet baseSet = (BaseSet)baseValue.getValue();
        IMetaType metaType = metaSet.getMemberType();

        long setId = insertWithId(String.format(INSERT_SET_SQL, getConfig().getBaseSetsTableName()),
                new Object[]{baseValue.getBatch().getId(), baseValue.getIndex(), baseValue.getRepDate()});

        String query;
        if (metaType.isSet())
        {
            List<Object[]> batchArgs = new ArrayList<Object[]>();

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();
            query = String.format(INSERT_SET_OF_SETS, getConfig().getBaseSetOfSimpleSetsTableName());

            while (itValue.hasNext())
            {
                IBaseValue baseValueChild = itValue.next();
                MetaSet baseSetChild = (MetaSet)metaType;

                long setIdChild = insertSimpleSet(baseValueChild, baseSetChild);

                Object[] insertArgs = new Object[] {
                        setId,
                        setIdChild,
                };
                batchArgs.add(insertArgs);
            }
            logger.debug(query);
            batchUpdateWithStats(query, batchArgs);
        }
        else
        {
            DataTypes dataType = metaSet.getTypeCode();
            switch(dataType)
            {
                case INTEGER:
                {
                    query = String.format(INSERT_SIMPLE_SET_VALUE_SQL,
                            getConfig().getBaseIntegerSetValuesTableName());
                    break;
                }
                case DATE:
                {
                    query = String.format(INSERT_SIMPLE_SET_VALUE_SQL,
                            getConfig().getBaseDateSetValuesTableName());
                    break;
                }
                case STRING:
                {
                    query = String.format(INSERT_SIMPLE_SET_VALUE_SQL,
                            getConfig().getBaseStringSetValuesTableName());
                    break;
                }
                case BOOLEAN:
                {
                    query = String.format(INSERT_SIMPLE_SET_VALUE_SQL,
                            getConfig().getBaseBooleanSetValuesTableName());
                    break;
                }
                case DOUBLE:
                {
                    query = String.format(INSERT_SIMPLE_SET_VALUE_SQL,
                            getConfig().getBaseDoubleSetValuesTableName());
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }

            List<Object[]> batchArgs = new ArrayList<Object[]>();

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();
            while (itValue.hasNext())
            {
                IBaseValue batchValueChild = itValue.next();
                Object[] insertArgs = new Object[] {
                        setId,
                        batchValueChild.getBatch().getId(),
                        batchValueChild.getIndex(),
                        batchValueChild.getRepDate(),
                        batchValueChild.getValue()
                };
                batchArgs.add(insertArgs);
            }
            logger.debug(query);
            batchUpdateWithStats(query, batchArgs);
        }

        return setId;
    }

    private long insertComplexSet(IBaseValue baseValue, MetaSet metaSet)
    {
        BaseSet baseSet = (BaseSet)baseValue.getValue();
        IMetaType metaType = metaSet.getMemberType();

        long setId = insertWithId(String.format(INSERT_SET_SQL, getConfig().getBaseSetsTableName()),
                new Object[]{baseValue.getBatch().getId(), baseValue.getIndex(), baseValue.getRepDate()});

        String query;
        if (metaType.isSet())
        {
            List<Object[]> batchArgs = new ArrayList<Object[]>();

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();
            query = String.format(INSERT_SET_OF_SETS, getConfig().getBaseSetOfComplexSetsTableName());

            while (itValue.hasNext())
            {
                IBaseValue baseValueChild = itValue.next();
                MetaSet baseSetChild = (MetaSet)metaType;

                long setIdChild = insertComplexSet(baseValueChild, baseSetChild);

                Object[] insertArgs = new Object[] {
                        setId,
                        setIdChild,
                };
                batchArgs.add(insertArgs);
            }
            logger.debug(query);
            batchUpdateWithStats(query, batchArgs);
        }
        else
        {
            List<Object[]> batchArgs = new ArrayList<Object[]>();

            Set<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> itValue = baseValues.iterator();
            while (itValue.hasNext()) {
                IBaseValue baseValueChild = itValue.next();

                BaseEntity baseEntityChild = (BaseEntity)baseValueChild.getValue();
                long baseEntityChildId = save(baseEntityChild);

                Object[] insertArgs = new Object[] {
                        setId,
                        baseValueChild.getBatch().getId(),
                        baseValueChild.getIndex(),
                        baseValue.getRepDate(),
                        baseEntityChildId
                };
                batchArgs.add(insertArgs);
            }
            logger.debug(INSERT_COMPLEX_SET_VALUE_SQL);
            batchUpdateWithStats(INSERT_COMPLEX_SET_VALUE_SQL, batchArgs);
        }

        return setId;
    }

    private void loadIntegerValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_INTEGER_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_INTEGER_VALUES.INDEX,
                        EAV_BE_INTEGER_VALUES.REP_DATE,
                        EAV_BE_INTEGER_VALUES.VALUE)
                .from(EAV_BE_INTEGER_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_INTEGER_VALUES.ENTITY_ID.equal((int)baseEntity.getId()));

        logger.debug(select.getSQL());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_INTEGER_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_INTEGER_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_INTEGER_VALUES.REP_DATE.getName()),
                            row.get(EAV_BE_INTEGER_VALUES.VALUE.getName())));
        }
    }

    private void loadDateValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_DATE_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_DATE_VALUES.INDEX,
                        EAV_BE_DATE_VALUES.REP_DATE,
                        EAV_BE_DATE_VALUES.VALUE)
                .from(EAV_BE_DATE_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_DATE_VALUES.ENTITY_ID.equal((int)baseEntity.getId()));

        logger.debug(select.getSQL());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_DATE_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_DATE_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_DATE_VALUES.REP_DATE.getName()),
                            DateUtils.convert((java.sql.Date) row.get(EAV_BE_DATE_VALUES.VALUE.getName()))));
        }
    }

    private void loadBooleanValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_BOOLEAN_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_BOOLEAN_VALUES.INDEX,
                        EAV_BE_BOOLEAN_VALUES.REP_DATE,
                        EAV_BE_BOOLEAN_VALUES.VALUE)
                .from(EAV_BE_BOOLEAN_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.equal((int)baseEntity.getId()));

        logger.debug(select.getSQL());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_BOOLEAN_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_BOOLEAN_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_BOOLEAN_VALUES.REP_DATE.getName()),
                            row.get(EAV_BE_BOOLEAN_VALUES.VALUE.getName())));
        }
    }

    private void loadStringValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_STRING_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_STRING_VALUES.INDEX,
                        EAV_BE_STRING_VALUES.REP_DATE,
                        EAV_BE_STRING_VALUES.VALUE)
                .from(EAV_BE_STRING_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_STRING_VALUES.ENTITY_ID.equal((int)baseEntity.getId()));

        logger.debug(select.getSQL());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_STRING_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_STRING_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_STRING_VALUES.REP_DATE.getName()),
                            row.get(EAV_BE_STRING_VALUES.VALUE.getName())));
        }
    }

    private void loadDoubleValues(BaseEntity baseEntity) {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_DOUBLE_VALUES.BATCH_ID,
                        EAV_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_DOUBLE_VALUES.INDEX,
                        EAV_BE_DOUBLE_VALUES.REP_DATE,
                        EAV_BE_DOUBLE_VALUES.VALUE)
                .from(EAV_BE_DOUBLE_VALUES)
                .join(EAV_SIMPLE_ATTRIBUTES).on(EAV_BE_DOUBLE_VALUES.ATTRIBUTE_ID.eq(EAV_SIMPLE_ATTRIBUTES.ID))
                .where(EAV_BE_DOUBLE_VALUES.ENTITY_ID.equal((int)baseEntity.getId()));

        logger.debug(select.getSQL());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_DOUBLE_VALUES.BATCH_ID.getName()));
            baseEntity.put(
                    (String) row.get(EAV_SIMPLE_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_DOUBLE_VALUES.INDEX.getName()),
                            (java.sql.Date) row.get(EAV_BE_DOUBLE_VALUES.REP_DATE.getName()),
                            row.get(EAV_BE_DOUBLE_VALUES.VALUE.getName())));
        }
    }

    private void loadComplexValues(BaseEntity baseEntity)
    {
        SelectForUpdateStep select = sqlGenerator
                .select(EAV_BE_COMPLEX_VALUES.BATCH_ID,
                        EAV_COMPLEX_ATTRIBUTES.NAME,
                        EAV_BE_COMPLEX_VALUES.INDEX,
                        EAV_BE_COMPLEX_VALUES.REP_DATE,
                        EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID)
                .from(EAV_BE_COMPLEX_VALUES)
                .join(EAV_COMPLEX_ATTRIBUTES).on(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID.eq(EAV_COMPLEX_ATTRIBUTES.ID))
                .where(EAV_BE_COMPLEX_VALUES.ENTITY_ID.equal((int)baseEntity.getId()));


        logger.debug(select.getSQL());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();

            Batch batch = batchRepository.getBatch((Long)row.get(EAV_BE_COMPLEX_VALUES.BATCH_ID.getName()));
            long entityValueId = (Long)row.get(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.getName());
            BaseEntity childBaseEntity = load(entityValueId);

            baseEntity.put(
                    (String) row.get(EAV_COMPLEX_ATTRIBUTES.NAME.getName()),
                    new BaseValue(
                            batch,
                            (Long) row.get(EAV_BE_COMPLEX_VALUES.INDEX.getName()),
                            (Date) row.get(EAV_BE_COMPLEX_VALUES.REP_DATE.getName()),
                            childBaseEntity));
        }
    }

    private void loadEntitySets(BaseEntity baseEntity) {
        String query;

        // entity simple sets
        query = String.format(SELECT_ENTITY_SETS_BY_ENTITY_ID_SQL,
                getConfig().getSimpleSetTableName(),
                getConfig().getBaseEntitySimpleSetsTableName(),
                getConfig().getBaseSetsTableName());
        loadEntitySets(baseEntity, query);

        // entity complex sets
        query = String.format(SELECT_ENTITY_SETS_BY_ENTITY_ID_SQL,
                getConfig().getComplexSetTableName(),
                getConfig().getBaseEntityComplexSetsTableName(),
                getConfig().getBaseSetsTableName());
        loadEntitySets(baseEntity, query);

        // entity set of sets
        query = String.format(SELECT_ENTITY_SETS_BY_ENTITY_ID_SQL,
                getConfig().getSetOfSetsTableName(),
                getConfig().getBaseEntitySetOfSetsTableName(),
                getConfig().getBaseSetsTableName());
        loadEntitySets(baseEntity, query);
    }

    private void loadEntitySets(BaseEntity baseEntity, String query) {
        MetaClass metaClass = baseEntity.getMeta();

        logger.debug(query);
        List<Map<String, Object>> rowsSet = queryForListWithStats(query, baseEntity.getId());

        Iterator<Map<String, Object>> itSet = rowsSet.iterator();
        while (itSet.hasNext())
        {
            Map<String, Object> rowSet = itSet.next();

            String attributeName = (String)rowSet.get("attribute_name");
            IMetaType metaType = metaClass.getMemberType(attributeName);

            BaseSet baseSet = new BaseSet((Integer)rowSet.get("id"), ((MetaSet)metaType).getMemberType());

            // load child values
            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSet);
            }
            else
            {
                loadSimpleSetValues(baseSet);
            }

            Batch batch = batchRepository.getBatch((Long)rowSet.get("batch_id"));
            baseEntity.put(attributeName, new BaseValue(batch, (Long)rowSet.get("index"),
                    (Date) rowSet.get("rep_date"), baseSet));
        }
    }

    private void loadSetOfSets(BaseSet baseSet, String query) {
        logger.debug(query);
        List<Map<String, Object>> rowsSet = queryForListWithStats(query, baseSet.getId());

        Iterator<Map<String, Object>> itSet = rowsSet.iterator();
        while (itSet.hasNext())
        {
            Map<String, Object> rowSet = itSet.next();

            String attributeName = (String)rowSet.get("attribute_name");
            IMetaType metaType = baseSet.getMemberType();

            BaseSet baseSetChild = new BaseSet((Integer)rowSet.get("id"), ((MetaSet)metaType).getMemberType());

            if (metaType.isComplex())
            {
                loadComplexSetValues(baseSetChild);
            }
            else
            {
                loadSimpleSetValues(baseSetChild);
            }


            Batch batch = batchRepository.getBatch((Long)rowSet.get("batch_id"));
            baseSet.put(attributeName, new BaseValue(batch, (Long)rowSet.get("index"),
                    (Date) rowSet.get("rep_date"), baseSetChild));
        }
    }

    private void loadSimpleSetValues(BaseSet baseSet)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (metaType.isComplex())
            throw new RuntimeException("Load the simple set values is not possible. " +
                    "Simple values ​​can not be added to an set of complex values.");

        if (metaType.isSet())
        {
            String query = String.format(SELECT_SETS_OF_SETS_BY_PARENT_SET_ID_SQL,
                    getConfig().getBaseSetOfSimpleSetsTableName(),
                    getConfig().getBaseSetsTableName());
            loadSetOfSets(baseSet, query);
        }
        else
        {
            MetaValue metaValue = (MetaValue)metaType;
            DataTypes dataType = metaValue.getTypeCode();

            String query;
            switch(dataType)
            {
                case INTEGER:
                {
                    query = String.format(SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL,
                            getConfig().getBaseIntegerSetValuesTableName());
                    break;
                }
                case DATE:
                {
                    query = String.format(SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL,
                            getConfig().getBaseDateSetValuesTableName());
                    break;
                }
                case STRING:
                {
                    query = String.format(SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL,
                            getConfig().getBaseStringSetValuesTableName());
                    break;
                }
                case BOOLEAN:
                {
                    query = String.format(SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL,
                            getConfig().getBaseBooleanSetValuesTableName());
                    break;
                }
                case DOUBLE:
                {
                    query = String.format(SELECT_SIMPLE_SET_VALUES_BY_SET_ID_SQL,
                            getConfig().getBaseDoubleSetValuesTableName());
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown type.");
            }

            List<Map<String, Object>> rowsValue = queryForListWithStats(query, baseSet.getId());
            Iterator<Map<String, Object>> itValue = rowsValue.iterator();
            while (itValue.hasNext())
            {
                Map<String, Object> rowValue = itValue.next();

                Batch batch = batchRepository.getBatch((Long)rowValue.get("batch_id"));
                baseSet.put(new BaseValue(batch, (Long)rowValue.get("index"),
                        (Date) rowValue.get("rep_date"), rowValue.get("value")));
            }
        }
    }

    private void loadComplexSetValues(BaseSet baseSet)
    {
        IMetaType metaType = baseSet.getMemberType();
        if (!metaType.isComplex())
            throw new RuntimeException("Load the complex set values is not possible. " +
                    "Complex values ​​can not be added to an set of simple values.");

        if (metaType.isSet())
        {
            String query = String.format(SELECT_SETS_OF_SETS_BY_PARENT_SET_ID_SQL,
                    getConfig().getBaseSetOfComplexSetsTableName(),
                    getConfig().getBaseSetsTableName());
            loadSetOfSets(baseSet, query);
        }
        else
        {
            List<Map<String, Object>> rowsValue = queryForListWithStats(SELECT_COMPLEX_SET_VALUES_BY_SET_ID_SQL, baseSet.getId());
            Iterator<Map<String, Object>> itValue = rowsValue.iterator();
            while (itValue.hasNext())
            {
                Map<String, Object> rowValue = itValue.next();

                Batch batch = batchRepository.getBatch((Long)rowValue.get("batch_id"));
                BaseEntity baseEntity = load((Long)rowValue.get("entity_value_id"));
                baseSet.put(new BaseValue(batch, (Long)rowValue.get("index"),
                        (Date) rowValue.get("rep_date"), baseEntity));
            }
        }
    }

}
