package kz.bsbnb.usci.portlet.report.export;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.portlet.report.Localization;
import kz.bsbnb.usci.portlet.report.ReportApplication;
import kz.bsbnb.usci.portlet.report.ReportPortletResource;
import kz.bsbnb.usci.portlet.report.dm.Report;
import kz.bsbnb.usci.portlet.report.dm.ReportInputParameter;
import kz.bsbnb.usci.portlet.report.ui.ConstantValues;
import kz.bsbnb.usci.portlet.report.ui.CustomDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class JasperReportExporter extends AbstractReportExporter{

    private final Logger logger = Logger.getLogger(FileDownloadComponent.class);
    
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
            loadStarted();
            CustomDataSource dataSource = getTargetReportComponent().loadData();
            Report report = getTargetReportComponent().getReport();
            final String reportName = report.getName();

            final String reportPath = StaticRouter.getReportFilesCatalog() + reportName + "\\";
            final String jasperFilePath = reportPath+reportName+".jasper";
            final String resourceFilePath = reportPath+reportName+"_"+ ReportApplication.getApplicationLocale().getLanguage()+".properties";
            logger.info("Report name: "+ reportName);
            logger.info("Report path: "+ reportPath);
            logger.info("Jasper file path: "+ jasperFilePath);
            logger.info("Resource file path: "+ resourceFilePath);
            JasperReport jasperReport = (JasperReport)JRLoader.loadObjectFromFile(jasperFilePath);
            PropertyResourceBundle resourceBundle = new PropertyResourceBundle(new FileInputStream(resourceFilePath));
            Enumeration<String> resourceEnumeration = resourceBundle.getKeys();
            while(resourceEnumeration.hasMoreElements()) {
                String resourceStringName = resourceEnumeration.nextElement();
                String resourceStringValue = resourceBundle.getString(resourceStringName);
                logger.info("Resource property: "+resourceStringName+"="+ resourceStringValue);
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
                logger.info("Report parameter "+entry.getKey()+"="+ entry.getValue());
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
            loadFinished();
        } catch(JRException jre) {
            logger.error("Report exception", jre);
        } catch(IOException ioe) {
            logger.info("File access exception", ioe);
        } catch(SQLException sqle) {
            logger.info("SQL exception", sqle);
        }
    }
}
