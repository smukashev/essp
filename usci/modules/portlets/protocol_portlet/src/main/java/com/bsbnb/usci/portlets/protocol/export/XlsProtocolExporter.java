package com.bsbnb.usci.portlets.protocol.export;

import com.bsbnb.usci.portlets.protocol.data.ProtocolDisplayBean;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.DateFormat;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class XlsProtocolExporter extends ProtocolExporter {

    private String[] properties;
    private String[] propertyNames;

    public XlsProtocolExporter(final String[] properties, final String[] propertyNames) {
        super();
        if (properties == null) {
            throw new IllegalArgumentException("Null is illegal value for properties array");
        }
        if (propertyNames == null) {
            throw new IllegalArgumentException("Null is illegal value for property names length");
        }
        if (properties.length != propertyNames.length) {
            throw new IllegalArgumentException("Properties and property names should contain equal number of elements");
        }
        this.properties = properties;
        this.propertyNames = propertyNames;
    }

    @Override
    protected byte[] export() throws ExportException {
        Method[] accessors = null;
        try {
            accessors = getAccessors();

            WritableWorkbook workbook = null;
            WritableFont times12font = new WritableFont(WritableFont.TIMES, 12);
            WritableFont times12fontBold = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD);
            WritableCellFormat times12format = new WritableCellFormat(times12font);
            WritableCellFormat times12formatBold = new WritableCellFormat(times12fontBold);
            CellView cellView = new CellView();
            // cellView.setAutosize(true); NEW

            times12formatBold.setAlignment(jxl.format.Alignment.CENTRE);
            times12formatBold.setBorder(Border.ALL, BorderLineStyle.THIN);
            times12format.setBorder(Border.ALL, BorderLineStyle.THIN);
            ByteArrayOutputStream baos = null;
            baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, true, "UTF-8");
            WorkbookSettings settings = new WorkbookSettings();
            // settings.setUseTemporaryFileDuringWrite(true); NEW
            workbook = Workbook.createWorkbook(ps, settings);
            WritableSheet sheet = workbook.createSheet("Sheet1", 0);

            int rowCounter = 0;
            for (int propertyIndex = 0; propertyIndex < propertyNames.length; propertyIndex++) {
                sheet.addCell(new Label(propertyIndex, rowCounter, propertyNames[propertyIndex], times12formatBold));
            }
            rowCounter++;

            String formatString = "dd/MM/yyyy hh:mm:ss";
            DateFormat dateFormat = new DateFormat(formatString);
            WritableCellFormat dateCellFormat = new WritableCellFormat(dateFormat);
            dateCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

            for (ProtocolDisplayBean protocol : getProtocols()) {
                for (int accessorIndex = 0; accessorIndex < accessors.length; accessorIndex++) {
                    Object value = accessors[accessorIndex].invoke(protocol);

                    if (value != null) {
                        if (value instanceof String) {
                            sheet.addCell(new jxl.write.Label(accessorIndex, rowCounter, value.toString(), times12format));
                        } else if (value instanceof Number) {
                            jxl.write.Number number = new jxl.write.Number(accessorIndex, rowCounter, Integer.parseInt(value.toString()), times12format);
                            sheet.addCell(number);
                        } else if (value instanceof Date) {
                            sheet.addCell(new jxl.write.DateTime(accessorIndex, rowCounter, (Date) value, dateCellFormat));
                        } else if (value instanceof Property) {
                            Object propertyValue = ((Property) value).getValue();
                            sheet.addCell(new Label(accessorIndex, rowCounter, propertyValue == null ? "" : propertyValue.toString(), times12format));
                        } else if (value instanceof Component) {
                            sheet.addCell(new Label(accessorIndex, rowCounter, ((Component) value).getCaption(), times12format));
                        }
                        sheet.setColumnView(accessorIndex, cellView);
                    }
                }
                rowCounter++;
            }
            workbook.write();
            workbook.close();
            return baos.toByteArray();
        } catch (WriteException we) {
            throw new ExportException(we);
        } catch (IOException ioe) {
            throw new ExportException(ioe);
        } catch (IllegalAccessException iae) {
            throw new ExportException(iae);
        } catch (InvocationTargetException ite) {
            throw new ExportException(ite);
        } catch (NoSuchMethodException nsme) {
            throw new ExportException(nsme);
        }
    }

    private Method[] getAccessors() throws NoSuchMethodException {
        Method[] result = new Method[properties.length];
        Class<ProtocolDisplayBean> protocolClass = ProtocolDisplayBean.class;
        for (int propertyIndex = 0; propertyIndex < properties.length; propertyIndex++) {
            String propertyName = properties[propertyIndex];
            String methodName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            result[propertyIndex] = protocolClass.getMethod(methodName);
        }
        return result;
    }

    @Override
    protected String getFileName() {
        String prefix = getFilenamePrefix();
        return (prefix==null ? "protocol" : prefix)+".xls";
    }

    @Override
    protected String getContentType() {
        return "application/vnd.ms-excel";
    }
}
