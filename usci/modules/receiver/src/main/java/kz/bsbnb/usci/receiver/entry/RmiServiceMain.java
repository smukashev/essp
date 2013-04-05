package kz.bsbnb.usci.receiver.entry;

import com.couchbase.client.CouchbaseClient;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.helper.impl.FileHelper;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.apache.log4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.io.File;
import java.rmi.RMISecurityManager;

/**
 *
 * @author k.tulbassiyev
 */
public class RmiServiceMain {
    private static Logger logger = Logger.getLogger(SimpleMain.class);

    public static void main(String args[]) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextRmi.xml");

        logger.info("RMI server started");

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
            logger.info("Security manager installed.");
        } else {
            logger.info("Security manager already exists.");
        }
    }
}
