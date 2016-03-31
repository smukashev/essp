package com.bsbnb.creditregistry.portlets.notifications.ui;

import com.bsbnb.creditregistry.portlets.notifications.PortalEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.notifications.data.DataProvider;
import com.bsbnb.creditregistry.portlets.notifications.thread.ConfigurationException;
import com.bsbnb.creditregistry.portlets.notifications.thread.DatabaseMailHandlerConfiguration;
import com.bsbnb.creditregistry.portlets.notifications.thread.MailHandler;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;
import org.apache.log4j.Logger;

import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class AdministratorComponent extends VerticalLayout {

    private final PortalEnvironmentFacade environment;
    private final DataProvider dataProvider;
    public final Logger logger = Logger.getLogger(AdministratorComponent.class);

    public AdministratorComponent(PortalEnvironmentFacade environment, DataProvider dataProvider) {
        this.environment = environment;
        this.dataProvider = dataProvider;
    }

    @Override
    public void attach() {
        setImmediate(true);
    }

    public void initializeUI() {
        removeAllComponents();
        logger.info("Loading admin interface");
        try {
            DatabaseMailHandlerConfiguration config = new DatabaseMailHandlerConfiguration(dataProvider);
            boolean isRunning = config.getLastLaunchMillis() != -1;
            Button manageButton = new Button(environment.getResourceString(isRunning ? Localization.STOP_MAIL_HANDLING : Localization.START_MAIL_HANDLING), new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    try {
                        DatabaseMailHandlerConfiguration config = new DatabaseMailHandlerConfiguration(dataProvider);
                        boolean isRunning = config.getLastLaunchMillis() != -1;
                        if (isRunning) {
                            config.setLastLaunchMillis(-1);
                        } else {
                            MailHandler handler = new MailHandler();
                            handler.createNewThread();
                        }
                        isRunning = !isRunning;
                        event.getButton().setCaption(environment.getResourceString(isRunning ? Localization.STOP_MAIL_HANDLING : Localization.START_MAIL_HANDLING));
                        MessageBox.Show(environment.getResourceString(isRunning ? Localization.MAIL_HANDLING_STARTED : Localization.MAIL_HANDLING_STOPPED), getApplication().getMainWindow());
                    } catch (ConfigurationException ce) {
                        logger.warn(null, ce);
                    }
                }
            });
            manageButton.setImmediate(true);
            addComponent(manageButton);
            setComponentAlignment(manageButton, Alignment.MIDDLE_CENTER);

            setSpacing(true);
        } catch (ConfigurationException ce) {
            logger.warn(null, ce);
        }
    }
}
