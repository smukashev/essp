package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

/**
 * @author k.tulbassiyev
 */
public class ProcessJob extends Thread {
    private BaseEntity baseEntity;
    private IEntityService entityService;
    private long timeSpent = 0;

    public ProcessJob(IEntityService entityService, BaseEntity baseEntity) {
        this.entityService = entityService;
        this.baseEntity = baseEntity;
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
