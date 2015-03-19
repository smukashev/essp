package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;

import java.util.List;

/**
 * Created by n.seitkozhayev on 2/19/15.
 */
public interface ReportMessageBeanRemoteBusiness {

    public List<ReportMessage> getMessagesByReport(Report report);

    public void addNewMessage(Report report, ReportMessage message, List<ReportMessageAttachment> attachments);

    public List<ReportMessageAttachment> getAttachmentsByReport(Report report);
}
