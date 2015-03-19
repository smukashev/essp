package com.bsbnb.creditregistry.portlets.approval.data;

//import com.bsbnb.creditregistry.dm.Report;
//import com.bsbnb.creditregistry.dm.ReportMessage;
//import com.bsbnb.creditregistry.dm.ReportMessageAttachment;
//import com.bsbnb.creditregistry.dm.maintenance.CrossCheck;
//import com.bsbnb.creditregistry.dm.maintenance.PortalUser;
//import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
//import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessage;
//import com.bsbnb.creditregistry.dm.maintenance.mail.MailMessageParameter;
//import com.bsbnb.creditregistry.dm.maintenance.mail.MailTemplateParameter;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.dm.ref.Shared;
//import com.bsbnb.creditregistry.dm.ref.shared.ReportType;
//import com.bsbnb.creditregistry.dm.ref.shared.SharedType;

//import com.bsbnb.creditregistry.ejb.api.ReportBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.ReportMessageBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.CrossCheckBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.SysconfigBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.mail.MailMessageBeanRemoteBusiness;
//
//import com.bsbnb.creditregistry.ejb.ref.business.remote.IRemoteSharedBusiness;
//import com.bsbnb.creditregistry.ejb.ref.exception.ResultInconsistentException;
//import com.bsbnb.creditregistry.ejb.ref.exception.ResultNotFoundException;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.ReportMessageBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.*;
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
    private RmiProxyFactoryBean reportMessageBusinessFactoryBean;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private ReportBeanRemoteBusiness reportBusiness;
    private ReportMessageBeanRemoteBusiness reportMessageBusiness;

//    private SysconfigBeanRemoteBusiness sysconfigBusiness;
//    private CrossCheckBeanRemoteBusiness crossCheckBusiness;
//    private IRemoteSharedBusiness sharedBusiness;
//    private MailMessageBeanRemoteBusiness mailMessageBusiness;

    public BeanDataProvider() {
        // portalUserBeanRemoteBusiness
        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);
        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        // reportBeanRemoteBusiness
        reportBusinessFactoryBean = new RmiProxyFactoryBean();
        reportBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/reportBeanRemoteBusiness");
        reportBusinessFactoryBean.setServiceInterface(ReportBeanRemoteBusiness.class);
        reportBusinessFactoryBean.afterPropertiesSet();
        reportBusiness = (ReportBeanRemoteBusiness) reportBusinessFactoryBean.getObject();

        // reportMessageBeanRemoteBusiness
        reportMessageBusinessFactoryBean = new RmiProxyFactoryBean();
        reportMessageBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/reportMessageBeanRemoteBusiness");
        reportMessageBusinessFactoryBean.setServiceInterface(ReportMessageBeanRemoteBusiness.class);
        reportMessageBusinessFactoryBean.afterPropertiesSet();
        reportMessageBusiness = (ReportMessageBeanRemoteBusiness) reportMessageBusinessFactoryBean.getObject();
    }

    @Override
    public List<Creditor> getCreditorsList(long userId) {
        return portalUserBusiness.getMainCreditorsInAlphabeticalOrder(userId);
    }

    @Override
    public List<ReportDisplayBean> getReportsForDate(List<Creditor> accessibleCreditors, Date reportDate) {
        List<Report> reports = reportBusiness.getReportsByReportDateAndCreditors(reportDate, accessibleCreditors);
        List<ReportDisplayBean> displayBeans = new ArrayList<>(reports.size());
        int rownum = 1;
        for (Report report : reports) {
            ReportDisplayBean displayBean = new ReportDisplayBean(report);
            displayBean.setRownum(rownum);
            displayBeans.add(displayBean);
            rownum++;
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.parse(Report.INITIAL_REPORT_DATE_STR);
        } catch (ParseException pe) {
            throw new RuntimeException("Initial report date is incorrectly formatted");
        }
    }

    @Override
    public Report getReport(Creditor creditor, Date reportDate) {
        List<Creditor> creditors = new ArrayList<>();
        creditors.add(creditor);
        List<Report> reports = reportBusiness.getReportsByReportDateAndCreditors(reportDate, creditors);
        if (reports.size() > 1) {
            throw new RuntimeException("Reports size > 1");
        }
        return reports.isEmpty() ? null : reports.get(0);
    }

    @Override
    public CrossCheck getLastCrossCheck(Creditor creditor, Date reportDate) {
//        List<CrossCheck> crossChecks = crossCheckBusiness.loadCrossCheck(Arrays.asList(creditor.getId()), reportDate);
//        return crossChecks.isEmpty() ? null : crossChecks.get(0);
        return null;
    }

    @Override
    public void updateReportStatus(Report report, ReportType status) {
//        Shared sharedStatus = sharedBusiness.findByC_T(status.getCode(), SharedType.REPORT_TYPE.getType());
//        report.setStatus(sharedStatus);
//        reportBusiness.updateReport(report);
    }

    @Override
    public List<ReportMessageAttachment> getReportAttachments(Report report) {
//        return reportMessageBusiness.getAttachmentsByReport(report);
        return null;
    }

    @Override
    public void sendApprovalNotifications(Creditor creditor, Report report, String username, Date sendDate, String text) {
//        List<PortalUser> notificationRecipients = portalUserBusiness.getPortalUsersHavingAccessToCreditor(creditor);
//        Properties mailMessageParameters = new Properties();
//        mailMessageParameters.setProperty("CREDITOR", creditor.getName());
//        mailMessageParameters.setProperty("STATUS", report.getStatus().getNameRu());
//        mailMessageParameters.setProperty("USERNAME", username);
//        mailMessageParameters.setProperty("REPORT_DATE", DATE_FORMAT.format(report.getReportDate()));
//        mailMessageParameters.setProperty("UPDATE_TIME", TIME_FORMAT.format(sendDate));
//        mailMessageParameters.setProperty("TEXT", text);
//        for (PortalUser portalUser : notificationRecipients) {
//            mailMessageBusiness.sendMailMessage("APPROVAL_UPDATE", portalUser.getUserId(), mailMessageParameters);
//        }
    }

//    private MailMessageParameter getMailMessageParameter(MailMessage mailMessage, MailTemplateParameter templateParameter, String value) {
//        MailMessageParameter mailMessageParameter = new MailMessageParameter();
//        mailMessageParameter.setMailMessage(mailMessage);
//        mailMessageParameter.setMailTemplateParameter(templateParameter);
//        mailMessageParameter.setValue(value);
//        return mailMessageParameter;
//    }

    @Override
    public void updateLastManualEditDate(Report report) {
//        report.setLastManualEditDate(new Date());
//        reportBusiness.updateReport(report);
    }
}
