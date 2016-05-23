package com.bsbnb.creditregistry.portlets.queue.data;

import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.themes.BaseTheme;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by bauka on 5/18/16.
 */
public class InputInfoDisplayBean implements Button.ClickListener{
    private InputInfo inputInfo;
    private DataProvider provider;
    private boolean selected;

    public InputInfoDisplayBean(InputInfo inputInfo, DataProvider provider) {
        this.inputInfo = inputInfo;
        this.provider = provider;
    }

    public InputInfo getInputInfo() {
        return inputInfo;
    }

    public String getCreditorName() {
        if (inputInfo != null && inputInfo.getCreditor() != null) {
            return inputInfo.getCreditor().getName();
        }
        return "---";
    }

    public String getFileName() {
        return (inputInfo == null || inputInfo.getFileName() == null) ? "" : new File(inputInfo.getFileName()).getName();
    }

    public Button getFileLink() {
        Button result = new Button(getFileName(), this);
        result.setStyleName(BaseTheme.BUTTON_LINK);
        result.addListener(this);
        return result;
    }

    public Date getReceiverDate() {
        return inputInfo == null ? null : inputInfo.getReceiverDate();
    }

    public String getReceiverType() {
        if (inputInfo == null || inputInfo.getReceiverType() == null)
            return "";

        /*return PortletEnvironmentFacade.get().isLanguageKazakh()
                ? inputInfo.getReceiverType().getNameKz()
                : inputInfo.getReceiverType().getNameRu();*/

        return inputInfo.getReceiverType().getNameRu();

    }

    public Date getCompletionDate() {
        return inputInfo == null ? null : inputInfo.getCompletionDate();
    }

    public Date getReportDate() {
        return inputInfo == null ? null : inputInfo.getReportDate();
    }

    public Date getStartDate() {
        return inputInfo == null ? null : inputInfo.getStartedDate();
    }

    public String getStatusName() {
        if (inputInfo == null || inputInfo.getStatus() == null)
            return "";

        /*return PortletEnvironmentFacade.get().isLanguageKazakh()
                ? inputInfo.getStatus().getNameKz()
                : inputInfo.getStatus().getNameRu();*/
        return inputInfo.getStatus().getNameRu();
    }

    public void buttonClick(Button.ClickEvent event) {
        Button button = event.getButton();

        final BigInteger batchId = inputInfo.getId();

        final BatchFullJModel batchFullJModel =  provider.getBatchFullModel(batchId);

        final File batchFile = new File("batch_" + batchId + ".zip");

        FileResource resource = new FileResource(batchFile, button.getApplication()) {
            @Override
            public DownloadStream getStream() {
                final DownloadStream ds = new DownloadStream(
                        new ByteArrayInputStream(batchFullJModel.getContent()),
                        "application/zip",
                        new File(batchFullJModel.getFileName()).getName()
                );

                ds.setParameter("Content-Length", String.valueOf(batchFullJModel.getContent()));
                ds.setCacheTime(DownloadStream.DEFAULT_CACHETIME);
                return ds;
            }
        };

        button.getWindow().open(resource, "_blank");

    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
