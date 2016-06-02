package kz.bsbnb.usci.portlet.report.export;

import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.portlet.report.dm.ReportController;
import kz.bsbnb.usci.portlet.report.dm.ReportLoad;
import kz.bsbnb.usci.portlet.report.dm.ReportLoadFile;
import kz.bsbnb.usci.portlet.report.ui.ReportComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public abstract class AbstractReportExporter extends VerticalLayout {

    public static final File REPORT_FILES_FOLDER = new File(StaticRouter.getReportFilesFolder());
    protected ReportComponent targetReportComponent;
    private ReportLoad load;
    private ReportController controller = new ReportController();
    private Properties reportProperties;

    protected void loadConfig(String configFilePath) {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            return;
        }
        FileInputStream configFileStream = null;
        try {
            configFileStream = new FileInputStream(configFile);
            reportProperties = new Properties();
            reportProperties.load(configFileStream);
        } catch (IOException ioe) {

        } finally {
            if (configFileStream != null) {
                try {
                    configFileStream.close();
                } catch (Exception e) {
                }
            }
        }
    }
    protected int getIntegerProperty(String key, int defaultValue) {
        try {
            return Integer.valueOf(getReportProperty(key));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    private String getReportProperty(String key) {
        if (reportProperties == null) {
            return "";
        }
        return reportProperties.getProperty(key, "");
    }

    public void setTargetReportComponent(ReportComponent target) {
        this.targetReportComponent = target;
    }

    @Override
    public void attach() {
        List<Component> actionComponents = getActionComponents();
        for (Component actionComponent : actionComponents) {
            addComponent(actionComponent);
        }
        setWidth("100%");
    }

    protected void loadStarted() {
        load = new ReportLoad();
        load.setPortalUserId(targetReportComponent.getConnect().getUserId());
        load.setReport(targetReportComponent.getReport());
        load.setStartTime(new Date());
        List<String> parameterCaptions = targetReportComponent.getParameterLocalizedNames();
        List<String> parameterValues = targetReportComponent.getParameterCaptions();
        StringBuilder noteBuilder = new StringBuilder();
        for (int i = 0; i < Math.min(parameterCaptions.size(), parameterValues.size()); i++) {
            if (i > 0) {
                noteBuilder.append(", ");
            }
            noteBuilder.append(parameterCaptions.get(i));
            noteBuilder.append(" = ");
            noteBuilder.append(parameterValues.get(i));
        }
        load.setNote(noteBuilder.toString());
        controller.insertOrUpdateReportLoad(load);
    }

    protected void addFileToLoad(ReportLoadFile file) {
        load.addFile(file);
        controller.insertOrUpdateReportLoad(load);
    }

    protected void loadFinished() {
        load.setFinishTime(new Date());

        controller.insertOrUpdateReportLoad(load);
    }

    protected abstract List<Component> getActionComponents();

    /**
     * @return the targetReportComponent
     */
    protected ReportComponent getTargetReportComponent() {
        return targetReportComponent;
    }
}
