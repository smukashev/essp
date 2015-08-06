package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.IGlobalService;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IBatchStatusDao;
import kz.bsbnb.usci.eav.persistance.dao.IEntityStatusDao;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author k.tulbassiyev
 */
@Service
public class BatchServiceImpl implements IBatchService {

    @Autowired
    private IBatchDao batchDao;

    @Autowired
    private IBatchStatusDao batchStatusDao;

    @Autowired
    private IEntityStatusDao entityStatusDao;

    @Autowired
    private IGlobalService globalService;

    @Value("${batch.save.dir}")
    private String batchSaveDir;

    @Override
    public long save(Batch batch) {
        return batchDao.save(batch);
    }

    @Override
    public Batch getBatch(long batchId) {
        Batch batch = batchDao.load(batchId);
        File file = new File(getFilePath(batch));

        try {
            byte[] bytes = FileCopyUtils.copyToByteArray(file);
            batch.setContent(bytes);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return batch;
    }

    @Override
    public long uploadBatch(Batch batch) {
        saveBatchFile(batch);
        return batchDao.save(batch);
    }

    private void saveBatchFile(Batch batch) {
        File outputFile = new File(getFilePath(batch));

        try {
            FileCopyUtils.copy(batch.getContent(), outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFilePath(Batch batch) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        return batchSaveDir + "/" + df.format(batch.getRepDate())
                + "/" + batch.getCreditorId() + "/" + batch.getHash();
    }

    @Override
    public Long addBatchStatus(BatchStatus batchStatus) {
        if (batchStatus.getStatusId() < 1 && batchStatus.getStatus() != null) {
            EavGlobal status = globalService.getGlobal(batchStatus.getStatus());
            batchStatus.setStatusId(status.getId());
        }
        return batchStatusDao.insert(batchStatus);
    }

    @Override
    public void endBatch(long batchId) {
        EavGlobal statusCompleted = globalService.getGlobal(BatchStatuses.COMPLETED);
        addBatchStatus(new BatchStatus()
                .setBatchId(batchId)
                .setStatusId(statusCompleted.getId())
        );
    }

    @Override
    public Long addEntityStatus(EntityStatus entityStatus) {
        if (entityStatus.getStatusId() < 1 && entityStatus.getStatus() != null) {
            EavGlobal status = globalService.getGlobal(entityStatus.getStatus());
            entityStatus.setStatusId(status.getId());
        }
        return entityStatusDao.insert(entityStatus);
    }

    @Override
    public List<EntityStatus> getEntityStatusList(long batchId) {
        List<EntityStatus> entityStatusList = entityStatusDao.getList(batchId);

        for (EntityStatus entityStatus : entityStatusList) {
            if (entityStatus.getStatusId() > 0 && entityStatus.getStatus() == null) {
                EavGlobal status = globalService.getGlobal(entityStatus.getStatusId());
                entityStatus.setStatus(EntityStatuses.valueOf(status.getCode()));
            }
        }

        return entityStatusList;
    }

    @Override
    public List<BatchStatus> getBatchStatusList(long batchId) {
        List<BatchStatus> batchStatusList = batchStatusDao.getList(batchId);

        for (BatchStatus batchStatus : batchStatusList) {
            if (batchStatus.getStatusId() > 0 && batchStatus.getStatus() == null) {
                EavGlobal status = globalService.getGlobal(batchStatus.getStatusId());
                batchStatus.setStatus(BatchStatuses.valueOf(status.getCode()));
            }
        }

        return batchStatusList;
    }

    @Override
    public List<Batch> getPendingBatchList() {
        return batchDao.getPendingBatchList();
    }

    @Override
    public List<Batch> getBatchListToSign(long userId) {
        return batchDao.getBatchListToSign(userId);
    }

    @Override
    public void signBatch(long batchId, String sign) {
        Batch batch = batchDao.load(batchId);
        batch.setSign(sign);
        batchDao.save(batch);
    }

    @Override
    public List<Batch> getAll(Date repDate) {
        return batchDao.getAll(repDate);
    }

    @Override
    public Map<String, String> getEntityStatusParams(long entityStatusId) {
        return batchDao.getEntityStatusParams(entityStatusId);
    }

    @Override
    public void addEntityStatusParams(long entityStatusId, Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            batchDao.addEntityStatusParam(entityStatusId, entry.getKey(), entry.getValue());
        }
    }

}
