package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessageParameter;
import kz.bsbnb.usci.eav.model.mail.MailTemplate;
import kz.bsbnb.usci.eav.model.mail.UserMailTemplate;

import java.util.List;
import java.util.Properties;

public interface IMailDao {
    List<UserMailTemplate> getUserMailTemplates(long userId);

    void saveUserMailTemplates(List<UserMailTemplate> userTemplates);

    List<MailMessage> getMailMessagesByUser(Long userId);

    List<MailMessageParameter> getParametersByMessage(MailMessage message);

    MailTemplate getMailTemplateByCode(String templateCode);

    void sendMailMessage(String templateCode, Long recipientUserId, Properties parametersByCode);

    void updateMailMessage(MailMessage message);

    boolean isTemplateEnabledForUser(Long templateId, long userId);

    List<MailMessage> getPendingMessages();

    boolean isMailHandlingOn();
    List<MailTemplate> getUserConfiguredTemplates();
    public void insertUserMailTemplate(UserMailTemplate userMailTemplate);
    Long getLastLaunchTime();
    void setLastLaunchMillis(long millis);
    void insertNewUsers(List<PortalUser> users);
    void notifyNBMaintenance(Batch batch);
}
