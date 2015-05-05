package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.eav.persistance.generated.tables.records.EavReportRecord;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IReportDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * Created by n.seitkozhayev on 2/18/15.
 */
@Repository
public class ReportDaoImpl extends JDBCSupport implements IReportDao {

    private final Logger logger  = LoggerFactory.getLogger(ReportDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    public Long insertReport(Report report, String username) {
        InsertOnDuplicateStep insert = context.insertInto(
                EAV_REPORT,
                EAV_REPORT.USERNAME,
                EAV_REPORT.CREDITOR_ID,
                EAV_REPORT.REPORT_DATE,
                EAV_REPORT.STATUS_ID,
                EAV_REPORT.TOTAL_COUNT,
                EAV_REPORT.ACTUAL_COUNT,
                EAV_REPORT.BEG_DATE,
                EAV_REPORT.END_DATE,
                EAV_REPORT.LAST_MANUAL_EDIT_DATE
        ).values(username,
                report.getCreditor().getId(),
                DataUtils.convert(report.getReportDate()),
                report.getStatusId(),
                report.getTotalCount(),
                report.getActualCount(),
                DataUtils.convert(report.getBeginningDate()),
                DataUtils.convert(report.getEndDate()),
                DataUtils.convert(report.getLastManualEditDate()));

        Long reportId = 1L;
        try {
            reportId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());
        } catch (Exception e) {
            Update update = context.update(EAV_REPORT)
                    .set(EAV_REPORT.USERNAME, username)
                    .set(EAV_REPORT.CREDITOR_ID, report.getCreditor().getId())
                            .set(EAV_REPORT.REPORT_DATE, DataUtils.convert(report.getReportDate()))
                                    .set(EAV_REPORT.STATUS_ID, report.getStatusId())
                                    .set(EAV_REPORT.TOTAL_COUNT, report.getTotalCount())
                                            .set(EAV_REPORT.ACTUAL_COUNT, report.getActualCount())
                                                    .set(EAV_REPORT.BEG_DATE, DataUtils.convert(report.getBeginningDate()))
                                                            .set(EAV_REPORT.END_DATE, DataUtils.convert(report.getEndDate()))
                                                                    .set(EAV_REPORT.LAST_MANUAL_EDIT_DATE, DataUtils.convert(report.getLastManualEditDate()))
                                                                    .where(EAV_REPORT.USERNAME.equal(username)).and(EAV_REPORT.REPORT_DATE.eq(DataUtils.convert(report.getBeginningDate())));

            updateWithStats(update.getSQL(), update.getBindValues().toArray());

            SelectForUpdateStep select = context
                    .select()
                    .from(EAV_REPORT)
                    .where(EAV_REPORT.USERNAME.equal(username)).and(EAV_REPORT.REPORT_DATE.eq(DataUtils.convert(report.getBeginningDate())));

            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

            for (Map<String, Object> row : rows) {
                reportId = ((BigDecimal)row.get(EAV_REPORT.ID.getName())).longValue();
                break;
            }
        }

        return reportId;
    }

    public List<Report> getReportsByReportDateAndCreditors(Date reportDate, List<Creditor> creditors) {
        ArrayList<Report> reports = new ArrayList<Report>();
        HashMap<Long, Creditor> creditorMap = new HashMap<Long, Creditor>();
        for (Creditor creditor : creditors) {
            creditorMap.put(creditor.getId(), creditor);
        }

        SelectForUpdateStep select = context
                .select()
                .from(EAV_REPORT)
                .where(EAV_REPORT.REPORT_DATE.equal(DataUtils.convert(reportDate)))
                .and(EAV_REPORT.CREDITOR_ID.in(creditorMap.keySet()));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            Report report = new Report();
            report.setId(((BigDecimal)row.get(EAV_REPORT.ID.getName())).longValue());
            report.setCreditor(creditorMap.get(((BigDecimal) row.get(EAV_REPORT.CREDITOR_ID.getName())).longValue()));
            report.setTotalCount(((BigDecimal) row.get(EAV_REPORT.TOTAL_COUNT.getName())).longValue());
            report.setActualCount(((BigDecimal) row.get(EAV_REPORT.ACTUAL_COUNT.getName())).longValue());
            report.setBeginningDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.BEG_DATE.getName())));
            report.setEndDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.END_DATE.getName())));
            report.setLastManualEditDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.LAST_MANUAL_EDIT_DATE.getName())));
            report.setStatusId(((BigDecimal) row.get(EAV_REPORT.STATUS_ID.getName())).longValue());
            report.setReportDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.REPORT_DATE.getName())));
            reports.add(report);
        }

        return reports;
    }

    public Date getFirstNotApprovedDate(Long creditorId) {
        Field<java.sql.Date> field = EAV_REPORT.REPORT_DATE.min().as("FIRST_NOT_APPROVED_DATE");
        SelectForUpdateStep select = context
                .select(field)
                .from(EAV_REPORT)
                .where(EAV_REPORT.CREDITOR_ID.eq(creditorId))
                .and(EAV_REPORT.STATUS_ID.notEqual(ReportStatus.COMPLETED.getStatusId()))
                .and(EAV_REPORT.STATUS_ID.notEqual(ReportStatus.ORGANIZATION_APPROVED.getStatusId()));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Date date = null;
        if (!rows.isEmpty()) {
            date = DataUtils.convert((Timestamp)rows.get(0).get("FIRST_NOT_APPROVED_DATE"));
        }
        return date;
    }

    public Date getLastApprovedDate(Long creditorId) {
        Field<java.sql.Date> field = EAV_REPORT.REPORT_DATE.max().as("LAST_APPROVED_DATE");

        SelectForUpdateStep select = context
                .select(field)
                .from(EAV_REPORT)
                .where(EAV_REPORT.CREDITOR_ID.eq(creditorId))
                .and(EAV_REPORT.STATUS_ID.eq(ReportStatus.COMPLETED.getStatusId())
                        .or(EAV_REPORT.STATUS_ID.eq(ReportStatus.ORGANIZATION_APPROVED.getStatusId())));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Date date = null;
        if (!rows.isEmpty()) {
            date = DataUtils.convert((Timestamp)rows.get(0).get("LAST_APPROVED_DATE"));
        }
        return date;
    }

    @Override
    public List<ReportMessage> getMessagesByReport(Report report) {
        ArrayList<ReportMessage> reportMessages = new ArrayList<ReportMessage>();

        SelectForUpdateStep select = context
                .select()
                .from(EAV_REPORT_MESSAGE)
                .where(EAV_REPORT_MESSAGE.REPORT_ID.equal(report.getId()));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            ReportMessage reportMessage = new ReportMessage();
            reportMessage.setId(((BigDecimal) row.get(EAV_REPORT_MESSAGE.ID.getName())).longValue());
            reportMessage.setReport(report);
            reportMessage.setSendDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT_MESSAGE.SEND_DATE.getName())));
            reportMessage.setText((String) row.get(EAV_REPORT_MESSAGE.TEXT.getName()));
            reportMessage.setUsername((String) row.get(EAV_REPORT_MESSAGE.USERNAME.getName()));

            reportMessages.add(reportMessage);
        }

        return reportMessages;
    }

    @Override
    public List<ReportMessageAttachment> getAttachmentsByReport(Report report) {
        ArrayList<ReportMessageAttachment> attachments = new ArrayList<ReportMessageAttachment>();

        SelectForUpdateStep select = context
                .select()
                .from(EAV_REPORT_MESSAGE_ATTACHMENT)
                .join(EAV_REPORT_MESSAGE)
                .on(EAV_REPORT_MESSAGE.ID.equal(EAV_REPORT_MESSAGE_ATTACHMENT.REPORT_MESSAGE_ID))
                .where(EAV_REPORT_MESSAGE.REPORT_ID.equal(report.getId()));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            ReportMessageAttachment attachment = new ReportMessageAttachment();
            attachment.setId(((BigDecimal) row.get(EAV_REPORT_MESSAGE_ATTACHMENT.ID.getName())).longValue());
            attachment.setContent((byte[]) row.get(EAV_REPORT_MESSAGE_ATTACHMENT.CONTENT.getName()));
            attachment.setFilename((String) row.get(EAV_REPORT_MESSAGE_ATTACHMENT.FILENAME.getName()));

            ReportMessage reportMessage = new ReportMessage();
            reportMessage.setId(((BigDecimal) row.get(EAV_REPORT_MESSAGE.ID.getName())).longValue());
            reportMessage.setReport(report);
            reportMessage.setSendDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT_MESSAGE.SEND_DATE.getName())));
            reportMessage.setText((String) row.get(EAV_REPORT_MESSAGE.TEXT.getName()));
            reportMessage.setUsername((String) row.get(EAV_REPORT_MESSAGE.USERNAME.getName()));
            attachment.setReportMessage(reportMessage);

            attachments.add(attachment);
        }

        return attachments;
    }

    @Override
    public void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments) {
        try {
            InsertOnDuplicateStep insert = context.insertInto(
                    EAV_REPORT_MESSAGE,
                    EAV_REPORT_MESSAGE.REPORT_ID,
                    EAV_REPORT_MESSAGE.SEND_DATE,
                    EAV_REPORT_MESSAGE.TEXT,
                    EAV_REPORT_MESSAGE.USERNAME
            ).values(report.getId(),
                    DataUtils.convert(message.getSendDate()),
                    message.getText(),
                    message.getUsername());

            Long messageId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

            for (ReportMessageAttachment attachment : attachments) {
                insert = context.insertInto(
                        EAV_REPORT_MESSAGE_ATTACHMENT,
                        EAV_REPORT_MESSAGE_ATTACHMENT.CONTENT,
                        EAV_REPORT_MESSAGE_ATTACHMENT.FILENAME,
                        EAV_REPORT_MESSAGE_ATTACHMENT.REPORT_MESSAGE_ID
                ).values(attachment.getContent(),
                        attachment.getFilename(),
                        messageId);
                insertWithId(insert.getSQL(), insert.getBindValues().toArray());
            }
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Duplicate ids in report_message or report_message_attachment");
        }


    }

    @Override
    public void updateReport(Report report, String username) {
        UpdateSetMoreStep<EavReportRecord> update = context.update(EAV_REPORT)
                .set(EAV_REPORT.TOTAL_COUNT, report.getTotalCount())
                .set(EAV_REPORT.ACTUAL_COUNT, report.getActualCount())
                .set(EAV_REPORT.STATUS_ID, report.getStatusId())
                .set(EAV_REPORT.LAST_MANUAL_EDIT_DATE, DataUtils.convert(report.getLastManualEditDate()));

        if (username != null) {
            update.set(EAV_REPORT.USERNAME, username);
        }

        update.where(EAV_REPORT.ID.equal(report.getId()));

        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }

    @Override
    public void updateReport(Report report) {
        updateReport(report, null);
    }

    @Override
    public void setTotalCount(long reportId, long totalCount) {
        Update update = context
                .update(EAV_REPORT)
                .set(EAV_REPORT.TOTAL_COUNT, totalCount)
                .where(EAV_REPORT.ID.equal(reportId));
        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }

    @Override
    public Report getReport(long creditorId, Date reportDate) {
        Select select = context.select().from(EAV_REPORT)
                .where(EAV_REPORT.CREDITOR_ID.equal(creditorId))
                .and(EAV_REPORT.REPORT_DATE.equal(DataUtils.convert(reportDate)));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.isEmpty()) {
            return null;
        }

        Map<String, Object> row = rows.get(0);

        Report report = new Report();
        report.setId(((BigDecimal) row.get(EAV_REPORT.ID.getName())).longValue());
        {
            Creditor creditor = new Creditor();
            creditor.setId(((BigDecimal) row.get(EAV_REPORT.CREDITOR_ID.getName())).longValue());

            report.setCreditor(creditor);
        }
        report.setTotalCount(((BigDecimal) row.get(EAV_REPORT.TOTAL_COUNT.getName())).longValue());
        report.setActualCount(((BigDecimal) row.get(EAV_REPORT.ACTUAL_COUNT.getName())).longValue());
        report.setBeginningDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.BEG_DATE.getName())));
        report.setEndDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.END_DATE.getName())));
        report.setLastManualEditDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.LAST_MANUAL_EDIT_DATE.getName())));
        report.setStatusId(((BigDecimal) row.get(EAV_REPORT.STATUS_ID.getName())).longValue());
        report.setReportDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.REPORT_DATE.getName())));

        return report;
    }
}
