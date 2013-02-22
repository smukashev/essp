package kz.bsbnb.usci.batch;

import kz.bsbnb.usci.batch.parser.IParser;
import kz.bsbnb.usci.batch.parser.factory.IParserFactory;
import kz.bsbnb.usci.batch.parser.listener.impl.RmiListener;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.storage.IStorage;
import kz.bsbnb.usci.sync.service.IDataService;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author k.tulbassiyev
 */
public class Main
{
    static Logger logger = Logger.getLogger(Main.class);

    private final static String FILE_PATH = "/opt/xmls/test.xml";

    public static void main(String args[]) throws IOException, SAXException
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        IStorage storage = ctx.getBean(IStorage.class);

        if(!storage.testConnection())
        {
            logger.error("Can't connect to storage.");
            System.exit(1);
        }

        IBatchDao batchDao = ctx.getBean(IBatchDao.class);

        Batch batch = new Batch(new Timestamp(new Date().getTime()));

        long batchId = batchDao.save(batch);

        Batch loadedBatch = batchDao.load(batchId);

        RmiProxyFactoryBean rmiProxyFactoryBean =
                ctx.getBean(org.springframework.remoting.rmi.RmiProxyFactoryBean.class);

        IDataService service = (IDataService)rmiProxyFactoryBean.getObject();

        RmiListener listener = new RmiListener(service);

        // JobListener listener = new JobListener(ctx.getBean(JobLauncher.class), ctx.getBean(Job.class));

        IParserFactory parserFactory = ctx.getBean(IParserFactory.class);

        IParser parser = parserFactory.getIParser(FILE_PATH, loadedBatch, listener);

        long startTime = System.currentTimeMillis();
        parser.parse();

        logger.info("TOTAL TIME : " + (System.currentTimeMillis() - startTime));
    }
}
