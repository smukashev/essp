package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.sync.service.IBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Потоко-безопасный асинхронный job для обновления actual count для батчей
 */
@Component
class ActualCountJob extends Thread {
    private final IBatchService batchService;

    private final Map<Long, Long> actualCountMap = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(ActualCountJob.class);

    ActualCountJob(IBatchService batchService) {
        this.batchService = batchService;
    }

    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            if (actualCountMap.size() > 0 && batchService.incrementActualCounts(actualCountMap))
                actualCountMap.clear();

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    void insertBatchId(Long batchId) {
        Long count = actualCountMap.get(batchId);
        count = count == null ? 0 : count;
        actualCountMap.put(batchId, count + 1);
    }
}
