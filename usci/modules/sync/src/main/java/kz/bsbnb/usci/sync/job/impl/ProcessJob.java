package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.sync.job.AbstractJob;

/**
 * @author k.tulbassiyev
 */
public class ProcessJob extends AbstractJob
{
    private BaseEntity baseEntity;

    private IEntityService entityService;

    public ProcessJob(IEntityService entityService, BaseEntity baseEntity)
    {
        this.entityService = entityService;
        this.baseEntity = baseEntity;
    }

    @Override
    public void run()
    {
        entityService.save(baseEntity);
    }

    public BaseEntity getBaseEntity()
    {
        return baseEntity;
    }
}
