

import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import org.junit.Test;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Bauyrzhan.Makhambeto on 12.01.2015.
 */
@Component
public class TestHolder extends JDBCSupport {
    ArrayList<SingleTest> tests = new ArrayList<SingleTest>();
    String lastWrong;

    public void addTest(SingleTest test){
        tests.add(test);
    }

    public int runBatch(){
        try {
            int n = tests.size();
            StringBuilder sql = new StringBuilder("select ");
            int i = 1;
            for(SingleTest test : tests){
                sql.append(String.format("(" + test.getSql() +") as \"%d\"" , i++ ));
                if(i<=n)
                    sql.append(",");
            }

            sql.append(" from dual");

            Map<String,Object> res = jdbcTemplate.queryForMap(sql.toString());

            i = 1;
            for(SingleTest test: tests){
                Integer received = ((BigDecimal)res.get(i++ +"")).intValue();
                if(received != test.answer){
                    lastWrong = test.getSql() + " received: " + received + ",expected: " + test.answer;
                    return -1;
                }
            }
            return 0;
        } finally  {
            tests.clear();
        }
    }

}
