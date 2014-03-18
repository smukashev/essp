package kz.bsbnb.usci.tool.status;

import java.io.Serializable;

public class ReceiverStatus implements Serializable
{
    private long queueSize;
    private long batchesInProgress;
    private long batchesCompleted;

    public long getQueueSize()
    {
        return queueSize;
    }

    public void setQueueSize(long queueSize)
    {
        this.queueSize = queueSize;
    }

    public long getBatchesInProgress()
    {
        return batchesInProgress;
    }

    public void setBatchesInProgress(long batchesInProgress)
    {
        this.batchesInProgress = batchesInProgress;
    }

    public long getBatchesCompleted()
    {
        return batchesCompleted;
    }

    public void setBatchesCompleted(long batchesCompleted)
    {
        this.batchesCompleted = batchesCompleted;
    }

    @Override
    public String toString()
    {
        return "ReceiverStatus{" +
                "queueSize=" + queueSize +
                ", batchesInProgress=" + batchesInProgress +
                ", batchesCompleted=" + batchesCompleted +
                '}';
    }
}
