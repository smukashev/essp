package kz.bsbnb.usci.receiver.service.impl;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.model.stats.QueryEntry;
import kz.bsbnb.usci.eav.model.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.QueueOrderType;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import kz.bsbnb.usci.tool.status.ReceiverStatus;
import kz.bsbnb.usci.tool.status.ReceiverStatusSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
@Service
public class BatchProcessServiceImpl implements IBatchProcessService {

    @Autowired
    private ZipFilesMonitor zipFilesMonitor;

    @Autowired
    private ReceiverStatusSingleton receiverStatusSingleton;

    @Autowired
    protected SQLQueriesStats sqlStats;

    @Override
    public void processBatch(String fileName, Long userId, boolean isNB) {
        zipFilesMonitor.readFiles(fileName, userId, isNB);
    }

    @Override
    public ReceiverStatus getStatus() {
        ReceiverStatus rs = receiverStatusSingleton.getStatus();

        HashMap<String, QueryEntry> stats = sqlStats.getStats();

        long time = 0;
        long count = 0;

        for (String query : stats.keySet()) {
            if (stats.get(query).count < 1)
                continue;

            time += stats.get(query).totalTime / stats.get(query).count;
            count++;
        }

        if (count > 0) {
            rs.setRulesEvaluationTimeAvg(time / count);
        }

        rs.setJobLauncherStatus(zipFilesMonitor.getJobLauncherQueue().getStatus());

        return rs;
    }

    @Override
    public HashMap<String, QueryEntry> getSQLStats() {
        return sqlStats.getStats();
    }

    @Override
    public void clearSQLStats() {
        sqlStats.clear();
    }

    @Override
    public boolean restartBatch(long id) {
        return zipFilesMonitor.restartBatch(id);
    }

    @Override
    public void CancelBatch(long id) { zipFilesMonitor.CancelBatch(id);}

    @Override
    public String getJobLauncherStatus() {
        return zipFilesMonitor.getJobLauncherQueue().getStatus();
    }

    @Override
    public void reloadJobLauncherConfig() {
        zipFilesMonitor.getJobLauncherQueue().reloadConfig();
    }

    @Override
    public List<InputInfo> getQueueListPreview(List<Creditor> creditors, Set<Long> priorityCreditors, QueueOrderType queueOrderType) {
        return zipFilesMonitor.getJobLauncherQueue().getOrderedFiles(creditors, priorityCreditors, queueOrderType);
    }
}
