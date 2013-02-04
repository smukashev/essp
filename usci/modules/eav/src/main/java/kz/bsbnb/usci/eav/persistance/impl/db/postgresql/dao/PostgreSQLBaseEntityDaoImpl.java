package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
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
    private String INSERT_DATE_VALUE_SQL;

    @PostConstruct
    public void init()
    {
        INSERT_ENTITY_SQL = String.format("INSERT INTO %s (containing_class_id) VALUES ( ? )", getConfig().getEntitiesTableName());
        INSERT_DATE_VALUE_SQL = String.format("INSERT INTO %s (entity_id, batch_id, attribute_id, date_value) VALUES ( ?, ?, ?, ? )", getConfig().getDateValuesTableName());
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
        throw new UnsupportedOperationException("Not supported yet.");
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
    public void remove(BaseEntity persistable) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        MetaClass metaClass = baseEntity.getMeta();
        Batch batch = baseEntity.getBatch();

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

        jdbcTemplate.batchUpdate(INSERT_DATE_VALUE_SQL, batchArgs);
    }

}
