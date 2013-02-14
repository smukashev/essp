package kz.bsbnb.usci.batch;

import kz.bsbnb.usci.batch.parser.IParser;
import kz.bsbnb.usci.batch.parser.IParserFactory;
import kz.bsbnb.usci.batch.parser.impl.MainParser;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import java.io.IOException;

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

        IParserFactory parserFactory = ctx.getBean(IParserFactory.class);

        IParser parser = parserFactory.getIParser("/opt/xmls/simple.xml");

        parser.parse();
    }
}
