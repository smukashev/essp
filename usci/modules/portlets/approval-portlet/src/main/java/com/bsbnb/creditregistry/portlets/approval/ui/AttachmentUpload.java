package com.bsbnb.creditregistry.portlets.approval.ui;

import com.bsbnb.creditregistry.portlets.approval.PortletEnvironmentFacade;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import kz.bsbnb.usci.cr.model.ReportMessageAttachment;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class AttachmentUpload extends HorizontalLayout
        implements Upload.StartedListener, Upload.FailedListener,
        Upload.FinishedListener, Upload.ProgressListener, Upload.SucceededListener,
        Upload.Receiver {
    private static final long MAX_FILE_LENGTH = 5*(1L<<20); //5MB

    private ReportMessageAttachment attachment;
    
    private Upload upload;
    private ProgressIndicator indicator;
    private ByteArrayOutputStream baos;
    private Label messageLabel;
    private final PortletEnvironmentFacade environment;
    private boolean uploadCancelled = false;
    private final Logger logger = org.apache.log4j.Logger.getLogger(AttachmentUpload.class);

    public AttachmentUpload(PortletEnvironmentFacade environment) {
        this.environment = environment;
    }
    
    public ReportMessageAttachment getAttachment() {
        return attachment;
    }

    @Override
    public void attach() {
        upload = new Upload("", this);
        upload.setButtonCaption(environment.getResourceString(Localization.UPLOAD_BUTTON_CAPTION));
        upload.setImmediate(true);
        upload.addListener((Upload.StartedListener) this);
        upload.addListener((Upload.FailedListener) this);
        upload.addListener((Upload.FinishedListener) this);
        upload.addListener((Upload.ProgressListener) this);
        upload.addListener((Upload.SucceededListener) this);
        addComponent(upload);
        
        //indicator
        indicator = new ProgressIndicator();
        //indicator.setCaption(getResourceString(Localization.UPLOAD_PROGRESS_MESSAGE.getKey()));
        indicator.setImmediate(true);
        indicator.setIndeterminate(false);
        indicator.setValue(0);
        indicator.setVisible(false);

        addComponent(indicator);
        
    }

    public void setMessage(String message) {
        if(messageLabel!=null) {
            removeComponent(messageLabel);
        }
        messageLabel = new Label(message);
        addComponent(messageLabel);
    }
    
    @Override
    public void uploadSucceeded(SucceededEvent event) {
        byte[] array = baos.toByteArray();
        String filename = event.getFilename();
        attachment = new ReportMessageAttachment();
        attachment.setFilename(filename);
        attachment.setContent(array);
        setMessage(filename);
    }

    @Override
    public void uploadStarted(Upload.StartedEvent event) {
        uploadCancelled = false;
        upload.setEnabled(false);
        if(event.getContentLength()>MAX_FILE_LENGTH) {
            uploadCancelled = true;
            upload.interruptUpload();
            setMessage(environment.getResourceString(Localization.FILE_EXCEEDED_FILE_LIMIT_OF_5_MB));
            return;
        }
        indicator.setVisible(true);
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        if (uploadCancelled) {
            return;
        }
        indicator.setVisible(false);
        upload.setEnabled(true);
        logger.error("Upload failed", event.getReason());
        setMessage(event.getReason().getMessage());
    }

    @Override
    public void uploadFinished(Upload.FinishedEvent event) {
        upload.setEnabled(true);
        indicator.setVisible(false);
    }

    @Override
    public void updateProgress(long readBytes, long contentLength) {
        double progress = readBytes * 1.0 / contentLength;
        indicator.setValue(progress);
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        baos = new ByteArrayOutputStream();
        return baos;
    }
}
