package com.bsbnb.creditregistry.portlets.crosscheck.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import jxl.WorkbookSettings;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

public class FormattedTable extends Table {

    /** Java сериализация UID. */
    private static final long serialVersionUID = 804740605642799781L;

    public FormattedTable(String caption) {
        super(caption);
        formatData = new HashMap<Object, String>();
    }
    private HashMap<Object, String> formatData;

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
        Object value = property.getValue();
        if (value == null) {
            return "";
        }
        if (formatData.containsKey(colId)) {
            String formatString = formatData.get(colId);
            if (Date.class.isAssignableFrom(property.getType())) {
                SimpleDateFormat sdf = new SimpleDateFormat(formatString);
                String result = sdf.format((Date) value);
                return result;
            } else if ("accountNumber".equals(formatString)) {
                String valueString = value.toString();
                if (valueString.length() == 7) {
                    return valueString.substring(0, 4) + " " + valueString.substring(4);
                } else {
                    return valueString;
                }
            } else if ("groupedNumber".equals(formatString)) {
                String valueString = value.toString();
                long longValue;
                try {
                    longValue = Long.parseLong(valueString);
                    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
                    dfs.setGroupingSeparator(' ');
                    DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
                    df.setDecimalFormatSymbols(dfs);
                    return df.format(longValue);
                } catch (NumberFormatException nfe) {
                    return valueString;
                }

            }
        }
        return super.formatPropertyValue(rowId, colId, property);
    }

    public byte[] exportToXLS(String headerRow, Object[] columnIDs) throws jxl.write.WriteException, IOException {
        return exportToXLS(new String[]{headerRow}, columnIDs);
    }

    public byte[] exportToXLS(String[] headerRows, Object[] columnIDs) throws jxl.write.WriteException, IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WorkbookSettings settings = new WorkbookSettings();
        //settings.setUseTemporaryFileDuringWrite(true); NEW
        WritableWorkbook workbook = jxl.Workbook.createWorkbook(baos, settings);
        WritableSheet sheet = workbook.createSheet(Localization.XLS_SHEET_HEADER.getValue(), 0);

        int rowCounter = 0;
        jxl.write.WritableCellFormat times12formatBold = new jxl.write.WritableCellFormat(new jxl.write.WritableFont(jxl.write.WritableFont.TIMES, 12, jxl.write.WritableFont.BOLD));
        times12formatBold.setAlignment(jxl.format.Alignment.CENTRE);
        times12formatBold.setBorder(Border.ALL, BorderLineStyle.THIN);
        if (headerRows != null) {
            for (String headerRow : headerRows) {
                sheet.addCell(new Label(1, rowCounter, headerRow, times12formatBold));
                rowCounter++;
            }
        }
        for (int columnIndex = 0; columnIndex < columnIDs.length; columnIndex++) {
            sheet.addCell(new jxl.write.Label(columnIndex, rowCounter, getColumnHeader(columnIDs[columnIndex]), times12formatBold));
        }
        rowCounter++;
        WritableFont times12Font = new jxl.write.WritableFont(jxl.write.WritableFont.TIMES, 12);
        WritableCellFormat times12Format = new WritableCellFormat(times12Font);
        NumberFormat groupedNumberFormat = new NumberFormat("### ### ### ##0");
        jxl.write.WritableCellFormat groupedCellFormat = new WritableCellFormat(times12Font, groupedNumberFormat);
        jxl.CellView cellView = new jxl.CellView();
       // cellView.setAutosize(true);// NEW
        WritableCellFormat[] dateFormats = new WritableCellFormat[columnIDs.length];
        for (int columnIndex = 0; columnIndex < columnIDs.length; columnIndex++) {
            String formatString = formatData.get(columnIDs[columnIndex]);
            if (formatString == null) {
                formatString = "dd.MM.yyyy";
            }
            dateFormats[columnIndex] = new jxl.write.WritableCellFormat(new jxl.write.DateFormat(formatString));
            dateFormats[columnIndex].setBorder(Border.ALL, BorderLineStyle.THIN);
        }
        for (Object itemID : getItemIds()) {
            for (int columnIndex = 0; columnIndex < columnIDs.length; columnIndex++) {
                Object columnID = columnIDs[columnIndex];


                Object value = getContainerProperty(itemID, columnIDs[columnIndex]).getValue();

                if (value != null) {
                    String formatString = formatData.get(columnID);
                    String valueString = value.toString();
                    if ("accountNumber".equals(formatString)) {
                        String contentString = valueString.length() == 7 ? valueString.substring(0, 4) + " " + valueString.substring(4) : valueString;
                        sheet.addCell(new jxl.write.Label(columnIndex, rowCounter, contentString, times12Format));
                    } else if ("groupedNumber".equals(formatString)) {
                        long longValue;
                        jxl.write.WritableCell cell;
                        try {
                            longValue = Long.parseLong(valueString);
                            cell = new jxl.write.Number(columnIndex, rowCounter, longValue, groupedCellFormat);
                        } catch (NumberFormatException nfe) {
                            cell = new jxl.write.Label(columnIndex, rowCounter, valueString, times12Format);
                        }
                        sheet.addCell(cell);
                    } else if (value instanceof String) {
                        sheet.addCell(new jxl.write.Label(columnIndex, rowCounter, valueString, times12Format));
                    } else if (value instanceof Number) {
                        jxl.write.Number number = new jxl.write.Number(columnIndex, rowCounter, Integer.parseInt(valueString), times12Format);
                        sheet.addCell(number);
                    } else if (value instanceof Date) {
                        sheet.addCell(new jxl.write.DateTime(columnIndex, rowCounter, (Date) value, dateFormats[columnIndex]));
                    } else if (value instanceof Property) {
                        Object propertyValue = ((Property) value).getValue();
                        sheet.addCell(new jxl.write.Label(columnIndex, rowCounter, propertyValue == null ? "" : propertyValue.toString(), times12Format));
                        sheet.setColumnView(columnIndex, cellView);
                    } else if (value instanceof Component) {
                        sheet.addCell(new jxl.write.Label(columnIndex, rowCounter, ((Component) value).getCaption(), times12Format));
                    }

                }
                sheet.setColumnView(columnIndex, cellView);
            }
            rowCounter++;
        }

        workbook.write();

        workbook.close();


        return baos.toByteArray();
    }
}