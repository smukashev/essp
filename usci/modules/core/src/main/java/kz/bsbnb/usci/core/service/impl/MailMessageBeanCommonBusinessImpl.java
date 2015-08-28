package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.MailMessageBeanCommonBusiness;
import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessageParameter;
import kz.bsbnb.usci.eav.model.mail.MailTemplate;
import kz.bsbnb.usci.eav.model.mail.UserMailTemplate;
import kz.bsbnb.usci.eav.persistance.dao.IMailDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MailMessageBeanCommonBusinessImpl implements MailMessageBeanCommonBusiness {
    @Autowired
    IMailDao mailDao;

    @Override
    public List<MailMessage> getMailMessagesByUser(Long userId) {
        return mailDao.getMailMessagesByUser(userId);
    }

    @Override
    public List<MailMessage> getPendingMessages() {
        return mailDao.getPendingMessages();
    }

    @Override
    public void updateMailMessage(MailMessage message) {
        mailDao.updateMailMessage(message);
    }

    @Override
    public void sendMailMessage(String templateCode, Long recipientUserId, Properties parametersByCode) {
        mailDao.sendMailMessage(templateCode, recipientUserId, parametersByCode);
    }

    @Override
    public List<MailMessageParameter> getParametersByMessage(MailMessage message) {
        return mailDao.getParametersByMessage(message);
    }

    @Override
    public List<UserMailTemplate> getUserMailTemplates(long userId) {
        createUserMailTemplatesIfNecessary(userId);
        return mailDao.getUserMailTemplates(userId);
    }

    private void createUserMailTemplatesIfNecessary(long userId) {
        List<UserMailTemplate> existingUserTemplates = mailDao.getUserMailTemplates(userId);
        List<MailTemplate> templates = mailDao.getUserConfiguredTemplates();
        Set<String> existingTemplateCodes = extractSetOfCodes(existingUserTemplates);
        for (MailTemplate template : templates) {
            if (!existingTemplateCodes.contains(template.getCode())) {
                UserMailTemplate userTemplate = new UserMailTemplate();
                userTemplate.setMailTemplate(template);
                userTemplate.setPortalUserId(userId);
                userTemplate.setEnabled(true);
                mailDao.insertUserMailTemplate(userTemplate);
            }
        }
    }

    private Set<String> extractSetOfCodes(List<UserMailTemplate> userTemplates) {
        HashSet<String> templateCodes = new HashSet<String>(userTemplates.size());
        for (UserMailTemplate userTemplate : userTemplates) {
            templateCodes.add(userTemplate.getMailTemplate().getCode());
        }
        return templateCodes;
    }

    @Override
    public void saveUserMailTemplates(List<UserMailTemplate> userTemplates) {
        mailDao.saveUserMailTemplates(userTemplates);
    }

    @Override
    public boolean isTemplateEnabledForUser(Long templateId, long userId) {
        createUserMailTemplatesIfNecessary(userId);
        return mailDao.isTemplateEnabledForUser(templateId, userId);
    }

    @Override
    public boolean isMailHandlingOn() {
        return mailDao.isMailHandlingOn();
    }
}
