import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

public class SingleTest {
    HashMap<String,Object> conditions;
    String tableName;
    Integer answer;

    public SingleTest(){
        conditions = new HashMap<String, Object>();
    }

    SingleTest(HashMap conditions, String tableName, Integer answer){
        this.conditions = conditions;
        this.tableName = tableName;
        this.answer = answer;
    }

    public void setTable(String tableName){
        this.tableName = tableName;
    }

    public void setAnswer(Integer answer){
        this.answer = answer;
    }

    public void addCond(String key, Object value){
        if(value instanceof String){
            String r = (String)value;
            if(!r.startsWith("date"))
                value = "'" + value + "'";
        }
        conditions.put(key, value);
    }

    public String getSql(){
        String sql = String.format("select count(1) from %s",tableName);
        StringBuilder where = new StringBuilder();
        int n = conditions.size();

        if(conditions.size() > 0){
            where.append(" where ");
            for(String condition : conditions.keySet()){
                n--;
                where.append(condition + "=" + conditions.get(condition));
                if(n > 0)
                    where.append(" and ");
            }
            sql = sql + where.toString();
        }
        return sql;
    }
}