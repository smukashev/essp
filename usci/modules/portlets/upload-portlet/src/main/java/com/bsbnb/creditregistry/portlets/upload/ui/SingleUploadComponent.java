package com.bsbnb.creditregistry.portlets.upload.ui;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import com.bsbnb.creditregistry.portlets.upload.PortletEnvironmentFacade;
import static com.bsbnb.creditregistry.portlets.upload.UploadApplication.log;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class SingleUploadComponent extends AbstractUploadComponent
        implements Upload.StartedListener, Upload.FailedListener,
        Upload.FinishedListener, Upload.ProgressListener, Upload.SucceededListener,
        Upload.Receiver {

    private Upload upload;
    private ProgressIndicator indicator;
    private ByteArrayOutputStream baos;
    private boolean uploadCancelled = false;

    public SingleUploadComponent(PortletEnvironmentFacade portletEnvironment) {
        super(portletEnvironment);
    }
    
    /**
     * Метод загружает стандартный интерфейс загрузки файла
     * Метод должен вызываться если у пользователя есть один и только один кредитор
     */
    @Override
    protected void initializeUploadComponents() {
        //upload
        upload = new Upload("", this);
        upload.setButtonCaption(getResourceString(Localization.UPLOAD_BUTTON_CAPTION.getKey()));
        upload.setImmediate(true);
        upload.addListener((Upload.StartedListener) this);
        upload.addListener((Upload.FailedListener) this);
        upload.addListener((Upload.FinishedListener) this);
        upload.addListener((Upload.ProgressListener) this);
        upload.addListener((Upload.SucceededListener) this);

        addComponent(upload);
        setComponentAlignment(upload, Alignment.TOP_CENTER);

        //indicator
        indicator = new ProgressIndicator();
        indicator.setCaption(getResourceString(Localization.UPLOAD_PROGRESS_MESSAGE.getKey()));
        indicator.setImmediate(true);
        indicator.setIndeterminate(false);
        indicator.setValue(0);
        indicator.setVisible(false);

        addComponent(indicator);
        setComponentAlignment(indicator, Alignment.TOP_CENTER);
    }

    @Override
    public void uploadStarted(StartedEvent event) {
        uploadCancelled = false;
        clearStatus();
        upload.setEnabled(false);
        if (!isFileValid(event.getContentLength(), event.getFilename())) {
            uploadCancelled = true;
            upload.interruptUpload();
            return;
        }
        addStatusMessage(getResourceString(Localization.UPLOAD_HAVE_STARTED_MESSAGE.getKey()), false);
        indicator.setVisible(true);
    }

    @Override
    public void uploadFailed(FailedEvent event) {
        if (uploadCancelled) {
            return;
        }
        indicator.setVisible(false);
        upload.setEnabled(true);
        log.log(Level.SEVERE, "Upload failed", event.getReason());
        addStatusMessage(event.getReason().getMessage(), true);
    }

    @Override
    public void uploadFinished(FinishedEvent event) {
        upload.setEnabled(true);
        indicator.setVisible(false);
    }

    @Override
    public void updateProgress(long readBytes, long contentLength) {
        double progress = readBytes * 1.0 / contentLength;
        indicator.setValue(progress);
    }

    @Override
    public void uploadSucceeded(SucceededEvent event) {
        log.log(Level.INFO, "Upload succeeded");
        byte[] array = baos.toByteArray();
        String fileName = event.getFilename();
        handleFile(array, fileName);
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        baos = new ByteArrayOutputStream();
        return baos;
    }

    
}
