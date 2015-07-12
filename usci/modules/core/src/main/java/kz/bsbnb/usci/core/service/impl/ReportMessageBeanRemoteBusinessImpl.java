package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.ReportMessageBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.cr.model.ReportMessage;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportMessageBeanRemoteBusinessImpl implements ReportMessageBeanRemoteBusiness {
    public List<ReportMessage> getMessagesByReport(Report report) {
        ArrayList<ReportMessage> reportMessages = new ArrayList<ReportMessage>();
        return reportMessages;
    }

    public void addNewMessage(Report report, ReportMessage message, List<ReportMessageAttachment> attachments) {
    }

    public List<ReportMessageAttachment> getAttachmentsByReport(Report report) {
        ArrayList<ReportMessageAttachment> reportMessageAttachments = new ArrayList<ReportMessageAttachment>();
        return reportMessageAttachments;
    }
}
