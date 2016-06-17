package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IReportDao;
import kz.bsbnb.usci.eav.repository.IEavGlobalRepository;
import kz.bsbnb.usci.eav.util.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ReportBeanRemoteBusinessImpl implements ReportBeanRemoteBusiness {
    private static final String CREDITOR_DATES = "CREDITOR_DATES";
    private static final String ORG_FIRST_DATE_SETTING = "ORG_FIRST_DATE_SETTING";
    private static final String DEFAULT_DATE_VALUE = "DEFAULT_DATE_VALUE";

    private final Logger logger = LoggerFactory.getLogger(ReportBeanRemoteBusinessImpl.class);

    @Autowired
    IReportDao reportDao;

    @Autowired
    private IEavGlobalRepository eavGlobalRepository;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Override
    public Date getReportDate(long creditorId) {
        /*Date firstNotApprovedDate = reportDao.getFirstNotApprovedDate(creditorId);
        if (firstNotApprovedDate != null) {
            return firstNotApprovedDate;
        }*/

        Date lastApprovedDate = reportDao.getLastApprovedDate(creditorId);
        if (lastApprovedDate != null) {
            IBaseEntity creditor = baseEntityProcessorDao.getBaseEntityLoadDao().load(creditorId);
            Integer period = (Integer) creditor.getEl("subject_type.report_period_duration_months");
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastApprovedDate);
            cal.add(Calendar.MONTH, period == null ? 1 : period);
            return cal.getTime();
        }

        try {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            //return dateFormat.parse(Report.INITIAL_REPORT_DATE_STR);
            String creditorDates = eavGlobalRepository.getValue(ORG_FIRST_DATE_SETTING, CREDITOR_DATES);
            String creditorFirstDate = eavGlobalRepository.getValue(ORG_FIRST_DATE_SETTING, DEFAULT_DATE_VALUE);

            String[] pairs = creditorDates.split(",");
            for(String pair: pairs) {
                String[] record = pair.split("=");
                Long cId = Long.parseLong(record[0]);
                String date = record[1];
                if(creditorId == cId) {
                    creditorFirstDate = date;
                    break;
                }
            }

            return new SimpleDateFormat("dd.MM.yyyy").parse(creditorFirstDate);
        } catch (ParseException pe) {
            logger.error(Errors.compose(Errors.E235, pe));
            throw new RuntimeException(Errors.compose(Errors.E235));
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

    @Override
    public Report getFirstReport(long creditorId) {
        return reportDao.getFirstReport(creditorId);
    }
}
