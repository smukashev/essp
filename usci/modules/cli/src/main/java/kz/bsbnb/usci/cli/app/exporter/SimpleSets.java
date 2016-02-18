package kz.bsbnb.usci.cli.app.exporter;

import kz.bsbnb.usci.cli.app.exporter.model.Query;
import org.jooq.Select;
import org.jooq.Table;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.lang.String;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITY_SIMPLE_SETS;


@Component
public class SimpleSets extends AbstractTable {

  StringSetValues eavStringSetValues;

  @Override
  public Table getTable() {
    return EAV_BE_ENTITY_SIMPLE_SETS;
  }

  @PostConstruct
  public void init(){
    eavStringSetValues = new StringSetValues();
    eavStringSetValues.setDataSource(jdbcTemplate.getDataSource());
    eavStringSetValues.setContext(context);
  }

  @Override
  public Query getQueries(Long entityId) {
    Select select = context.selectFrom(getTable())
        .where(getTable().field("ENTITY_ID").eq(entityId));

    List<String> qList = new ArrayList<>();
    List<Long> eList = new ArrayList<>();
    Query ret = new Query(qList, eList);
    Set<Long> sets = new HashSet<>();

    List<Map<String,Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());

    for(Map<String,Object> row : rows) {
      StringBuilder fields = new StringBuilder("insert into " + getTable().getName() + "( ");
      StringBuilder vals = new StringBuilder(" values (");

      Iterator<String> iterator = row.keySet().iterator();

      while(iterator.hasNext()) {
        String field = iterator.next();
        Object val = row.get(field);

        if(val instanceof String) {
          val = "'" + val + "'";
        }

        if(val instanceof Date || val instanceof Timestamp) {
          val = "date '"+val.toString().substring(0, 10)+"'";
        }


        fields.append(field);
        vals.append(val);

        if(iterator.hasNext()) {
          fields.append(',');
          vals.append(',');
        }
      }

      if(row.get("SET_ID") != null)
        sets.add(((BigDecimal) row.get("SET_ID")).longValue());

      fields.append(")");
      vals.append(");");

      qList.add(fields.toString() + vals.toString());
    }

    for(Long setId : sets) {
      Query query = eavStringSetValues.getQueries(setId);
      qList.addAll(query.getQueries());
    }

    return ret;
  }
}
