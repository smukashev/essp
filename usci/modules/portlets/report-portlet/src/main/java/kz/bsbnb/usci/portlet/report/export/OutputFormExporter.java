package kz.bsbnb.usci.portlet.report.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.PropertyResourceBundle;
import java.util.concurrent.Exchanger;
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

import kz.bsbnb.usci.portlet.report.ReportApplication;
import kz.bsbnb.usci.portlet.report.Localization;
import org.apache.log4j.Logger;

import static kz.bsbnb.usci.portlet.report.ReportApplication.logTime;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class OutputFormExporter extends TemplatedPagedXlsReportExporter {

    WritableFont times12Font = new WritableFont(WritableFont.TIMES, 12);
    WritableCellFormat times12Format = new WritableCellFormat(times12Font);
    WritableCellFormat numberFormat = new WritableCellFormat(new NumberFormat("# ### ##0"));
    WritableCellFormat dateFormat = new WritableCellFormat(jxl.write.DateFormats.DEFAULT);

    private int idIndex = 1;
    private final Logger logger = Logger.getLogger(OutputFormExporter.class);
    private int firstOutputColumnIndex = 1;
    protected int rowIndex;
    protected int recordCounter;
    protected ResultSetMetaData rsmd;
    private boolean isIdTheSame;
    private int firstRecordNumber;
    private int previousId;
    private int previousIdRowIndex;
    private int idCounter = 0;


    private boolean shouldMergeColumn(int columnIndex) throws SQLException {
        return !isPledgeColumn(columnIndex) && columnIndex != 2;
    }

    private boolean isPledgeColumn(int columnIndex) throws SQLException {
        return rsmd.getColumnName(columnIndex).toUpperCase().startsWith("PLEDGE");
    }

    @Override
    protected void loadConfig(String configFilePath) {
        super.loadConfig(configFilePath);
        firstOutputColumnIndex = getIntegerProperty("first-output-column-index", 1);
    }


    @Override
    protected boolean generatePage(String exportFilePrefix, int sheetNumber) throws WriteException, IOException, SQLException {logger.info("Output report");
        WorkbookSettings settings = new WorkbookSettings();
        settings.setUseTemporaryFileDuringWrite(true);
        settings.setRationalization(false);
        settings.setMergedCellChecking(false);


        numberFormat.setFont(times12Font);
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
            logger.info("Error setting formats", we);
        }
        CellView autosizeCellView = new CellView();
        autosizeCellView.setAutosize(true);
        WritableWorkbook workbook = null;
        ResultSet dataSource = null;

        try {
            File xlsFile = File.createTempFile("Records", ".xls", AbstractReportExporter.REPORT_FILES_FOLDER);
            workbook = Workbook.createWorkbook(new FileOutputStream(xlsFile), settings);
            WritableSheet currentSheet = workbook.createSheet("Report", sheetNumber);
            previousId = -1;
            previousIdRowIndex = writeHeaderToSheet(currentSheet) + 1;
            rowIndex = previousIdRowIndex;
            int recordsBySheet = getRecordsBySheet();
            firstRecordNumber = (sheetNumber - 1) * recordsBySheet + 1;
            int lastRecordNumber = sheetNumber * recordsBySheet;
            recordCounter = firstRecordNumber;
            ReportApplication.setStartTime();
            dataSource = getTargetReportComponent().getResultSet(firstRecordNumber, lastRecordNumber);
            ReportApplication.logTime("After query");
            rsmd = dataSource.getMetaData();
            int startIdIndex = idIndex;
            if(dataSource!=null) {

                while (dataSource.next()) {
                    writeRow(dataSource, currentSheet);
                    rowIndex++;
                    if (rowIndex % 1000 == 0) {
                        ReportApplication.logTime(String.format("Row #%d", rowIndex));
                    }
                    recordCounter++;
                }
            }
            mergeRowIfNecessary(currentSheet);
            ReportApplication.logTime("After reading data source");
            workbook.write();
            workbook.close();
            ReportApplication.logTime("After writing");
            recordCounter--;
            logger.info("Record counter: "+ recordCounter);
            if (recordCounter > 0) {
                if (recordCounter >= firstRecordNumber) {
                    if(exportFilePrefix.contains("CreditsList"))
                        exportFilePrefix = exportFilePrefix.replaceAll("Pledge","");

                    int lastIdIndex = idIndex-1;
                    String cleanFilename = String.format("%s%d-%d.xls", exportFilePrefix, startIdIndex, lastIdIndex);
                    FileDownloadComponent downloadComponent = new FileDownloadComponent(cleanFilename.replace(".xls", ".zip"), cleanFilename, xlsFile);
                    downloadComponent.setCaption(String.format(Localization.LOAD_RECORDS_FROM_TO.getValue(), startIdIndex, lastIdIndex));
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
            if (dataSource != null) {
                try {
                    if(!dataSource.getStatement().isClosed()) {
                        dataSource.getStatement().close();
                    }
                    dataSource.close();
                } catch (Exception e) {
                }
            }

        }
    }

    protected void writeRow(ResultSet dataSource, WritableSheet currentSheet) throws SQLException, WriteException {
        handleNewId(dataSource.getInt(2), currentSheet, dataSource);
        for (int columnIndex = 1; columnIndex <= rsmd.getColumnCount(); columnIndex++) {
            if (columnIndex == 2) {
                //Пропускаем id
                continue;
            }
            if (isIdTheSame) {
                if (!isPledgeColumn(columnIndex)) {
                    continue;
                }
            }
            int columnNumber = columnIndex == 1 ? 1 : columnIndex - 1;
            if (rsmd.getColumnType(columnIndex) == Types.NUMERIC) {
                double value=0;
                try {
                    value = dataSource.getDouble(columnIndex);
                } catch (SQLException e) {
                    logger.error("Error at columnIndex: "+columnIndex+" columnName: "+rsmd.getColumnName(columnIndex), e);
                    throw e;
                }
                if (columnNumber == 1) {
                    value = idCounter;
                }
                currentSheet.addCell(new jxl.write.Number(columnNumber, rowIndex, value, numberFormat));
            } else if (rsmd.getColumnType(columnIndex) == Types.TIMESTAMP && dataSource.getDate(columnIndex) != null) {
                currentSheet.addCell(new jxl.write.DateTime(columnNumber, rowIndex, dataSource.getDate(columnIndex), dateFormat));
            } else {
                Object value=null;
                try {
                    value = dataSource.getObject(columnIndex);
                } catch(SQLException e) {
                    logger.error("Error at columnIndex: "+columnIndex+" columnName: "+rsmd.getColumnName(columnIndex), e);
                    throw e;
                }
                if (value == null) {
                    value = "";
                }
                currentSheet.addCell(new Label(columnNumber, rowIndex, value.toString(), times12Format));
            }

        }
    }
    private void handleNewId(int id, WritableSheet currentSheet, ResultSet dataSource) throws WriteException, SQLException {
        if (previousId == id) {
            isIdTheSame = true;
            return;
        }
        isIdTheSame = false;
        idCounter++;
        idIndex++;
        mergeRowIfNecessary(currentSheet);
        if (previousId == -1) {
            firstRecordNumber = idCounter;
        }
        previousId = id;
        previousIdRowIndex = rowIndex;
        //storeDuplicatesInfo(dataSource);
    }
    private void mergeRowIfNecessary(WritableSheet currentSheet) throws WriteException, SQLException {
        if (rowIndex - 1 <= previousIdRowIndex) {
            return;
        }
        for (int columnIndex = 1; columnIndex <= rsmd.getColumnCount(); columnIndex++) {
            if (shouldMergeColumn(columnIndex)) {
                int columnNumber = columnIndex == 1 ? 1 : columnIndex - 1;
                columnNumber -= (1 - firstOutputColumnIndex);
                currentSheet.mergeCells(columnNumber, previousIdRowIndex, columnNumber, rowIndex - 1);
            }
        }
    }
}
