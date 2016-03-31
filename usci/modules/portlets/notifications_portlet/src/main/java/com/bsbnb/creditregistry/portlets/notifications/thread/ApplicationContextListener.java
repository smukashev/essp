package com.bsbnb.creditregistry.portlets.notifications.thread;

import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ApplicationContextListener implements ServletContextListener {

    public final Logger logger = Logger.getLogger(ApplicationContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Context initialized");
        try {
            MailHandler handler = new MailHandler();
            try {
                handler.createNewThread();
            } catch (ConfigurationException ce) {
                logger.warn(null , ce);
            }
        } catch (Exception ex) {
            logger.warn("Unexpected exception", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Context destroyed");
    }
}
