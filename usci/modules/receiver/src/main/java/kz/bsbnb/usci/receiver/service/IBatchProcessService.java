package kz.bsbnb.usci.receiver.service;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.model.stats.QueryEntry;
import kz.bsbnb.usci.eav.util.QueueOrderType;
import kz.bsbnb.usci.tool.status.ReceiverStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IBatchProcessService {
    void processBatch(String fileName, Long userId, boolean isNB);

    ReceiverStatus getStatus();

    boolean restartBatch(long id);
    void CancelBatch(long id);

    void reloadJobLauncherConfig();

    String getJobLauncherStatus();

    HashMap<String, QueryEntry> getSQLStats();

    void clearSQLStats();

    List<InputInfo> getQueueListPreview(List<Creditor> creditors,
                                        Set<Long> priorityCreditors, QueueOrderType queueOrderType);

    long parseCreditorId(byte[] bytes);
}
