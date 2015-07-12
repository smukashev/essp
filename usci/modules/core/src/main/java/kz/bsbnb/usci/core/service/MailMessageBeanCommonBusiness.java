package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessageParameter;
import kz.bsbnb.usci.eav.model.mail.UserMailTemplate;

import java.util.List;
import java.util.Properties;

public interface MailMessageBeanCommonBusiness {
    List<MailMessage> getMailMessagesByUser(Long userId);

    List<MailMessage> getPendingMessages();

    void updateMailMessage(MailMessage message);

    void sendMailMessage(String templateCode, Long recipientUserId, Properties parametersByCode);

    List<MailMessageParameter> getParametersByMessage(MailMessage message);

    List<UserMailTemplate> getUserMailTemplates(long userId);

    void saveUserMailTemplates(List<UserMailTemplate> userTemplates);

    boolean isTemplateEnabledForUser(Long templateId, long userId);

    boolean isMailHandlingOn();
}
