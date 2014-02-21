package kz.bsbnb.usci.receiver.service.impl;

import com.couchbase.client.CouchbaseClient;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
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
    private ZipFilesMonitor zipFilesMonitor;

    @PostConstruct
    public void init() {
    }

    @Override
    public void processBatch(String fileName, Long userId) {
        zipFilesMonitor.readFiles(fileName, userId);
    }

    @Override
    public void processBatchWithoutUser(String fileName) {
        zipFilesMonitor.readFilesWithoutUser(fileName);
    }
}
