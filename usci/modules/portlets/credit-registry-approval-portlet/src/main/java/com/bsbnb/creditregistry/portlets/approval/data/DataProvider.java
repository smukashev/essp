package com.bsbnb.creditregistry.portlets.approval.data;

//import com.bsbnb.creditregistry.dm.ReportMessage;
//import com.bsbnb.creditregistry.dm.ReportMessageAttachment;
//import com.bsbnb.creditregistry.dm.maintenance.CrossCheck;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.dm.ref.shared.ReportType;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportType;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {

    public List<Creditor> getCreditorsList(long userId);

    public List<ReportDisplayBean> getReportsForDate(List<Creditor> accessibleCreditors, Date reportDate);

    public Date getCurrentReportDate(Creditor creditor);

    public List<ReportMessage> getReportMessages(Report report);

    public void addNewMessage(ReportMessage message, Report report, List<ReportMessageAttachment> attachments);

    public Date getInitialReportDate();

    public Report getReport(Creditor creditor, Date reportDate);

    public CrossCheck getLastCrossCheck(Creditor creditor, Date reportDate);

    public void updateReportStatus(Report report, ReportType status);

    public void updateLastManualEditDate(Report report);

    public List<ReportMessageAttachment> getReportAttachments(Report report);

    public void sendApprovalNotifications(Creditor creditor, Report report, String username, Date sendDate, String text);
}
