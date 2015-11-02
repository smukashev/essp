package kz.bsbnb.usci.receiver.queue.impl;

import kz.bsbnb.usci.receiver.queue.JobInfo;
import kz.bsbnb.usci.receiver.queue.QueueOrder;

import java.util.List;

/**
 * Created by bauyrzhan.makhambeto on 28/10/2015.
 */
public abstract class AbstractQueueOrder implements QueueOrder {
    public JobInfo getNextFile(List<JobInfo> files) {
        JobInfo result = null;
        for (JobInfo file : files) {
            if (result == null || compare(result, file) > 0) {
                result = file;
            }
        }
        return result;
    }
}
