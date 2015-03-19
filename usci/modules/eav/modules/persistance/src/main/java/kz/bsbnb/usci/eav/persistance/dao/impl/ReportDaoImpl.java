package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.eav.persistance.generated.tables.records.ReportRecord;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportStatus;
import kz.bsbnb.usci.cr.model.Shared;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IReportDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
                .where(EAV_REPORT.REPORT_DATE.eq(DataUtils.convert(reportDate)))
                .and(EAV_REPORT.CREDITOR_ID.in(creditorMap.keySet()));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            Report report = new Report();
            report.setId((Long)row.get(EAV_REPORT.ID.getName()));
            report.setCreditor(creditorMap.get(row.get(EAV_REPORT.CREDITOR_ID.getName())));
            report.setTotalCount((Long) row.get(EAV_REPORT.TOTAL_COUNT.getName()));
            report.setActualCount((Long) row.get(EAV_REPORT.ACTUAL_COUNT.getName()));
            report.setBeginningDate(DataUtils.convert((Timestamp) rows.get(0).get(EAV_REPORT.BEG_DATE.getName())));
            report.setEndDate(DataUtils.convert((Timestamp) rows.get(0).get(EAV_REPORT.END_DATE.getName())));
            report.setLastManualEditDate(DataUtils.convert((Timestamp) rows.get(0).get(EAV_REPORT.LAST_MANUAL_EDIT_DATE.getName())));
            report.setStatusId((Long)row.get(EAV_REPORT.STATUS_ID.getName()));

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
}
