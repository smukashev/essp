package com.bsbnb.creditregistry.portlets.notifications.ui;

/*
import com.bsbnb.creditregistry.dm.maintenance.PortalUser;
import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessage;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailTemplate;
import com.bsbnb.creditregistry.dm.maintenance.mail.UserMailTemplate;
import com.bsbnb.creditregistry.dm.ref.Creditor;
import com.bsbnb.creditregistry.dm.ref.Shared;
import com.bsbnb.creditregistry.dm.ref.SubjectType;
import com.bsbnb.creditregistry.dm.ref.shared.MailMessageStatus;
*/
import com.bsbnb.creditregistry.portlets.notifications.data.DataProvider;
import com.bsbnb.creditregistry.portlets.notifications.data.NotificationDisplayBean;
import com.bsbnb.creditregistry.portlets.notifications.thread.ConfigurationException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TestDataProvider /*implements DataProvider*/ {
    /*
    private static final Map<String, String> configurationMap = new HashMap<String, String>();

    public List<Creditor> getCreditors(long userId, boolean isUserAdmin) {
        Creditor testCreditor = new Creditor();
        testCreditor.setId(BigInteger.ONE);
        testCreditor.setName("Test bank");
        SubjectType subjectType = new SubjectType();
        subjectType.setNameRu("Test subject type");
        testCreditor.setSubjectType(subjectType);
        return Arrays.asList(testCreditor);
    }

    @Override
    public Sysconfig getConfig(String key) throws ConfigurationException {
        Sysconfig config = new Sysconfig();
        config.setValue(configurationMap.get(key));
        return config;
    }

    @Override
    public void saveConfig(Sysconfig config) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<NotificationDisplayBean> getUserMessages(Long userId) {
        MailMessage sampleMessage = new MailMessage();
        sampleMessage.setCreationDate(new Date());
        sampleMessage.setId(BigInteger.ONE);
        sampleMessage.setRecipientUserId(BigInteger.ZERO);
        sampleMessage.setSendingDate(new Date());
        Shared sampleStatus = new Shared();
        sampleStatus.setNameRu("name");
        sampleMessage.setStatus(new Shared());
        MailTemplate sampleTemplate = new MailTemplate();
        sampleTemplate.setCode("code");
        sampleTemplate.setId(BigInteger.ONE);
        sampleTemplate.setNameKz("nameKz");
        sampleTemplate.setNameRu("nameRu");
        sampleTemplate.setSubject("subject");
        sampleTemplate.setText("text");
        return Arrays.asList(new NotificationDisplayBean(sampleMessage));
    }

    public String getMessageText(NotificationDisplayBean displayBean) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<UserMailTemplate> getMailSettings(long userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveUserSettings(List<UserMailTemplate> updatedSettings) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMessageText(MailMessage message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<MailMessage> getMessagesToSend() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateMailMessage(MailMessage message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Shared getMailMessageStatus(MailMessageStatus mailMessageStatus) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PortalUser getPortalUserByUserId(long userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMessageSubject(MailMessage message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isTemplateSendingEnabled(MailTemplate mailTemplate, long recipientUserId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    */
}
