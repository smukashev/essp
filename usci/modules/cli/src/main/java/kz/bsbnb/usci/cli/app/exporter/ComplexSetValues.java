package kz.bsbnb.usci.cli.app.exporter;

import kz.bsbnb.usci.cli.app.exporter.model.Query;
import org.jooq.Select;
import org.jooq.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.String;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_SET_VALUES;


public class ComplexSetValues extends AbstractTable {
  @Override
  public Table getTable() {
    return EAV_BE_COMPLEX_SET_VALUES;
  }

  @Override
  public Query getQueries(Long setId) {
    Select select = context.selectFrom(getTable())
        .where(getTable().field("SET_ID").eq(setId));

    List<String> qList = new ArrayList<>();
    List<Long> eList = new ArrayList<>();
    Query ret = new Query(qList, eList);

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

        if(val instanceof Date) {
          val = "date '"+val.toString().substring(0, 10)+"'";
        }


        fields.append(field);
        vals.append(val);

        if(iterator.hasNext()) {
          fields.append(',');
          vals.append(',');
        }
      }

      if(row.get("ENTITY_VALUE_ID") != null)
        eList.add(((BigDecimal) row.get("ENTITY_VALUE_ID")).longValue());

      fields.append(")");
      vals.append(");");

      qList.add(fields.toString() + vals.toString());
    }

    return ret;
  }


}
