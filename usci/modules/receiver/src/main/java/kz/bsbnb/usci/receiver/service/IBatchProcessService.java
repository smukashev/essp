package kz.bsbnb.usci.receiver.service;

import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.util.QueueOrderType;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.tool.status.ReceiverStatus;

import java.util.HashMap;

import java.util.List;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IBatchProcessService {
    public void processBatch(String fileName, Long userId, boolean isNB);
    public void processBatch(String fileName, Long userId);
    public void processBatchWithoutUser(String fileName);
    public ReceiverStatus getStatus();
    HashMap<String, QueryEntry> getSQLStats();
    void clearSQLStats();
    public boolean restartBatch(long id);
    public String getJobLauncherStatus();
    public void reloadJobLauncherConfig();
    public List<InputInfo> getQueueListPreview(List<Creditor> creditors, Set<Long> priorityCreditors, QueueOrderType queueOrderType);
}
