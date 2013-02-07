package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
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
    private String INSERT_DATE_VALUE_SQL;
    private String SELECT_DATE_VALUES_BY_ENTITY_ID_SQL;

    @Autowired
    private IMetaClassDao postgreSQLMetaClassDaoImpl;

    @PostConstruct
    public void init()
    {
        INSERT_ENTITY_SQL = String.format("INSERT INTO %s (containing_class_id) VALUES ( ? )", getConfig().getEntitiesTableName());
        SELECT_ENTITY_BY_ID_SQL = String.format("SELECT * FROM %s WHERE id = ?", getConfig().getEntitiesTableName());
        DELETE_ENTITY_BY_ID_SQL = String.format("DELETE FROM %s WHERE id = ?", getConfig().getEntitiesTableName());
        INSERT_DATE_VALUE_SQL = String.format("INSERT INTO %s (entity_id, batch_id, attribute_id, date_value) VALUES ( ?, ?, ?, ? )", getConfig().getDateValuesTableName());
        SELECT_DATE_VALUES_BY_ENTITY_ID_SQL = String.format(
                "SELECT savpp.batch_id,\n" +
                "       savpp.attribute_name,\n" +
                "       savpp.date_value\n" +
                "  FROM (SELECT (rank() over(PARTITION BY sav.attribute_id ORDER BY sav.batch_id DESC)) AS num_pp,\n" +
                "               sav.*\n" +
                "          FROM (SELECT dv.batch_id,\n" +
                "                       dv.attribute_id,\n" +
                "                       dv.name as attribute_name,\n" +
                "                       dv.date_value\n" +
                "                  FROM %s dv,\n" +
                "                       %s sa\n" +
                "                 WHERE dv.entity_id = ?\n" +
                "                   AND dv.attribute_id = sa.id) sav\n" +
                "         ) savpp\n" +
                " WHERE savpp.num_pp = 1\n", getConfig().getDateValuesTableName(), getConfig().getSimpleAttributesTableName());
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
    public BaseEntity load(long id) {
        if(id < 1)
            return null;

        BaseEntity baseEntity = new BaseEntity();
        baseEntity.setId(id);

        loadBaseEntity(baseEntity);
        loadDateAttributeValues(baseEntity);

        return baseEntity;
    }

    @Override
    @Transactional
    public long save(BaseEntity baseEntity) {
        if(baseEntity.getMeta() == null)
        {
            throw new IllegalArgumentException("MetaClass must be set in the BaseEntity before entity insertion to DB.");
        }

        long baseEntityId = 0;
        if (baseEntity.getId() < 1) {
            baseEntityId = insertBaseEntity(baseEntity);
            baseEntity.setId(baseEntityId);
        }

        Set<String> dateAttributeNames = baseEntity.getPresentDateAttributeNames();
        if (!dateAttributeNames.isEmpty()) {
            insertDateAttributeValues(baseEntity, dateAttributeNames);
        }

        return baseEntityId;
    }

    @Override
    public void remove(BaseEntity baseEntity) {
        if(baseEntity.getId() < 1)
        {
            throw new IllegalArgumentException("Can't remove BaseEntity without id.");
        }

        updateWithStats(DELETE_ENTITY_BY_ID_SQL, baseEntity.getId());
    }

    @Override
    public BaseEntity load(BaseEntity baseEntity, boolean eager) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private long insertBaseEntity(BaseEntity baseEntity) {
        if(baseEntity.getMeta().getId() < 1) {
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

    private void insertDateAttributeValues(BaseEntity baseEntity, Set<String> attributeNames) {
        /*MetaClass metaClass = baseEntity.getMeta();

        Long[] entityIds = new Long[attributeNames.size()];
        Long[] batchIds = new Long[attributeNames.size()];
        Long[] attributeIds = new Long[attributeNames.size()];
        Date[] attributeValues = new Date[attributeNames.size()];

        int i = 0;
        Iterator<String> it = attributeNames.iterator();
        while (it.hasNext()) {
            String attributeNameForInsert = it.next();

            IMetaType metaType = metaClass.getMemberType(attributeNameForInsert);
            MetaValue metaValue = (MetaValue)metaType;

            entityIds[i] = baseEntity.getId();
            batchIds[i] = batch.getId();
            attributeIds[i] = metaValue.getId();
            attributeValues[i] = baseEntity.getDate(attributeNameForInsert);

            i++;
        }

        List<Object[]> batchArgs = new ArrayList<Object[]>();
        batchArgs.add(entityIds);
        batchArgs.add(batchIds);
        batchArgs.add(attributeIds);
        batchArgs.add(attributeValues);

        batchUpdateWithStats(INSERT_DATE_VALUE_SQL, batchArgs);*/

        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void loadDateAttributeValues(BaseEntity baseEntity) {
        List<Map<String, Object>> rows = queryForListWithStats(SELECT_DATE_VALUES_BY_ENTITY_ID_SQL, baseEntity.getId());

        Iterator<Map<String, Object>> it = rows.iterator();
        while (it.hasNext())
        {
            Map<String, Object> row = it.next();
                baseEntity.set(
                        (String) row.get("attribute_name"),
                        new Date(((java.sql.Date) row.get("date_value")).getTime())
                );
        }
    }

    private void loadBaseEntity(BaseEntity baseEntity) {
        if(baseEntity.getId() < 1)
        {
            throw new IllegalArgumentException("Base entity does not have id. Can't load.");
        }

        List<Map<String, Object>> rows = queryForListWithStats(SELECT_ENTITY_BY_ID_SQL, baseEntity.getId());

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one base entity found. Can't load.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalArgumentException("Class not found. Can't load.");
        }

        Map<String, Object> row = rows.get(0);

        if(row != null)
        {
            MetaClass meta = postgreSQLMetaClassDaoImpl.load((Integer)row.get("class_id"));

            baseEntity.setId((Integer)row.get("id"));
            baseEntity.setMeta(meta);
        }
        else
        {
            logger.error("Can't load BaseEntity, empty data set.");
        }
    }

}
