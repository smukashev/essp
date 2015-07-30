package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessageParameter;
import kz.bsbnb.usci.eav.model.mail.MailTemplate;
import kz.bsbnb.usci.eav.model.mail.UserMailTemplate;

import java.util.List;
import java.util.Properties;

/**
 * Created by Bauyrzhan.Makhambeto on 19/06/2015.
 */
public interface IMailDao {
    public List<UserMailTemplate> getUserMailTemplates(long userId);
    public void saveUserMailTemplates(List<UserMailTemplate> userTemplates);
    public List<MailMessage> getMailMessagesByUser(Long userId);
    public List<MailMessageParameter> getParametersByMessage(MailMessage message);
    public MailTemplate getMailTemplateByCode(String templateCode);
    public void sendMailMessage(String templateCode, Long recipientUserId, Properties parametersByCode);
    public void updateMailMessage(MailMessage message);
    boolean isTemplateEnabledForUser(Long templateId, long userId);
    List<MailMessage> getPendingMessages();
    boolean isMailHandlingOn();
    List<MailTemplate> getUserConfiguredTemplates();
    public void insertUserMailTemplate(UserMailTemplate userMailTemplate);
}
