package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;

import java.util.Date;
import java.util.List;

public interface ReportBeanRemoteBusiness {
    public Long insert(Report report, String username);

    public Date getReportDate(long creditorId);

    public Report getByCreditor_ReportDate(Creditor creditor, Date reportDate);

    public List<Report> getReportsByReportDateAndCreditors(Date reportDate, List<Creditor> creditors);

    public List<ReportMessage> getMessagesByReport(Report report);

    public List<ReportMessageAttachment> getAttachmentsByReport(Report report);

    public void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments);

    public void updateReport(Report report);
}
