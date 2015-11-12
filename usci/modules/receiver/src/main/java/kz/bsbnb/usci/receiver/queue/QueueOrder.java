package kz.bsbnb.usci.receiver.queue;

import kz.bsbnb.usci.eav.model.json.BatchInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public interface QueueOrder {
    public JobInfo getNextFile(List<JobInfo> files);
    public int compare(JobInfo jobInfo1, JobInfo jobInfo2);
}
