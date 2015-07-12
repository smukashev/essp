package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;

import java.util.List;

public interface ReportMessageBeanRemoteBusiness {
    List<ReportMessage> getMessagesByReport(Report report);

    void addNewMessage(Report report, ReportMessage message, List<ReportMessageAttachment> attachments);

    List<ReportMessageAttachment> getAttachmentsByReport(Report report);
}
