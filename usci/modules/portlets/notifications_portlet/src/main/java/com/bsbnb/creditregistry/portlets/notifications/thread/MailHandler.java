package com.bsbnb.creditregistry.portlets.notifications.thread;

/*
import com.bsbnb.creditregistry.dm.maintenance.PortalUser;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessage;
import com.bsbnb.creditregistry.dm.ref.shared.MailMessageStatus;
*/
import com.bsbnb.creditregistry.portlets.notifications.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.notifications.data.DataProvider;
import com.liferay.util.portlet.PortletProps;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessageStatuses;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;


/**
 *
 * @author Aidar.Myrzahanov
 */
public class MailHandler implements Runnable {

    private static final int SLEEPING_INTERVAL = 10000;
    private final MailHandlerConfiguration configuration;
    private long threadStartTimeMillis = -1;
    private final DataProvider provider;
    public final Logger logger = Logger.getLogger(MailHandler.class);

    public MailHandler() {
        provider = new BeanDataProvider();
        configuration = new DatabaseMailHandlerConfiguration(provider);
    }

    public void createNewThread() throws ConfigurationException {
        threadStartTimeMillis = System.currentTimeMillis();
        logger.info("New thread started: "+ threadStartTimeMillis);
        Thread thread = new Thread(this);
        thread.setName("mail-handler-thread" + threadStartTimeMillis);
        thread.setDaemon(true);
        thread.start();
    }

    private void checkMailMessages() {
        try {
            if (!configuration.isMailHandlingOn()) {
                logger.info("Mail handling off");
                return;
            }
        } catch (ConfigurationException ce) {
            logger.info("Configuration exception", ce);
        }


        try {
            List<MailMessage> mailMessages = provider.getMessagesToSend();
            if (mailMessages.isEmpty()) {
                logger.info("No mail messages to send");
                return;
            }
            for (MailMessage mailMessage : mailMessages) {
                handleMailMessage(mailMessage);
            }
        } catch (ConfigurationException ce) {
            logger.error("Configuration exception", ce);
        } catch (MessagingException t) {
            logger.error("Unexpected exception. ", t);
        } catch (UnsupportedEncodingException t) {
            logger.error("Unexpected exception. ", t);
        }
    }


    private void handleMailMessage(MailMessage mailMessage) throws UnsupportedEncodingException, ConfigurationException, MessagingException {
        long recipientUserId = mailMessage.getRecipientUserId().longValue();
        boolean isSending = provider.isTemplateSendingEnabled(mailMessage.getMailTemplate(), recipientUserId);
        PortalUser recipient = provider.getPortalUserByUserId(recipientUserId);

        if(recipient == null) {
            logger.info("Uesr with id " + recipientUserId  +" returned as null, sending not possible");
            mailMessage.setStatusId(MailMessageStatuses.USER_NOT_FOUND);
            mailMessage.setSendingDate(new Date());
            provider.updateMailMessage(mailMessage);
            return;
        }

        if (!recipient.isActive() ) {
            isSending = false;
        }

        if (isSending) {
            sendMailMessage(mailMessage, recipient.getEmailAddress());
            mailMessage.setStatusId(MailMessageStatuses.SENT);
        } else {
            mailMessage.setStatusId(MailMessageStatuses.REJECTED_BY_USER_SETTINGS);
        }
        mailMessage.setSendingDate(new Date());
        provider.updateMailMessage(mailMessage);
    }

    private void sendMailMessage(MailMessage mailMessage, String email) throws ConfigurationException, MessagingException, UnsupportedEncodingException {
        String host = PortletProps.get("smtp.host");
        String sender = PortletProps.get("mail.sender");

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(properties);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject(provider.getMessageSubject(mailMessage));
        message.setText(provider.getMessageText(mailMessage), "utf-8", "html");

        logger.info("sent to: " + host + ", sender: " + sender);
        //Transport.send(message);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(SLEEPING_INTERVAL);
            while (true) {
                try {
                    configuration.setLastLaunchMillis(threadStartTimeMillis);
                    break;
                } catch (ConfigurationException ce) {
                    logger.warn(null, ce);
                    Thread.sleep(SLEEPING_INTERVAL);
                }
            }
            while (true) {
                Thread.sleep(SLEEPING_INTERVAL);
                try {
                    Long lastLaunchTimeMillis = null;
                    try {
                        lastLaunchTimeMillis = configuration.getLastLaunchMillis();
                    } catch (ConfigurationException ce) {
                        logger.error(Errors.decompose(ce.getMessage()));
                    }
                    //Mail sending thread can be stopped by setting smtp host config to empty string
                    if (isSmtpHostConfigEmpty()) {
                        break;
                    }
                    if (lastLaunchTimeMillis != null) {
                        if (lastLaunchTimeMillis != threadStartTimeMillis) {
                            logger.warn("Last launch time doesn't match");
                            break;
                        }
                        checkMailMessages();
                    } else {
                        logger.warn("Last launch time not found");
                        break;
                    }
                } catch (Exception ex) {
                    logger.warn("Unexpected exception", ex);
                }
            }

        } catch (InterruptedException ie) {
            logger.warn("Thread sleep fail", ie);
        }
        try {
            configuration.setLastLaunchMillis(-1);
        } catch (ConfigurationException ce) {
            logger.info(null, ce);
        } catch (Exception ex) {
            logger.info("Unexpected exception", ex);
        }

        logger.info("Thread finished");
    }

    private boolean isSmtpHostConfigEmpty() {
        String smtpHost = null;
        try {
            smtpHost = PortletProps.get("smtp.host");
        } catch (Exception ce) {
            //return true;
        }

        if (smtpHost == null || smtpHost.isEmpty() || smtpHost.trim().isEmpty()) {
            logger.info("Mail smtp host is empty");
            return true;
        }

        return false;
    }
}
