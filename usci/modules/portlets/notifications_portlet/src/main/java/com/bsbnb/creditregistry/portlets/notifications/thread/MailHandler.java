package com.bsbnb.creditregistry.portlets.notifications.thread;

/*
import com.bsbnb.creditregistry.dm.maintenance.PortalUser;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessage;
import com.bsbnb.creditregistry.dm.ref.shared.MailMessageStatus;
*/
import static com.bsbnb.creditregistry.portlets.notifications.NotificationsApplication.log;
import com.bsbnb.creditregistry.portlets.notifications.data.BeanDataProvider;
import com.bsbnb.creditregistry.portlets.notifications.data.DataProvider;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class MailHandler implements Runnable {

    private static final int SLEEPING_INTERVAL = 10000;
    private final MailHandlerConfiguration configuration;
    private long threadStartTimeMillis = -1;
    private final DataProvider provider;

    public MailHandler() {
        provider = null; //new BeanDataProvider();
        configuration = new DatabaseMailHandlerConfiguration(provider);
    }

    public void createNewThread() throws ConfigurationException {
        threadStartTimeMillis = System.currentTimeMillis();
        log.log(Level.INFO, "New thread started: {0}", threadStartTimeMillis);
        Thread thread = new Thread(this);
        thread.setName("mail-handler-thread" + threadStartTimeMillis);
        thread.setDaemon(true);
        thread.start();
    }

    private void checkMailMessages() {
        try {
            if (!configuration.isMailHandlingOn()) {
                log.log(Level.INFO, "Mail handling off");
                return;
            }
        } catch (ConfigurationException ce) {
            log.log(Level.INFO, "Configuration exception", ce);
        }

        /*
        try {
            List<MailMessage> mailMessages = provider.getMessagesToSend();
            if (mailMessages.isEmpty()) {
                log.log(Level.INFO, "No mail messages to send");
                return;
            }
            for (MailMessage mailMessage : mailMessages) {
                handleMailMessage(mailMessage);
            }
        } catch (ConfigurationException ce) {
            log.log(Level.SEVERE, "Configuration exception", ce);
        } catch (MessagingException t) {
            log.log(Level.SEVERE, "Unexpected exception. ", t);
        } catch (UnsupportedEncodingException t) {
            log.log(Level.SEVERE, "Unexpected exception. ", t);
        }
        */
    }

    /*
    private void handleMailMessage(MailMessage mailMessage) throws UnsupportedEncodingException, ConfigurationException, MessagingException {
        long recipientUserId = mailMessage.getRecipientUserId().longValue();
        boolean isSending = provider.isTemplateSendingEnabled(mailMessage.getMailTemplate(), recipientUserId);;
        PortalUser recipient = provider.getPortalUserByUserId(recipientUserId);
        if (!recipient.isActive()) {
            isSending = false;
        }

        if (isSending) {
            sendMailMessage(mailMessage, recipient.getEmailAddress());
            mailMessage.setStatus(provider.getMailMessageStatus(MailMessageStatus.SENT));
        } else {
            mailMessage.setStatus(provider.getMailMessageStatus(MailMessageStatus.REJECTED_BY_USER_SETTINGS));
        }
        mailMessage.setSendingDate(new Date());
        provider.updateMailMessage(mailMessage);
    }*/

    /*
    private void sendMailMessage(MailMessage mailMessage, String email) throws ConfigurationException, AddressException, MessagingException, UnsupportedEncodingException {
        String host = configuration.getSmtpHost();
        String sender = configuration.getMailSender();

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(properties);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject(provider.getMessageSubject(mailMessage));
        message.setText(provider.getMessageText(mailMessage), "utf-8", "html");

        Transport.send(message);
    }*/

    @Override
    public void run() {
        try {
            Thread.sleep(SLEEPING_INTERVAL);
            while (true) {
                try {
                    configuration.setLastLaunchMillis(threadStartTimeMillis);
                    break;
                } catch (ConfigurationException ce) {
                    log.log(Level.WARNING, "", ce);
                    Thread.sleep(SLEEPING_INTERVAL);
                }
            }
            while (true) {
                Thread.sleep(SLEEPING_INTERVAL);
                try {
                    Long databaseLaunchTimeMillis = null;
                    try {
                        databaseLaunchTimeMillis = configuration.getLastLaunchMillis();
                    } catch (ConfigurationException ce) {
                        log.log(Level.WARNING, "", ce);
                    }
                    //Mail sending thread can be stopped by setting smtp host config to empty string
                    if (isSmtpHostConfigEmpty()) {
                        break;
                    }
                    if (databaseLaunchTimeMillis != null) {
                        if (databaseLaunchTimeMillis != threadStartTimeMillis) {
                            log.log(Level.WARNING, "Last launch time doesn't match");
                            break;
                        }
                        checkMailMessages();
                    } else {
                        log.log(Level.WARNING, "Last launch time not found");
                        break;
                    }
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Unexpected exception", ex);
                }
            }

        } catch (InterruptedException ie) {
            log.log(Level.WARNING, "Thread sleep fail", ie);
        }
        try {
            configuration.setLastLaunchMillis(-1);
        } catch (ConfigurationException ce) {
            log.log(Level.INFO, "", ce);
        } catch (Exception ex) {
            log.log(Level.INFO, "Unexpected exception", ex);
        }

        log.log(Level.INFO, "Thread finished");
    }

    private boolean isSmtpHostConfigEmpty() {
        try {
            String smtpHost = configuration.getSmtpHost();
            if (smtpHost == null || smtpHost.isEmpty() || smtpHost.trim().isEmpty()) {
                log.log(Level.INFO, "Mail smtp host is empty");
                return true;
            }
        } catch (ConfigurationException ce) {
            return true;
        }
        return false;
    }
}
