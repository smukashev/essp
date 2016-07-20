package kz.bsbnb.usci.receiver.listener.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.exceptions.BatchNotFoundException;
import kz.bsbnb.usci.eav.model.mail.MailTemplate;
import kz.bsbnb.usci.receiver.listener.IListener;
import kz.bsbnb.usci.receiver.queue.JobLauncherQueue;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.tool.status.ReceiverStatusSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Properties;

@Component
public class BatchJobListener implements IListener {

    @Autowired
    private ReceiverStatusSingleton receiverStatusSingleton;

    @Autowired
    private IServiceRepository serviceFactory;

    @Autowired
    JobLauncherQueue jobLauncherQueue;

    private static long lastTime;

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        long batchId = jobExecution.getJobInstance().getJobParameters().getLong("batchId");
        System.out.println("Началась обработка батча: " + batchId);
        lastTime = System.currentTimeMillis();
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        long batchId = jobExecution.getJobInstance().getJobParameters().getLong("batchId");

        IBatchService batchService = serviceFactory.getBatchService();
        Batch batch = batchService.getBatch(batchId);

        Properties properties = new Properties();
        properties.put("FILENAME", batch.getFormattedFileName());
        serviceFactory.getMailMessageBeanCommonBusiness().sendMailMessage(MailTemplate.FILE_PROCESSING_COMPLETED,
            batch.getUserId(), properties);

        batchService.endBatch(batchId);
        receiverStatusSingleton.batchEnded();
        jobLauncherQueue.jobFinished();

        double secs = Math.round((System.currentTimeMillis() - lastTime) / 1000);
        double minutes = Math.round(secs / 60);

        System.out.println("Закончен батч : " + batch.getId() + " (" + minutes + " мин) (" + secs + " сек" + ")"
                + " (" + new Date() + ");");
    }
}
