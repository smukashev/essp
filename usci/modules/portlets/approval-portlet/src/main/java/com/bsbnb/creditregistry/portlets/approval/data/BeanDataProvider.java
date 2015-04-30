package com.bsbnb.creditregistry.portlets.approval.data;

import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.*;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public BeanDataProvider() {
        // portalUserBeanRemoteBusiness
        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://localhost:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);
        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        // reportBeanRemoteBusiness
        reportBusinessFactoryBean = new RmiProxyFactoryBean();
        reportBusinessFactoryBean.setServiceUrl("rmi://localhost:1099/reportBeanRemoteBusiness");
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
        List<Report> reports = reportBusiness.getReportsByReportDateAndCreditors(reportDate, accessibleCreditors);
        List<ReportDisplayBean> displayBeans = new ArrayList<ReportDisplayBean>(reports.size());
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
        return reportBusiness.getMessagesByReport(report);
    }

    @Override
    public void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments) {
        reportBusiness.addNewMessage(message, report, attachments);
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
        List<Creditor> creditors = new ArrayList<Creditor>();
        creditors.add(creditor);
        List<Report> reports = reportBusiness.getReportsByReportDateAndCreditors(reportDate, creditors);
        if (reports.size() > 1) {
            throw new RuntimeException("Reports size > 1");
        }
        return reports.isEmpty() ? null : reports.get(0);
    }

    @Override
    public CrossCheck getLastCrossCheck(Creditor creditor, Date reportDate) {
        /* todo: implement */
//        List<CrossCheck> crossChecks = crossCheckBusiness.loadCrossCheck(Arrays.asList(creditor.getId()), reportDate);
//        return crossChecks.isEmpty() ? null : crossChecks.get(0);
        return null;
    }

    @Override
    public void updateReportStatus(Report report, ReportType status) {
        report.setStatusId(ReportType.STATUS_CODE_ID_MAP.get(status.getCode()));
        reportBusiness.updateReport(report);
    }

    @Override
    public List<ReportMessageAttachment> getReportAttachments(Report report) {
        return reportBusiness.getAttachmentsByReport(report);
    }

    @Override
    public void sendApprovalNotifications(Creditor creditor, Report report, String username, Date sendDate, String text) {
        /* todo: implement */
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
        report.setLastManualEditDate(new Date());
        reportBusiness.updateReport(report);
    }
}
