package kz.bsbnb.usci.receiver;

import kz.bsbnb.usci.tool.couchbase.factory.ICouchbaseClientFactory;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusSingleton;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

public class Main {
    public static void main(String args[]) throws IOException, ParserConfigurationException,
            SAXException, ParseException, InterruptedException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContextZip.xml");

        IServiceRepository serviceFactory = ctx.getBean(IServiceRepository.class);

        IBatchService batchService = serviceFactory.getBatchService();
        ICouchbaseClientFactory couchbaseClientFactory = ctx.getBean(ICouchbaseClientFactory.class);
        StatusSingleton statusSingleton = ctx.getBean(StatusSingleton.class);

        ZipFilesMonitor monitor = ctx.getBean(ZipFilesMonitor.class);

        monitor.monitor(Paths.get("/home/ktulbassiyev/Batches"));

    }
}
