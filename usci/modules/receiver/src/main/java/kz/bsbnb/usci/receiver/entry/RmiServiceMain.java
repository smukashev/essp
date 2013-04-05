package kz.bsbnb.usci.receiver.entry;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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
