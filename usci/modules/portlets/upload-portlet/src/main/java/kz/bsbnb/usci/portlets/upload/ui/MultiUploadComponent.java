package kz.bsbnb.usci.portlets.upload.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

import kz.bsbnb.usci.portlets.upload.UploadApplication;
import org.vaadin.easyuploads.MultiFileUpload;

import kz.bsbnb.usci.portlets.upload.PortletEnvironmentFacade;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class MultiUploadComponent extends AbstractUploadComponent {

    public MultiUploadComponent(PortletEnvironmentFacade portletEnvironment) {
        super(portletEnvironment);
    }

    @Override
    protected void initializeUploadComponents() {
        Label descriptionLabel = new Label(getResourceString(Localization.MULTI_UPLOAD_DESCRIPTION.getKey()), Label.CONTENT_XHTML);
        
        
        final AbstractUploadComponent parent = this;
        MultiFileUpload upload = new MultiFileUpload() {

            @Override
            protected void handleFile(File file, String fileName, String mimeType, long length) {
                if (isFileValid(length, fileName)) {
                    byte[] content = new byte[(int) file.length()];
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        fis.read(content);
                        parent.handleFile(content, fileName);
                    } catch (IOException ex) {
                        UploadApplication.log.log(Level.WARNING, "", ex);
                    } finally {
                        try {
                            fis.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
            
            @Override
            protected String getAreaText() {
                return parent.getResourceString(Localization.MULTI_UPLOAD_AREA_TEXT.getKey());
            }
        };
        upload.setImmediate(true);
        upload.setUploadButtonCaption(getResourceString(Localization.UPLOAD_BUTTON_CAPTION.getKey()));
        
        addComponent(descriptionLabel);
        setComponentAlignment(descriptionLabel, Alignment.TOP_CENTER);
        addComponent(upload);
        setComponentAlignment(upload, Alignment.TOP_CENTER);
    }
}
