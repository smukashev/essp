package kz.bsbnb.usci.portlet.report.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import org.apache.log4j.Logger;


/**
 *
 * @author Aidar.Myrzahanov
 */
public class InputInfoDisplayBean implements Button.ClickListener {

    private InputInfo inputInfo;
    private DataProvider provider;
    private final Logger logger = Logger.getLogger(InputInfoDisplayBean.class);

    private final String path = "C:\\tmp_zips";

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
        return "---";
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return inputInfo == null ? "" : new File(inputInfo.getFileName()).getName();
    }

    public Button getFileLink() {
        Button result = new Button(getFileName(), this);
        result.setStyleName(BaseTheme.BUTTON_LINK);
        result.addListener(this);
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

        final BigInteger batchId = inputInfo.getId();

        final BatchFullJModel batchFullJModel = provider.getBatchFullModel(batchId);


        final File batchFile = new File(path + "batch_" + batchId + ".zip");

        if(!batchFile.exists()) {
            try {
                batchFile.createNewFile();
            } catch(IOException e) {
                logger.error(e.getMessage(),e);
            }
        } else {
            batchFile.delete();
        }

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(batchFile);
            fos.write(batchFullJModel.getContent());
            fos.close();
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }

        FileResource resource = new FileResource(batchFile, button.getApplication()) {
            @Override
            public String getFilename() {
                return batchFile.getName();
            }

        };

        button.getWindow().open(resource, "_blank");

    }
}
