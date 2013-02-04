package kz.bsbnb.batch;

import kz.bsbnb.batch.helper.impl.FileHelper;
import kz.bsbnb.batch.parser.impl.MainParser;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import java.io.File;
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

        FileHelper fileHelper = (FileHelper) ctx.getBean("fileHelper");

        MainParser mainParser = new MainParser(fileHelper.getFileBytes(new File("/opt/xmls/1.xml")));
        mainParser.parse();
    }
}
