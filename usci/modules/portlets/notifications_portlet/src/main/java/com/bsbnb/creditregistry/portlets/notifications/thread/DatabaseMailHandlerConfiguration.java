package com.bsbnb.creditregistry.portlets.notifications.thread;

//import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
import com.bsbnb.creditregistry.portlets.notifications.data.DataProvider;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class DatabaseMailHandlerConfiguration implements MailHandlerConfiguration {

    private static final String LAST_MAIL_HANDLER_LAUNCH_TIME_CODE = "LAST_MAIL_HANDLER_LAUNCH_TIME";
    private static final String IS_MAIL_HANDLING_ON_CODE = "IS_MAIL_HANDLING_ON";
    private static final String MAIL_HOST_CODE = "MAIL_HOST";
    private static final String MAIL_SENDER_CODE = "MAIL_SENDER";
    private static long lastLaunchMillis;

    private final DataProvider dataProvider;

    public DatabaseMailHandlerConfiguration(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public long getLastLaunchMillis() throws ConfigurationException {
        try {
            //return Long.parseLong(dataProvider.getConfig(LAST_MAIL_HANDLER_LAUNCH_TIME_CODE).getValue());
            return lastLaunchMillis;
        } catch (NumberFormatException nfe) {
            throw new ConfigurationException("Couldn't parse " + LAST_MAIL_HANDLER_LAUNCH_TIME_CODE + " configuration value", nfe);
        }
    }

    @Override
    public void setLastLaunchMillis(long millis) throws ConfigurationException {
        //Sysconfig config = dataProvider.getConfig(LAST_MAIL_HANDLER_LAUNCH_TIME_CODE);
        //config.setValue(millis + "");
        //dataProvider.saveConfig(config);
        synchronized (this) {
            lastLaunchMillis = millis;
        }
    }

    public boolean isMailHandlingOn() throws ConfigurationException {
        return dataProvider.isMailHandlingOn();
    }

    public String getSmtpHost() throws ConfigurationException {
        //return dataProvider.getConfig(MAIL_HOST_CODE).getValue();
        return "";
    }

    public String getMailSender() throws ConfigurationException {
        //return dataProvider.getConfig(MAIL_SENDER_CODE).getValue();
        return "";
    }
}
