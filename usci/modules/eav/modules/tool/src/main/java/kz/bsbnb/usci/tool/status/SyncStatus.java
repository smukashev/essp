package kz.bsbnb.usci.tool.status;

import java.io.Serializable;

public class SyncStatus implements Serializable {
    private long queueSize;
    private long threadsCount;
    private double avgTime;
    private long executorCnt;
    private double avgExecutor;

    public long getQueueSize() {
        return queueSize;
    }

    void setQueueSize(long queueSize) {
        this.queueSize = queueSize;
    }

    public long getThreadsCount() {
        return threadsCount;
    }

    void setThreadsCount(long threadsCount) {
        this.threadsCount = threadsCount;
    }

    public double getAvgTime() {
        return avgTime;
    }

    void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    public void setExectuorStat(long executorCnt, double avgTime) {
        this.executorCnt = executorCnt;
        this.avgExecutor = avgTime;
    }

    @Override
    public String toString() {
        return "SyncStatus{" +
                "queueSize=" + queueSize +
                ", threadsCount=" + threadsCount +
                ", avgTime=" + avgTime +
                ", executorCnt=" + executorCnt +
                ", avgExecutor=" + avgExecutor +
                '}';
    }
}
