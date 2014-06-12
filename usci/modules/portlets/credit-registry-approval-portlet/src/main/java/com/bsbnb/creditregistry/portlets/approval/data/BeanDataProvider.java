package com.bsbnb.creditregistry.portlets.approval.data;

//import com.bsbnb.creditregistry.dm.Report;
import com.bsbnb.creditregistry.dm.ReportMessage;
import com.bsbnb.creditregistry.dm.ReportMessageAttachment;
import com.bsbnb.creditregistry.dm.maintenance.CrossCheck;
import com.bsbnb.creditregistry.dm.maintenance.PortalUser;
import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessage;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessageParameter;
import com.bsbnb.creditregistry.dm.maintenance.mail.MailTemplateParameter;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.dm.ref.Shared;
//import com.bsbnb.creditregistry.dm.ref.shared.ReportType;
import com.bsbnb.creditregistry.dm.ref.shared.SharedType;

//import com.bsbnb.creditregistry.ejb.api.ReportBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.api.ReportMessageBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.api.maintenance.CrossCheckBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.api.maintenance.SysconfigBeanRemoteBusiness;
import com.bsbnb.creditregistry.ejb.api.maintenance.mail.MailMessageBeanRemoteBusiness;

import com.bsbnb.creditregistry.ejb.ref.business.remote.IRemoteSharedBusiness;
import com.bsbnb.creditregistry.ejb.ref.exception.ResultInconsistentException;
import com.bsbnb.creditregistry.ejb.ref.exception.ResultNotFoundException;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportType;
import kz.bsbnb.usci.cr.model.Shared;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import static com.bsbnb.creditregistry.portlets.approval.ApprovalApplication.log;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {
    private RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean reportBusinessFactoryBean;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private ReportBeanRemoteBusiness reportBusiness;

    private ReportMessageBeanRemoteBusiness reportMessageBusiness;
    private SysconfigBeanRemoteBusiness sysconfigBusiness;
    private CrossCheckBeanRemoteBusiness crossCheckBusiness;
    private IRemoteSharedBusiness sharedBusiness;
    private MailMessageBeanRemoteBusiness mailMessageBusiness;

    public BeanDataProvider() {
        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();


        reportBusinessFactoryBean = new RmiProxyFactoryBean();
        reportBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/reportBeanRemoteBusiness");
        reportBusinessFactoryBean.setServiceInterface(ReportBeanRemoteBusiness.class);

        reportBusinessFactoryBean.afterPropertiesSet();
        reportBusiness = (ReportBeanRemoteBusiness) reportBusinessFactoryBean.getObject();
    }

    @Override
    public List<Creditor> getCreditorsList(long userId) {
        return portalUserBusiness.getMainCreditorsInAlphabeticalOrder(userId);
    }

    @Override
    public List<ReportDisplayBean> getReportsForDate(List<Creditor> accessibleCreditors, Date reportDate) {
        List<Report> reports = reportBusiness.getReportsByReportDate(reportDate);
        List<ReportDisplayBean> displayBeans = new ArrayList<ReportDisplayBean>(reports.size());
        int rownum = 1;
        Set<Long> accessibleCreditorIds = new HashSet<Long>(accessibleCreditors.size());
        for (Creditor accessibleCreditor : accessibleCreditors) {
            accessibleCreditorIds.add(accessibleCreditor.getId());
        }
        for (Report report : reports) {
            if (accessibleCreditorIds.contains(report.getCreditor().getId())) {
                ReportDisplayBean displayBean = new ReportDisplayBean(report);
                displayBean.setRownum(rownum);
                displayBeans.add(displayBean);
                rownum++;
            }
        }
        return displayBeans;
    }

    @Override
    public Date getCurrentReportDate(Creditor creditor) {
        return reportBusiness.getReportDate(creditor.getId());
    }

    @Override
    public List<ReportMessage> getReportMessages(Report report) {
        return reportMessageBusiness.getMessagesByReport(report);
    }

    @Override
    public void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments) {
        reportMessageBusiness.addNewMessage(report, message, attachments);
    }

    @Override
    public Date getInitialReportDate() {
        try {
            Sysconfig initialReportDateConfig = sysconfigBusiness.getSysconfigByKey("INITIAL_REPORT_DATE");
            String initialReportDateString = initialReportDateConfig.getValue();
            SimpleDateFormat initialReportDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return initialReportDateFormat.parse(initialReportDateString);
        } catch (ParseException pe) {
            log.log(Level.SEVERE, "Initial report date is incorrectly formatted", pe);
        }
        return null;
    }

    @Override
    public Report getReport(Creditor creditor, Date reportDate) {
        return reportBusiness.getByCreditor_ReportDate(creditor, reportDate);
    }

    @Override
    public CrossCheck getLastCrossCheck(Creditor creditor, Date reportDate) {
        List<CrossCheck> crossChecks = crossCheckBusiness.loadCrossCheck(Arrays.asList(creditor.getId()), reportDate);
        return crossChecks.isEmpty() ? null : crossChecks.get(0);
    }

    @Override
    public void updateReportStatus(Report report, ReportType status) {
        Shared sharedStatus = sharedBusiness.findByC_T(status.getCode(), SharedType.REPORT_TYPE.getType());
        report.setStatus(sharedStatus);
        reportBusiness.updateReport(report);
    }

    @Override
    public List<ReportMessageAttachment> getReportAttachments(Report report) {
        return reportMessageBusiness.getAttachmentsByReport(report);
    }

    @Override
    public void sendApprovalNotifications(Creditor creditor, Report report, String username, Date sendDate, String text) {
        List<PortalUser> notificationRecipients = portalUserBusiness.getPortalUsersHavingAccessToCreditor(creditor);
        Properties mailMessageParameters = new Properties();
        mailMessageParameters.setProperty("CREDITOR", creditor.getName());
        mailMessageParameters.setProperty("STATUS", report.getStatus().getNameRu());
        mailMessageParameters.setProperty("USERNAME", username);
        mailMessageParameters.setProperty("REPORT_DATE", DATE_FORMAT.format(report.getReportDate()));
        mailMessageParameters.setProperty("UPDATE_TIME", TIME_FORMAT.format(sendDate));
        mailMessageParameters.setProperty("TEXT", text);
        for (PortalUser portalUser : notificationRecipients) {
            mailMessageBusiness.sendMailMessage("APPROVAL_UPDATE", portalUser.getUserId(), mailMessageParameters);
        }
    }

    private MailMessageParameter getMailMessageParameter(MailMessage mailMessage, MailTemplateParameter templateParameter, String value) {
        MailMessageParameter mailMessageParameter = new MailMessageParameter();
        mailMessageParameter.setMailMessage(mailMessage);
        mailMessageParameter.setMailTemplateParameter(templateParameter);
        mailMessageParameter.setValue(value);
        return mailMessageParameter;
    }

    @Override
    public void updateLastManualEditDate(Report report) {
        report.setLastManualEditDate(new Date());
        reportBusiness.updateReport(report);
    }
}
