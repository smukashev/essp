package kz.bsbnb.usci.receiver.queue.impl;

import kz.bsbnb.usci.receiver.queue.JobInfo;

/**
 * Created by bauyrzhan.makhambeto on 19/10/2015.
 */
public class MinimumWeightOrder extends AbstractQueueOrder {
    @Override
    public int compare(JobInfo j1, JobInfo j2) {
        return j1.getBatchInfo().getContentSize() - j2.getBatchInfo().getContentSize();
    }
}
