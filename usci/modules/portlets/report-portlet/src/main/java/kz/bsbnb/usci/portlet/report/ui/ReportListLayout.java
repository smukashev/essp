package kz.bsbnb.usci.portlet.report.ui;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;
import kz.bsbnb.usci.portlet.report.Localization;
import kz.bsbnb.usci.portlet.report.ReportApplication;
import kz.bsbnb.usci.portlet.report.ReportPortletResource;
import kz.bsbnb.usci.portlet.report.dm.DatabaseConnect;
import kz.bsbnb.usci.portlet.report.dm.ExportType;
import kz.bsbnb.usci.portlet.report.dm.Report;
import kz.bsbnb.usci.portlet.report.dm.ReportController;
import kz.bsbnb.usci.portlet.report.export.*;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportListLayout extends VerticalLayout {

    private static final String LOCALIZATION_PREFIX = "MAIN-LAYOUT";
    private static final String[] COLUMN_ORDER = new String[]{"localizedName"};
    private DatabaseConnect connect;
    private List<Report> reports;
    private ReportComponent reportComponent;

    private String getResourceString(String key) {
        return ReportApplication.getResourceString(LOCALIZATION_PREFIX + "." + key);
    }
    private VerticalLayout headerLayout;
    private Table reportsTable;
    private Button showReportsListButton;
    private VerticalLayout reportComponentLayout;
    private static final Logger logger = Logger.getLogger(ReportListLayout.class);

    private String[] getColumnHeaders() {
        String[] columns = COLUMN_ORDER;
        String[] headers = new String[columns.length];
        String columnLocalizationPrefix = "REPORTS-TABLE-COLUMN.";
        for (int i = 0; i < columns.length; i++) {
            headers[i] = getResourceString(columnLocalizationPrefix + columns[i]);
        }
        return headers;
    }
    
    public ReportListLayout(DatabaseConnect connect) {
        this.connect = connect;
    }

    @Override
    public void attach() {
        headerLayout = new VerticalLayout();
        displayReportHeader(Localization.START_HEADER.getValue());

        ReportController reportController = new ReportController();
        reports = reportController.loadReports(connect);
        BeanItemContainer<Report> reportsContainer = new BeanItemContainer<Report>(Report.class, reports);
        reportsTable = new Table(Localization.REPORTS_TABLE_CAPTION.getValue(), reportsContainer);
        reportsTable.setVisibleColumns(COLUMN_ORDER);
        reportsTable.setColumnHeaders(getColumnHeaders());
        reportsTable.addListener(new ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                Report report = (Report) event.getItemId();
                displayReport(report);
            }
        });
        reportsTable.setWidth("100%");
        //
        //showReportsListButton
        //
        showReportsListButton = new Button(Localization.SHOW_REPORTS_LIST_BUTTON_CAPTION.getValue(), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                reportComponentLayout.removeAllComponents();
                reportComponentLayout.setVisible(false);
                showReportsListButton.setVisible(false);
                reportsTable.setVisible(true);
            }
        });
        showReportsListButton.setIcon(ReportPortletResource.ARROW_LEFT_ICON);
        showReportsListButton.setStyleName(BaseTheme.BUTTON_LINK);
        showReportsListButton.setVisible(false);
        //
        //reportComponentLayout
        //
        reportComponentLayout = new VerticalLayout();
        reportComponentLayout.setWidth("100%");
        reportComponentLayout.setVisible(false);


        final UriFragmentUtility ufu = new UriFragmentUtility();
        ufu.addListener(new UriFragmentUtility.FragmentChangedListener() {
            @Override
            public void fragmentChanged(UriFragmentUtility.FragmentChangedEvent source) {
                try {
                    String fragment = source.getUriFragmentUtility().getFragment();
                    String[] keyValuePairs = fragment.split(",");
                    Properties properties = new Properties();
                    for (String keyValuePair : keyValuePairs) {
                        int splitIndex = keyValuePair.indexOf('=');
                        if (splitIndex >= 0) {
                            String key = keyValuePair.substring(0, splitIndex).toUpperCase();
                            String value = keyValuePair.substring(splitIndex + 1);
                            properties.setProperty(key, value);
                        }
                    }
                    navigateFromUrl(properties);
                } catch (Property.ReadOnlyException e) {
                    e.printStackTrace();
                } catch (Property.ConversionException e) {
                    e.printStackTrace();
                }
            }
        });


        //
        //MainLayout
        //
        setSpacing(true);
        setMargin(true);
        addComponent(showReportsListButton);
        setComponentAlignment(showReportsListButton, Alignment.TOP_RIGHT);
        addComponent(headerLayout);
        addComponent(reportsTable);
        addComponent(reportComponentLayout);
        addComponent(ufu);
    }

    private String getReportHeaderString(String reportName) {
        return String.format(Localization.SELECTED_REPORT_HEADER.getValue(), reportName);
    }

    private void navigateFromUrl(Properties properties) {
        String reportName = properties.getProperty("REPORT");
        for (Report report : reports) {
            if (report.getName().equalsIgnoreCase(reportName)) {
                displayReport(report);
                reportComponent.setParameters(properties);
                break;
            }
        }
    }

    private void displayReport(Report report) {
        displayReportHeader(getReportHeaderString(report.getLocalizedName()));
        reportComponent = new ReportComponent(report, connect);
        String reportName = report.getName();
        if ("BanksWithData".equalsIgnoreCase(reportName)) {
            reportComponent.addReportExporter(new BanksWithDataTableReportExporter());
            reportComponent.addReportExporter(new JasperReportExporter(connect.getUser()));
        } else if(reportName.contains("Pledge")) {
            reportComponent.addReportExporter(new OutputFormExporter());
            logger.info("Output form exporter applied");
        } else {
            for (ExportType exportType : report.getExportTypesList()) {
                if (ExportType.JASPER_XLS.equals(exportType.getName())) {
                    reportComponent.addReportExporter(new JasperReportExporter(connect.getUser()));
                } else if (ExportType.TABLE_VAADIN.equals(exportType.getName())) {
                    TableReportExporter tableReportExporter = new TableReportExporter();
                    tableReportExporter.setUser(connect.getUser());
                    reportComponent.addReportExporter(tableReportExporter);
                } else if (ExportType.TEMPLATE_XLS.equals(exportType.getName())) {
                    reportComponent.addReportExporter(new TemplatedPagedXlsReportExporter());
                } else {
                    logger.warn("Unknown export type: "+ exportType.getName());
                }
            }
        }
        reportComponent.setWidth("100%");
        reportComponentLayout.removeAllComponents();
        reportComponentLayout.addComponent(reportComponent);
        reportsTable.setVisible(false);
        reportComponentLayout.setVisible(true);
        showReportsListButton.setVisible(true);
    }

    private void displayReportHeader(String displayText) {
        headerLayout.removeAllComponents();
        Label headerLabel = new Label(displayText, Label.CONTENT_XHTML);
        headerLabel.setImmediate(true);
        headerLayout.addComponent(headerLabel);
    }
}
