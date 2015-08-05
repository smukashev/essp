package kz.bsbnb.usci.receiver;

import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor;
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

        Global b = ctx.getBean(Global.class);

        ZipFilesMonitor monitor = ctx.getBean(ZipFilesMonitor.class);
        monitor.monitor(Paths.get(b.getBatchesDir()));

    }
}
