package kz.bsbnb.usci.receiver.listener.impl;

import kz.bsbnb.usci.eav.model.exceptions.BatchNotFoundException;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.listener.IListener;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusSingleton;
import kz.bsbnb.usci.tool.status.ReceiverStatus;
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

/**
 * @author k.tulbassiyev
 */
@Component
public class BatchJobListener implements IListener {

    @Autowired
    protected StatusSingleton statusSingleton;

    @Autowired
    private ReceiverStatusSingleton receiverStatusSingleton;

    @Autowired
    private IServiceRepository serviceFactory;

    private final Logger logger = LoggerFactory.getLogger(BatchJobListener.class);

    @AfterJob
    public void afterJob(JobExecution jobExecution) {

            long batchId = jobExecution.getJobInstance().getJobParameters().getLong("batchId");
            long userId = jobExecution.getJobInstance().getJobParameters().getLong("userId");
            System.out.println(" --- AFTER JOB --- batch: " + batchId + ", userId: " + userId);

        try {
            Properties properties = new Properties();
            BatchFullJModel batch = statusSingleton.getBatch(batchId);
            properties.put("FILENAME", batch.getFileName());
//            serviceFactory.getMailMessageBeanCommonBusiness().sendMailMessage("FILE_PROCESSING_COMPLETED", batch.getUserId(), properties);
        } catch (BatchNotFoundException e) {
            logger.error("batch with id: " + batchId + " not found");
        }

            statusSingleton.endBatch(batchId, userId);
            receiverStatusSingleton.batchEnded();
    }

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        System.out.println(" --- BEFORE JOB ---");
    }
}
