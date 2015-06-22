package com.bsbnb.creditregistry.portlets.notifications.thread;

import static com.bsbnb.creditregistry.portlets.notifications.NotificationsApplication.log;
import java.util.logging.Level;
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ApplicationContextListener /*implements ServletContextListener */{
/*
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.log(Level.INFO, "Context initialized");
        try {
            MailHandler handler = new MailHandler();
            try {
                handler.createNewThread();
            } catch (ConfigurationException ce) {
                log.log(Level.WARNING, "", ce);
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unexpected exception", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.log(Level.INFO, "Context destroyed");
    }
    */
}
