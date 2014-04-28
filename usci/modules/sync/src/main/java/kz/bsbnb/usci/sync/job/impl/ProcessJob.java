package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.sync.job.AbstractJob;
import kz.bsbnb.usci.tool.couchbase.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusSingleton;

import java.util.Date;

/**
 * @author k.tulbassiyev
 */
public class ProcessJob extends AbstractJob {
    private BaseEntity baseEntity;
    private IEntityService entityService;
    private long timeSpent = 0;

    protected StatusSingleton statusSingleton;

    public ProcessJob(IEntityService entityService, BaseEntity baseEntity, StatusSingleton statusSingleton) {
        this.entityService = entityService;
        this.baseEntity = baseEntity;
        this.statusSingleton = statusSingleton;
    }

    @Override
    public void run() {
        //TODO: Remove hardcode (credit specific attributes)
        Date contractDate = null;
        String contractNo = null;
        if (baseEntity.getMeta().getClassName().equals("credit"))
        {
            contractDate = (Date)baseEntity.getEl("primary_contract.date");
            contractNo = (String)baseEntity.getEl("primary_contract.no");
        }

        statusSingleton.addContractStatus(baseEntity.getBatchId(), new ContractStatusJModel(
                baseEntity.getBatchIndex() - 1,
                EntityStatuses.PROCESSING, null, new Date(),
                contractNo,
                contractDate));

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
