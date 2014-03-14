package kz.bsbnb.usci.tool.status;

import org.springframework.stereotype.Component;

@Component
public class SyncStatusSingleton
{
    SyncStatus status = new SyncStatus();

    public void put(long queueSize, long threadsCount, long maxThreadsCount, double avgTime) {
        status.setQueueSize(queueSize);
        status.setThreadsCount(threadsCount);
        status.setMaxThreadsCount(maxThreadsCount);
        status.setAvgTime(avgTime);
    }

    public SyncStatus getStatus()
    {
        return status;
    }
}
