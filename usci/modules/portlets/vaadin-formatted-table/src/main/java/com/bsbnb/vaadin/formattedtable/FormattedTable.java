package com.bsbnb.vaadin.formattedtable;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.CellFormat;
import jxl.format.VerticalAlignment;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Logger;

public class FormattedTable extends Table {
    private final HashMap<Object, String> formatData = new HashMap<>();

    private WritableCellFormat mainFormat;
    private WritableFont mainFont;
    private WritableFont headerFont;
    private WritableCellFormat headerFormat;
    private WritableCellFormat numberFormat;
    private Map<Object, WritableCellFormat> columnFormats;
    private CellView cellView;

    private String excelHeaderRow;
    private String excelSheetName = "Sheet";

    private final SimpleDateFormat DATE_FORMATTER_FROM_CURRENT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat DATE_FORMATTER_TO_GMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    {
        final Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
        DATE_FORMATTER_TO_GMT.setCalendar(cal);
    }

    private final Logger logger = Logger.getLogger(FormattedTable.class);

    public FormattedTable() {
        this(null);
    }

    public FormattedTable(String caption) {
        super(caption);
        alwaysRecalculateColumnWidths = true;
    }

    public FormattedTable(String caption, Container container) {
        this(caption);
        setContainerDataSource(container);
    }

    public void addFormat(Object propertyId, String formatString) {
        formatData.put(propertyId, formatString);
    }

    @Override
    public void setVisibleColumns(Object[] visibleColumns) {
        Container container = getContainerDataSource();
        if (container instanceof BeanItemContainer) {
            BeanItemContainer beanItemContainer = (BeanItemContainer) container;
            for (Object obj : visibleColumns) {
                if (obj != null && obj.toString().contains(".")) {
                    beanItemContainer.addNestedContainerProperty(obj.toString());
                }
            }
        }
        super.setVisibleColumns(visibleColumns);
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property) {
        if (property == null) {
            return "";
        }
        Object value = property.getValue();
        if (value == null) {
            return "";
        }
        if (formatData.containsKey(colId)) {
            return displayValue(value, formatData.get(colId));
        }
        return super.formatPropertyValue(rowId, colId, property);
    }

    protected String displayValue(Object value, String format) {
        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format((Date) value);
        }
        return value.toString();
    }

    protected WritableFont getMainFont() {
        return mainFont;
    }

    public byte[] exportToXLS() throws WriteException, IOException {
        return exportToXLS(getVisibleColumns(), getColumnHeaders());
    }

    public byte[] exportToXLS(Object[] columnIDs) throws WriteException, IOException {
        String[] columnHeaders = getHeaders(columnIDs);
        return exportToXLS(columnIDs, columnHeaders);
    }

    public byte[] exportToXLS(Object[] columnIDs, String[] columnsHeaders) throws WriteException, IOException {
        initFontsAndFormats();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableWorkbook workbook = Workbook.createWorkbook(baos);
        WritableSheet sheet = workbook.createSheet(excelSheetName, 0);

        columnFormats = createColumnFormats(columnIDs);

        int rowCounter = 0;
        if (excelHeaderRow != null) {
            sheet.addCell(new Label(1, rowCounter, excelHeaderRow, headerFormat));
            rowCounter++;
        }

        writeColumnHeaders(sheet, rowCounter, columnsHeaders);
        rowCounter++;

        writeTable(columnIDs, rowCounter, sheet);
        workbook.write();
        workbook.close();
        return baos.toByteArray();
    }

    private String[] getHeaders(Object[] columnIDs) {
        String[] columnHeaders = new String[columnIDs.length];
        for (int i = 0; i < columnHeaders.length; i++) {
            columnHeaders[i] = getColumnHeader(columnIDs[i]);
        }
        return columnHeaders;
    }

    private void writeTable(Object[] columnIDs, int rowCounter, WritableSheet sheet) throws WriteException, NumberFormatException {
        for (Object itemId : getItemIds()) {
            for (int columnIndex = 0; columnIndex < columnIDs.length; columnIndex++) {
                writeCell(itemId, columnIDs[columnIndex], columnIndex, rowCounter, sheet);
            }
            rowCounter++;
        }
    }

    private void writeCell(Object itemId, Object columnID, int columnIndex, int rowIndex, WritableSheet sheet) throws WriteException, NumberFormatException {
        Object value = getContainerProperty(itemId, columnID).getValue();
        String format = formatData.get(columnID);
        WritableCell cell = getCell(value, columnIndex, rowIndex, format);
        cell.setCellFormat(getCellFormat(value, columnID, format));
        sheet.addCell(cell);
        sheet.setColumnView(columnIndex, cellView);
    }

    private void writeColumnHeaders(WritableSheet sheet, int rowCounter, String[] columnsHeaders) throws WriteException {
        for (int columnIndex = 0; columnIndex < columnsHeaders.length; columnIndex++) {
            sheet.addCell(new Label(columnIndex, rowCounter, columnsHeaders[columnIndex], headerFormat));
        }
    }

    private HashMap<Object, WritableCellFormat> createColumnFormats(Object[] columnIDs) throws WriteException {
        HashMap<Object, WritableCellFormat> result = new HashMap<Object, WritableCellFormat>(columnIDs.length);
        for (Object columnID : columnIDs) {
            String formatString = "dd.MM.yyyy";
            if (formatData.containsKey(columnID)) {
                formatString = formatData.get(columnID);
            }
            WritableCellFormat columnFormat = new WritableCellFormat(new DateFormat(formatString));
            columnFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
            result.put(columnID, columnFormat);
        }
        return result;
    }

    protected void initFontsAndFormats() throws WriteException {
        mainFont = new WritableFont(WritableFont.TIMES, 12);
        mainFormat = new WritableCellFormat(mainFont);
        mainFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        mainFormat.setVerticalAlignment(VerticalAlignment.TOP);

        headerFont = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD);
        headerFormat = new WritableCellFormat(headerFont);
        headerFormat.setAlignment(jxl.format.Alignment.CENTRE);
        headerFormat.setVerticalAlignment(VerticalAlignment.TOP);
        headerFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

        cellView = new CellView();
        cellView.setAutosize(true);

        numberFormat = new WritableCellFormat(new NumberFormat("# ### ### ### ##0"));
        numberFormat.setFont(mainFont);
        numberFormat.setShrinkToFit(true);
        numberFormat.setVerticalAlignment(VerticalAlignment.TOP);
        numberFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
    }

    protected WritableCell getCell(Object value, int columnIndex, int rowIndex, String format) throws NumberFormatException {
        if (value == null) {
            value = "";
        }
        if ((value instanceof Integer) || (value instanceof Long)) {
            return new jxl.write.Number(columnIndex, rowIndex, Long.parseLong(value.toString()));
        }
        if (value instanceof Double) {
            return new jxl.write.Number(columnIndex, rowIndex, (Double) value);
        }
        if (value instanceof Date) {
            return new DateTime(columnIndex, rowIndex, toGMT((Date) value));
        }
        if (value instanceof Property) {
            Object propertyValue = ((Property) value).getValue();
            return new Label(columnIndex, rowIndex, propertyValue == null ? "" : propertyValue.toString());
        }
        if (value instanceof Component) {
            String componentCaption = ((Component) value).getCaption();
            return new Label(columnIndex, rowIndex, componentCaption);
        }
        return new jxl.write.Label(columnIndex, rowIndex, value.toString());
    }

    public Date toGMT(final Date base) {
        try {
            if (base == null) {
                return null;
            }
            final String date = DATE_FORMATTER_FROM_CURRENT.format(base);
            return DATE_FORMATTER_TO_GMT.parse(date);
        } catch (ParseException e) {
            logger.error(e.getMessage(),e);
            return base;
        }
    }

    protected CellFormat getCellFormat(Object value, Object columnID, String format) {
        if (value instanceof Date) {
            return columnFormats.get(columnID);
        }
        if (value instanceof Number) {
            return numberFormat;
        }
        return mainFormat;
    }

    public void downloadXls(String filename) {
        downloadXls(filename, getVisibleColumns(), getColumnHeaders());
    }

    public void downloadXls(String filename, Object[] columnIDs) {
        downloadXls(filename, columnIDs, getHeaders(columnIDs));
    }

    public void downloadXls(String filename, Object[] columnIDs, String[] columnHeaders) {
        try {
            final byte[] bytes = exportToXLS(columnIDs, columnHeaders);
            StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {
                @Override
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
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(),ioe);
        } catch (WriteException we) {
            logger.error(we.getMessage(),we);
        }
    }

    public void setExcelHeaderRow(String excelHeaderRow) {
        this.excelHeaderRow = excelHeaderRow;
    }

    public void setExcelSheetName(String excelSheetName) {
        this.excelSheetName = excelSheetName;
    }
}
