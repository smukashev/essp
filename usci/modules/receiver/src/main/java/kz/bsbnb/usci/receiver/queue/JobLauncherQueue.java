package kz.bsbnb.usci.receiver.queue;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.eav.util.QueueOrderType;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;

import java.util.List;
import java.util.Set;

/**
 * Created by bauyrzhan.makhambeto on 19/10/2015.
 */
public interface JobLauncherQueue {
    JobInfo getNextJob();

    String getStatus();

    void jobFinished();

    void addJob(long batchId, BatchInfo batchInfo);

    List<InputInfo> getOrderedFiles(List<Creditor> creditors, Set<Long> creditorsWithPriority, QueueOrderType queueOrderType);

    void changeQueueType(QueueOrderType orderType);

    void reloadConfig();
}
