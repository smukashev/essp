package com.bsbnb.creditregistry.portlets.approval.ui;

import com.bsbnb.creditregistry.portlets.approval.ApprovalPortletResource;
import com.bsbnb.creditregistry.portlets.approval.PortletEnvironmentFacade;
import com.bsbnb.creditregistry.portlets.approval.data.DataProvider;
import com.bsbnb.creditregistry.portlets.approval.data.DatabaseConnect;
import com.bsbnb.creditregistry.portlets.approval.data.ReportDisplayBean;
import com.bsbnb.creditregistry.portlets.approval.data.ReportDisplayBean.ReportDisplayListener;
import com.bsbnb.vaadin.filterableselector.FilterableSelect;
import com.bsbnb.vaadin.filterableselector.SelectionCallback;
import com.bsbnb.vaadin.filterableselector.Selector;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.*;
import jxl.write.Label;
import kz.bsbnb.ddlutils.model.Database;
import kz.bsbnb.usci.cr.model.Creditor;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Number;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CreditorsListLayout extends VerticalLayout implements ReportDisplayListener {

    private DataProvider provider;
    private PortletEnvironmentFacade environment;
    
    private FilterableSelect<Creditor> creditorsSelect;
    private DateField reportDateField;
    private VerticalLayout tableLayout;
    private VerticalLayout listLayout;
    private VerticalLayout reportDateLayout;
    private BeanItemContainer<ReportDisplayBean> reportsContainer;
    private final Logger logger = org.apache.log4j.Logger.getLogger(AttachmentUpload.class);

    public CreditorsListLayout(DataProvider provider, PortletEnvironmentFacade environment) {
        this.provider = provider;
        this.environment = environment;
    }

    @Override
    public void attach() {
        final List<Creditor> creditorsList = provider.getCreditorsList(environment.getUserID());
        creditorsSelect = new FilterableSelect<Creditor>(creditorsList, new Selector<Creditor>() {

            public String getCaption(Creditor item) {
                return item.getName();
            }

            public Object getValue(Creditor item) {
                return item;
            }

            public String getType(Creditor item) {
                return item.getSubjectType().getNameRu();
            }
        });
        
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        reportDateField = new DateField(environment.getResourceString(Localization.REPORT_DATE_CAPTION), calendar.getTime());
        reportDateField.setDateFormat("dd.MM.yyyy");

        Button showTableForReportDateButton = new Button(environment.getResourceString(Localization.SHOW_CREDITORS_CAPTION), new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                creditorsSelect.getSelectedElements(new SelectionCallback<Creditor>() {

                    @Override
                    public void selected(List<Creditor> selectedCreditors) {
                        displayDataForReportDate((Date) reportDateField.getValue(), selectedCreditors);
                    }
                });
            }
        });
        Button previousReportDateButton = new Button(null, new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                Date currentDate = (Date) reportDateField.getValue();
                if(currentDate==null) {
                    currentDate = new Date();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDate);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                calendar.clear();
                calendar.set(year, month, 1);
                calendar.add(Calendar.MONTH, -1);
                reportDateField.setValue(calendar.getTime());
            }
        });
        previousReportDateButton.setIcon(ApprovalPortletResource.ARROW_BACK_ICON);
        previousReportDateButton.setStyleName(BaseTheme.BUTTON_LINK);
        Button nextReportDateButton = new Button(null, new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                Date currentDate = (Date) reportDateField.getValue();
                if(currentDate==null) {
                    currentDate = new Date();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentDate);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                calendar.clear();
                calendar.set(year, month, 1);
                calendar.add(Calendar.MONTH, 1);
                reportDateField.setValue(calendar.getTime());
            }
        });
        nextReportDateButton.setIcon(ApprovalPortletResource.ARROW_FORWARD_ICON);
        nextReportDateButton.setStyleName(BaseTheme.BUTTON_LINK);
        
        HorizontalLayout reportDateFieldLayout = new HorizontalLayout();
        reportDateFieldLayout.setSpacing(false);
        reportDateFieldLayout.addComponent(previousReportDateButton);
        reportDateFieldLayout.setComponentAlignment(previousReportDateButton, Alignment.MIDDLE_LEFT);
        reportDateFieldLayout.addComponent(reportDateField);
        reportDateFieldLayout.setComponentAlignment(reportDateField, Alignment.MIDDLE_LEFT);
        reportDateFieldLayout.addComponent(nextReportDateButton);
        reportDateFieldLayout.setComponentAlignment(nextReportDateButton, Alignment.MIDDLE_LEFT);

        tableLayout = new VerticalLayout();

        listLayout = new VerticalLayout();

        listLayout.addComponent(creditorsSelect);
        
        listLayout.addComponent(reportDateFieldLayout);
        listLayout.addComponent(showTableForReportDateButton);
        listLayout.addComponent(tableLayout);

        reportDateLayout = new VerticalLayout();
        reportDateLayout.setVisible(false);

        addComponent(listLayout);
        addComponent(reportDateLayout);
        if (creditorsList.size() > 1) {
            creditorsSelect.selectElements(creditorsList.toArray(new Creditor[creditorsList.size()]));
        }
    }

    private void displayDataForReportDate(Date reportDate, List<Creditor> selectedCreditors) {
        reportDateLayout.setVisible(false);
        listLayout.setVisible(true);
        reportDateField.setValue(reportDate);
        tableLayout.removeAllComponents();
        Button exportToXlsButton = new Button(environment.getResourceString(Localization.EXPORT_TO_XLS_BUTTON_CAPTION), new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                try {
                    downloadXls("approval.xls");
                } catch (WriteException we) {
                    logger.error(null, we);
                } catch (IOException ioe) {
                    logger.error(null, ioe);
                } 
            }
        });
        exportToXlsButton.setIcon(ApprovalPortletResource.EXCEL_ICON);

        DatabaseConnect procedur = new DatabaseConnect(environment.getUser());
        List<Object> params = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(reportDate);
        cal.add(Calendar.MONTH, -1);
        Date maturityDate = cal.getTime();
        params.add(maturityDate);
        params.add(reportDate);

        Map<Long, Long> actualCounts = new HashMap<>();
        try {
            ResultSet result = procedur.getResultSetFromStoredProcedure("CREDITOR_ACTUAL_COUNT", params);
            while(result.next()) {
                Long creditor_id =  result.getLong("ref_creditor_id");
                Long actual_count =  result.getLong("actual_count");
                actualCounts.put(creditor_id,actual_count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<ReportDisplayBean> reports = provider.getReportsForDate(selectedCreditors, reportDate);
        for (ReportDisplayBean report : reports) {
            Long code = report.getReport().getCreditor().getId();
            report.getReport().setActualCount(actualCounts.get(code));
            report.addReportDisplayListener(this);
        }

        reportsContainer = new BeanItemContainer<ReportDisplayBean>(ReportDisplayBean.class, reports);
        FormattedTable reportsTable = new FormattedTable();
        reportsTable.setContainerDataSource(reportsContainer);
        String[] columnNames = environment.getResourceString(Localization.CREDITORS_TABLE_COLUMN_NAMES).split("\\|");
        String[] columnCaptions = environment.getResourceString(Localization.CREDITORS_TABLE_COLUMN_CAPTIONS).split("\\|");
        reportsTable.setVisibleColumns(columnNames);
        reportsTable.setColumnHeaders(columnCaptions);
        reportsTable.addFormat("beginDate", "dd.MM.yyyy HH:mm:ss");
        reportsTable.addFormat("endDate", "dd.MM.yyyy HH:mm:ss");
        reportsTable.setColumnWidth("creditorNameLink", 200);
        reportsTable.setColumnWidth("actualCount", 140);
        reportsTable.setColumnWidth("beginDate", 150);
        reportsTable.setColumnWidth("endDate", 140);
        reportsTable.setColumnWidth("statusLink", 150);
        reportsTable.setStyleName("colored-table");
        reportsTable.setCellStyleGenerator(new Table.CellStyleGenerator() {
            public String getStyle(Object itemId, Object propertyId) {
                ReportDisplayBean item = (ReportDisplayBean) itemId;
                return item.getColor();
            }
        });
        reportsTable.sort(new Object[]{"statusLink"}, new boolean[]{false});
        reportsTable.setWidth("100%");
        
        tableLayout.addComponent(exportToXlsButton);
        tableLayout.setComponentAlignment(exportToXlsButton, Alignment.TOP_RIGHT);
        tableLayout.addComponent(reportsTable);
    }

    public void displayReport(Creditor creditor, Date reportDate) {
        listLayout.setVisible(false);
        reportDateLayout.removeAllComponents();
        final ReportDateLayout reportDateComponent = new ReportDateLayout(provider, environment, creditor, reportDate);
        Button goBackLink = new Button(environment.getResourceString(Localization.RETURN_TO_LIST_BUTTON_CAPTION), new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                creditorsSelect.getSelectedElements(new SelectionCallback<Creditor>() {

                    @Override
                    public void selected(List<Creditor> selectedItems) {
                        displayDataForReportDate(reportDateComponent.getReportDate(), selectedItems);
                    }
                });
                
            }
        });
        goBackLink.setIcon(ApprovalPortletResource.ARROW_LEFT_ICON);
        goBackLink.setStyleName(BaseTheme.BUTTON_LINK);
        reportDateLayout.addComponent(goBackLink);
        reportDateLayout.setComponentAlignment(goBackLink, Alignment.TOP_RIGHT);
        reportDateLayout.addComponent(reportDateComponent);
        reportDateLayout.setVisible(true);
    }

    private byte[] exportToXLS() throws WriteException, IOException {

        WritableFont times12font = new WritableFont(WritableFont.TIMES, 12);
        WritableFont times12fontBold = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD);
        WritableCellFormat times12format = new WritableCellFormat(times12font);
        WritableCellFormat times12formatBold = new WritableCellFormat(times12fontBold);
        CellView cellView = new CellView();
        cellView.setSize(10000);

        times12formatBold.setAlignment(jxl.format.Alignment.CENTRE);
        times12formatBold.setBorder(Border.ALL, BorderLineStyle.THIN);
        times12format.setBorder(Border.ALL, BorderLineStyle.THIN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableWorkbook workbook = Workbook.createWorkbook(baos);
        WritableSheet sheet = workbook.createSheet("Report", 0);

        WritableCellFormat dateFormat = new WritableCellFormat(new DateFormat("dd.MM.yyyy"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        int rowCounter = 0;
        sheet.addCell(new jxl.write.Label(0, rowCounter, environment.getResourceString(Localization.REPORT_HEADER)));
        sheet.mergeCells(0, rowCounter, 5, rowCounter);
        rowCounter++;
        String reportDateHeader = String.format(environment.getResourceString(Localization.REPORT_DATE_HEADER), sdf.format((Date) reportDateField.getValue()));
        sheet.addCell(new jxl.write.Label(0, rowCounter, reportDateHeader));
        sheet.mergeCells(0, rowCounter, 5, rowCounter);
        rowCounter++;

        String[] exportColumnNames = environment.getResourceString(Localization.EXPORT_TABLE_COLUMN_NAMES).split("\\|");
        String[] exportColumnCaptions = environment.getResourceString(Localization.EXPORT_TABLE_COLUMN_CAPTIONS).split("\\|");

        for (int columnIndex = 0; columnIndex < exportColumnCaptions.length; columnIndex++) {
            String columnHeader = exportColumnCaptions[columnIndex];
            sheet.addCell(new Label(columnIndex, rowCounter, columnHeader, times12formatBold));
        }
        rowCounter++;

        for (ReportDisplayBean report : reportsContainer.getItemIds()) {
            for (int columnIndex = 0; columnIndex < exportColumnNames.length; columnIndex++) {
                Property property = reportsContainer.getContainerProperty(report, exportColumnNames[columnIndex]);
                Object value = property.getValue();
                if (value instanceof String) {
                    sheet.addCell(new jxl.write.Label(columnIndex, rowCounter, value.toString(), times12format));

                } else if (value instanceof Number) {
                    jxl.write.Number number = new jxl.write.Number(columnIndex, rowCounter, Integer.parseInt(value.toString()), times12format);
                    sheet.addCell(number);
                } else if (value instanceof Date) {
                    sheet.addCell(new jxl.write.DateTime(columnIndex, rowCounter, (Date) value, dateFormat));
                }
                sheet.setColumnView(columnIndex, cellView);
            }
            rowCounter++;
        }
        workbook.write();
        workbook.close();


        return baos.toByteArray();
    }

    public void downloadXls(String filename) throws WriteException, IOException {
        final byte[] bytes = exportToXLS();
        StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {
            public InputStream getStream() {
                return new ByteArrayInputStream(bytes);
            }
        };
        StreamResource resource = new StreamResource(streamSource, filename, getApplication()) {
            @Override
            public DownloadStream getStream() {
                DownloadStream downloadStream = super.getStream();
                downloadStream.setParameter("Content-Disposition", "attachment;filename=" + getFilename());
                downloadStream.setContentType("application/vnd.ms-excel");
                downloadStream.setCacheTime(0);
                return downloadStream;
            }
        };
        getWindow().open(resource, "_blank");
    }
}
