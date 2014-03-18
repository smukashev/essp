package kz.bsbnb.usci.tool.status;

import java.io.Serializable;

public class SyncStatus implements Serializable
{
    long queueSize;
    long threadsCount;
    long maxThreadsCount;
    double avgTime;

    public long getQueueSize()
    {
        return queueSize;
    }

    public void setQueueSize(long queueSize)
    {
        this.queueSize = queueSize;
    }

    public long getThreadsCount()
    {
        return threadsCount;
    }

    public void setThreadsCount(long threadsCount)
    {
        this.threadsCount = threadsCount;
    }

    public long getMaxThreadsCount()
    {
        return maxThreadsCount;
    }

    public void setMaxThreadsCount(long maxThreadsCount)
    {
        this.maxThreadsCount = maxThreadsCount;
    }

    public double getAvgTime()
    {
        return avgTime;
    }

    public void setAvgTime(double avgTime)
    {
        this.avgTime = avgTime;
    }

    @Override
    public String toString()
    {
        return "SyncStatus{" +
                "queueSize=" + queueSize +
                ", threadsCount=" + threadsCount +
                ", maxThreadsCount=" + maxThreadsCount +
                ", avgTime=" + avgTime +
                '}';
    }
}
