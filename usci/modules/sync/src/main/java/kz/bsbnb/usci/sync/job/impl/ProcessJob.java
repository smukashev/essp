package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

/**
 * @author k.tulbassiyev
 */
public class ProcessJob extends Thread {
    private final BaseEntity baseEntity;
    private final IEntityService entityService;
    public boolean statusCode;

    private long timeSpent = 0;

    ProcessJob(IEntityService entityService, BaseEntity baseEntity) {
        this.entityService = entityService;
        this.baseEntity = baseEntity;
    }

    @Override
    public void run() {
        long t1 = System.currentTimeMillis();
        this.statusCode = entityService.process(baseEntity);
        timeSpent = System.currentTimeMillis() - t1;
    }

    BaseEntity getBaseEntity() {
        return baseEntity;
    }

    long getTimeSpent() {
        return timeSpent;
    }
}
