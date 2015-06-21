package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.MailMessageBeanCommonBusiness;
import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessageParameter;
import kz.bsbnb.usci.eav.model.mail.UserMailTemplate;
import kz.bsbnb.usci.eav.persistance.dao.IMailDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

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
        return null;
    }

    @Override
    public void updateMailMessage(MailMessage message) {

    }

    @Override
    public void sendMailMessage(String templateCode, Long recipientUserId, Properties parametersByCode) {

    }

    @Override
    public List<MailMessageParameter> getParametersByMessage(MailMessage message) {
        return mailDao.getParametersByMessage(message);
    }

    @Override
    public List<UserMailTemplate> getUserMailTemplates(long userId) {
        return mailDao.getUserMailTemplates(userId);
    }

    @Override
    public void saveUserMailTemplates(List<UserMailTemplate> userTemplates) {
        mailDao.saveUserMailTemplates(userTemplates);
    }

    @Override
    public boolean isTemplateEnabledForUser(String templateCode, long userId) {
        return false;
    }
}
