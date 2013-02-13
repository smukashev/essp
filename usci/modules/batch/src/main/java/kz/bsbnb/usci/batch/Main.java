package kz.bsbnb.usci.batch;

import kz.bsbnb.usci.batch.helper.impl.FileHelper;
import kz.bsbnb.usci.batch.parser.impl.MainParser;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
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

        FileHelper fileHelper = (FileHelper) ctx.getBean("fileHelper");

        MainParser mainParser = new MainParser(
                fileHelper.getFileBytes(new File("/opt/xmls/simple.xml")));

        mainParser.parse();
    }
}
