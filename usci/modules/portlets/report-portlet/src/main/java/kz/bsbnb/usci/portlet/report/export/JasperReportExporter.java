package kz.bsbnb.usci.portlet.report.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import kz.bsbnb.usci.portlet.report.ReportApplication;
import kz.bsbnb.usci.portlet.report.ReportPortletResource;
import kz.bsbnb.usci.portlet.report.dm.Report;
import kz.bsbnb.usci.portlet.report.ui.CustomDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;

import kz.bsbnb.usci.portlet.report.Localization;
import kz.bsbnb.usci.portlet.report.dm.ReportInputParameter;
import kz.bsbnb.usci.portlet.report.ui.ConstantValues;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class JasperReportExporter extends AbstractReportExporter{
    
    
    
    @Override
    public List<Component> getActionComponents() {
        Button exportToXlsButton = new Button(Localization.DOWNLOAD_XLS_BUTTON_CAPTION.getValue(), new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                exportToXls();
            }
        });
        exportToXlsButton.setIcon(ReportPortletResource.EXCEL_ICON);
        return Arrays.asList(new Component[]{exportToXlsButton});
    }

    protected void exportToXls() {
        try {
            CustomDataSource dataSource = getTargetReportComponent().loadData();
            Report report = getTargetReportComponent().getReport();
            final String reportName = report.getName();
            final String reportPath = ConstantValues.REPORT_FILES_CATALOG+reportName+"\\";
            final String jasperFilePath = reportPath+reportName+".jasper";
            final String resourceFilePath = reportPath+reportName+"_"+ ReportApplication.getApplicationLocale().getLanguage()+".properties";
            ReportApplication.log.log(Level.INFO, "Report name: {0}", reportName);
            ReportApplication.log.log(Level.INFO, "Report path: {0}", reportPath);
            ReportApplication.log.log(Level.INFO, "Jasper file path: {0}", jasperFilePath);
            ReportApplication.log.log(Level.INFO, "Resource file path: {0}", resourceFilePath);
            JasperReport jasperReport = (JasperReport)JRLoader.loadObjectFromFile(jasperFilePath);
            PropertyResourceBundle resourceBundle = new PropertyResourceBundle(new FileInputStream(resourceFilePath));
            Enumeration<String> resourceEnumeration = resourceBundle.getKeys();
            while(resourceEnumeration.hasMoreElements()) {
                String resourceStringName = resourceEnumeration.nextElement();
                String resourceStringValue = resourceBundle.getString(resourceStringName);
                ReportApplication.log.log(Level.INFO, "Resource property: {0}={1}", new Object[] {resourceStringName, resourceStringValue});
            }
            HashMap<String,Object> reportParameters = new HashMap<String, Object>();
            reportParameters.put("REPORT_RESOURCE_BUNDLE", resourceBundle);
            reportParameters.put("USERNAME", "");
            List<ReportInputParameter> parameters = report.getInputParameters();
            int parametersCount = parameters.size();
            List<Object> parameterValues = getTargetReportComponent().getParameterValues();
            for(int parameterIndex = 0; parameterIndex<parametersCount; parameterIndex++) {
                ReportInputParameter parameter = parameters.get(parameterIndex);
                Object value = parameterValues.get(parameterIndex);
                if(value!=null) {
                    String valueString;
                    if(value instanceof Date) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                        valueString = sdf.format((Date) value);
                    } else {
                        valueString = value.toString();
                    }
                    reportParameters.put(parameter.getParameterName(), valueString);
                }
            }
            for(Entry<String,Object> entry : reportParameters.entrySet()) {
                ReportApplication.log.log(Level.INFO, "Report parameter {0}={1}", new Object[]{entry.getKey(), entry.getValue()});
            }
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, reportParameters, dataSource);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JExcelApiExporter exporterXls = new JExcelApiExporter();
            exporterXls.setParameter(JRXlsExporterParameter.CHARACTER_ENCODING, "UTF-8");
            exporterXls.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
            exporterXls.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
            exporterXls.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
            exporterXls.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
            exporterXls.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
            exporterXls.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, baos);
            exporterXls.setParameter(JRXlsExporterParameter.MAXIMUM_ROWS_PER_SHEET, 100000);
            exporterXls.setParameter(JRXlsExporterParameter.IS_IGNORE_CELL_BORDER, Boolean.FALSE);
            exporterXls.exportReport();
            byte[] bytes = baos.toByteArray();
            final ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(zipBaos);
            zos.setLevel(9);
            zos.putNextEntry(new ZipEntry(reportName+".xls"));
            zos.write(bytes);
            zos.closeEntry();
            zos.close();
            StreamResource.StreamSource streamSource = new StreamResource.StreamSource() {

                @Override
                public InputStream getStream() {
                    return new ByteArrayInputStream(zipBaos.toByteArray());
                }
            };
            StreamResource resource = new StreamResource(streamSource, reportName+".zip", getApplication()) {

                @Override
                public DownloadStream getStream() {
                    DownloadStream downloadStream = super.getStream();
                    downloadStream.setParameter("Content-Disposition", "attachment;filename=" + getFilename());
                    downloadStream.setContentType("application/zip");
                    downloadStream.setCacheTime(0);
                    return downloadStream;
                }
            };
            (getWindow()).open(resource);
        } catch(JRException jre) {
            ReportApplication.log.log(Level.INFO, "Report exception", jre);
        } catch(IOException ioe) {
            ReportApplication.log.log(Level.INFO, "File access exception", ioe);
        } catch(SQLException sqle) {
            ReportApplication.log.log(Level.INFO, "SQL exception", sqle);
        }
    }
}
