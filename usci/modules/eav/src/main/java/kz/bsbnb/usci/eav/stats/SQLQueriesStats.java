package kz.bsbnb.usci.eav.stats;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Scope(value = "singleton")
public class SQLQueriesStats {
    private HashMap<String, QueryEntry> stats = new HashMap<String, QueryEntry>();

    public void put(String query, long time)
    {
        QueryEntry qe = stats.get(query);

        if(qe == null)
        {
            qe = new QueryEntry();
            qe.maxTime = time;
            qe.minTime = time;
            qe.totalTime = time;
            qe.count = 1;
            stats.put(query, qe);
        }
        else
        {
            qe.count++;
            qe.totalTime += time;
            if(time > qe.maxTime)
                qe.maxTime = time;

            if(time < qe.minTime)
                qe.minTime = time;
        }
    }

    public HashMap<String, QueryEntry> getStats()
    {
        return stats;
    }
}
