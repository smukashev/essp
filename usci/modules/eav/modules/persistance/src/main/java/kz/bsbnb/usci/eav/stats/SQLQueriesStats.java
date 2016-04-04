package kz.bsbnb.usci.eav.stats;

import kz.bsbnb.usci.eav.StaticRouter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope
public class SQLQueriesStats implements InitializingBean {
    private final static boolean statsEnabled = StaticRouter.isDevMode();
    private volatile boolean flag = false;

    private final Map<String, QueryEntry> stats = new HashMap<>();

    private final StatsWorker statsWorker = new StatsWorker();

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(statsWorker).start();
    }

    private class QueryData {
        String query;
        long time;

        QueryData(String query, long time) {
            this.query = query;
            this.time = time;
        }
    }

    public void put(String query, long time) {
        if (statsEnabled)
            statsWorker.addData(query, time);
    }

    public HashMap<String, QueryEntry> getStats() {
        flag = true;
        HashMap<String, QueryEntry> ret = new HashMap<>();

        for (String query : stats.keySet()) {
            QueryEntry cur = stats.get(query);
            QueryEntry qe = new QueryEntry();
            qe.maxTime = cur.maxTime;
            qe.minTime = cur.minTime;
            qe.totalTime = cur.totalTime;
            qe.count = cur.count;
            ret.put(query, qe);
        }

        flag = false;
        return ret;
    }

    public void clear() {
        flag = true;
        stats.clear();
        flag = false;
    }

    private class StatsWorker implements Runnable {
        private final int maxSize = 100000000;

        private int prevIndex = 0;
        private int lastIndex = 0;

        private final QueryData dataArray[] = new QueryData[maxSize];

        public void addData(String query, long time) {
            if (lastIndex == maxSize)
                lastIndex = 0;

            dataArray[lastIndex++] = new QueryData(query, time);
        }

        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                while(flag) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                int tmpLastIndex = lastIndex;

                if (tmpLastIndex == prevIndex) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                if (tmpLastIndex < prevIndex) {
                    doWork(prevIndex, maxSize);
                    prevIndex = 0;
                    doWork(prevIndex, tmpLastIndex);
                    prevIndex = tmpLastIndex;
                } else {
                    doWork(prevIndex, tmpLastIndex);
                    prevIndex = tmpLastIndex;
                }
            }
        }

        private void doWork(int prevIndex, int lastIndex) {
            for (int i = prevIndex; i < lastIndex; i++) {
                QueryData qd = dataArray[i];

                if (qd == null)
                    continue;

                QueryEntry qe = stats.get(qd.query);

                if (qe == null) {
                    qe = new QueryEntry();
                    qe.maxTime = qd.time;
                    qe.minTime = qd.time;
                    qe.totalTime = qd.time;
                    qe.count = 1;
                    stats.put(qd.query, qe);
                } else {
                    qe.count++;
                    qe.totalTime += qd.time;

                    if (qd.time > qe.maxTime)
                        qe.maxTime = qd.time;

                    if (qd.time < qe.minTime)
                        qe.minTime = qd.time;
                }

                dataArray[i] = null;
            }
        }
    }
}
