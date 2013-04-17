package kz.bsbnb.usci.receiver.listener.impl;

import kz.bsbnb.usci.receiver.listener.IListener;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.stereotype.Component;

/**
 * @author k.tulbassiyev
 */
@Component
public class BatchJobListener implements IListener {
    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        System.out.println(" --- AFTER JOB ---");
    }

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        System.out.println(" --- BEFORE JOB ---");
    }
}
