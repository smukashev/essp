package kz.bsbnb.usci.receiver.queue;

import kz.bsbnb.usci.eav.model.json.BatchInfo;

/**
 * Created by bauyrzhan.makhambeto on 19/10/2015.
 */
public class JobInfo {
    long batchId;
    BatchInfo batchInfo;

    public JobInfo(long batchId, BatchInfo batchInfo) {
        this.batchId = batchId;
        this.batchInfo = batchInfo;
    }

    public long getBatchId() {
        return batchId;
    }

    public BatchInfo getBatchInfo() {
        return batchInfo;
    }
}
