package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessageParameter;
import kz.bsbnb.usci.eav.model.mail.UserMailTemplate;

import java.util.List;

/**
 * Created by Bauyrzhan.Makhambeto on 19/06/2015.
 */
public interface IMailDao {
    public List<UserMailTemplate> getUserMailTemplates(long userId);
    public void saveUserMailTemplates(List<UserMailTemplate> userTemplates);
    public List<MailMessage> getMailMessagesByUser(Long userId);
    public List<MailMessageParameter> getParametersByMessage(MailMessage message);
}
