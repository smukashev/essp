package kz.bsbnb.usci.receiver.service.impl;

import com.couchbase.client.CouchbaseClient;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.receiver.service.IBatchReceive;
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author k.tulbassiyev
 */
@WebService
public class BatchServiceWs implements IBatchReceive {

    @Autowired
    IServiceRepository serviceRepository;

    @Autowired
    ICouchbaseClientFactory couchbaseClientFactory;

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    IBatchService batchService;
    CouchbaseClient couchbaseClient;

    @PostConstruct
    public void init() {
        batchService = serviceRepository.getBatchService();

        couchbaseClient = couchbaseClientFactory.getCouchbaseClient();
    }

    @PreDestroy
    public void destroy() {
        couchbaseClient.shutdown();
    }

    @WebMethod
    @Override
    public long process(byte[] bytes) {
        Batch batch = new Batch(new java.sql.Date(new java.util.Date().getTime()));
        long batchId = batchService.save(batch);

        couchbaseClient.set("batch:"+batchId, 0, bytes);

        /*try {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addParameter("fileName", new JobParameter(FILE_PATH));
            jobParametersBuilder.addParameter("batchId", new JobParameter(batchId));

            jobLauncher.run(job, jobParametersBuilder.toJobParameters());
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return batchId;
    }
}
