package kz.bsbnb.usci.portlet.report.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import jxl.CellView;
import jxl.Range;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.CellFormat;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.portlet.report.Localization;
import static kz.bsbnb.usci.portlet.report.ReportApplication.log;
import static kz.bsbnb.usci.portlet.report.ReportApplication.setStartTime;
import static kz.bsbnb.usci.portlet.report.ReportApplication.logTime;
import kz.bsbnb.usci.portlet.report.ReportPortletResource;
import kz.bsbnb.usci.portlet.report.ui.ConstantValues;
import com.bsbnb.util.translit.Transliterator;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TemplatedPagedXlsReportExporter extends AbstractReportExporter {

    private Integer headerBottom;
    private String templateFilePath;
    //TODO: Определять количество записей перед началом выгрузки
    private Button exportButton;
    protected VerticalLayout showProgressLayout;
    private ProgressIndicator progressIndicator;
    protected List<FileDownloadComponent> downloadComponents = Collections.synchronizedList(new ArrayList<FileDownloadComponent>());
    //private List<ReportLoadFile> filesToDownload = new ArrayList<ReportLoadFile>();

    @Override
    public List<Component> getActionComponents() {
        String reportFolderPath = StaticRouter.getReportFilesCatalog() + getTargetReportComponent().getReport().getName() + File.separator;
        templateFilePath = reportFolderPath + "template.xls";
        loadConfig(reportFolderPath + "export.properties");
        exportButton = new Button(Localization.DOWNLOAD_XLS_BUTTON_CAPTION.getValue(), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                launchExport();
            }
        });
        exportButton.setIcon(ReportPortletResource.EXCEL_ICON);
        exportButton.setImmediate(true);
        showProgressLayout = new VerticalLayout();
        showProgressLayout.setSpacing(true);
        showProgressLayout.setImmediate(true);
        showProgressLayout.addListener(new ComponentAttachListener() {

            public void componentAttachedToContainer(ComponentAttachEvent event) {
                VerticalLayout layout = (VerticalLayout) event.getContainer();
                layout.setComponentAlignment(event.getAttachedComponent(), com.vaadin.ui.Alignment.TOP_CENTER);
            }
        });
        showProgressLayout.setWidth("100%");
        VerticalLayout progressIndicatorLayout = new VerticalLayout();
        progressIndicator = new ProgressIndicator();
        progressIndicator.setIndeterminate(true);
        progressIndicator.setImmediate(true);
        progressIndicator.setVisible(false);
        progressIndicatorLayout.addComponent(progressIndicator);
        progressIndicatorLayout.setComponentAlignment(progressIndicator, com.vaadin.ui.Alignment.MIDDLE_CENTER);
        progressIndicatorLayout.setWidth("100%");
        return Arrays.asList(new Component[]{exportButton, showProgressLayout, progressIndicatorLayout});
    }

    private void launchExport() {
        loadStarted();

        showProgressLayout.removeAllComponents();
        exportButton.setEnabled(false);
        exportButton.setImmediate(true);
        com.vaadin.ui.Label label = new com.vaadin.ui.Label(Localization.REPORT_BUILD_STARTED.getValue(), com.vaadin.ui.Label.CONTENT_XHTML);
        showProgressLayout.addComponent(label);
        progressIndicator = new ProgressIndicator();
        progressIndicator.setImmediate(true);
        progressIndicator.setPollingInterval(100);
        progressIndicator.setValue(0.0);
        progressIndicator.addListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
            }
        });
        showProgressLayout.addComponent(progressIndicator);
        Runnable backgroundExport = new Runnable() {

            public void run() {
                ResultSet dataSource = null;
                try {
                    String exportFilenamePrefix = getFilenamePrefix();
                    int sheetNumber = 0;
                    boolean hasRecordsToLoad;
                    do {
                        sheetNumber++;
                        hasRecordsToLoad = generatePage(exportFilenamePrefix, sheetNumber);
                        for (FileDownloadComponent downloadComponent : new ArrayList<FileDownloadComponent>(downloadComponents)) {
                            showProgressLayout.addComponent(downloadComponent);
                        }
                        downloadComponents.clear();
                    } while (hasRecordsToLoad);

                    com.vaadin.ui.Label finishedLabel = new com.vaadin.ui.Label(Localization.REPORT_BUILD_FINISHED.getValue(), com.vaadin.ui.Label.CONTENT_XHTML);
                    showProgressLayout.addComponent(finishedLabel);
                    List<String> allFileNames = new ArrayList<String>(showProgressLayout.getComponentCount());
                    List<File> allFiles = new ArrayList<File>(showProgressLayout.getComponentCount());
                    Iterator<Component> componentIterator = showProgressLayout.getComponentIterator();
                    while (componentIterator.hasNext()) {
                        Component component = componentIterator.next();
                        if (component instanceof FileDownloadComponent) {
                            FileDownloadComponent fdc = (FileDownloadComponent) component;
                            allFileNames.addAll(fdc.getFilenames());
                            allFiles.addAll(fdc.getFiles());
                        }
                    }

                    if (allFileNames.size() > 1) {
                        FileDownloadComponent downloadAllComponent = new FileDownloadComponent(exportFilenamePrefix + ".zip");
                        downloadAllComponent.setCaption(Localization.LOAD_RESULTS.getValue());
                        downloadAllComponent.addAllFiles(allFileNames, allFiles);
                        showProgressLayout.addComponent(downloadAllComponent);
                        downloadAllComponent.zipFile();
                    }

                    for (File file : allFiles) {
                        try {
                            if (!file.delete()) {
                                log.log(Level.WARNING, "Failed to delete temporary xls file: {0}", file.getAbsolutePath());
                            }
                        } catch (Exception e) {
                            log.log(Level.WARNING, "Failed to delete temporary xls file with exception", e);
                        }
                    }

                } catch (WriteException we) {
                    showProgressLayout.addComponent(new com.vaadin.ui.Label(Localization.EXPORT_FAILED.getValue(), com.vaadin.ui.Label.CONTENT_XHTML));
                    log.log(Level.SEVERE, "Export error", we);
                } catch (IOException ioe) {
                    showProgressLayout.addComponent(new com.vaadin.ui.Label(Localization.EXPORT_FAILED.getValue(), com.vaadin.ui.Label.CONTENT_XHTML));
                    log.log(Level.SEVERE, "Export error", ioe);
                } catch (SQLException sqle) {
                    showProgressLayout.addComponent(new com.vaadin.ui.Label(Localization.EXPORT_FAILED.getValue(), com.vaadin.ui.Label.CONTENT_XHTML));
                    log.log(Level.SEVERE, "Export error", sqle);
                } catch (Exception e) {
                    showProgressLayout.addComponent(new com.vaadin.ui.Label(Localization.EXPORT_FAILED.getValue(), com.vaadin.ui.Label.CONTENT_XHTML));
                    log.log(Level.SEVERE, "Export error", e);
                } finally {
                    if (dataSource != null) {
                        try {
                            dataSource.close();
                        } catch (SQLException sqle) {
                        }
                    }
                }
                loadFinished();
                progressIndicator.setVisible(false);
                exportButton.setEnabled(true);
            }
        };
        Thread thread = new Thread(backgroundExport);
        thread.setDaemon(true);
        thread.start();
    }

    private String getFilenamePrefix() {
        StringBuilder exportFilenamePrefix = new StringBuilder(getTargetReportComponent().getReport().getName());
        List<String> parametersStrings = getTargetReportComponent().getParameterCaptions();
        for (String parameterString : parametersStrings) {
            exportFilenamePrefix.append(Transliterator.transliterate(parameterString));
        }
        if (exportFilenamePrefix.length() >= 30) {
            return exportFilenamePrefix.substring(0, 30);
        }
        return exportFilenamePrefix.toString();
    }

    protected boolean generatePage(String exportFilePrefix, int sheetNumber) throws WriteException, IOException, SQLException {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setUseTemporaryFileDuringWrite(true);
        settings.setRationalization(false);
        settings.setMergedCellChecking(false);
        WritableFont times12Font = new WritableFont(WritableFont.TIMES, 12);
        WritableCellFormat times12Format = new WritableCellFormat(times12Font);
        WritableCellFormat numberFormat = new WritableCellFormat(new NumberFormat("# ### ##0"));

        numberFormat.setFont(times12Font);
        WritableCellFormat dateFormat = new WritableCellFormat(jxl.write.DateFormats.DEFAULT);
        dateFormat.setFont(times12Font);
        try {
            times12Format.setBorder(Border.ALL, BorderLineStyle.THIN);
            times12Format.setWrap(true);
            times12Format.setVerticalAlignment(VerticalAlignment.TOP);
            numberFormat.setShrinkToFit(true);
            numberFormat.setVerticalAlignment(VerticalAlignment.TOP);
            numberFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
            dateFormat.setShrinkToFit(true);
            dateFormat.setVerticalAlignment(VerticalAlignment.TOP);
            dateFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        } catch (WriteException we) {
            log.log(Level.INFO, "Error setting formats", we);
        }
        CellView autosizeCellView = new CellView();
        autosizeCellView.setAutosize(true);
        WritableWorkbook workbook = null;
        FileOutputStream fos = null;
        try {
            File xlsFile = File.createTempFile("Records", ".xls", REPORT_FILES_FOLDER);
            fos = new FileOutputStream(xlsFile);
            workbook = Workbook.createWorkbook(fos, settings);
            WritableSheet currentSheet = workbook.createSheet("Report", sheetNumber);
            setStartTime();
            logTime("Before header write");
            int rowIndex = writeHeaderToSheet(currentSheet) + 1;
            logTime("After header write");
            int recordsBySheet = getRecordsBySheet();
            int firstRecordNumber = (sheetNumber - 1) * recordsBySheet + 1;
            int lastRecordNumber = sheetNumber * recordsBySheet;
            int recordCounter = firstRecordNumber;
            logTime("Before query");
            ResultSet dataSource = getTargetReportComponent().getResultSet(firstRecordNumber, lastRecordNumber);
            logTime("After query");
            ResultSetMetaData rsmd = dataSource.getMetaData();
            while (dataSource.next()) {
                for (int columnIndex = 1; columnIndex <= rsmd.getColumnCount(); columnIndex++) {
                    int columnNumber = columnIndex;
                    if (rsmd.getColumnType(columnIndex) == Types.NUMERIC) {
                        currentSheet.addCell(new jxl.write.Number(columnNumber, rowIndex, dataSource.getDouble(columnIndex), numberFormat));
                    } else if (rsmd.getColumnType(columnIndex) == Types.TIMESTAMP && dataSource.getDate(columnIndex) != null) {
                        currentSheet.addCell(new jxl.write.DateTime(columnNumber, rowIndex, dataSource.getDate(columnIndex), dateFormat));
                    } else {
                        Object value = dataSource.getObject(columnIndex);
                        if (value == null) {
                            value = "";
                        }
                        currentSheet.addCell(new Label(columnNumber, rowIndex, value.toString(), times12Format));
                    }
                }
                rowIndex++;
                recordCounter++;
            }
            workbook.write();
            workbook.close();
            logTime("After xls file write");
            recordCounter--;
            log.log(Level.INFO, "Checked record counter: {0}", recordCounter);
            if (recordCounter > 0) {
                String cleanFilename = String.format("%s%d-%d.xls", exportFilePrefix, firstRecordNumber, recordCounter);
                log.log(Level.INFO, "Clean file name: {0}",cleanFilename);
                log.log(Level.INFO, "XLS file path: {0}", xlsFile.getAbsolutePath());
                FileDownloadComponent downloadComponent = new FileDownloadComponent(cleanFilename.replace(".xls", ".zip"), cleanFilename, xlsFile);
                downloadComponent.setCaption(String.format(Localization.LOAD_RECORDS_FROM_TO.getValue(), firstRecordNumber, recordCounter));
                downloadComponent.setImmediate(true);
                downloadComponent.zipFile();
                addFileToLoad(downloadComponent.getLoadedFile());
                downloadComponents.add(downloadComponent);
            } else {
                showProgressLayout.addComponent(new com.vaadin.ui.Label(Localization.NO_RECORDS.getValue(), com.vaadin.ui.Label.CONTENT_XHTML));
            }
            return recordCounter >= lastRecordNumber;
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to close workbook", e);
                }
            }
            
            if(fos!=null) {
                try {
                fos.close();
                } catch(Exception e) {
                    log.log(Level.WARNING, "Failed to close file stream", e);
                }
            }
        }
    }
    /*
     * Загружает настройки выгрузки из каталога отчета
     * Текущие настройки:
     * Координаты заголовка отчета в файле шаблона xls: header-top, header-bottom, header-left, header-right
     */

    private void loadConfig(String configFilePath) {
        log.log(Level.INFO, "Config file path: {0}", configFilePath);
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            return;
        }
        log.log(Level.INFO, "Config file exists");
        FileInputStream configFileStream = null;
        try {
            configFileStream = new FileInputStream(configFile);
            PropertyResourceBundle configuration = new PropertyResourceBundle(configFileStream);
            headerBottom = getIntegerFromConfiguration(configuration, "header-bottom");
        } catch (IOException ioe) {
            log.log(Level.WARNING, "Error reading configuration file", ioe);
        } finally {
            if (configFileStream != null) {
                try {
                    configFileStream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private boolean isConfigurationValid() {
        return headerBottom != null;
    }

    protected int getRecordsBySheet() {
        return 50000;
    }

    private Integer getIntegerFromConfiguration(PropertyResourceBundle configuration, String key) {
        String value = configuration.getString(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            log.log(Level.WARNING, "Failed to parse integer from config: {0}={1}", new Object[]{key, value});
        }
        return null;
    }

    protected int writeHeaderToSheet(WritableSheet sheet) throws RowsExceededException, WriteException {
        log.log(Level.INFO, "Template file: {0}", templateFilePath);
        if (!isConfigurationValid()) {
            return 0;
        }
        log.log(Level.INFO, "The configuration is valid");
        File file = new File(templateFilePath);
        if (!file.exists()) {
            return 0;
        }
        log.log(Level.INFO, "Template file exists");
        Workbook templateWorkbook = null;
        WritableWorkbook tempWorkbook = null;
        try {
            List<String> parameterNames = getTargetReportComponent().getParameterNames();
            List<String> parameterCaptions = getTargetReportComponent().getParameterCaptions();
            Map<String, String> parameters = new HashMap<String, String>();
            for (int i = 0; i < parameterNames.size(); i++) {
                log.log(Level.INFO, "Parameter name: {0}", parameterNames.get(i));
                parameters.put(parameterNames.get(i), parameterCaptions.get(i));
            }
            templateWorkbook = Workbook.getWorkbook(file);
            templateWorkbook.getSheet(0);
            tempWorkbook = Workbook.createWorkbook(File.createTempFile("rep", ".xls"), templateWorkbook);
            WritableSheet templateSheet = tempWorkbook.getSheet(0);
            Map<CellFormat, WritableCellFormat> definedFormats = new HashMap<CellFormat, WritableCellFormat>();
            for (int colIdx = 0; colIdx < templateSheet.getColumns(); colIdx++) {
                sheet.setColumnView(colIdx, templateSheet.getColumnView(colIdx));
                for (int rowIdx = 0; rowIdx < templateSheet.getRows(); rowIdx++) {
                    if (colIdx == 0) {
                        sheet.setRowView(rowIdx, templateSheet.getRowView(rowIdx));
                    }
                    WritableCell readCell = templateSheet.getWritableCell(colIdx, rowIdx);
                    WritableCell newCell = readCell.copyTo(colIdx, rowIdx);
                    String content = newCell.getContents();
                    if (content.startsWith("$P")) {

                        String parameterName = content.substring(2);
                        log.log(Level.INFO, "Found parameter template: {0}", parameterName);
                        if (parameters.containsKey(parameterName)) {
                            newCell = new Label(colIdx, rowIdx, parameters.get(parameterName).toString());
                        }
                    } else if ("$REPORT_NAME".equals(content)) {
                        newCell = new Label(colIdx, rowIdx, getTargetReportComponent().getReport().getLocalizedName());
                    }
                    CellFormat readFormat = readCell.getCellFormat();
                    if (readFormat != null) {
                        if (!definedFormats.containsKey(readFormat)) {
                            definedFormats.put(readFormat, new WritableCellFormat(readFormat));
                        }
                        newCell.setCellFormat(definedFormats.get(readFormat));
                    }
                    sheet.addCell(newCell);
                }
            }
            for (Range range : templateSheet.getMergedCells()) {
                sheet.mergeCells(range.getTopLeft().getColumn(), range.getTopLeft().getRow(), range.getBottomRight().getColumn(), range.getBottomRight().getRow());
            }
            return headerBottom;
        } catch (IOException ioe) {
            log.log(Level.WARNING, "Error reading template file", ioe);
        } catch (BiffException be) {
            log.log(Level.WARNING, "Error parsing template file", be);
        } finally {
            if (tempWorkbook != null) {
                try {
                    tempWorkbook.close();
                } catch (IOException ex) {
                }
            }
            if (templateWorkbook != null) {
                templateWorkbook.close();
            }
        }
        return 0;
    }
}
