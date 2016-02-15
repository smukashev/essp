package kz.bsbnb.usci.cli.app.exporter;

import kz.bsbnb.usci.cli.app.exporter.model.Query;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.String;

public abstract class AbstractTable implements ITable {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected DSLContext context;

    protected JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Query getQueries(Long entityId) {
        Select select = context.selectFrom(getTable())
            .where(getTable().field("ENTITY_ID").eq(entityId));

        List<java.lang.String> qList = new ArrayList<>();
        List<Long> eList = new ArrayList<>();
        Query ret = new Query(qList, eList);

        List<Map<java.lang.String,Object>> rows = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray());

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

    public void setContext(DSLContext context) {
        this.context = context;
    }
}
