package kz.bsbnb.usci.sync.job.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.sync.job.AbstractJob;
import kz.bsbnb.usci.tool.couchbase.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusSingleton;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author k.tulbassiyev
 */
public class ProcessJob extends AbstractJob {
    private BaseEntity baseEntity;
    private IEntityService entityService;
    private long timeSpent = 0;

    @Autowired
    protected StatusSingleton statusSingleton;

    public ProcessJob(IEntityService entityService, BaseEntity baseEntity) {
        this.entityService = entityService;
        this.baseEntity = baseEntity;
    }

    @Override
    public void run() {
        Date contractDate = (Date)baseEntity.getEl("primary_contract.date");
        String contractNo = (String)baseEntity.getEl("primary_contract.no");

        statusSingleton.addContractStatus(baseEntity.getBatchId(), new ContractStatusJModel(
                baseEntity.getBatchIndex() - 1,
                EntityStatuses.PROCESSING, null, new Date(),
                contractNo,
                contractDate));

        long t1 = System.currentTimeMillis();
        entityService.save(baseEntity);
        timeSpent = System.currentTimeMillis() - t1;
    }

    public BaseEntity getBaseEntity() {
        return baseEntity;
    }

    public long getTimeSpent() {
        return timeSpent;
    }
}
