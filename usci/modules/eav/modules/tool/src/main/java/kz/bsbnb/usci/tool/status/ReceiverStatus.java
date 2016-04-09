package kz.bsbnb.usci.tool.status;

import java.io.Serializable;

public class ReceiverStatus implements Serializable
{
    private long queueSize;
    private long batchesInProgress;
    private long batchesCompleted;
    private long rulesEvaluationTimeAvg;
    private String jobLauncherStatus;

    long getQueueSize()
    {
        return queueSize;
    }

    void setQueueSize(long queueSize)
    {
        this.queueSize = queueSize;
    }

    long getBatchesInProgress()
    {
        return batchesInProgress;
    }

    void setBatchesInProgress(long batchesInProgress)
    {
        this.batchesInProgress = batchesInProgress;
    }

    long getBatchesCompleted()
    {
        return batchesCompleted;
    }

    void setBatchesCompleted(long batchesCompleted)
    {
        this.batchesCompleted = batchesCompleted;
    }

    public long getRulesEvaluationTimeAvg() {
        return rulesEvaluationTimeAvg;
    }

    public void setRulesEvaluationTimeAvg(long rulesEvaluationTimeAvg) {
        this.rulesEvaluationTimeAvg = rulesEvaluationTimeAvg;
    }

    public void setJobLauncherStatus(String jobLauncherStatus) {
        this.jobLauncherStatus = jobLauncherStatus;
    }

    @Override
    public String toString() {
        return "ReceiverStatus{" +
                "queueSize=" + queueSize +
                ", batchesInProgress=" + batchesInProgress +
                ", batchesCompleted=" + batchesCompleted +
                ", rulesEvaluationTimeAvg=" + rulesEvaluationTimeAvg +
                "} \n" + jobLauncherStatus;
    }
}
