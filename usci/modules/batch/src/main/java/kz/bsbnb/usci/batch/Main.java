package kz.bsbnb.usci.batch;

import kz.bsbnb.usci.batch.parser.IParser;
import kz.bsbnb.usci.batch.parser.IParserFactory;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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

    public static void main(String args[]) throws IOException, SAXException
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        IBatchDao batchDao = ctx.getBean(IBatchDao.class);

        Batch batch = new Batch(new Timestamp(new Date().getTime()));

        long batchId = batchDao.save(batch);

        Batch loadedBatch = batchDao.load(batchId);

        IParserFactory parserFactory = ctx.getBean(IParserFactory.class);

        IParser parser = parserFactory.getIParser("/opt/xmls/simple_with_arrays.xml", loadedBatch);

        parser.parse();
    }
}
