package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.sync.service.IBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Потоко-безопасный асинхронный job для обновления actual count для батчей
 */
@Component
public class ActualCountJob extends Thread {

    IBatchService batchService;

    //Мап ключ: batchId, значение: колво
    Map<Long, Long> batchActualCount = new HashMap<Long,Long>();

    Logger logger = LoggerFactory.getLogger(ActualCountJob.class);

    public ActualCountJob(IBatchService batchService) {
        this.batchService = batchService;
    }

    public void run() {

        try{
            while(true) {
                Map<Long, Long> m = new HashMap<>();
                for (Entry<Long, Long> entry : batchActualCount.entrySet()) {
                    m.put(entry.getKey(), entry.getValue());
                }

                if (m.size() > 0 && batchService.incrementActualCounts(m)) {
                    synchronized (this) {
                        for (Entry<Long, Long> entry : m.entrySet()) {
                            Long count = batchActualCount.get(entry.getKey());
                            count -= entry.getValue();
                            if (count > 0)
                                batchActualCount.put(entry.getKey(), count);
                            else
                                batchActualCount.remove(entry.getKey());
                        }
                    }
                }

                Thread.sleep(5000);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    //synchronized нужен для атомичности
    public synchronized void insertBatchId(Long batchId){
        Long count = batchActualCount.get(batchId);
        count = count == null ? 0 : count;
        batchActualCount.put(batchId, count + 1);
    }
}
