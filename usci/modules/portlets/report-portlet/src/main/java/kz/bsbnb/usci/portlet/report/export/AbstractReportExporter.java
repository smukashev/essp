package kz.bsbnb.usci.portlet.report.export;

import kz.bsbnb.usci.portlet.report.dm.ReportController;
import kz.bsbnb.usci.portlet.report.dm.ReportLoad;
import kz.bsbnb.usci.portlet.report.dm.ReportLoadFile;
import kz.bsbnb.usci.portlet.report.ui.ReportComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public abstract class AbstractReportExporter extends VerticalLayout {

    public static final File REPORT_FILES_FOLDER = new File("C:\\Portal_afn\\generated_reports\\");
    protected ReportComponent targetReportComponent;
    private ReportLoad load;
    private ReportController controller = new ReportController();

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
