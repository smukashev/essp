package kz.bsbnb.usci.receiver.service.impl;

import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import kz.bsbnb.usci.tool.status.ReceiverStatus;
import kz.bsbnb.usci.tool.status.ReceiverStatusSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author k.tulbassiyev
 */
@Service
public class BatchProcessServiceImpl implements IBatchProcessService {

    @Autowired
    private ZipFilesMonitor zipFilesMonitor;

    @Autowired
    private ReceiverStatusSingleton receiverStatusSingleton;

    @PostConstruct
    public void init() {
    }

    @Override
    public void processBatch(String fileName, Long userId) {
        zipFilesMonitor.readFiles(fileName, userId);
    }

    @Override
    public void processBatchWithoutUser(String fileName) {
        zipFilesMonitor.readFilesWithoutUser(fileName);
    }

    @Override
    public ReceiverStatus getStatus()
    {
        return receiverStatusSingleton.getStatus();
    }

    @Override
    public boolean restartBatch(long id) {
        return zipFilesMonitor.restartBatch(id);
    }
}
