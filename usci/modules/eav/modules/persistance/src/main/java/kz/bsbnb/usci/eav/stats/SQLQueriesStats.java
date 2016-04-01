package kz.bsbnb.usci.eav.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Scope
public class SQLQueriesStats {
    private final HashMap<String, QueryEntry> stats = new HashMap<>();

    private static final boolean statsEnabled = true;

    public void put(final String query, final double time) {
        if (statsEnabled) {
            new Thread() {
                @Override
                public void run() {
                    synchronized (stats) {
                        QueryEntry qe = stats.get(query);

                        if (qe == null) {
                            qe = new QueryEntry();
                            qe.maxTime = time;
                            qe.minTime = time;
                            qe.totalTime = time;
                            qe.count = 1;
                            stats.put(query, qe);
                        } else {
                            qe.count++;
                            qe.totalTime += time;

                            if (time > qe.maxTime)
                                qe.maxTime = time;

                            if (time < qe.minTime)
                                qe.minTime = time;
                        }
                    }
                }
            }.start();
        }
    }

    public HashMap<String, QueryEntry> getStats() {
        HashMap<String, QueryEntry> ret = new HashMap<>();

        synchronized (stats) {
            for (String query : stats.keySet()) {
                QueryEntry cur = stats.get(query);
                QueryEntry qe = new QueryEntry();
                qe.maxTime = cur.maxTime;
                qe.minTime = cur.minTime;
                qe.totalTime = cur.totalTime;
                qe.count = cur.count;
                ret.put(query, qe);
            }
        }

        return ret;
    }

    public void clear() {
        synchronized (stats) {
            stats.clear();
        }
    }
}
