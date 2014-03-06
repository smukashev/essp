package kz.bsbnb.usci.receiver.listener.impl;

import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.listener.IListener;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusSingleton;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author k.tulbassiyev
 */
@Component
public class BatchJobListener implements IListener {

    @Autowired
    protected StatusSingleton statusSingleton;

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        long batchId = jobExecution.getJobInstance().getJobParameters().getLong("batchId");
        long userId = jobExecution.getJobInstance().getJobParameters().getLong("batchId");
        System.out.println(" --- AFTER JOB --- batch: " + batchId + ", userId: " + userId);

        statusSingleton.endBatch(batchId, userId);
    }

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        System.out.println(" --- BEFORE JOB ---");
    }
}
