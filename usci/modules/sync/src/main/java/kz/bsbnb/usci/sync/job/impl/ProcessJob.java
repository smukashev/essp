package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.job.AbstractJob;

/**
 * @author k.tulbassiyev
 */
public class ProcessJob extends AbstractJob {
    private BaseEntity baseEntity;
    private IEntityService entityService;

    public ProcessJob(IEntityService entityService, BaseEntity baseEntity) {
        this.entityService = entityService;
        this.baseEntity = baseEntity;
    }

    @Override
    public void run() {
        long t1 = System.currentTimeMillis();
        entityService.save(baseEntity);
        long t2 = System.currentTimeMillis() - t1;

        System.out.println("[sync][save]                :               " + t2);
    }

    public BaseEntity getBaseEntity() {
        return baseEntity;
    }
}
