package com.bsbnb.creditregistry.portlets.approval.ui;

import com.bsbnb.creditregistry.portlets.approval.ApprovalPortletResource;
import com.bsbnb.creditregistry.portlets.approval.PortletEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.approval.bpm.ApprovalBusiness;
import com.bsbnb.creditregistry.portlets.approval.data.CrossCheckLink;
import com.bsbnb.creditregistry.portlets.approval.data.DataProvider;
import com.bsbnb.util.translit.Transliterator;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.util.ReportStatus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportDateLayout extends VerticalLayout {

    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat MESSAGE_TIME_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private DataProvider provider;
    private PortletEnvironmentFacade environment;
    private Creditor creditor;
    private Date currentReportDate;
    private Date reportDate;
    private Report report;
    private VerticalLayout reportDateDetailsLayout;

    public ReportDateLayout(DataProvider provider, PortletEnvironmentFacade environment, Creditor creditor) {
        this.provider = provider;
        this.environment = environment;
        this.creditor = creditor;
        this.currentReportDate = provider.getCurrentReportDate(creditor);
        reportDate = currentReportDate;
    }

    public ReportDateLayout(DataProvider provider, PortletEnvironmentFacade environment, Creditor creditor, Date reportDate) {
        this(provider, environment, creditor);
        this.reportDate = reportDate;
    }

    @Override
    public void attach() {
        Label creditorLabel = new Label("<h2 style='text-align:center'>" + creditor.getName() + "</h2>", Label.CONTENT_XHTML);
        reportDateDetailsLayout = new VerticalLayout();
        addComponent(creditorLabel);
        setComponentAlignment(creditorLabel, Alignment.MIDDLE_CENTER);
        addComponent(reportDateDetailsLayout);

        loadReportDate(reportDate);
    }

    private void loadReportDate(Date newReportDate) {
        currentReportDate = provider.getCurrentReportDate(creditor);
        reportDate = newReportDate;
        reportDateDetailsLayout.removeAllComponents();

        HorizontalLayout crossCheckStatusLayout = new HorizontalLayout();
        Label crossCheckStatusLabel = new Label("<b>" + environment.getResourceString(Localization.CROSS_CHECK_RESULT_LABEL_CAPTION) + ": </b>", Label.CONTENT_XHTML);
        CrossCheck lastCrossCheck = provider.getLastCrossCheck(creditor, reportDate);
        String crossCheckLinkCaption = lastCrossCheck == null
                ? environment.getResourceString(Localization.CROSS_CHECK_DID_NOT_RUN)
                : lastCrossCheck.getStatus().getNameRu();
        CrossCheckLink crossCheckLink = new CrossCheckLink(crossCheckLinkCaption, creditor.getId(), reportDate);
        crossCheckStatusLayout.addComponent(crossCheckStatusLabel);
        crossCheckStatusLayout.addComponent(crossCheckLink);

        Date initialReportDate = provider.getInitialReportDate();
        Calendar calendar = Calendar.getInstance();
        HorizontalLayout reportDatesLayout = new HorizontalLayout();
        int reportPeriodDurationMonths = creditor.getSubjectType().getReportPeriodDurationMonths();
        if (newReportDate.after(initialReportDate)) {
            calendar.setTime(newReportDate);
            calendar.add(Calendar.MONTH, -reportPeriodDurationMonths);
            final Date previousReportDate = calendar.getTime();
            Button previousReportDateLink = new Button(DEFAULT_DATE_FORMAT.format(calendar.getTime()), new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    loadReportDate(previousReportDate);
                }
            });
            previousReportDateLink.setStyleName(BaseTheme.BUTTON_LINK);
            previousReportDateLink.setSizeUndefined();
            reportDatesLayout.addComponent(previousReportDateLink);
            reportDatesLayout.setComponentAlignment(previousReportDateLink, Alignment.MIDDLE_LEFT);
            reportDatesLayout.setExpandRatio(previousReportDateLink, 1);
        } else {
            Label placeHolderLabel = new Label("");
            placeHolderLabel.setSizeUndefined();
            reportDatesLayout.addComponent(placeHolderLabel);
            reportDatesLayout.setComponentAlignment(placeHolderLabel, Alignment.MIDDLE_LEFT);
            reportDatesLayout.setExpandRatio(placeHolderLabel, 1);
        }

        final Label reportDateLabel = new Label("<b text-align:center>" + DEFAULT_DATE_FORMAT.format(reportDate) + "</b>", Label.CONTENT_XHTML);
        reportDateLabel.setSizeUndefined();

        reportDatesLayout.addComponent(reportDateLabel);
        reportDatesLayout.setComponentAlignment(reportDateLabel, Alignment.MIDDLE_CENTER);
        reportDatesLayout.setExpandRatio(reportDateLabel, 1);
        calendar.setTime(newReportDate);
        calendar.add(Calendar.MONTH, reportPeriodDurationMonths);
        final Date nextReportDate = calendar.getTime();
        if (provider.getReport(creditor, nextReportDate) != null || nextReportDate.equals(currentReportDate)) {

            final Button nextReportDateLink = new Button(DEFAULT_DATE_FORMAT.format(calendar.getTime()), new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    loadReportDate(nextReportDate);
                }
            });
            nextReportDateLink.setStyleName(BaseTheme.BUTTON_LINK);
            nextReportDateLink.setSizeUndefined();
            reportDatesLayout.addComponent(nextReportDateLink);
            reportDatesLayout.setComponentAlignment(nextReportDateLink, Alignment.MIDDLE_RIGHT);
            reportDatesLayout.setExpandRatio(nextReportDateLink, 1);
        } else {
            Label placeHolderLabel = new Label("");
            placeHolderLabel.setSizeUndefined();
            reportDatesLayout.addComponent(placeHolderLabel);
            reportDatesLayout.setComponentAlignment(placeHolderLabel, Alignment.MIDDLE_RIGHT);
            reportDatesLayout.setExpandRatio(placeHolderLabel, 1);
        }
        reportDatesLayout.setWidth("100%");

        reportDateDetailsLayout.addComponent(crossCheckStatusLayout);
        reportDateDetailsLayout.setComponentAlignment(crossCheckStatusLayout, Alignment.MIDDLE_CENTER);
        reportDateDetailsLayout.addComponent(reportDatesLayout);
        report = provider.getReport(creditor, reportDate);

        if (report == null || report.getStatusId() == null) {
            Label noReportsLabel = new Label("<h2>" + environment.getResourceString(Localization.NO_REPORTS_MESSAGE) + "</h2>", Label.CONTENT_XHTML);
            reportDateDetailsLayout.addComponent(noReportsLabel);
            return;
        }

        Label horizontalLine = new Label("<hr/>", Label.CONTENT_XHTML);

        Label contractsCountLabel = new Label("<b>"
                + environment.getResourceString(Localization.CONTRACTS_COUNT_LABEL_CAPTION)
                + ": </b>"
                + report.getActualCount(), Label.CONTENT_XHTML);
        contractsCountLabel.setSizeUndefined();

        final String reportStatusCaption = String.format(environment.getResourceString(Localization.REPORT_STATUS_LABEL_CAPTION), report.getStatus().getNameRu());
        Label reportStatusLabel = new Label("<b style='text-align: center'>" + reportStatusCaption + "</b>", Label.CONTENT_XHTML);
        reportStatusLabel.setSizeUndefined();

        Button approveReportButton = null;
        if (!ReportStatus.COMPLETED.code().equals(report.getStatus().getCode())) {
            String approveReportButtonCaption = environment.getResourceString(Localization.APPROVE_REPORT_BUTTON_CAPTION);
            if (environment.isNbUser()) {
                approveReportButton = new Button(approveReportButtonCaption, new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent event) {
                        updateReportStatus(ReportStatus.COMPLETED);
                    }
                });
            } else if (environment.isBankUser() && reportDate.equals(currentReportDate)
                    && !ReportStatus.ORGANIZATION_APPROVED.code().equals(report.getStatus().getCode())
                    && !ReportStatus.ORGANIZATION_APPROVING.code().equals(report.getStatus().getCode())) {
                approveReportButton = new Button(approveReportButtonCaption, new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent event) {
                        ApprovalBusiness approvalBusiness = new ApprovalBusiness();
                        HashMap<String, String> data = new HashMap<String, String>();
                        data.put("reportId", report.getId().toString());
                        data.put("actualCount", report.getActualCount().toString());
                        data.put("creditorName",creditor.getName().replace("\"", "\\\""));
                        data.put("begDate", Long.toString(report.getBeginningDate().getTime()));
                        data.put("endDate", Long.toString(report.getEndDate().getTime()));
                        data.put("userName", environment.getUsername());
                        data.put("reportDate", DEFAULT_DATE_FORMAT.format(reportDate));
                        data.put("creditorId", creditor.getId() + "");

                        approvalBusiness.startApprovalProcess(data);
                        updateReportStatus(ReportStatus.ORGANIZATION_APPROVING);
                    }
                });
            }
        }
        Button undoApproveReportButton = null;
        if (environment.isNbUser()
                && (report.getStatus().getCode().equals(ReportStatus.COMPLETED.code())
                || report.getStatus().getCode().equals(ReportStatus.ORGANIZATION_APPROVED.code()))) {
            String undoApproveReportButtonCaption = environment.getResourceString(Localization.UNDO_APPROVE_REPORT_BUTTON_CAPTION);
            undoApproveReportButton = new Button(undoApproveReportButtonCaption, new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent event) {
                    updateReportStatus(ReportStatus.IN_PROGRESS);
                }
            });
        }

        List<ReportMessage> messages = provider.getReportMessages(report);
        List<ReportMessageAttachment> attachments = provider.getReportAttachments(report);
        HashMap<Long, List<ReportMessageAttachment>> attachmentsByMessages = new HashMap<Long, List<ReportMessageAttachment>>(attachments.size());
        for (ReportMessageAttachment attachment : attachments) {
            List<ReportMessageAttachment> messageAttachments;
            if (!attachmentsByMessages.containsKey(attachment.getReportMessage().getId())) {
                messageAttachments = new ArrayList<ReportMessageAttachment>();
            } else {
                messageAttachments = attachmentsByMessages.get(attachment.getReportMessage().getId());
            }
            messageAttachments.add(attachment);
            attachmentsByMessages.put(attachment.getReportMessage().getId(), messageAttachments);
        }
        VerticalLayout messagesLayout = new VerticalLayout();
        messagesLayout.setSpacing(false);
        for (ReportMessage message : messages) {
            Label usernameLabel = new Label("<b>" + message.getUsername() + "</b> - <i>" + MESSAGE_TIME_FORMAT.format(message.getSendDate()) + "</i>", Label.CONTENT_XHTML);
            Label textLabel = new Label(message.getText(), Label.CONTENT_XHTML);
            messagesLayout.addComponent(usernameLabel);
            messagesLayout.addComponent(textLabel);
            List<ReportMessageAttachment> messageAttachments = attachmentsByMessages.get(message.getId());
            if (messageAttachments != null && !messageAttachments.isEmpty()) {
                HorizontalLayout messageAttachmentsLayout = new HorizontalLayout();
                for (final ReportMessageAttachment messageAttachment : messageAttachments) {
                    Button attachmentButton = new Button(messageAttachment.getFilename(), new Button.ClickListener() {
                        public void buttonClick(Button.ClickEvent event) {
                            StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(messageAttachment.getContent());
                                }
                            };
                            final String transliteratedFilename = Transliterator.transliterate(messageAttachment.getFilename());
                            StreamResource resource = new StreamResource(streamSource, transliteratedFilename, getApplication()) {
                                @Override
                                public DownloadStream getStream() {
                                    DownloadStream downloadStream = super.getStream();
                                    downloadStream.setParameter("Content-Disposition", "attachment;filename=" + getFilename());
                                    downloadStream.setContentType("application/octet-stream");
                                    downloadStream.setCacheTime(0);
                                    return downloadStream;
                                }
                            };
                            getWindow().open(resource, "_blank");
                        }
                    });
                    attachmentButton.setStyleName(BaseTheme.BUTTON_LINK);
                    attachmentButton.setIcon(ApprovalPortletResource.ATTACHMENT_ICON);
                    messageAttachmentsLayout.addComponent(attachmentButton);
                }
                messagesLayout.addComponent(messageAttachmentsLayout);
            }
            messagesLayout.addComponent(new Label("<hr/>", Label.CONTENT_XHTML));
        }

        Label newMessageLabel = new Label("<b>" + environment.getResourceString(Localization.NEW_MESSAGE_CAPTION) + "</b>", Label.CONTENT_XHTML);
        newMessageLabel.setSizeUndefined();
        final RichTextArea newMessageTextArea = new RichTextArea();
        newMessageTextArea.setWidth("100%");

        final VerticalLayout attachmentsLayout = new VerticalLayout();
        Button addAttachmentButton = new Button(environment.getResourceString(Localization.ADD_ATTACHMENT_BUTTON_CAPTION), new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                attachmentsLayout.addComponent(new AttachmentUpload(environment));
            }
        });

        Button addNewMessageButton = new Button(environment.getResourceString(Localization.ADD_NEW_MESSAGE_BUTTON_CAPTION), new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                List<ReportMessageAttachment> attachments = new ArrayList<ReportMessageAttachment>(attachmentsLayout.getComponentCount());
                for (int componentIndex = 0; componentIndex < attachmentsLayout.getComponentCount(); componentIndex++) {
                    AttachmentUpload upload = (AttachmentUpload) attachmentsLayout.getComponent(componentIndex);
                    if (upload.getAttachment() != null) {
                        attachments.add(upload.getAttachment());
                    }
                }
                addNewMessage((String) newMessageTextArea.getValue(), attachments);
            }
        });
        messagesLayout.addComponent(newMessageLabel);
        messagesLayout.setComponentAlignment(newMessageLabel, Alignment.MIDDLE_CENTER);
        messagesLayout.addComponent(newMessageTextArea);
        messagesLayout.addComponent(attachmentsLayout);
        messagesLayout.addComponent(addAttachmentButton);
        messagesLayout.addComponent(addNewMessageButton);

        reportDateDetailsLayout.addComponent(contractsCountLabel);
        reportDateDetailsLayout.setComponentAlignment(contractsCountLabel, Alignment.MIDDLE_CENTER);
        reportDateDetailsLayout.addComponent(reportStatusLabel);
        reportDateDetailsLayout.setComponentAlignment(reportStatusLabel, Alignment.MIDDLE_CENTER);
        if (environment.isBankUser() || (environment.isNbUser() && environment.isApprovalAuthority())) {
            if (approveReportButton != null) {
                reportDateDetailsLayout.addComponent(approveReportButton);
                reportDateDetailsLayout.setComponentAlignment(approveReportButton, Alignment.MIDDLE_CENTER);
            }
            if (undoApproveReportButton != null) {
                reportDateDetailsLayout.addComponent(undoApproveReportButton);
                reportDateDetailsLayout.setComponentAlignment(undoApproveReportButton, Alignment.MIDDLE_CENTER);
            }
        }
        if (environment.isAdministrator()) {
            final String lastEditDateString;
            if(report.getLastManualEditDate()==null) {
                lastEditDateString = environment.getResourceString(Localization.EMPTY_DATE_STRING);
            } else {
                lastEditDateString = DEFAULT_DATE_FORMAT.format(report.getLastManualEditDate());
            }
            Label lastManualEditDateLabel = new Label(
                    String.format(environment.getResourceString(Localization.LAST_MANUAL_EDIT_CAPTION), lastEditDateString),
                    Label.CONTENT_XHTML);
            lastManualEditDateLabel.setSizeUndefined();
            Button updateLastManualEditButton = new Button(environment.getResourceString(Localization.UPDATE_LAST_MANUAL_EDIT_DATE), new  Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    provider.updateLastManualEditDate(report);
                    loadReportDate(reportDate);
                }
            });
            reportDateDetailsLayout.addComponent(lastManualEditDateLabel);
            reportDateDetailsLayout.setComponentAlignment(lastManualEditDateLabel, Alignment.MIDDLE_CENTER);
            reportDateDetailsLayout.addComponent(updateLastManualEditButton);
            reportDateDetailsLayout.setComponentAlignment(updateLastManualEditButton, Alignment.MIDDLE_CENTER);
        }
        reportDateDetailsLayout.addComponent(horizontalLine);
        reportDateDetailsLayout.addComponent(messagesLayout);
    }

    private void updateReportStatus(ReportStatus newStatus) {
        provider.updateReportStatus(report, newStatus);
        loadReportDate(reportDate);
        addNewMessage(String.format(environment.getResourceString(Localization.REPORT_APPROVE_MESSAGE_TEMPLATE), report.getStatus().getNameRu()), new ArrayList<ReportMessageAttachment>());
    }

    private void addNewMessage(String text, List<ReportMessageAttachment> attachments) {
        ReportMessage message = new ReportMessage();
        message.setReport(report);
        Date sendDate = new Date();
        message.setSendDate(sendDate);
        message.setText(text);
        message.setUsername(environment.getUsername());
        provider.addNewMessage(message, report, attachments);
        loadReportDate(reportDate);
        provider.sendApprovalNotifications(creditor, report, environment.getUsername(), sendDate, text);
    }

    public Date getReportDate() {
        return reportDate;
    }
}
