package kz.bsbnb.usci.receiver.entry;

import com.couchbase.client.CouchbaseClient;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.receiver.singleton.StatusSingleton;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

/**
 * @author abukabayev
 */
public class SimpleZipMain {
    public static void main(String args[]) throws IOException, ParserConfigurationException, SAXException, ParseException, InterruptedException {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextZip.xml");

        IServiceRepository serviceFactory = ctx.getBean(IServiceRepository.class);
        IBatchService batchService = serviceFactory.getBatchService();

        ICouchbaseClientFactory couchbaseClientFactory = ctx.getBean(ICouchbaseClientFactory.class);
        CouchbaseClient client = couchbaseClientFactory.getCouchbaseClient();

        StatusSingleton statusSingleton = ctx.getBean(StatusSingleton.class);

        ZipFilesMonitor monitor = new ZipFilesMonitor();
        monitor.monitor(Paths.get("/home/abukabayev/workspace/testZip/pathfolder"));

    }
}
