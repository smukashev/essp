package com.bsbnb.creditregistry.portlets.notifications.data;

/*
import com.bsbnb.creditregistry.dm.maintenance.PortalUser;
import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessage;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessageParameter;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailTemplate;
import com.bsbnb.creditregistry.dm.maintenance.mail.UserMailTemplate;
import com.bsbnb.creditregistry.dm.ref.Shared;
import com.bsbnb.creditregistry.dm.ref.shared.MailMessageStatus;
import com.bsbnb.creditregistry.dm.ref.shared.SharedType;
import com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.api.maintenance.SysconfigBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.api.maintenance.mail.MailMessageBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.ref.business.remote.IRemoteSharedBusiness;
import com.bsbnb.creditregistry.ejb.ref.exception.ResultInconsistentException;
import com.bsbnb.creditregistry.ejb.ref.exception.ResultNotFoundException;
*/
import static com.bsbnb.creditregistry.portlets.notifications.NotificationsApplication.log;
import com.bsbnb.creditregistry.portlets.notifications.thread.ConfigurationException;
import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.model.mail.*;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.*;
import java.util.logging.Level;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {



    private RmiProxyFactoryBean protocolBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean inputInfoBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean inputFileBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean remoteCreditorBusinessFactoryBean;
    private RmiProxyFactoryBean mailBusinessFactoryBean;

    private RemoteCreditorBusiness creditorBusiness;
    private ProtocolBeanRemoteBusiness protocolBusiness;
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputFileBeanRemoteBusiness inputFileBusiness;
    private MailMessageBeanCommonBusiness mailMessageBusiness;



    //private PortalUserBeanRemoteBusiness portalUserBusiness;
    /*
    private SysconfigBeanRemoteBusiness sysconfigBeanRemoteBusiness;
    private MailMessageBeanRemoteBusiness mailMessageBusiness;
    private IRemoteSharedBusiness sharedBusiness;
    */

    public BeanDataProvider() {
        /*
        Properties props = new Properties();
        props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
        props.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
        props.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        props.setProperty("org.omg.CORBA.ORBInitialPort", "3800");
        try {
            InitialContext context = new InitialContext(props);
            portalUserBusiness = (PortalUserBeanRemoteBusiness) context.lookup(PortalUserBeanRemoteBusiness.class.getName());
            sysconfigBeanRemoteBusiness = (SysconfigBeanRemoteBusiness) context.lookup(SysconfigBeanRemoteBusiness.class.getName());
            mailMessageBusiness = (MailMessageBeanRemoteBusiness) context.lookup(MailMessageBeanRemoteBusiness.class.getName());
            sharedBusiness = (IRemoteSharedBusiness) context.lookup(IRemoteSharedBusiness.class.getName());
        } catch (NamingException ne) {
            log.log(Level.SEVERE, "Exception occured while initializing beans", ne);
        }*/
        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);
        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        mailBusinessFactoryBean = new RmiProxyFactoryBean();
        mailBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/mailRemoteBusiness");
        mailBusinessFactoryBean.setServiceInterface(MailMessageBeanCommonBusiness.class);
        mailBusinessFactoryBean.afterPropertiesSet();
        mailMessageBusiness = (MailMessageBeanCommonBusiness) mailBusinessFactoryBean.getObject();


        /*
        remoteCreditorBusinessFactoryBean = new RmiProxyFactoryBean();
        remoteCreditorBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/remoteCreditorBusiness");
        remoteCreditorBusinessFactoryBean.setServiceInterface(RemoteCreditorBusiness.class);

        remoteCreditorBusinessFactoryBean.afterPropertiesSet();
        creditorBusiness = (RemoteCreditorBusiness) remoteCreditorBusinessFactoryBean.getObject();

        protocolBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        protocolBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/protocolBeanRemoteBusiness");
        protocolBeanRemoteBusinessFactoryBean.setServiceInterface(ProtocolBeanRemoteBusiness.class);

        protocolBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        protocolBusiness = (ProtocolBeanRemoteBusiness) protocolBeanRemoteBusinessFactoryBean.getObject();

        inputInfoBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputInfoBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/inputInfoBeanRemoteBusiness");
        inputInfoBeanRemoteBusinessFactoryBean.setServiceInterface(InputInfoBeanRemoteBusiness.class);

        inputInfoBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputInfoBusiness = (InputInfoBeanRemoteBusiness) inputInfoBeanRemoteBusinessFactoryBean.getObject();

        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        inputFileBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputFileBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/inputFileBeanRemoteBusiness");
        inputFileBeanRemoteBusinessFactoryBean.setServiceInterface(InputFileBeanRemoteBusiness.class);

        inputFileBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputFileBusiness = (InputFileBeanRemoteBusiness) inputFileBeanRemoteBusinessFactoryBean.getObject();
        */
    }

    @Override
    public List<NotificationDisplayBean> getUserMessages(Long userId) {
        List<MailMessage> mailMessages = mailMessageBusiness.getMailMessagesByUser(userId);
        ArrayList<NotificationDisplayBean> displayBeans = new ArrayList<NotificationDisplayBean>(mailMessages.size());
        for (MailMessage mailMessage : mailMessages) {
            displayBeans.add(new NotificationDisplayBean(mailMessage));
        }
        return displayBeans;
    }

    @Override
    public String getMessageText(MailMessage message) {
        String messageText = message.getMailTemplate().getText();
        return substitureParametersWithValues(message, messageText);
    }

    @Override
    public String getMessageSubject(MailMessage message) {
        String messageSubject = message.getMailTemplate().getSubject();
        return substitureParametersWithValues(message, messageSubject);
    }

    @Override
    public List<UserMailTemplate> getMailSettings(long userId) {
        return mailMessageBusiness.getUserMailTemplates(userId);
    }

    @Override
    public void saveUserSettings(List<UserMailTemplate> updatedSettings) {
        mailMessageBusiness.saveUserMailTemplates(updatedSettings);
    }

    @Override
    public List<MailMessage> getMessagesToSend() {
        return mailMessageBusiness.getPendingMessages();
    }

    @Override
    public void updateMailMessage(MailMessage message) {
        mailMessageBusiness.updateMailMessage(message);
    }

    /*
    public Shared getMailMessageStatus(MailMessageStatus mailMessageStatus) {
        try {
            return sharedBusiness.findByC_T(mailMessageStatus.getCode(), SharedType.MAIL_MESSAGE_STATUS.getType());
        } catch (ResultInconsistentException rie) {
            log.log(Level.SEVERE, null, rie);
        } catch (ResultNotFoundException rnfe) {
            log.log(Level.SEVERE, null, rnfe);
        }
        return null;
    }*/

    public PortalUser getPortalUserByUserId(long userId) {
        return portalUserBusiness.getUser(userId);
    }

    private String substitureParametersWithValues(MailMessage message, String messageText) {
        List<MailMessageParameter> parameters = mailMessageBusiness.getParametersByMessage(message);
        for (MailMessageParameter parameter : parameters) {
            String code = parameter.getMailTemplateParameter().getCode();
            String value = parameter.getValue();
            if (value == null) {
                value = "";
            }
            if (code != null) {
                messageText = messageText.replace("%" + code + "%", value);
            }
        }
        return messageText;
    }

    @Override
    public boolean isTemplateSendingEnabled(MailTemplate template, long recipientUserId) {
        if (template.getConfigurationTypeId() == MailConfigurationTypes.OBLIGATORY) {
            //Данный шаблон не настраивается пользователем, а высылается в обязательном порядке
            return true;
        }
        //если шаблон настраивается пользователем, проверяются настройки
        return mailMessageBusiness.isTemplateEnabledForUser(template.getId(), recipientUserId);
    }

    @Override
    public boolean isMailHandlingOn() {
        return mailMessageBusiness.isMailHandlingOn();
    }
}
