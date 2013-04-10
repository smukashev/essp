package kz.bsbnb.usci.receiver.listener;

import org.springframework.batch.core.JobExecution;

/**
 * @author k.tulbassiyev
 */
public interface IListener {
    public void beforeJob(JobExecution jobExecution);
    public void afterJob(JobExecution jobExecution);
}
