package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;

import java.util.Date;
import java.util.List;

public interface IReportDao {
    Long insertReport(Report report, String username);

    List<Report> getReportsByReportDateAndCreditors(Date reportDate, List<Creditor> creditors);

    Date getFirstNotApprovedDate(Long creditorId);

    Date getLastApprovedDate(Long creditorId);

    Date getLastReportDate(Long creditorId);

    List<ReportMessage> getMessagesByReport(Report report);

    List<ReportMessageAttachment> getAttachmentsByReport(Report report);

    void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments);

    void updateReport(Report report, String username);

    void updateReport(Report report);

    Report getReport(long creditorId, Date reportDate);

    void setTotalCount(long reportId, long totalCount);

    Report getFirstReport(long creditorId);

    Report getMaxApprovedReport(long creditorId);
}
