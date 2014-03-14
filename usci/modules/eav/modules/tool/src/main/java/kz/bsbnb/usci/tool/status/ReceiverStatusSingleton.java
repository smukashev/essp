package kz.bsbnb.usci.tool.status;

import org.springframework.stereotype.Component;

@Component
public class ReceiverStatusSingleton
{
    ReceiverStatus status = new ReceiverStatus();

    public synchronized void batchStarted() {
        status.setBatchesInProgress(status.getBatchesInProgress() + 1);
        status.setQueueSize(status.getQueueSize() - 1);
    }

    public synchronized void batchEnded() {
        status.setBatchesCompleted(status.getBatchesCompleted() + 1);
        status.setBatchesInProgress(status.getBatchesInProgress() - 1);
    }

    public synchronized void batchReceived() {
        status.setQueueSize(status.getQueueSize() + 1);
    }

    public ReceiverStatus getStatus()
    {
        return status;
    }
}
