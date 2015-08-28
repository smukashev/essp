package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;
import kz.bsbnb.usci.eav.persistance.dao.IReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ReportBeanRemoteBusinessImpl implements ReportBeanRemoteBusiness {
    @Autowired
    IReportDao reportDao;

    @Override
    public Date getReportDate(long creditorId) {
        Date firstNotApprovedDate = reportDao.getFirstNotApprovedDate(creditorId);
        if (firstNotApprovedDate != null) {
            return firstNotApprovedDate;
        }

        Date lastApprovedDate = reportDao.getLastApprovedDate(creditorId);
        if (lastApprovedDate != null) {
            // todo: add creditor.getSubjectType().getReportPeriodDurationMonths()
            return lastApprovedDate;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.parse(Report.INITIAL_REPORT_DATE_STR);
        } catch (ParseException pe) {
            throw new RuntimeException("Unable to parse the initial report date.");
        }
    }

    @Override
    public Report getReport(long creditorId, Date reportDate) {
        return reportDao.getReport(creditorId, reportDate);
    }

    @Override
    public Date getLastApprovedDate(long creditorId) {
        return reportDao.getLastApprovedDate(creditorId);
    }

    @Override
    public Date getLastReportDate(long creditorId) {
        return reportDao.getLastReportDate(creditorId);
    }

    @Override
    public Long insert(Report report, String username) {
        return reportDao.insertReport(report, username);
    }


    @Override
    @Deprecated
    public Report getByCreditor_ReportDate(Creditor creditor, Date reportDate) {
        return null;
    }

    @Override
    public List<Report> getReportsByReportDateAndCreditors(Date reportDate, List<Creditor> creditors) {
        return reportDao.getReportsByReportDateAndCreditors(reportDate, creditors);
    }

    @Override
    public List<ReportMessage> getMessagesByReport(Report report) {
        return reportDao.getMessagesByReport(report);
    }

    @Override
    public List<ReportMessageAttachment> getAttachmentsByReport(Report report) {
        return reportDao.getAttachmentsByReport(report);
    }

    @Override
    public void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments) {
        reportDao.addNewMessage(message, report, attachments);
    }

    @Override
    public void updateReport(Report report, String username) {
        reportDao.updateReport(report, username);
    }

    @Override
    public void updateReport(Report report) {
        reportDao.updateReport(report);
    }

    @Override
    public void setTotalCount(long reportId, long totalCount) {
        reportDao.setTotalCount(reportId, totalCount);
    }
}
