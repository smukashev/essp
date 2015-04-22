package com.bsbnb.usci.portlets.protocol.data;

import java.io.File;
import java.util.Date;

//import com.bsbnb.creditregistry.dm.maintenance.InputFile;
//import com.bsbnb.creditregistry.dm.maintenance.InputInfo;
import com.bsbnb.usci.portlets.protocol.PortletEnvironmentFacade;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class InputInfoDisplayBean implements Button.ClickListener {

    private InputInfo inputInfo;
    private DataProvider provider;

    public InputInfoDisplayBean(InputInfo inputInfo, DataProvider provider) {
        this.inputInfo = inputInfo;
        this.provider = provider;
    }

    /**
     * @return the inputInfo
     */
    public InputInfo getInputInfo() {
        return inputInfo;
    }

    /**
     * @return the creditorName
     */
    public String getCreditorName() {
        if (inputInfo != null && inputInfo.getCreditor() != null) {
            return inputInfo.getCreditor().getName();
        }
        return "CREDITOR NAME";
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return inputInfo == null ? "" : inputInfo.getFileName();
    }

    public Button getFileLink() {
        Button result = new Button(getFileName(), this);
        result.setStyleName(BaseTheme.BUTTON_LINK);
        return result;
    }

    /**
     * @return the receiverDate
     */
    public Date getReceiverDate() {
        return inputInfo == null ? null : inputInfo.getReceiverDate();
    }

    /**
     * @return the receiverType
     */
    public String getReceiverType() {
        if (inputInfo == null || inputInfo.getReceiverType() == null) {
            return "";
        }
        return PortletEnvironmentFacade.get().isLanguageKazakh()
                ? inputInfo.getReceiverType().getNameKz()
                : inputInfo.getReceiverType().getNameRu();
    }

    /**
     * @return the completionDate
     */
    public Date getCompletionDate() {
        return inputInfo == null ? null : inputInfo.getCompletionDate();
    }

    /**
     * @return the reportDate
     */
    public Date getReportDate() {
        return inputInfo == null ? null : inputInfo.getReportDate();
    }
    
    public Date getStartDate() {
        return inputInfo == null ? null : inputInfo.getStartedDate();
    }

    /**
     * @return the statusName
     */
    public String getStatusName() {
        if (inputInfo == null || inputInfo.getStatus() == null) {
            return "";
        }
        return PortletEnvironmentFacade.get().isLanguageKazakh()
                ? inputInfo.getStatus().getNameKz()
                : inputInfo.getStatus().getNameRu();
    }

    public void buttonClick(ClickEvent event) {
        Button button = event.getButton();
        if (inputInfo != null) {
            InputFile inputFile = provider.getFileByInputInfo(this);
            if (inputFile != null) {
                FileResource resource = new FileResource(new File(inputFile.getFilePath()), button.getApplication()) {
                    @Override
                    public String getFilename() {
                        return getFileName(); 
                    }
                    
                };
                button.getWindow().open(resource, "_blank");
            }
        }
    }
}
