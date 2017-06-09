package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author k.tulbassiyev
 */
@Service
public class BatchServiceImpl implements IBatchService {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier(value = "remoteBatchService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    private kz.bsbnb.usci.core.service.IBatchService remoteBatchService;

    @PostConstruct
    public void init() {
        remoteBatchService = (kz.bsbnb.usci.core.service.IBatchService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public long save(Batch batch) {
        return remoteBatchService.save(batch);
    }

    @Override
    public Batch getBatch(long batchId) {
        return remoteBatchService.getBatch(batchId);
    }

    @Override
    public long uploadBatch(Batch batch) {
        return remoteBatchService.uploadBatch(batch);
    }

    @Override
    public Long addBatchStatus(BatchStatus batchStatus) {
        return remoteBatchService.addBatchStatus(batchStatus);
    }

    @Override
    public Long addEntityStatus(EntityStatus entityStatus) {
        return remoteBatchService.addEntityStatus(entityStatus);
    }

    @Override
    public void endBatch(long batchId) {
        remoteBatchService.endBatch(batchId);
    }

    @Override
    public List<EntityStatus> getEntityStatusList(long batchId) {
        return remoteBatchService.getEntityStatusList(batchId);
    }

    @Override
    public List<BatchStatus> getBatchStatusList(long batchId) {
        return remoteBatchService.getBatchStatusList(batchId);
    }

    @Override
    public List<Batch> getPendingBatchList() {
        return remoteBatchService.getPendingBatchList();
    }

    @Override
    public List<Batch> getBatchListToSign(long userId) {
        return remoteBatchService.getBatchListToSign(userId);
    }

    @Override
    public void signBatch(long batchId, String sign, String signInfo, Date signTime) {
        remoteBatchService.signBatch(batchId, sign, signInfo, signTime);
    }

    @Override
    public List<Batch> getAll(Date repDate) {
        return remoteBatchService.getAll(repDate);
    }

    @Override
    public boolean incrementActualCounts(Map<Long, Long> batchesToUpdate) {
        return remoteBatchService.incrementActualCounts(batchesToUpdate);
    }

    @Override
    public boolean clearActualCount(long batchId) {
        return remoteBatchService.clearActualCount(batchId);
    }
}
