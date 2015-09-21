package kz.bsbnb.usci.receiver.listener.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.exceptions.BatchNotFoundException;
import kz.bsbnb.usci.receiver.listener.IListener;
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

import java.util.Properties;

@Component
public class BatchJobListener implements IListener {

    @Autowired
    private ReceiverStatusSingleton receiverStatusSingleton;

    @Autowired
    private IServiceRepository serviceFactory;

    private static long lastTime;

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        long batchId = jobExecution.getJobInstance().getJobParameters().getLong("batchId");

        IBatchService batchService = serviceFactory.getBatchService();
        
        Properties properties = new Properties();
        Batch batch = batchService.getBatch(batchId);
        properties.put("FILENAME", batch.getFileName());
//        serviceFactory.getMailMessageBeanCommonBusiness().sendMailMessage("FILE_PROCESSING_COMPLETED", batch.getUserId(), properties);

        batchService.endBatch(batchId);
        receiverStatusSingleton.batchEnded();

        System.out.println(batch.getFileName() + " finished(" +
                ((System.currentTimeMillis() - lastTime) / 1000) + " sec" + ")");
    }

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        System.out.println(" --- BEFORE JOB ---");
        lastTime = System.currentTimeMillis();
    }
}
