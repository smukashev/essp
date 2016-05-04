package com.bsbnb.usci.portlets.crosscheck.ui;

import com.bsbnb.usci.portlets.crosscheck.CrossCheckPortletResource;
import com.bsbnb.usci.portlets.crosscheck.PortletEnvironmentFacade;
import com.bsbnb.usci.portlets.crosscheck.data.CrossCheckExecutor;
import com.bsbnb.usci.portlets.crosscheck.data.CrossCheckMessageDisplayWrapper;
import com.bsbnb.usci.portlets.crosscheck.data.DataProvider;
import com.bsbnb.usci.portlets.crosscheck.dm.Creditor;
import com.bsbnb.usci.portlets.crosscheck.dm.CrossCheck;
import com.bsbnb.usci.portlets.crosscheck.dm.SubjectType;
import com.bsbnb.util.translit.Transliterator;
import com.bsbnb.vaadin.filterableselector.FilterableSelect;
import com.bsbnb.vaadin.filterableselector.SelectionCallback;
import com.bsbnb.vaadin.filterableselector.Selector;
import com.bsbnb.vaadin.messagebox.MessageBox;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.*;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import jxl.CellView;
import jxl.WorkbookSettings;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.*;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;


public class CrossCheckLayout extends VerticalLayout {
    private List<Creditor> creditorsList;
    private DataProvider provider;
    private PortletEnvironmentFacade facade;

    private FilterableSelect<Creditor> creditorsSelector;
    private DateField dateField;
    private Button showButton;
    private Button crossCheckButton;
    private HorizontalLayout buttonsLayout;
    private Button exportFileInfoTableButton;
    private Label crossCheckHeaderLabel;
    private HorizontalLayout crossCheckHeaderLayout;
    private BeanItemContainer<CrossCheck> crossCheckContainer;
    private FormattedTable crossCheckTable;
    private VerticalLayout crossCheckLayout;
    private Button exportMessagesTableButton;
    private Label messagesHeaderLabel;
    private HorizontalLayout messagesHeaderLayout;
    private BeanItemContainer<CrossCheckMessageDisplayWrapper> messagesContainer;
    private FormattedTable messagesTable;
    private VerticalLayout messagesLayout;
    private VerticalLayout updatedLayout;
    private CrossCheck lastCrossCheck;
    private ProgressIndicator indicator;
    private Label helpMessage;
    private String viewType;
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private final Logger logger = Logger.getLogger(CrossCheckLayout.class);

    public CrossCheckLayout(String viewType, PortletEnvironmentFacade facade, DataProvider provider) {
        this.viewType = viewType;
        this.facade = facade;
        this.provider = provider;
        logger.info("User ID: "+ facade.getUserID());
    }
   
    @Override
    public void attach() {
        creditorsList = provider.getCreditorsList(facade.getCreditorId());
        creditorsSelector = new FilterableSelect<Creditor>(creditorsList, new Selector<Creditor>() {

            private boolean isKz = "KZ".equalsIgnoreCase(facade.getCurrentLanguage());

            public String getCaption(Creditor item) {
                return item.getName();
            }

            public Object getValue(Creditor item) {
                return item.getId();
            }

            public String getType(Creditor item) {
                SubjectType subjectType = item.getSubjectType();
                if (subjectType == null) {
                    return null;
                } else {
                    return isKz ? subjectType.getNameKz() : subjectType.getNameRu();
                }
            }
        });

        creditorsSelector.setImmediate(true);

        if (!creditorsSelector.hasElements()) {
            addComponent(creditorsSelector);
            return;
        }

        //dateField
        dateField = new DateField(Localization.DATE_FIELD_CAPTION.getValue());

        if (creditorsList.size() != 1) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            dateField.setValue(calendar.getTime());
        } else {
            dateField.setValue(provider.getCreditorsReportDate(creditorsList.get(0)));
        }

        dateField.setDateFormat("dd.MM.yyyy");
        dateField.setWidth(200, UNITS_PIXELS);

        //showButton
        showButton = new Button(Localization.SHOW_BUTTON_CAPTION.getValue(), new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                showCrossCheck();
            }
        });

        //crossCheckButton
        crossCheckButton = new Button(Localization.RUN_CROSS_CHECK_BUTTON_CAPTION.getValue(),
                new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                buttonsLayout.setEnabled(false);
                crossCheck();
            }
        });

        if ("DEVELOPMENT".equals(viewType))
            crossCheckButton.setEnabled(false);

        crossCheckButton.setImmediate(true);

        Resource excelIcon = new CrossCheckPortletResource("excel.png");
        Button batchExportToExcel = new Button(Localization.BATCH_EXPORT_TO_EXCEL.getValue(),
                new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                creditorsSelector.getSelectedElements(new SelectionCallback<Creditor>() {
                    public void selected(List<Creditor> selectedItems) {
                        exportCrossCheckToExcel(selectedItems);
                     }
                });
            }
        });

        batchExportToExcel.setIcon(excelIcon);

        //buttonsLayout
        buttonsLayout = new HorizontalLayout();
        buttonsLayout.addComponent(showButton);
        buttonsLayout.addComponent(crossCheckButton);

        if (creditorsList.size() > 1)
            buttonsLayout.addComponent(batchExportToExcel);

        String businessRulesUrl = facade.getBusinessRulesUrl();
        if (businessRulesUrl != null && !businessRulesUrl.isEmpty()) {
            Link businessRulesLink = new Link(Localization.BUSINESS_RULES.getValue(),
                    new ExternalResource(businessRulesUrl));

            businessRulesLink.setIcon(excelIcon);
            buttonsLayout.addComponent(businessRulesLink);
        }

        buttonsLayout.setSpacing(true);
        //fileInfoHeaderLabel
        crossCheckHeaderLabel = new Label("<h3>" + Localization.INFO_FILE_TABLE_CAPTION.getValue() + "</h3>",
                Label.CONTENT_XHTML);
        //exportFileInfoTableButton
        exportFileInfoTableButton = new Button(Localization.EXPORT_TABLE_TO_XLS.getValue(), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                try {
                    byte[] exportBytes = crossCheckTable.exportToXLS(Localization.CROSS_CHECK_EXCEL_CAPTION.getValue(),
                            Localization.CROSS_CHECK_TABLE_COLUMNS.getValue().split(","));
                    downloadXLS(exportBytes, "crosschecks.xls");
                } catch (WriteException ex) {
                    logger.warn(null, ex);
                } catch (IOException ex) {
                    logger.warn(null, ex);
                }
            }
        });

        exportFileInfoTableButton.setImmediate(true);
        exportFileInfoTableButton.setIcon(new CrossCheckPortletResource("excel.png"));
        
        //fileInfoHeaderLayout
        crossCheckHeaderLayout = new HorizontalLayout();
        crossCheckHeaderLayout.addComponent(crossCheckHeaderLabel);
        crossCheckHeaderLayout.addComponent(exportFileInfoTableButton);
        crossCheckHeaderLayout.setComponentAlignment(crossCheckHeaderLabel, Alignment.MIDDLE_LEFT);
        crossCheckHeaderLayout.setComponentAlignment(exportFileInfoTableButton, Alignment.MIDDLE_RIGHT);
        crossCheckHeaderLayout.setWidth("100%");
        
        //fileInfoTable
        final String[] crossCheckColumns = Localization.CROSS_CHECK_TABLE_COLUMNS.getValue().split(",");
        final String[] crossCheckHeaders = Localization.CROSS_CHECK_TABLE_HEADERS.getValue().split(",");

        crossCheckTable = new FormattedTable("");
        crossCheckTable.setImmediate(true);
        crossCheckTable.setWidth("100%");
        crossCheckTable.setSelectable(true);
        crossCheckContainer = new BeanItemContainer<CrossCheck>(CrossCheck.class);
        crossCheckTable.setContainerDataSource(crossCheckContainer);
        crossCheckTable.setVisibleColumns(crossCheckColumns);
        crossCheckTable.setColumnHeaders(crossCheckHeaders);
        crossCheckTable.addFormat("dateBegin", "dd.MM.yyyy HH:mm:ss");
        crossCheckTable.addFormat("dateEnd", "dd.MM.yyyy HH:mm:ss");
        crossCheckTable.addFormat("reportDate", "dd.MM.yyyy");
        crossCheckTable.sort(new Object[]{"dateBegin", "creditor.name"}, new boolean[]{false, true});

        crossCheckTable.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                if (event != null && event.getProperty() != null && event.getProperty().getValue() != null) {
                    updatePackageErrorsTable((CrossCheck) event.getProperty().getValue());
                }
              }
        });
        //fileInfoLayout
        crossCheckLayout = new VerticalLayout();
        crossCheckLayout.setSpacing(false);
        crossCheckLayout.addComponent(crossCheckHeaderLayout);
        crossCheckLayout.addComponent(crossCheckTable);
        crossCheckLayout.setVisible(false);
        //packageErrorsHeaderLabel
        messagesHeaderLabel = new Label("<h3>" + Localization.PACKAGE_ERRORS_TABLE_CAPTION.getValue()
                + "</h3>", Label.CONTENT_XHTML);
        //exportPackageErrorsTableButton
        exportMessagesTableButton = new Button(Localization.EXPORT_TABLE_TO_XLS.getValue(),
                new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                 if (lastCrossCheck != null) {
                    exportToExcel(Arrays.asList(lastCrossCheck));
                }
            }
        });

        exportMessagesTableButton.setIcon(excelIcon);

        //packageErrorsHeaderLayout
        messagesHeaderLayout = new HorizontalLayout();
        messagesHeaderLayout.addComponent(messagesHeaderLabel);
        messagesHeaderLayout.addComponent(exportMessagesTableButton);
        messagesHeaderLayout.setComponentAlignment(messagesHeaderLabel, Alignment.MIDDLE_LEFT);
        messagesHeaderLayout.setComponentAlignment(exportMessagesTableButton, Alignment.MIDDLE_RIGHT);

        messagesHeaderLayout.setWidth("100%");
        //packageErrorsTable
        messagesTable = new FormattedTable("");
        messagesContainer = new BeanItemContainer<CrossCheckMessageDisplayWrapper>(CrossCheckMessageDisplayWrapper.class);
        messagesTable.setContainerDataSource(messagesContainer);
        messagesTable.setVisibleColumns(Localization.CROSS_CHECK_MESSAGE_TABLE_COLUMNS.getValue().split(","));
        messagesTable.setStyleName("colored-table");
        messagesTable.setCellStyleGenerator(new Table.CellStyleGenerator() {
            @Override
            public String getStyle(Object itemId, Object propertyId) {
                if (itemId instanceof CrossCheckMessageDisplayWrapper) {
                    CrossCheckMessageDisplayWrapper ccm = (CrossCheckMessageDisplayWrapper) itemId;
                    if (ccm.getIsError() != 0) {
                        return "red";
                    } else {
                        return "lightgreen";
                    }
                } else {
                    return null;
                }
            }
        });

        messagesTable.setColumnHeaders(Localization.CROSS_CHECK_MESSAGE_TABLE_HEADERS.getValue().split(","));
        messagesTable.setImmediate(true);
        messagesTable.setColumnWidth("description", 200);
        messagesTable.setWidth("100%");
        messagesTable.setSelectable(true);

        for (String propertyID : Localization.CROSS_CHECK_MESSAGE_TABLE_COLUMNS.getValue().split(",")) {
            float ratio = 1.0f;

            if ("description".equals(propertyID))
                ratio = 2.0f;

            messagesTable.setColumnExpandRatio(propertyID, ratio);
        }

        messagesTable.addFormat("description", "accountNumber");
        messagesTable.addFormat("innerValue", "groupedNumber");
        messagesTable.addFormat("outerValue", "groupedNumber");
        messagesTable.addFormat("difference", "groupedNumber");
        messagesTable.setColumnAlignment("innerValue", Table.ALIGN_RIGHT);
        messagesTable.setColumnAlignment("outerValue", Table.ALIGN_RIGHT);
        messagesTable.setColumnAlignment("difference", Table.ALIGN_RIGHT);
        messagesTable.addListener(new Property.ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                if (event != null && event.getProperty() != null && event.getProperty().getValue() != null) {

                    CrossCheckMessageDisplayWrapper message =
                            (CrossCheckMessageDisplayWrapper) event.getProperty().getValue();

                    if (helpMessage != null && updatedLayout.getComponentIndex(helpMessage) >= 0)
                        updatedLayout.removeComponent(helpMessage);

                    if (message.getHelp() != null && message.getHelp().length() > 0) {
                        helpMessage = new Label(message.getHelp(), Label.CONTENT_XHTML);
                        updatedLayout.addComponent(helpMessage);

                    }
                }
            }
        });
        //packageErrorsLayout
        messagesLayout = new VerticalLayout();
        messagesLayout.setSpacing(false);
        messagesLayout.addComponent(messagesHeaderLayout);
        messagesLayout.addComponent(messagesTable);
        messagesLayout.setVisible(false);
        //updatedLayout
        updatedLayout = new VerticalLayout();
        updatedLayout.setSpacing(true);
        updatedLayout.addComponent(crossCheckLayout);
        updatedLayout.addComponent(messagesLayout);
        //MainLayout
        setSpacing(true);

        if ("DEVELOPMENT".equals(viewType)) {
            Label developmentMessageLabel = new Label(
                    Localization.DEVELOPMENT_MESSAGE.getValue(), Label.CONTENT_XHTML);

            addComponent(developmentMessageLabel);
        }
        addComponent(creditorsSelector);
        addComponent(dateField);
        addComponent(buttonsLayout);
        addComponent(updatedLayout);

        final UriFragmentUtility ufu = new UriFragmentUtility();

        ufu.addListener(new UriFragmentUtility.FragmentChangedListener() {
            public void fragmentChanged(FragmentChangedEvent source) {
                try {
                    Date reportDate = null;
                    BigInteger creditorId = null;
                    String creditorIdMatcher = "[0-9]+";
                    String fragment = source.getUriFragmentUtility().getFragment();
                    String[] tokens = fragment.split("/");

                    for (String token : tokens) {
                        logger.info("Token: "+ token);

                        if (token.matches(creditorIdMatcher)) {
                            try {
                                creditorId = BigInteger.valueOf(Long.parseLong(token));
                                logger.info("Got creditor id: "+ creditorId);
                            } catch (NumberFormatException nfe) {

                            }
                        } else {
                            try {
                                reportDate = DEFAULT_DATE_FORMAT.parse(token);
                                logger.info("Got report date: "+ reportDate);
                            } catch (ParseException pe) {
                            }
                        }
                    }
                    logger.info("Creditor id from fragment: "+ creditorId);
                    logger.info("Report date: "+ reportDate);

                    if (creditorId != null && reportDate != null && creditorsSelector.containsElement(creditorId)) {
                        logger.info("Selecting");
                        creditorsSelector.selectElements(new Object[]{creditorId});
                        dateField.setValue(reportDate);
                        showCrossCheck();
                    }
                } catch (Exception e) {
                    logger.error("Exception in uri handling", e);
                }
            }
        });

        addComponent(ufu);
    }

    private void exportCrossCheckToExcel(List<Creditor> selectedItems) {
        Date reportDate = (Date) dateField.getValue();
        
        if (reportDate == null)
            MessageBox.Show(Localization.EMPTY_DATE_FIELD.getValue(), getWindow());

        List<CrossCheck> crossChecks = provider.getCrossChecks(selectedItems.toArray(new Creditor[0]),
                (Date) dateField.getValue());

        logger.info("SIZE: " + crossChecks.size());

        Set<String> creditorNames = new HashSet<String>();
        List<CrossCheck> uniqueCreditorsCrossChecks = new ArrayList<CrossCheck>();

        for (CrossCheck crossCheck : crossChecks) {
            if (!creditorNames.contains(crossCheck.getCreditor().getName())) {
                creditorNames.add(crossCheck.getCreditor().getName());
                uniqueCreditorsCrossChecks.add(crossCheck);
            }
        }

        logger.info("UNIQUE SIZE: " + crossChecks.size());

        exportToExcel(uniqueCreditorsCrossChecks);
    }

    protected List<CrossCheck> loadCrossChecks(Creditor[] creditors, Date date) throws UIException {
        if (date == null)
            throw new UIException(Localization.MESSAGE_DATE_NOT_SELECTED.getValue());

        if (creditors.length == 0)
            return new ArrayList<CrossCheck>();

        List<CrossCheck> crossCheckList = provider.getCrossChecks(creditors, date);

        if (crossCheckList.isEmpty())
            throw new UIException(Localization.MESSAGE_NO_DATA_FOUND.getValue());

        return crossCheckList;
    }

    private String getFilenameXLS(List<CrossCheck> uniqueCreditorsCrossChecks) {
        StringBuilder filename;
   
        Date reportDate = uniqueCreditorsCrossChecks.isEmpty() ? null : uniqueCreditorsCrossChecks.get(0).getReportDate();

        if (uniqueCreditorsCrossChecks.size() != 1) {
            filename = new StringBuilder(Localization.XLS_BATCH_MESSAGES_EXPORT_FILENAME_PREFIX.getValue());
        } else {
            filename = new StringBuilder(Localization.XLS_MESSAGES_EXPORT_FILENAME_PREFIX.getValue());
            CrossCheck crossCheck = uniqueCreditorsCrossChecks.get(0);
            filename.append(Transliterator.transliterate(crossCheck.getCreditor().getName()));
        }

        if (filename.length() > 25) {
            filename.setLength(25);
        }

        if (reportDate != null) {
            filename.append("[");
            filename.append(DEFAULT_DATE_FORMAT.format(reportDate));
            filename.append("]");
        }

        filename.append(".xls");

        return filename.toString();
    }

    private void showCrossCheck() {
        if (helpMessage != null && updatedLayout.getComponentIndex(helpMessage) >= 0) {
            updatedLayout.removeComponent(helpMessage);
        }

        crossCheckLayout.setVisible(false);
        messagesLayout.setVisible(false);

        creditorsSelector.getSelectedElements(new SelectionCallback<Creditor>() {
            public void selected(List<Creditor> creditors) {
                loadCrossCheck(creditors.toArray(new Creditor[0]));
            }
        });
    }

    private void loadCrossCheck(Creditor[] selectedCreditors) {
        List<CrossCheck> crossCheckList;

        try {
            crossCheckList = loadCrossChecks(selectedCreditors, (Date) dateField.getValue());
        } catch (UIException ex) {
            MessageBox.Show(ex.getMessage(), getWindow());
            return;
        }

        updateCrossCheckTable(crossCheckList);
    }

    private void updateCrossCheckTable(List<CrossCheck> crossCheckList) {
        crossCheckContainer.removeAllItems();

        if (crossCheckList == null || crossCheckList.isEmpty())
            return;

        crossCheckContainer.addAll(crossCheckList);
        crossCheckTable.sort(new Object[]{"dateBegin", "creditorName"}, new boolean[]{false, true});
        crossCheckTable.select(crossCheckTable.firstItemId());
        crossCheckLayout.setVisible(true);

        messagesLayout.setVisible(true);
    }

    private void updatePackageErrorsTable(CrossCheck crossCheck) throws IllegalArgumentException {
        lastCrossCheck = crossCheck;
        messagesLayout.setVisible(false);
        messagesContainer.removeAllItems();
        messagesContainer.addAll(provider.getMessages(crossCheck));
        messagesLayout.setVisible(true);
    }

    private void crossCheck() {
        if (dateField.getValue() == null) {
            MessageBox.Show(Localization.MESSAGE_DATE_NOT_SELECTED.getValue(), getWindow());
            return;
        }

        final Date reportDate = (Date) dateField.getValue();
        creditorsSelector.getSelectedElements(new SelectionCallback<Creditor>() {
            public void selected(final List<Creditor> creditors) {
                updatedLayout.setVisible(false);

                indicator = new ProgressIndicator();
                indicator.setIndeterminate(true);
                indicator.setPollingInterval(500);

                addComponent(indicator);

                Thread t = new Thread(new Runnable() {

                    public void run() {
                        runCrossCheck(creditors.toArray(new Creditor[0]), reportDate);
                        updatedLayout.setVisible(true);
                        showCrossCheck();
                        buttonsLayout.setEnabled(true);
                        removeComponent(indicator);
                    }
                });

                t.start();
            }
        });
    }

    private void runCrossCheck(Creditor[] selectedCreditors, Date reportDate) {
        try {
            CrossCheckExecutor.get().crossCheck(facade.getUserID(), selectedCreditors, reportDate);
            showCrossCheck();
        } catch (SQLException sqle) {
            logger.error("Cross check failed", sqle);
            String message = String.format(Localization.MESSAGE_CROSS_CHECK_FAILED.getValue(), sqle.getMessage());
            MessageBox.Show(message, getWindow());
        }
    }

    private void downloadXLS(final byte[] bytes, String filename) {
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

        (getWindow()).open(resource);
    }

    private WritableFont times12Font;
    private WritableCellFormat headerFormat;
    private WritableCellFormat times12FormatBold;
    private WritableCellFormat times12FormatRed;
    private WritableCellFormat times12FormatGreen;
    private WritableCellFormat groupedCellFormatGreen;
    private WritableCellFormat groupedCellFormatRed;
    private CellView autoSizeCellView;

    private void exportToExcel(List<CrossCheck> uniqueCreditorsCrossChecks) {
        try {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            WorkbookSettings settings = new WorkbookSettings();
            //settings.setUseTemporaryFileDuringWrite(true); NEW
            times12Font = new jxl.write.WritableFont(jxl.write.WritableFont.TIMES, 12);
            WritableFont times12Bold = new jxl.write.WritableFont(jxl.write.WritableFont.TIMES, 12,
                    jxl.write.WritableFont.BOLD);

            headerFormat = new WritableCellFormat(times12Bold);
            times12FormatBold = new WritableCellFormat(times12Bold);
            times12FormatBold.setAlignment(jxl.format.Alignment.CENTRE);
            times12FormatBold.setBorder(Border.ALL, BorderLineStyle.THIN);
            times12FormatGreen = new jxl.write.WritableCellFormat(times12Font);
            times12FormatGreen.setBackground(Colour.LIGHT_GREEN);
            times12FormatGreen.setBorder(Border.ALL, BorderLineStyle.THIN);
            times12FormatRed = new WritableCellFormat(times12Font);
            times12FormatRed.setBackground(Colour.RED);
            times12FormatRed.setBorder(Border.ALL, BorderLineStyle.THIN);

            NumberFormat groupedNumberFormat = new NumberFormat("### ### ### ##0");

            groupedCellFormatGreen = new WritableCellFormat(times12Font, groupedNumberFormat);
            groupedCellFormatGreen.setBackground(Colour.LIGHT_GREEN);
            groupedCellFormatGreen.setBorder(Border.ALL, BorderLineStyle.THIN);
            groupedCellFormatRed = new WritableCellFormat(times12Font, groupedNumberFormat);
            groupedCellFormatRed.setBackground(Colour.RED);
            groupedCellFormatRed.setBorder(Border.ALL, BorderLineStyle.THIN);
            autoSizeCellView = new jxl.CellView();
            autoSizeCellView.setAutosize(true);

            WritableWorkbook workbook = jxl.Workbook.createWorkbook(baos, settings);
            for (CrossCheck crossCheck : uniqueCreditorsCrossChecks)
                writeCrossCheckOnSheet(crossCheck, workbook);

            workbook.write();
            workbook.close();
            downloadXLS(baos.toByteArray(), getFilenameXLS(uniqueCreditorsCrossChecks));
        } catch (IOException ioe) {
            logger.warn("Exception on cross check loading", ioe);
            MessageBox.Show(Localization.MESSAGE_FAILED_TO_LOAD_CROSS_CHECK.getValue(), getWindow());
        } catch (WriteException we) {
            logger.warn("Exception occured while writing cross check", we);
            MessageBox.Show(Localization.MESSAGE_FAILED_TO_LOAD_CROSS_CHECK.getValue(), getWindow());
        }
    }

    private WritableSheet writeCrossCheckOnSheet(CrossCheck crossCheck, WritableWorkbook workbook)
            throws WriteException {

        WritableSheet sheet = workbook.createSheet(crossCheck.getCreditor().getName(), 0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

        int rowCounter = 1;

        sheet.addCell(new jxl.write.Label(1, rowCounter++, String.format(
                Localization.EXCEL_HEADER_ORGANIZATION.getValue(),
                crossCheck.getCreditor().getName()), headerFormat));

        sheet.addCell(new jxl.write.Label(1, rowCounter++, String.format(
                Localization.EXCEL_HEADER_REPORT_DATE.getValue(),
                sdf.format(crossCheck.getReportDate())), headerFormat));

        String[] columnNames = Localization.CROSS_CHECK_MESSAGE_TABLE_HEADERS.getValue().split(",");

        for (int columnIndex = 0; columnIndex < columnNames.length; columnIndex++)
            sheet.addCell(new jxl.write.Label(columnIndex, rowCounter,
                    columnNames[columnIndex], times12FormatBold));

        rowCounter++;

        for (CrossCheckMessageDisplayWrapper message : provider.getMessages(crossCheck)) {
            WritableCellFormat usedGroupedCellFormat = message.getIsError() == 0 ?
                    groupedCellFormatGreen : groupedCellFormatRed;

            WritableCellFormat usedCellFormat = message.getIsError() == 0 ?
                    times12FormatGreen : times12FormatRed;

            String description = message.getDescription();

            if (description.length() == 7)
                description = description.length() == 7 ? description.substring(0, 4) + " "
                        + description.substring(4) : description;

            sheet.addCell(new jxl.write.Label(0, rowCounter, description, usedCellFormat));
            addNumberCell(sheet, 1, rowCounter, message.getInnerValue(), usedGroupedCellFormat, usedCellFormat);
            addNumberCell(sheet, 2, rowCounter, message.getOuterValue(), usedGroupedCellFormat, usedCellFormat);
            addNumberCell(sheet, 3, rowCounter, message.getDifference(), usedGroupedCellFormat, usedCellFormat);

            rowCounter++;

            for (int i = 0; i < 4; i++)
                sheet.setColumnView(i, autoSizeCellView);
        }

        return sheet;
    }

    private void addNumberCell(WritableSheet sheet, int columnNumber, int rowNumber, String value,
                               WritableCellFormat numberFormat, WritableCellFormat textFormat) throws WriteException {
        Long longValue = null;

        try {
            longValue = Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            logger.warn(nfe.getMessage());
        }

        if (longValue != null)
            sheet.addCell(new jxl.write.Number(columnNumber, rowNumber, longValue, numberFormat));
        else
            sheet.addCell(new jxl.write.Label(columnNumber, rowNumber, value, textFormat));

    }
}