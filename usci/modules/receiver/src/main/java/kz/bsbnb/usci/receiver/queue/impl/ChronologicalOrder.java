package kz.bsbnb.usci.receiver.queue.impl;

import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.receiver.queue.JobInfo;
import kz.bsbnb.usci.receiver.queue.QueueOrder;

import java.util.List;
import java.util.Set;

public class ChronologicalOrder extends AbstractQueueOrder{
    @Override
    public int compare(JobInfo j1, JobInfo j2) {
        return (int) (j1.getBatchId() - j2.getBatchId());
    }
}
