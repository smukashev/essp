package com.bsbnb.creditregistry.portlets.notifications.data;

/*
import com.bsbnb.creditregistry.dm.maintenance.PortalUser;
import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessage;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailTemplate;
import com.bsbnb.creditregistry.dm.maintenance.mail.UserMailTemplate;
import com.bsbnb.creditregistry.dm.ref.Shared;
import com.bsbnb.creditregistry.dm.ref.shared.MailMessageStatus;
*/
import com.bsbnb.creditregistry.portlets.notifications.thread.ConfigurationException;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailTemplate;
import kz.bsbnb.usci.eav.model.mail.UserMailTemplate;

import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {

    //public Sysconfig getConfig(String key) throws ConfigurationException;

    //public void saveConfig(Sysconfig config) throws ConfigurationException;

    public List<NotificationDisplayBean> getUserMessages(Long userId);

    public String getMessageText(MailMessage message);


    public String getMessageSubject(MailMessage message);

    public List<UserMailTemplate> getMailSettings(long userId);

    public void saveUserSettings(List<UserMailTemplate> updatedSettings);

    public List<MailMessage> getMessagesToSend();

    public void updateMailMessage(MailMessage message);

    //public Shared getMailMessageStatus(MailMessageStatus mailMessageStatus);

    public PortalUser getPortalUserByUserId(long userId);

    public boolean isTemplateSendingEnabled(MailTemplate mailTemplate, long recipientUserId);
}
