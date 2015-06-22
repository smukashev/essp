package com.bsbnb.creditregistry.portlets.notifications.thread;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface MailHandlerConfiguration {

    long getLastLaunchMillis() throws ConfigurationException;

    void setLastLaunchMillis(long millis) throws ConfigurationException;

    boolean isMailHandlingOn() throws ConfigurationException;

    String getSmtpHost() throws ConfigurationException;

    String getMailSender() throws ConfigurationException;
}
