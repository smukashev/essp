package kz.bsbnb.usci.receiver.service;

import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.tool.status.ReceiverStatus;

import java.util.HashMap;

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
}
