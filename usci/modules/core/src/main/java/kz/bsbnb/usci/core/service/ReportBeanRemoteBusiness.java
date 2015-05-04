package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;

import java.util.Date;
import java.util.List;

public interface ReportBeanRemoteBusiness {
    Long insert(Report report, String username);

    Date getReportDate(long creditorId);

    Date getLastApprovedDate(long creditorId);

    Report getReport(long creditorId, Date reportDate);

    Report getByCreditor_ReportDate(Creditor creditor, Date reportDate);

    List<Report> getReportsByReportDateAndCreditors(Date reportDate, List<Creditor> creditors);

    List<ReportMessage> getMessagesByReport(Report report);

    List<ReportMessageAttachment> getAttachmentsByReport(Report report);

    void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments);

    void updateReport(Report report);

    void setTotalCount(long reportId, long totalCount);
}
