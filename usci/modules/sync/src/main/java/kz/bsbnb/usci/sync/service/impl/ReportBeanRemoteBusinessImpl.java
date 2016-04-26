package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;
import kz.bsbnb.usci.sync.service.ReportBeanRemoteBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Service
public class ReportBeanRemoteBusinessImpl implements ReportBeanRemoteBusiness {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier(value = "reportBeanRemoteBusiness")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    private kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness remoteReportBusiness;

    @PostConstruct
    public void init() {
        remoteReportBusiness =
                (kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness) rmiProxyFactoryBean.getObject();
    }

    @Override
    public Date getReportDate(long creditorId) {
        return remoteReportBusiness.getReportDate(creditorId);
    }

    @Override
    public Date getLastApprovedDate(long creditorId) {
        return remoteReportBusiness.getLastApprovedDate(creditorId);
    }

    @Override
    public Report getReport(long creditorId, Date reportDate) {
        return remoteReportBusiness.getReport(creditorId, reportDate);
    }

    @Override
    public Long insert(Report report, String username) {
        return remoteReportBusiness.insert(report, username);
    }

    @Override
    public Report getByCreditor_ReportDate(Creditor creditor, Date reportDate) {
        return remoteReportBusiness.getByCreditor_ReportDate(creditor, reportDate);
    }

    @Override
    public List<Report> getReportsByReportDateAndCreditors(Date reportDate, List<Creditor> creditors) {
        return remoteReportBusiness.getReportsByReportDateAndCreditors(reportDate, creditors);
    }

    @Override
    public List<ReportMessage> getMessagesByReport(Report report) {
        return remoteReportBusiness.getMessagesByReport(report);
    }

    @Override
    public List<ReportMessageAttachment> getAttachmentsByReport(Report report) {
        return remoteReportBusiness.getAttachmentsByReport(report);
    }

    @Override
    public void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments) {
        remoteReportBusiness.addNewMessage(message, report, attachments);
    }

    @Override
    public void updateReport(Report report, String username) {
        remoteReportBusiness.updateReport(report, username);
    }

    @Override
    public void setTotalCount(long reportId, long totalCount) {
        remoteReportBusiness.setTotalCount(reportId, totalCount);
    }

    @Override
    public Report getFirstReport(long creditorId) {
        return remoteReportBusiness.getFirstReport(creditorId);
    }
}
