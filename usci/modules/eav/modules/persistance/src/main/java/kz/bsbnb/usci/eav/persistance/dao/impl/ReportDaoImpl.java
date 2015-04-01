package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.eav.persistance.generated.tables.records.ReportRecord;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IReportDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
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

    public List<Report> getReportsByReportDateAndCreditors(Date reportDate, List<Creditor> creditors) {
        ArrayList<Report> reports = new ArrayList<>();
        HashMap<Long, Creditor> creditorMap = new HashMap<>();
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
            report.setCreditor(creditorMap.get(row.get(EAV_REPORT.CREDITOR_ID.getName())));
            report.setTotalCount(((BigDecimal) row.get(EAV_REPORT.TOTAL_COUNT.getName())).longValue());
            report.setActualCount(((BigDecimal) row.get(EAV_REPORT.ACTUAL_COUNT.getName())).longValue());
            report.setBeginningDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.BEG_DATE.getName())));
            report.setEndDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.END_DATE.getName())));
            report.setLastManualEditDate(DataUtils.convert((Timestamp) row.get(EAV_REPORT.LAST_MANUAL_EDIT_DATE.getName())));
            report.setStatusId(((BigDecimal)row.get(EAV_REPORT.STATUS_ID.getName())).longValue());

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
        ArrayList<ReportMessage> reportMessages = new ArrayList<>();

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
        ArrayList<ReportMessageAttachment> attachments = new ArrayList<>();

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
}