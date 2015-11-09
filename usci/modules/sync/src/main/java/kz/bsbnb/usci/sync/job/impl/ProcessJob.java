package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.service.IBatchService;

/**
 * @author k.tulbassiyev
 */
public class ProcessJob extends Thread {
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
