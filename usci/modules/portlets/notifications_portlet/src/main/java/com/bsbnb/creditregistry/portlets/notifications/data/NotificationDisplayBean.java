package com.bsbnb.creditregistry.portlets.notifications.data;

//import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessage;
import kz.bsbnb.usci.eav.model.mail.MailMessageStatus;

import java.util.Date;

/**
 * Класс обертка вокруг сущности MailMessage
 * Используется для представления данных в пользовательском интерфейсе
 * @author Aidar.Myrzahanov
 */
public class NotificationDisplayBean {

    private final MailMessage message;
    
    public NotificationDisplayBean(MailMessage mailMessage) {
        this.message = mailMessage;
    }
    
    public String getTypeName() {
        return message.getMailTemplate().getNameRu();
    }
    
    public String getSubject() {
        return message.getMailTemplate().getSubject();
    }
    
    public Date getCreationDate() {
        return message.getCreationDate();
    }
    
    public Date getSendingDate() {
        return message.getSendingDate();
    }
    
    public String getStatus() {

        switch (message.getStatus()) {
            case BATCH_STOPPED:
                return "Обработка прервана";
            case PROCESSING:
                return "Сообщение ожидает обработки";
            case REJECTED_BY_USER_SETTINGS:
                return "Настройки пользователя не позволили отправить сообщение";
            case SENT:
                return "Сообщение отправлено";
        }

        return "";
    }

    public MailMessage getMessage() {
        return message;
    }
}
