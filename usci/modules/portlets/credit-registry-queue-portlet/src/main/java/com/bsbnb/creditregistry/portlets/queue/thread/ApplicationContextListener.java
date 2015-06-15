package com.bsbnb.creditregistry.portlets.queue.thread;

import static com.bsbnb.creditregistry.portlets.queue.QueueApplication.log;
import com.bsbnb.creditregistry.portlets.queue.data.BeanDataProvider;
import java.util.logging.Level;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ApplicationContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        log.log(Level.INFO, "Context initialized");
        try {
            QueueHandler handler = new QueueHandler();
            try {
                handler.createNewThread();
            } catch (ConfigurationException ce) {
                log.log(Level.WARNING, "", ce);
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unexpected exception", ex);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {

        log.log(Level.INFO, "Context destroyed");
        try {
            //запуск потока, который пытается остановить очередь
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            try {
                                DatabaseQueueConfiguration config = new DatabaseQueueConfiguration(new BeanDataProvider());
                                config.setLastLaunchMillis(-1);
                                break;
                            } catch (ConfigurationException ce) {
                                log.log(Level.WARNING, "", ce);
                            }
                            Thread.sleep(10000);
                        }
                    } catch (Exception ex) {
                        log.log(Level.WARNING, "Unexpected exception", ex);
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unexpected exception", ex);
        }
    }
}
