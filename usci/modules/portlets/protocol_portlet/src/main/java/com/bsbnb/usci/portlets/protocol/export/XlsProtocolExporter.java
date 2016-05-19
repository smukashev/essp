package com.bsbnb.usci.portlets.protocol.export;

import com.bsbnb.usci.portlets.protocol.data.ProtocolDisplayBean;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.*;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Number;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class XlsProtocolExporter extends ProtocolExporter {

    private String[] properties;
    private String[] propertyNames;
    private final Logger logger = Logger.getLogger(XlsProtocolExporter.class);

    public XlsProtocolExporter(final String[] properties, final String[] propertyNames) {
        super();
        if (properties == null) {
            logger.error(Errors.getError(Errors.E249));
            throw new IllegalArgumentException(Errors.compose(Errors.E249));
        }
        if (propertyNames == null) {
            logger.error(Errors.getError(Errors.E250));
            throw new IllegalArgumentException(Errors.compose(Errors.E250));
        }
        if (properties.length != propertyNames.length) {
            logger.error(Errors.getError(Errors.E251));
            throw new IllegalArgumentException(Errors.compose(Errors.E251));
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
            cellView.setSize(10000);
            // cellView.setAutosize(true); NEW

            times12formatBold.setAlignment(jxl.format.Alignment.CENTRE);
            times12formatBold.setBorder(Border.ALL, BorderLineStyle.THIN);
            times12format.setBorder(Border.ALL, BorderLineStyle.THIN);
            times12format.setWrap(true);
            ByteArrayOutputStream baos = null;
            baos = new ByteArrayOutputStream();
//            PrintStream ps = new PrintStream(baos, true, "UTF-8");
//            WorkbookSettings settings = new WorkbookSettings();
            // settings.setUseTemporaryFileDuringWrite(true); NEW
            workbook = Workbook.createWorkbook(baos);
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
                            String stringValue = value.toString();
                            jxl.write.Number number = new jxl.write.Number(accessorIndex, rowCounter, Integer.parseInt(stringValue.substring(0, stringValue.indexOf("."))), times12format);
                            sheet.addCell(number);
                        } else if (value instanceof Date) {
                            sheet.addCell(new jxl.write.DateTime(accessorIndex, rowCounter, (Date) value, dateCellFormat));
                        } else if (value instanceof Property) {
                            Object propertyValue = ((Property) value).getValue();
                            sheet.addCell(new Label(accessorIndex, rowCounter, propertyValue == null ? "" : propertyValue.toString(), times12format));
                        } else if (value instanceof Component) {
                            sheet.addCell(new Label(accessorIndex, rowCounter, ((Component) value).getCaption(), times12format));
                        }
                    } else {
                        sheet.addCell(new Blank(accessorIndex, rowCounter, times12format));
                    }
                    sheet.setColumnView(accessorIndex, cellView);
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
