package kz.bsbnb.dao;

import kz.bsbnb.*;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;

@Component
//@Scope(value = "thread")
public class DataEntityDao {

    JdbcTemplate jdbcTemplate;

    MetaClassDao metaClassDao;

    @Autowired
    public void setDataSource(DataSource source){
        jdbcTemplate = new JdbcTemplate(source);
    }

    /**
     * 1) sequence generation technique ???
     * 2) fk -> eav_be_entities
     *
     * @param entity
     */
    public void insert(DataEntity entity) {
        insertEBE(entity);
        MetaClass meta = entity.getMeta();
        StringBuilder buf = new StringBuilder(100);
        buf.append("insert into ")
                .append(meta.getClassName());
        buf.append(" (");
        buf.append("creditor_id,");
        buf.append("report_date,");
        buf.append("entity_id,");
        Iterator<String> it = entity.getAttributes().iterator();
        while(it.hasNext()) {
            buf.append(it.next());
            if(it.hasNext())
                buf.append(",");
        }
        buf.append(") values (?,?,?,");

        it = entity.getAttributes().iterator();
        Object[] values = new Object[entity.getAttributes().size() + 3];
        int i = 0;
        values[i++] = entity.getCreditorId();
        values[i++] = entity.getReportDate();
        values[i++] = entity.getId();
        while(it.hasNext()) {
            values[i++] = entity.getBaseValue(it.next()).getValue();
            buf.append("?");
            if(it.hasNext())
                buf.append(",");
        }
        buf.append(")");

        System.out.println(buf.toString());

        jdbcTemplate.update(buf.toString(),values);

    }

    private void insertEBE(DataEntity entity) {
        MetaClass meta = entity.getMeta();
        /*StringBuilder buf = new StringBuilder(100);
        buf.append("insert into ")
                .append("EAV_BE_ENTITIES");
        buf.append(" (");
        buf.append("creditor_id,");
        buf.append("entity_id,");
        buf.append("class_id,");
        buf.append("entity_key");
        //buf.append(") values (?,SEQ_ENTITY.nexvtal,?) returning entity_id");
        buf.append(") values (?,2,?,?) returning into entity_id");

        /*jdbcTemplate.update(buf.toString(),
                        new Object[]{entity.getCreditorId(),
                        meta.getId(),""});*/
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("EAV_BE_ENTITIES")
                .usingGeneratedKeyColumns("ENTITY_ID");
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("creditor_id", entity.getCreditorId());
        //parameters.put("entity_id", 1L);
        parameters.put("class_id", meta.getId());
        parameters.put("entity_key"," ");
        parameters.put("IS_DELETED","0");
        parameters.put("SYSTEM_DATE", new Date());
        Number number = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
        System.out.println(number.longValue());
        entity.setId(number.longValue());


    }


    public DataEntity load(long id, long creditorId, Date reportDate) {
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM EAV_BE_ENTITIES where ENTITY_ID = :ENTITY_ID", id);

        if(maps.size() < 1)
            throw new RuntimeException("No such entity with id:" + id);

        if(maps.size() != 1)
            throw new RuntimeException("Incorrect fetch size: " + id);

        Map<String, Object> row = maps.iterator().next();
        long classId = ((BigDecimal) row.get("CLASS_ID")).longValue();
        MetaClass metaClass = metaClassDao.load(classId);
        DataEntity entity = new DataEntity(metaClass)
                .withReportDate(reportDate);
        StringBuilder buf = new StringBuilder(100);
        buf.append("SELECT * FROM ");
        buf.append(metaClass.getClassName());
        buf.append(" WHERE ENTITY_ID = ?");
        buf.append(" AND CREDITOR_ID = ?");
        buf.append(" AND REPORT_DATE = ?");

        maps = jdbcTemplate.queryForList(buf.toString(), id, creditorId, reportDate);
        assert maps.size() == 1;

        Map<String, Object> next = maps.iterator().next();
        for (String attribute : metaClass.getAttributeNames()) {
            if(next.get(attribute) != null) {
                IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
                IMetaType metaType = metaAttribute.getMetaType();
                if(!metaType.isComplex()) {
                    if(!metaType.isSet()) {
                        MetaValue metaSimple = (MetaValue) metaType;
                        Object value = next.get(attribute);
                        switch (metaSimple.getTypeCode()) {
                            case DOUBLE:
                                entity.setDataValue(attribute, new DataDoubleValue(value));
                                break;
                            case STRING:
                                entity.setDataValue(attribute, new DataStringValue(value));
                                break;
                            case DATE:
                                entity.setDataValue(attribute, new DataDateValue(value));
                                break;

                        }

                    }
                }
            }
        }
        return entity;
    }

    @Autowired
    public void setMetaSource(MetaClassDao metaClassDao) {
        this.metaClassDao = metaClassDao;
    }
}
