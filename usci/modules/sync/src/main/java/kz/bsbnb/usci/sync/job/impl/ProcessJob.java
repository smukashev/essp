package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.job.AbstractJob;
import kz.bsbnb.usci.sync.service.IBatchService;

/**
 * @author k.tulbassiyev
 */
public class ProcessJob extends AbstractJob {
    private BaseEntity baseEntity;
    private IEntityService entityService;
    private IBatchService batchService;
    private long timeSpent = 0;

    public ProcessJob(IEntityService entityService, BaseEntity baseEntity, IBatchService batchService) {
        this.entityService = entityService;
        this.baseEntity = baseEntity;
        this.batchService = batchService;
    }

    @Override
    public void run() {
       /* EntityStatus entityStatus = new EntityStatus()
                .setBatchId(baseEntity.getBatchId())
                .setEntityId(baseEntity.getId())
                .setStatus(EntityStatuses.PROCESSING)
                .setReceiptDate(new Date())
                .setIndex(baseEntity.getBatchIndex() - 1);

        Map<String, String> params = StatusProperties.getSpecificParams(baseEntity);

        Long entityStatusId = batchService.addEntityStatus(entityStatus);
        batchService.addEntityStatusParams(entityStatusId, params);*/

        long t1 = System.currentTimeMillis();
        entityService.process(baseEntity);
        timeSpent = System.currentTimeMillis() - t1;
    }

    public BaseEntity getBaseEntity() {
        return baseEntity;
    }

    public long getTimeSpent() {
        return timeSpent;
    }
}
