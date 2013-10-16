package com.bsbnb.creditregistry.portlets.report.ui;

import java.util.List;
import java.util.logging.Level;

import com.bsbnb.creditregistry.portlets.report.Localization;
import com.bsbnb.creditregistry.portlets.report.ReportApplication;
import static com.bsbnb.creditregistry.portlets.report.ReportApplication.log;
import com.bsbnb.creditregistry.portlets.report.ReportPortletResource;
import com.bsbnb.creditregistry.portlets.report.dm.DatabaseConnect;
import com.bsbnb.creditregistry.portlets.report.dm.ExportType;
import com.bsbnb.creditregistry.portlets.report.dm.Report;
import com.bsbnb.creditregistry.portlets.report.dm.ReportController;
import com.bsbnb.creditregistry.portlets.report.export.BanksWithDataTableReportExporter;
import com.bsbnb.creditregistry.portlets.report.export.JasperReportExporter;
import com.bsbnb.creditregistry.portlets.report.export.OutputFormExporter;
import com.bsbnb.creditregistry.portlets.report.export.TableReportExporter;
import com.bsbnb.creditregistry.portlets.report.export.TemplatedPagedXlsReportExporter;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class ReportListLayout extends VerticalLayout {

    private static final String LOCALIZATION_PREFIX = "MAIN-LAYOUT";
    private static final String[] COLUMN_ORDER = new String[]{"localizedName"};
    private DatabaseConnect connect;

    private String getResourceString(String key) {
        return ReportApplication.getResourceString(LOCALIZATION_PREFIX + "." + key);
    }
    private VerticalLayout headerLayout;
    private Table reportsTable;
    private Button showReportsListButton;
    private VerticalLayout reportComponentLayout;

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
        List<Report> reports = reportController.loadReports();
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
    }

    private String getReportHeaderString(String reportName) {
        return String.format(Localization.SELECTED_REPORT_HEADER.getValue(), reportName);
    }

    private void displayReport(Report report) {
        displayReportHeader(getReportHeaderString(report.getLocalizedName()));
        ReportComponent reportComponent = new ReportComponent(report, connect);
        String reportName = report.getName();
        System.out.println("-1");
        if ("BanksWithData".equalsIgnoreCase(reportName)) {
            System.out.println("0");
            reportComponent.addReportExporter(new BanksWithDataTableReportExporter());
            System.out.println("00");
            reportComponent.addReportExporter(new JasperReportExporter());
            System.out.println("000");
        } else if(reportName.contains("Pledge")) {
            reportComponent.addReportExporter(new OutputFormExporter());
            log.log(Level.INFO, "Output form exporter applied");
        } else {
            for (ExportType exportType : report.getExportTypesList()) {
                if (ExportType.JASPER_XLS.equals(exportType.getName())) {
                    reportComponent.addReportExporter(new JasperReportExporter());
                } else if (ExportType.TABLE_VAADIN.equals(exportType.getName())) {
                    reportComponent.addReportExporter(new TableReportExporter());
                } else if (ExportType.TEMPLATE_XLS.equals(exportType.getName())) {
                    reportComponent.addReportExporter(new TemplatedPagedXlsReportExporter());
                } else {
                    log.log(Level.WARNING, "Unknown export type: {0}", exportType.getName());
                }
            }
        }
        System.out.println("1");
        reportComponent.setWidth("100%");
        System.out.println("2");
        reportComponentLayout.removeAllComponents();
        System.out.println("3");
        reportComponentLayout.addComponent(reportComponent);
        System.out.println("4");
        reportsTable.setVisible(false);
        System.out.println("5");
        reportComponentLayout.setVisible(true);
        System.out.println("6");
        showReportsListButton.setVisible(true);
        System.out.println("7");
    }

    private void displayReportHeader(String displayText) {
        headerLayout.removeAllComponents();
        Label headerLabel = new Label(displayText, Label.CONTENT_XHTML);
        headerLabel.setImmediate(true);
        headerLayout.addComponent(headerLabel);
    }
}
