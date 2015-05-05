package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;

import java.util.Date;
import java.util.List;

/**
 * Created by n.seitkozhayev on 2/18/15.
 */
public interface IReportDao {
    public Long insertReport(Report report, String username);
    public List<Report> getReportsByReportDateAndCreditors(Date reportDate, List<Creditor> creditors);
    public Date getFirstNotApprovedDate(Long creditorId);
    public Date getLastApprovedDate(Long creditorId);
    public List<ReportMessage> getMessagesByReport(Report report);
    public List<ReportMessageAttachment> getAttachmentsByReport(Report report);
    public void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments);
    public void updateReport(Report report, String username);
    public void updateReport(Report report);
    public Report getReport(long creditorId, Date reportDate);
    public void setTotalCount(long reportId, long totalCount);
}
