package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author k.tulbassiyev
 */
@Service
public class BatchServiceImpl implements IBatchService {
    @Autowired
    @Qualifier(value = "remoteBatchService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    kz.bsbnb.usci.core.service.IBatchService remoteBatchService;

    @PostConstruct
    public void init() {
        remoteBatchService = (kz.bsbnb.usci.core.service.IBatchService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public long save(Batch batch) {
        return remoteBatchService.save(batch);
    }

    @Override
    public Batch load(long batchId) {
        return remoteBatchService.load(batchId);
    }

    @Override
    public long uploadBatch(Batch batch) {
        return remoteBatchService.uploadBatch(batch);
    }

    @Override
    public void addBatchStatus(long batchId, long statusId) {
        remoteBatchService.addBatchStatus(batchId, statusId, null);
    }

    @Override
    public void addBatchStatus(long batchId, long statusId, String description) {
        remoteBatchService.addBatchStatus(batchId, statusId, description);
    }

    @Override
    public void addBatchStatus(long batchId, BatchStatuses batchStatus) {
        remoteBatchService.addBatchStatus(batchId, batchStatus, null);
    }

    @Override
    public void addBatchStatus(long batchId, BatchStatuses batchStatus, String description) {
        remoteBatchService.addBatchStatus(batchId, batchStatus, description);
    }

    @Override
    public void addEntityStatus(long batchId, long entityId, long statusId, Long index) {
        remoteBatchService.addEntityStatus(batchId, entityId, statusId, index, null);
    }

    @Override
    public void addEntityStatus(long batchId, long entityId, long statusId, Long index, String description) {
        remoteBatchService.addEntityStatus(batchId, entityId, statusId, index, description);
    }

    @Override
    public void addEntityStatus(long batchId, long entityId, EntityStatuses entityStatus, Long index) {
        remoteBatchService.addEntityStatus(batchId, entityId, entityStatus, index, null);
    }

    @Override
    public void addEntityStatus(long batchId, long entityId, EntityStatuses entityStatus, Long index, String description) {
        remoteBatchService.addEntityStatus(batchId, entityId, entityStatus, index, description);
    }

    @Override
    public void endBatch(long batchId) {
        remoteBatchService.endBatch(batchId);
    }
}
