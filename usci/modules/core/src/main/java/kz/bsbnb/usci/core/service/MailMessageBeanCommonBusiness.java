package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessageParameter;
import kz.bsbnb.usci.eav.model.mail.UserMailTemplate;

import java.util.List;
import java.util.Properties;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface MailMessageBeanCommonBusiness {

    /**
     * Метод возвращает список сообщений, отправленных пользователю
     */
    public List<MailMessage> getMailMessagesByUser(Long userId);

    /**
     * Метод возвращает список сообщений, ожидающих отправки
     */
    public List<MailMessage> getPendingMessages();

    /**
     * Метод обновляет запись сообщения
     */
    public void updateMailMessage(MailMessage message);

    /**
     * Устанавливает сообщение с заданным шаблоном для отправки
     *
     * @param templateCode - Код шаблона почтового сообщения
     * @param recipientUserId - ID получателя сообщения - пользователя портала
     * @param parametersByCode - Набор параметров сообщения (код параметра -
     * значение), null - если сообщение отправляется без параметров
     */
    public void sendMailMessage(String templateCode, Long recipientUserId, Properties parametersByCode);

    public List<MailMessageParameter> getParametersByMessage(MailMessage message);

    public List<UserMailTemplate> getUserMailTemplates(long userId);

    public void saveUserMailTemplates(List<UserMailTemplate> userTemplates);

    public boolean isTemplateEnabledForUser(Long templateId, long userId);
}
