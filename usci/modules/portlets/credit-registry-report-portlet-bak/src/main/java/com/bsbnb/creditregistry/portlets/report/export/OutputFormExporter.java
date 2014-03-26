package com.bsbnb.creditregistry.portlets.report.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.bsbnb.creditregistry.portlets.report.Localization;
import static com.bsbnb.creditregistry.portlets.report.ReportApplication.log;
import static com.bsbnb.creditregistry.portlets.report.ReportApplication.setStartTime;
import static com.bsbnb.creditregistry.portlets.report.ReportApplication.logTime;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class OutputFormExporter extends TemplatedPagedXlsReportExporter {

    private int idIndex = 1;

    @Override
    protected boolean generatePage(String exportFilePrefix, int sheetNumber) throws WriteException, IOException, SQLException {
        log.log(Level.INFO, "Output report");
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
        //ResultSet dataSource = null;

        try {
            File xlsFile = File.createTempFile("Records", ".xls", AbstractReportExporter.REPORT_FILES_FOLDER);
            workbook = Workbook.createWorkbook(new FileOutputStream(xlsFile), settings);
            WritableSheet currentSheet = workbook.createSheet("Report", sheetNumber);
            int rowIndex = writeHeaderToSheet(currentSheet) + 1;
            int recordsBySheet = getRecordsBySheet();
            int firstRecordNumber = (sheetNumber - 1) * recordsBySheet + 1;
            int lastRecordNumber = sheetNumber * recordsBySheet;
            int recordCounter = firstRecordNumber;
            setStartTime();
            //dataSource = getTargetReportComponent().getResultSet(firstRecordNumber, lastRecordNumber);
            logTime("After query");
            //ResultSetMetaData rsmd = dataSource.getMetaData();
            int previousId = -1;
            int previousRowIndex = rowIndex;
            int startIdIndex = idIndex;
            /*while (dataSource.next()) {
                int id = dataSource.getInt(2);
                int startColumnIndex = 1;
                int finishColumnIndex = rsmd.getColumnCount();
                if (previousId == -1) {
                    previousId = id;
                } else if (previousId != id) {
                    idIndex++;
                    previousId = id;
                    if (rowIndex - 1 > previousRowIndex) {
                        for (int columnIndex = 1; columnIndex <= rsmd.getColumnCount(); columnIndex++) {
                            if (columnIndex != 23 && columnIndex != 24 && columnIndex != 2) {
                                int columnNumber = columnIndex == 1 ? 1 : columnIndex - 1;
                                currentSheet.mergeCells(columnNumber, previousRowIndex, columnNumber, rowIndex - 1);
                            }
                        }
                    }
                    previousRowIndex = rowIndex;
                } else {
                    startColumnIndex = 23;
                    finishColumnIndex = 24;
                }

                for (int columnIndex = startColumnIndex; columnIndex <= finishColumnIndex; columnIndex++) {
                    if (columnIndex != 2) {
                        int columnNumber = columnIndex == 1 ? 1 : columnIndex - 1;
                        if (rsmd.getColumnType(columnIndex) == Types.NUMERIC) {
                            double value = dataSource.getDouble(columnIndex);
                            if (columnNumber == 1) {
                                value = idIndex;
                            }
                            currentSheet.addCell(new jxl.write.Number(columnNumber, rowIndex, value, numberFormat));
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
                }
                rowIndex++;
                if (rowIndex % 1000 == 0) {
                    logTime(String.format("Row #%d", rowIndex));
                }
                recordCounter++;
            }
            if (rowIndex - 1 > previousRowIndex) {
                for (int columnIndex = 1; columnIndex <= rsmd.getColumnCount(); columnIndex++) {
                    if (columnIndex != 23 && columnIndex != 24 && columnIndex != 2) {
                        int columnNumber = columnIndex == 1 ? 1 : columnIndex - 1;
                        currentSheet.mergeCells(columnNumber, previousRowIndex, columnNumber, rowIndex - 1);
                    }
                }
            }*/
            logTime("After reading data source");
            workbook.write();
            workbook.close();
            logTime("After writing");
            recordCounter--;
            log.log(Level.INFO, "Record counter: {0}", recordCounter);
            if (recordCounter > 0) {
                if (recordCounter >= firstRecordNumber) {
                    String cleanFilename = String.format("%s%d-%d.xls", exportFilePrefix, startIdIndex, idIndex);
                    FileDownloadComponent downloadComponent = new FileDownloadComponent(cleanFilename.replace(".xls", ".zip"), cleanFilename, xlsFile);
                    downloadComponent.setCaption(String.format(Localization.LOAD_RECORDS_FROM_TO.getValue(), startIdIndex, idIndex));
                    downloadComponent.setImmediate(true);
                    downloadComponent.zipFile();
                    addFileToLoad(downloadComponent.getLoadedFile());
                    downloadComponents.add(downloadComponent);
                    idIndex++;
                }
            } else {
                showProgressLayout.addComponent(new com.vaadin.ui.Label(Localization.NO_RECORDS.getValue(), com.vaadin.ui.Label.CONTENT_XHTML));
            }
            if (recordCounter < firstRecordNumber) {
                idIndex = 1;
            }
            return recordCounter > firstRecordNumber;
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                }
            }
            /*if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (Exception e) {
                }
            }*/
        }
    }
}
