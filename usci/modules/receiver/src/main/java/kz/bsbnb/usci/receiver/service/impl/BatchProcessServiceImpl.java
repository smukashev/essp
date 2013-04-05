package kz.bsbnb.usci.receiver.service.impl;

import com.couchbase.client.CouchbaseClient;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Date;

/**
 * @author k.tulbassiyev
 */
@Service
public class BatchProcessServiceImpl implements IBatchProcessService {
    @Autowired
    private IServiceRepository serviceFactory;

    @Autowired
    private ICouchbaseClientFactory couchbaseClientFactory;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier(value = "batchJob")
    private Job batchJob;

    private IBatchService batchService;
    private CouchbaseClient couchbaseClient;

    @PostConstruct
    public void init() {
        batchService = serviceFactory.getBatchService();
        couchbaseClient = couchbaseClientFactory.getCouchbaseClient();
    }

    @Override
    public long processBatch(String fileName, String creditor, byte[] bytes) {
        Batch batch = new Batch(new Date(new java.util.Date().getTime()));
        long batchId = batchService.save(batch);

        couchbaseClient.set("batch:" + batchId, 0, bytes);

        try {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addParameter("batchId", new JobParameter(batchId));

            jobLauncher.run(batchJob, jobParametersBuilder.toJobParameters());
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        } finally {
            couchbaseClient.shutdown();
        }

        return batchId;
    }
}
