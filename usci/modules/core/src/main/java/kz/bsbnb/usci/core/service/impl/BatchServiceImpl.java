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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    public Batch load(long batchId) {
        return batchDao.load(batchId);
    }

    @Override
    public long uploadBatch(Batch batch) {
        saveBatchFile(batch);
        return batchDao.save(batch);
    }

    private void saveBatchFile(Batch batch) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        String filePath = batchSaveDir + "/" + df.format(batch.getRepDate())
                + "/" + batch.getCreditorId() + "/" + batch.getHash();

        File outputFile = new File(filePath);

        try {
            FileCopyUtils.copy(batch.getContent(), outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addBatchStatus(long batchId, long statusId, String description) {
        BatchStatus batchStatus = new BatchStatus();
        batchStatus.setBatchId(batchId);
        batchStatus.setStatusId(statusId);
        batchStatus.setDescription(description);
        batchStatus.setReceiptDate(new Date());
        batchStatusDao.insert(batchStatus);
    }

    @Override
    public void addBatchStatus(long batchId, BatchStatuses batchStatus, String description) {
        EavGlobal status = globalService.getGlobal(batchStatus);
        addBatchStatus(batchId, status.getId(), description);
    }

    @Override
    public void addEntityStatus(long batchId, long entityId, long statusId, Long index, String description) {
        EntityStatus entityStatus = new EntityStatus();
        entityStatus.setBatchId(batchId);
        entityStatus.setEntityId(entityId);
        entityStatus.setStatusId(statusId);
        entityStatus.setDescription(description);
        entityStatus.setReceiptDate(new Date());
        entityStatus.setIndex(index);
        entityStatusDao.insert(entityStatus);
    }

    @Override
    public void addEntityStatus(long batchId, long entityId, EntityStatuses entityStatus, Long index, String description) {
        EavGlobal status = globalService.getGlobal(entityStatus);
        addEntityStatus(batchId, entityId, status.getId(), index, description);
    }

    @Override
    public void endBatch(long batchId) {
        EavGlobal statusCompleted = globalService.getGlobal(BatchStatuses.COMPLETED);
        addBatchStatus(batchId, statusCompleted.getId(), null);
    }

}
