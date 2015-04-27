package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.sync.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Service
public class ReportBeanRemoteBusinessImpl implements ReportBeanRemoteBusiness
{
    @Autowired
    @Qualifier(value = "reportBeanRemoteBusiness")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness remoteReportBusiness;

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
    public void updateReport(Report report) {
        remoteReportBusiness.updateReport(report);
    }
}